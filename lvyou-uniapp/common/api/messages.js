/**
 * 站内消息 API（路径对齐 AppMessagesController）
 */
import { get, postJson } from './request.js';
import { getMessageDetailIcon, getMessageListIcon } from '../messages-icons.js';

const PREFIX = '/app/messages';
const PAGE_SIZE = 10;

export const MSG_DETAIL_PATH = '/pages/msg/detail';
export { PAGE_SIZE as MESSAGE_PAGE_SIZE };

/** 消息列表分页 POST /app/messages/page（需 App JWT） */
export function fetchMessagePage(params = {}) {
	const data = {
		page: params.page || 1,
		limit: params.limit || PAGE_SIZE
	};
	if (params.type != null) {
		data.type = params.type;
	}
	if (params.isRead != null) {
		data.isRead = params.isRead;
	}
	return postJson(`${PREFIX}/page`, data);
}

/** 未读消息数 GET /app/messages/unread-count（需 App JWT；含 count/systemCount/userCount） */
export function fetchMessageUnreadCount() {
	return get(`${PREFIX}/unread-count`);
}

/** 按类型统计未读数（兼容旧版 unread-count 无分类型字段时使用） */
export async function fetchUnreadCountByType(type) {
	const res = await fetchMessagePage({ page: 1, limit: 1, type, isRead: 0 });
	if (res.code === 0 && res.data) {
		return Number(res.data.total ?? 0);
	}
	return 0;
}

/** 消息详情 POST /app/messages/info（需 App JWT） */
export function fetchMessageInfo(id) {
	return postJson(`${PREFIX}/info`, { id });
}

/** 标记已读 POST /app/messages/read（需 App JWT） */
export function markMessageRead(id) {
	return postJson(`${PREFIX}/read`, { id });
}

/** 全部标记已读 POST /app/messages/read-all（需 App JWT；type 可选） */
export function markAllMessagesRead(type) {
	const data = {};
	if (type != null) {
		data.type = type;
	}
	return postJson(`${PREFIX}/read-all`, data);
}

/** 列表 Tab 对应的消息类型：1=系统公告，2=用户消息 */
export function resolveMessageTypeFilter(tabIndex) {
	if (tabIndex === 1) return 1;
	if (tabIndex === 2) return 2;
	return undefined;
}

export function truncateMessageText(text, max = 60) {
	const normalized = String(text || '').replace(/\s+/g, ' ').trim();
	if (normalized.length <= max) return normalized;
	return `${normalized.slice(0, max)}...`;
}

export function formatMessageTime(createTime) {
	if (!createTime) return '';
	const normalized = String(createTime).replace('T', ' ').slice(0, 19);
	const date = new Date(normalized.replace(/-/g, '/'));
	if (Number.isNaN(date.getTime())) {
		return String(createTime).slice(0, 16);
	}
	const now = new Date();
	const startOfToday = new Date(now.getFullYear(), now.getMonth(), now.getDate());
	const startOfYesterday = new Date(startOfToday.getTime() - 86400000);
	const hh = String(date.getHours()).padStart(2, '0');
	const mm = String(date.getMinutes()).padStart(2, '0');
	if (date >= startOfToday) {
		return `今天 ${hh}:${mm}`;
	}
	if (date >= startOfYesterday) {
		return `昨天 ${hh}:${mm}`;
	}
	const month = String(date.getMonth() + 1).padStart(2, '0');
	const day = String(date.getDate()).padStart(2, '0');
	return `${month}-${day} ${hh}:${mm}`;
}

/** 将服务端 MessagesVo 映射为列表/详情展示结构 */
export function mapMessageVo(vo) {
	const type = Number(vo?.type) || 1;
	const isSystem = type === 1;
	const category = isSystem ? 'system' : 'activity';
	return {
		id: vo.id,
		type,
		category,
		categoryLabel: isSystem ? '系统通知' : '活动提醒',
		icon: getMessageListIcon(category),
		iconDetail: getMessageDetailIcon(category),
		title: vo.title || '',
		summary: truncateMessageText(vo.content),
		content: vo.content || '',
		time: formatMessageTime(vo.createTime),
		unread: Number(vo.isRead) === 0,
		linkUrl: vo.url || '',
		linkText: vo.url ? '查看详情' : ''
	};
}

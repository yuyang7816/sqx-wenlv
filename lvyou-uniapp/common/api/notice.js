/**
 * 公告 API（路径对齐 AppNoticeController）
 */
import { get } from './request.js';
import { appendLocationParams } from '../location-params.js';

const PREFIX = '/app/notice';

/** 列表页 UI 文案 */
export const NOTICE_UI = {
	listTitle: '平台公告',
	detailTitle: '公告详情',
	tabAll: '全部',
	tabActivity: '活动',
	tabScenic: '景区',
	tabTraffic: '交通',
	tabPolicy: '政策',
	tagPinned: '置顶',
	tagNew: '最新',
	emptyText: '暂无相关公告',
	sourcePrefix: '来源',
	readMore: '查看详情'
};

/** 分类 Tab（与服务端 category 字段对齐） */
export const NOTICE_CATEGORIES = [
	{ key: 'all', label: NOTICE_UI.tabAll },
	{ key: 'activity', label: NOTICE_UI.tabActivity },
	{ key: 'scenic', label: NOTICE_UI.tabScenic },
	{ key: 'traffic', label: NOTICE_UI.tabTraffic },
	{ key: 'policy', label: NOTICE_UI.tabPolicy }
];

/** 公告列表（分页，公开接口，仅已发布且未过期） */
export function fetchNoticeList(params = {}) {
	return get(`${PREFIX}/list`, appendLocationParams({
		page: params.page || 1,
		limit: params.limit || 10,
		...(params.category && params.category !== 'all' ? { category: params.category } : {})
	}, params));
}

/** 公告详情（公开接口） */
export function fetchNoticeDetail(id) {
	return get(`${PREFIX}/${id}`);
}

/** 公告跑马灯（公开接口，最新 3 条） */
export function fetchNoticeMarquee(locationParams = {}) {
	return get(`${PREFIX}/marquee`, appendLocationParams({}, locationParams));
}

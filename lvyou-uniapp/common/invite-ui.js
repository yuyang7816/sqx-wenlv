/**
 * 邀请分销 UI 文案与展示常量（无 mock 数据）
 */

export const INVITE_UI = {
	inviteTitle: '邀请好友',
	teamTitle: '我的团队',
	commissionTitle: '收益明细',
	copyBtn: '复制文案',
	posterBtn: '生成海报',
	posterPreviewTitle: '海报预览',
	closeBtn: '关闭',
	savePosterBtn: '保存海报',
	myTeamLink: '我的团队',
	commissionLink: '收益明细',
	copySuccess: '邀请语已复制',
	copyFail: '复制失败，请重试',
	saveSuccess: '海报已保存到相册',
	saveFail: '保存失败，请检查相册权限',
	loginTip: '请先登录',
	loadFail: '加载失败',
	networkError: '网络异常，请稍后重试',
	teamDataBadge: '团队数据',
	teamDataDesc: '邀请好友加入，实时查看团队规模',
	inviteCountLabel: '邀请好友（人）',
	earningsLabel: '我的收益',
	rebateTitle: '返佣说明',
	inviteFriendsBtn: '邀请好友',
	teamMembersTitle: '团队成员',
	teamMembersDesc: '按邀请层级查看成员加入时间',
	commissionOverviewBadge: '收益概览',
	commissionOverviewDesc: '清晰区分团队贡献结构，便于快速查看返佣来源',
	totalEarnings: '总收益',
	level1Earnings: '一级收益',
	level2Earnings: '二级收益',
	commissionDetailTitle: '收益明细',
	commissionDetailDesc: '按邀请层级查看单笔奖励记录',
	level1Tab: '一级',
	level2Tab: '二级',
	colNo: '编号',
	colAvatar: '头像',
	colNickname: '昵称',
	colTime: '时间',
	colReward: '奖励',
	emptyData: '暂无数据',
	inviteCodePrefix: '邀请码：'
};

const DEFAULT_COPY_TEMPLATE = '我在省钱兄文旅发现了不错的出行好去处，邀请你一起体验！邀请码：{code}\n点击链接注册：{link}';

const ROUTE_OPTION_IGNORE_KEYS = new Set([
	'phone',
	'scene',
	'id',
	'token',
	'mode',
	'tab',
	'redirect',
	'from',
	'back'
]);
const INVITE_CODE_STORAGE_KEY = 'ry_invite_code';
const INVITE_CODE_PATTERN = /^[A-Za-z0-9_-]{4,32}$/;

const RESERVED_INVITE_CODES = new Set(['mode', 'sms', 'password', 'query', 'token', 'phone']);

export function normalizeInviteCode(value) {
	const code = String(value || '').trim();
	if (!INVITE_CODE_PATTERN.test(code) || RESERVED_INVITE_CODES.has(code.toLowerCase())) {
		return '';
	}
	return code;
}

function readStoredInviteCode() {
	try {
		return normalizeInviteCode(uni.getStorageSync(INVITE_CODE_STORAGE_KEY));
	} catch (e) {
		return '';
	}
}

export function persistInviteCode(value) {
	const code = normalizeInviteCode(value);
	if (!code) return '';
	try {
		uni.setStorageSync(INVITE_CODE_STORAGE_KEY, code);
	} catch (e) {
		// 存储失败时仍返回已解析的邀请码，供当前页填充
	}
	return code;
}

/** H5 落地页可能是 /?code=XXX，路由跳转后页面 options 里不再带这个参数 */
function readInviteCodeFromLocation() {
	// #ifdef H5
	if (typeof window === 'undefined' || !window.location) return '';
	const search = new URLSearchParams(window.location.search || '');
	const fromSearch = search.get('code') || search.get('inviteCode') || search.get('invitationCode');
	if (fromSearch) return fromSearch;
	const hash = window.location.hash || '';
	const queryIndex = hash.indexOf('?');
	if (queryIndex >= 0) {
		const hashParams = new URLSearchParams(hash.slice(queryIndex + 1));
		return hashParams.get('code') || hashParams.get('inviteCode') || hashParams.get('invitationCode') || '';
	}
	// #endif
	return '';
}

function readInviteCodeFromOptions(options = {}) {
	const query = options.query && typeof options.query === 'object' ? options.query : options;
	const fromQuery = query.code || query.inviteCode || query.invitationCode;
	if (fromQuery) return fromQuery;
	const keys = Object.keys(query || {}).filter((key) => !ROUTE_OPTION_IGNORE_KEYS.has(key) && key !== 'query');
	if (keys.length === 1) {
		const key = keys[0];
		const value = query[key];
		const valueText = value == null ? '' : String(value).trim();
		if (!valueText && INVITE_CODE_PATTERN.test(key)) {
			return key;
		}
	}
	return '';
}

/** 启动或页面进入时记住邀请码，供之后打开注册页自动填充 */
export function captureInviteCode(options = {}) {
	return persistInviteCode(readInviteCodeFromOptions(options) || readInviteCodeFromLocation());
}

/** 从页面 onLoad options 解析邀请码（兼容 ?code=XXX 与 ?HUIPVG 形式） */
export function resolveInviteCodeFromOptions(options = {}) {
	return captureInviteCode(options) || readStoredInviteCode();
}

// 尽早读取落地地址，避免 H5 路由改写 URL 后邀请码丢失
captureInviteCode();

/**
 * 构建邀请分享链接（与后端 InviteSupport.buildQrContent 一致）
 */
export function buildInviteShareUrl(shareLink, inviteCode) {
	const code = String(inviteCode || '').trim();
	const base = String(shareLink || '').trim();
	if (!code) {
		return base;
	}
	if (!base) {
		// #ifdef H5
		if (typeof window !== 'undefined' && window.location?.origin) {
			return `${window.location.origin}/pages/my/register?code=${encodeURIComponent(code)}`;
		}
		// #endif
		return code;
	}
	if (base.includes('code=')) {
		return base;
	}
	const joiner = base.includes('?') ? '&' : '?';
	return `${base}${joiner}code=${encodeURIComponent(code)}`;
}

/**
 * 格式化邀请文案（支持 {code}、{link} 占位符，并确保包含分享链接）
 * @param {object} config 邀请配置（copyText、shareLink）
 * @param {string} inviteCode 用户邀请码
 * @param {string} [shareUrlOverride] 优先使用的分享链接（如后端 summary.qrContent，与二维码一致）
 */
export function formatInviteCopyText(config, inviteCode, shareUrlOverride) {
	const override = String(shareUrlOverride || '').trim();
	const shareUrl = override || buildInviteShareUrl(config?.shareLink, inviteCode);
	let text = (config && config.copyText) || DEFAULT_COPY_TEMPLATE;
	text = text.replace(/\{code\}/g, inviteCode || '');
	text = text.replace(/\{link\}/g, shareUrl || '');
	if (shareUrl && !text.includes(shareUrl)) {
		text = `${text.trim()}\n注册链接：${shareUrl}`;
	}
	return text.trim();
}

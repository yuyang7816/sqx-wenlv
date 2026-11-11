/** 协议 key（对齐 AppAgreementController 路径参数） */
export const AGREEMENT_KEYS = {
	USER: 'user',
	PRIVACY: 'privacy',
	ABOUT: 'about'
};

/** 设置页协议入口列表 */
export const AGREEMENT_LIST = [
	{ key: AGREEMENT_KEYS.USER, title: '用户协议' },
	{ key: AGREEMENT_KEYS.PRIVACY, title: '隐私政策' }
];

/** 协议标题映射（服务端仅返回 HTML 正文） */
export const AGREEMENT_TITLE_MAP = {
	[AGREEMENT_KEYS.USER]: '用户服务协议',
	[AGREEMENT_KEYS.PRIVACY]: '隐私政策',
	[AGREEMENT_KEYS.ABOUT]: '关于我们'
};

export function getAgreementTitle(key) {
	return AGREEMENT_TITLE_MAP[key] || '协议详情';
}

export function buildAgreementUrl(key) {
	return `/pages/my/agreement?type=${encodeURIComponent(key)}`;
}

/** 跳转协议详情页（内容由 agreement 页 onLoad 拉取） */
export function openAgreement(key) {
	if (!key) {
		uni.showToast({ title: '协议不存在', icon: 'none' });
		return;
	}
	uni.navigateTo({ url: buildAgreementUrl(key) });
}

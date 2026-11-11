/** 非遗商品订单确认页 UI 文案（无 mock 数据） */

export const HERITAGE_ORDER_UI = {
	navTitle: '\u786e\u8ba4\u8ba2\u5355',
	sectionAddress: '\u6536\u8d27\u5730\u5740',
	sectionGoods: '\u5546\u54c1\u4fe1\u606f',
	sectionFee: '\u8d39\u7528\u660e\u7ec6',
	sectionRemark: '\u8ba2\u5355\u5907\u6ce8',
	addrEmpty: '\u8bf7\u9009\u62e9\u6536\u8d27\u5730\u5740',
	addrDefault: '\u9ed8\u8ba4',
	labelGoods: '\u5546\u54c1\u91d1\u989d',
	labelShip: '\u8fd0\u8d39',
	shipFree: '\u514d\u8fd0\u8d39',
	labelTotal: '\u5b9e\u4ed8\u91d1\u989d',
	labelQty: '\u6570\u91cf',
	remarkPlaceholder: '\u9009\u586b\uff0c\u53ef\u544a\u77e5\u5546\u5bb6\u60a8\u7684\u7279\u6b8a\u9700\u6c42',
	btnSubmit: '\u63d0\u4ea4\u8ba2\u5355',
	footer: '\u975e\u9057 \u00b7 \u5320\u5fc3\u6587\u521b',
	payTitle: '\u9009\u62e9\u652f\u4ed8\u65b9\u5f0f',
	payAmountLabel: '\u5b9e\u4ed8\u91d1\u989d',
	payMethodLabel: '\u652f\u4ed8\u65b9\u5f0f',
	payBtn: '\u786e\u8ba4\u652f\u4ed8',
	paySafe: '\u652f\u4ed8\u73af\u5883\u5b89\u5168\u53ef\u4fe1',
	payMethodBalance: '\u4f59\u989d\u652f\u4ed8',
	payMethodAlipay: '\u652f\u4ed8\u5b9d',
	payMethodWechat: '\u5fae\u4fe1\u652f\u4ed8',
	payBalanceDesc: '\u53ef\u7528\u4f59\u989d',
	payRecommend: '\u63a8\u8350',
	toastNeedAddress: '\u8bf7\u9009\u62e9\u6536\u8d27\u5730\u5740',
	toastPaySuccess: '\u652f\u4ed8\u6210\u529f',
	toastBalanceInsufficient: '\u4f59\u989d\u4e0d\u8db3',
	currencySymbol: '\u00a5',
	shopName: '\u975e\u9057\u597d\u7269',
	labelCoupon: '\u4f18\u60e0\u5238',
	labelDiscount: '\u4f18\u60e0\u62b5\u6263',
	couponSelectHint: '\u5f20\u53ef\u7528',
	couponNone: '\u6682\u65e0\u53ef\u7528',
	couponGoSelect: '\u53bb\u9009\u62e9',
	loginTip: '\u8bf7\u5148\u767b\u5f55',
	orderCreating: '\u521b\u5efa\u8ba2\u5355\u4e2d...',
	createOrderFail: '\u521b\u5efa\u8ba2\u5355\u5931\u8d25',
	payConfirmTitle: '\u786e\u8ba4\u652f\u4ed8',
	payConfirmContent: '\u5b9e\u4ed8\u91d1\u989d',
	payProcessing: '\u652f\u4ed8\u5904\u7406\u4e2d...',
	paySelectMethod: '\u8bf7\u9009\u62e9\u652f\u4ed8\u65b9\u5f0f'
};

export const HERITAGE_PAYMENT_OPTIONS = [
	{ key: 'balance', labelKey: 'payMethodBalance', icon: 'rmb-circle-fill', color: '#B45D2D' },
	{ key: 'alipay', labelKey: 'payMethodAlipay', icon: 'zhifubao-circle-fill', color: '#1677FF', recommend: true },
	{ key: 'wechat', labelKey: 'payMethodWechat', icon: 'weixin-circle-fill', color: '#07C160' }
];

export function getHeritagePaymentLabel(option) {
	return HERITAGE_ORDER_UI[option.labelKey] || option.key;
}

export function formatHeritageAmount(amount) {
	const value = Number(amount);
	if (!Number.isFinite(value)) return `${HERITAGE_ORDER_UI.currencySymbol}0`;
	return `${HERITAGE_ORDER_UI.currencySymbol}${value}`;
}

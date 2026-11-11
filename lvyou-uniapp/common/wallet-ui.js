/**
 * 用户钱包 UI 文案与展示常量（无 mock 数据）
 */

export const WALLET_WITHDRAW_ACCOUNT_KEY = 'ry_wallet_withdraw_account';
export const MERCHANT_WITHDRAW_ACCOUNT_KEY = 'ry_merchant_withdraw_account';

export const WALLET_UI = {
	walletTitle: '我的钱包',
	rechargeTitle: '充值',
	withdrawTitle: '提现',
	transactionsTitle: '资金明细',
	withdrawRecordsTitle: '提现记录',
	balanceLabel: '账户余额（元）',
	availableLabel: '可提现金额',
	totalRechargeLabel: '累计充值',
	totalConsumeLabel: '累计消费',
	totalWithdrawLabel: '累计提现',
	rechargeBtn: '充值',
	withdrawBtn: '提现',
	transactionsLink: '资金明细',
	withdrawRecordsLink: '提现记录',
	accountBindTitle: '提现账号绑定',
	accountBindLink: '提现账号绑定',
	accountBindMethodLabel: '绑定方式',
	accountBindAlipayTip: '请填写本人实名支付宝账号，用于提现到账',
	accountBindTip: '绑定信息仅用于提现，请确保账号真实有效',
	accountBindSave: '保存绑定',
	accountBindSaved: '绑定信息已保存',
	accountNotBoundTip: '请先绑定对应提现方式的收款账号',
	accountNotBound: '未绑定，点击前往设置',
	accountBoundAlipay: '已绑定支付宝',
	accountBoundWechat: '已绑定微信收款码',
	alipayBindTitle: '绑定支付宝',
	rechargeAmountLabel: '充值金额',
	withdrawAmountLabel: '提现金额',
	customAmountPlaceholder: '请输入金额',
	rechargeMethodLabel: '充值方式',
	withdrawMethodLabel: '提现方式',
	alipayAccountLabel: '支付宝账号',
	alipayAccountPlaceholder: '请输入支付宝账号（手机号/邮箱）',
	alipayNameLabel: '真实姓名',
	alipayNamePlaceholder: '请输入支付宝实名姓名',
	wechatQrLabel: '微信收款码',
	wechatQrTip: '请上传本人微信收款码，用于提现到账',
	wechatQrUpload: '上传收款码',
	wechatQrChange: '更换收款码',
	confirmRecharge: '确认充值',
	confirmWithdraw: '确认提现',
	rechargeSuccess: '充值成功',
	withdrawSuccess: '提现申请已提交',
	withdrawPendingTip: '预计 1-3 个工作日到账',
	withdrawRulesTip: '单笔提现 {min}-{max} 元，手续费 {rate}%',
	withdrawFeeLabel: '手续费',
	withdrawActualLabel: '实际到账',
	withdrawAmountAboveMax: '超出最高提现金额',
	amountInvalid: '请输入有效金额',
	amountMinRecharge: '最低充值 1 元',
	amountMinWithdraw: '最低提现 10 元',
	balanceInsufficient: '余额不足',
	withdrawExceedAvailable: '超出可提现金额',
	alipayAccountRequired: '请填写支付宝账号',
	alipayNameRequired: '请填写真实姓名',
	wechatQrRequired: '请上传微信收款码',
	loginTip: '请先登录',
	loadFail: '加载失败，请稍后重试',
	networkError: '网络异常，请稍后重试',
	emptyTransactions: '暂无资金明细',
	emptyWithdrawRecords: '暂无提现记录',
	allTab: '全部',
	incomeTab: '收入',
	expenseTab: '支出',
	statusPending: '处理中',
	statusSuccess: '已到账',
	statusFailed: '提现失败',
	currencySymbol: '¥',
	withdrawToAlipay: '提现至支付宝',
	withdrawToWechat: '提现至微信',
	accountLabel: '到账账户',
	realNameLabel: '收款姓名',
	methodLabel: '提现方式',
	rechargeConfirmTitle: '确认充值',
	rechargeConfirmContent: '是否确认充值',
	withdrawConfirmTitle: '确认提现',
	withdrawConfirmContent: '是否确认提现',
	payProcessing: '支付处理中...',
	uploadFail: '图片上传失败'
};

export const RECHARGE_AMOUNTS = [50, 100, 200, 500, 1000];
export const WITHDRAW_AMOUNTS = [100, 200, 500, 1000];

export const RECHARGE_METHODS = [
	{ key: 'wechat', label: '微信支付', icon: 'weixin-circle-fill', color: '#07C160' },
	{ key: 'alipay', label: '支付宝', icon: 'zhifubao-circle-fill', color: '#1677FF' }
];

export const WITHDRAW_METHODS = [
	{ key: 'alipay', label: '支付宝', icon: 'zhifubao-circle-fill', color: '#1677FF' },
	{ key: 'wechat', label: '微信', icon: 'weixin-circle-fill', color: '#07C160' }
];

export const DEFAULT_WITHDRAW_RULES = {
	minAmount: '10.00',
	maxAmount: '50000.00',
	feeRate: '0.00'
};

export const EMPTY_WALLET_SUMMARY = {
	balance: '0.00',
	available: '0.00',
	totalRecharge: '0.00',
	totalConsume: '0.00',
	totalWithdraw: '0.00',
	withdrawCount: 0,
	withdrawRules: { ...DEFAULT_WITHDRAW_RULES }
};

export function getWithdrawRules(source) {
	const rules = source?.withdrawRules || {};
	return {
		minAmount: rules.minAmount || DEFAULT_WITHDRAW_RULES.minAmount,
		maxAmount: rules.maxAmount || DEFAULT_WITHDRAW_RULES.maxAmount,
		feeRate: rules.feeRate || DEFAULT_WITHDRAW_RULES.feeRate
	};
}

export function calcWithdrawBreakdown(applyAmount, rules) {
	const amount = Number(applyAmount) || 0;
	const rate = Number(rules?.feeRate) || 0;
	const feeAmount = Number((amount * rate / 100).toFixed(2));
	const actualAmount = Number(Math.max(amount - feeAmount, 0).toFixed(2));
	return { applyAmount: amount, feeAmount, actualAmount };
}

export function buildWithdrawRulesTip(rules, ui = WALLET_UI) {
	const current = getWithdrawRules({ withdrawRules: rules });
	return ui.withdrawRulesTip
		.replace('{min}', current.minAmount)
		.replace('{max}', current.maxAmount)
		.replace('{rate}', current.feeRate);
}

export function validateWithdrawAmount(amount, rules, available) {
	const applyAmount = Number(amount);
	if (!Number.isFinite(applyAmount) || applyAmount <= 0) {
		return { ok: false, message: WALLET_UI.amountInvalid };
	}
	const currentRules = getWithdrawRules({ withdrawRules: rules });
	const min = Number(currentRules.minAmount) || 10;
	const max = Number(currentRules.maxAmount) || 50000;
	if (applyAmount < min) {
		return { ok: false, message: `最低提现 ${min} 元` };
	}
	if (applyAmount > max) {
		return { ok: false, message: WALLET_UI.withdrawAmountAboveMax };
	}
	const availableAmount = Number(available);
	if (Number.isFinite(availableAmount) && applyAmount > availableAmount) {
		return { ok: false, message: WALLET_UI.withdrawExceedAvailable };
	}
	const breakdown = calcWithdrawBreakdown(applyAmount, currentRules);
	if (breakdown.actualAmount <= 0) {
		return { ok: false, message: '扣除手续费后到账金额须大于0' };
	}
	return { ok: true, breakdown };
}

export function formatMoney(value) {
	const num = Number(value);
	if (!Number.isFinite(num)) return '0.00';
	return num.toFixed(2);
}

export function formatTransactionAmount(amount) {
	const num = Number(amount) || 0;
	if (num > 0) {
		return `+${WALLET_UI.currencySymbol}${formatMoney(num)}`;
	}
	if (num < 0) {
		return `-${WALLET_UI.currencySymbol}${formatMoney(Math.abs(num))}`;
	}
	return `${WALLET_UI.currencySymbol}${formatMoney(0)}`;
}

export function getWithdrawStatusText(status) {
	if (status === 'pending') return WALLET_UI.statusPending;
	if (status === 'failed') return WALLET_UI.statusFailed;
	return WALLET_UI.statusSuccess;
}

export function formatWithdrawAccountText(record) {
	if (!record) return '';
	if (record.method === 'alipay') {
		return `${record.account || ''}${record.realName ? `（${record.realName}）` : ''}`;
	}
	return record.account || WALLET_UI.wechatQrLabel;
}

/** 读取本地保存的提现账户（支付宝/微信收款码） */
export function getWithdrawAccount() {
	const defaults = { alipayAccount: '', alipayName: '', wechatQrCode: '' };
	try {
		const cached = uni.getStorageSync(WALLET_WITHDRAW_ACCOUNT_KEY);
		if (cached && typeof cached === 'object') {
			return { ...defaults, ...cached };
		}
	} catch (e) { /* ignore */ }
	return { ...defaults };
}

/** 保存提现账户到本地，便于下次填写 */
export function saveWithdrawAccount(partial) {
	const next = { ...getWithdrawAccount(), ...(partial || {}) };
	try {
		uni.setStorageSync(WALLET_WITHDRAW_ACCOUNT_KEY, next);
	} catch (e) { /* ignore */ }
	return next;
}

/** 商户提现账户（与用户钱包隔离） */
export function getMerchantWithdrawAccount() {
	const defaults = { alipayAccount: '', alipayName: '', wechatQrCode: '', preferredMethod: 'alipay' };
	try {
		const cached = uni.getStorageSync(MERCHANT_WITHDRAW_ACCOUNT_KEY);
		if (cached && typeof cached === 'object') {
			return { ...defaults, ...cached };
		}
	} catch (e) { /* ignore */ }
	return { ...defaults };
}

export function saveMerchantWithdrawAccount(partial) {
	const next = { ...getMerchantWithdrawAccount(), ...(partial || {}) };
	try {
		uni.setStorageSync(MERCHANT_WITHDRAW_ACCOUNT_KEY, next);
	} catch (e) { /* ignore */ }
	return next;
}

export function getWithdrawAccountSummary(method, account) {
	const data = account || getWithdrawAccount();
	if (method === 'alipay') {
		const accountNo = String(data.alipayAccount || '').trim();
		const name = String(data.alipayName || '').trim();
		if (accountNo && name) {
			return `${accountNo}（${name}）`;
		}
		return WALLET_UI.accountNotBound;
	}
	if (method === 'wechat') {
		if (String(data.wechatQrCode || '').trim()) {
			return WALLET_UI.accountBoundWechat;
		}
		return WALLET_UI.accountNotBound;
	}
	return WALLET_UI.accountNotBound;
}

export function validateWithdrawAccount(method, account) {
	const data = account || getWithdrawAccount();
	if (method === 'alipay') {
		if (!String(data.alipayAccount || '').trim()) {
			return { ok: false, message: WALLET_UI.alipayAccountRequired };
		}
		if (!String(data.alipayName || '').trim()) {
			return { ok: false, message: WALLET_UI.alipayNameRequired };
		}
		return { ok: true };
	}
	if (method === 'wechat') {
		if (!String(data.wechatQrCode || '').trim()) {
			return { ok: false, message: WALLET_UI.wechatQrRequired };
		}
		return { ok: true };
	}
	return { ok: false, message: WALLET_UI.amountInvalid };
}

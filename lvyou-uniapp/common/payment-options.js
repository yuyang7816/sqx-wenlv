/**
 * H5 微信支付展示条件：微信内置浏览器 + 公众号登录 + 后台开启公众号
 */
import { BOOKING_UI, PAYMENT_OPTIONS, getPaymentLabel } from './booking-ui.js';
import { fetchConfigInfo } from './api/config.js';
import { getUserInfo, hasToken } from './api/auth.js';

/** 充值可用渠道（与订单支付一致，不含余额） */
export function getRechargePaymentOptions() {
	return PAYMENT_OPTIONS.filter((option) => option.key !== 'balance');
}

/** 按当前环境过滤并格式化支付渠道列表 */
export function buildVisiblePaymentMethods(options, context = {}, ui = BOOKING_UI) {
	return filterPaymentOptions(options, context).map((option) => ({
		...option,
		label: getPaymentLabel(option, ui),
		desc: option.recommend ? ui.payRecommend : ''
	}));
}

/** 构建充值页可选支付渠道 */
export function buildRechargePaymentMethods(context = {}, ui = BOOKING_UI) {
	return buildVisiblePaymentMethods(getRechargePaymentOptions(), context, ui);
}

/** 当前选中渠道不可用时回退到默认渠道 */
export function resolvePayMethodSelection(methods, currentKey) {
	const keys = methods.map((item) => item.key);
	if (currentKey && keys.includes(currentKey)) return currentKey;
	return pickDefaultPayMethodKey(methods);
}

export function isWechatInAppBrowser() {
	/* #ifdef H5 */
	if (typeof navigator === 'undefined') return false;
	return /MicroMessenger/i.test(navigator.userAgent || '');
	/* #endif */
	/* #ifndef H5 */
	return false;
	/* #endif */
}

export function isWechatMpLoginUser(user) {
	if (!user) return false;
	const platform = String(user.platform || '').trim();
	if (platform === '公众号') return true;
	return Boolean(String(user.mpOpenId || '').trim());
}

export function resolveWechatMpEnabled(value) {
	if (value == null || value === '') return true;
	if (typeof value === 'boolean') return value;
	const normalized = String(value).trim().toLowerCase();
	return normalized !== '0' && normalized !== 'false' && normalized !== 'off';
}

export function shouldShowWechatPayOption(context = {}) {
	/* #ifdef H5 */
	if (!isWechatInAppBrowser()) return false;
	if (!resolveWechatMpEnabled(context.wechatMpEnabled)) return false;
	if (!context.isWechatMpUser) return false;
	return true;
	/* #endif */
	/* #ifndef H5 */
	return true;
	/* #endif */
}

export function filterPaymentOptions(options, context = {}) {
	return options.filter((option) => {
		if (option.key !== 'wechat') return true;
		return shouldShowWechatPayOption(context);
	});
}

export function pickDefaultPayMethodKey(methods) {
	const balance = methods.find((item) => item.key === 'balance');
	if (balance && !balance.disabled) return 'balance';
	const alipay = methods.find((item) => item.key === 'alipay');
	if (alipay && !alipay.disabled) return 'alipay';
	const first = methods.find((item) => !item.disabled);
	return first ? first.key : 'alipay';
}

export async function fetchPaymentVisibilityContext() {
	let wechatMpEnabled = true;
	let isWechatMpUser = false;
	try {
		const configRes = await fetchConfigInfo();
		if (configRes?.code === 0 && configRes.data != null) {
			wechatMpEnabled = resolveWechatMpEnabled(configRes.data.wechatMpEnabled);
		}
		if (hasToken()) {
			const userRes = await getUserInfo();
			if (userRes?.code === 0 && userRes.data) {
				isWechatMpUser = isWechatMpLoginUser(userRes.data);
			}
		}
	} catch (e) {
		/* ignore */
	}
	return { wechatMpEnabled, isWechatMpUser };
}

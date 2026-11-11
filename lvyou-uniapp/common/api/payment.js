/**
 * 支付 API（路径对齐 PayController / AppWalletController.pay）
 */
import { post, postJson } from './request.js';
import { USER_H5_ORIGIN } from '../domain.js';

export { fetchWalletSummary } from './wallet.js';

const WALLET_PREFIX = '/app/wallet';
const PAY_PREFIX = '/app/base/pay';

/** 余额支付订单 */
export function payOrderByWallet(orderNo) {
	return postJson(`${WALLET_PREFIX}/pay`, { orderNo });
}

/** 获取当前端支付 tradeType（条件编译） */
export function getPayTradeType() {
	// #ifdef MP-WEIXIN
	return 'JSAPI';
	// #endif
	// #ifdef APP-PLUS
	return 'APP';
	// #endif
	// #ifdef H5
	return 'H5';
	// #endif
	return 'H5';
}

/** 充值支付 H5 回跳至钱包页 */
export function getWalletRechargeRedirectUri() {
	// #ifdef H5
	if (typeof window !== 'undefined' && window.location && window.location.origin) {
		return `${window.location.origin}/pages/my/wallet`;
	}
	// #endif
	return `${USER_H5_ORIGIN}/pages/my/wallet`;
}

/**
 * 钱包充值第三方支付（businessType=wallet_recharge）
 * @param {{ rechargeNo: string, amount: number|string, methodKey: 'wechat'|'alipay', openId?: string }} params
 */
export function payWalletRecharge(params) {
	const channel = mapPayMethodToChannel(params.methodKey);
	return payOrderByChannel({
		orderNo: params.rechargeNo,
		businessType: 'wallet_recharge',
		channel,
		amount: params.amount,
		redirectUri: getWalletRechargeRedirectUri(),
		openId: params.openId
	});
}

/** 保证金缴纳 H5 回跳 */
export function getMerchantDepositRedirectUri() {
	// #ifdef H5
	if (typeof window !== 'undefined' && window.location && window.location.origin) {
		return `${window.location.origin}/merchant-portal/center/wallet-deposit`;
	}
	// #endif
	return `${USER_H5_ORIGIN}/merchant-portal/center/wallet-deposit`;
}

/**
 * 商户保证金第三方支付（businessType=merchant_deposit）
 * @param {{ payNo: string, amount: number|string, methodKey: 'wechat'|'alipay', openId?: string }} params
 */
export function payMerchantDeposit(params) {
	const channel = mapPayMethodToChannel(params.methodKey);
	return payOrderByChannel({
		orderNo: params.payNo,
		businessType: 'merchant_deposit',
		channel,
		amount: params.amount,
		redirectUri: getMerchantDepositRedirectUri(),
		openId: params.openId
	});
}

/** 支付成功回跳地址 */
export function getPayRedirectUri() {
	// #ifdef H5
	if (typeof window !== 'undefined' && window.location && window.location.origin) {
		return `${window.location.origin}/pages/order/list`;
	}
	// #endif
	return `${USER_H5_ORIGIN}/pages/order/list`;
}

/**
 * 第三方支付（支付宝/微信）
 * @param {{ orderNo: string, amount: number|string, channel: 'ali'|'wx', openId?: string }} params
 */
export function payOrderByChannel(params) {
	const payload = {
		orderNo: params.orderNo,
		businessType: params.businessType || 'tourism',
		tradeType: getPayTradeType(),
		channel: params.channel,
		amount: params.amount,
		redirectUri: params.redirectUri || getPayRedirectUri()
	};
	if (params.openId) {
		payload.openId = params.openId;
	}
	return post(`${PAY_PREFIX}/pay`, payload);
}

/** 支付方式 key → 服务端 channel */
export function mapPayMethodToChannel(methodKey) {
	if (methodKey === 'wechat') return 'wx';
	if (methodKey === 'alipay') return 'ali';
	return '';
}

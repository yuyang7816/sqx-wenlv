/**
 * 全局支付弹框（与 hotel-confirm BookingPayPopup 样式一致）
 */
import { ref, computed } from 'vue';
import {
	BOOKING_UI,
	PAYMENT_OPTIONS,
	formatMoney,
	getPaymentLabel
} from './booking-ui.js';
import {
	filterPaymentOptions,
	pickDefaultPayMethodKey,
	fetchPaymentVisibilityContext
} from './payment-options.js';
import {
	fetchWalletSummary,
	payOrderByWallet,
	payOrderByChannel,
	mapPayMethodToChannel
} from './api/payment.js';
import { isH5PaymentRedirectResult, launchH5PaymentFromResponse } from './h5-payment-launch.js';

export const showPayPopup = ref(false);
export const selectedPayMethod = ref('balance');
export const paying = ref(false);
export const walletBalance = ref(0);
export const paymentVisibility = ref({
	wechatMpEnabled: true,
	isWechatMpUser: false
});

const payContext = ref(null);

function resolveUi(context) {
	return context?.ui || BOOKING_UI;
}

function resolveLabelFn(context) {
	return context?.getPaymentLabel || getPaymentLabel;
}

function buildPaymentMethods(context) {
	const amount = Number(context?.amount ?? 0);
	const ui = resolveUi(context);
	const labelFn = resolveLabelFn(context);
	const options = filterPaymentOptions(context?.paymentOptions || PAYMENT_OPTIONS, paymentVisibility.value);
	return options.map((option) => {
		const label = labelFn(option, ui);
		if (option.key === 'balance') {
			const balance = walletBalance.value;
			const insufficient = amount > balance;
			return {
				...option,
				label,
				desc: `${ui.payBalanceDesc} ${formatMoney(balance)}`,
				disabled: insufficient,
				recommend: false
			};
		}
		return {
			...option,
			label,
			desc: option.recommend ? ui.payRecommend : '',
			disabled: false
		};
	});
}

async function loadWalletBalance() {
	try {
		if (typeof payContext.value?.loadWalletBalance === 'function') {
			walletBalance.value = Number(await payContext.value.loadWalletBalance()) || 0;
			return;
		}
		const res = await fetchWalletSummary();
		if (res.code === 0 && res.data) {
			walletBalance.value = Number(res.data.balance) || 0;
		}
	} catch (e) {
		/* ignore */
	}
}

async function loadPaymentVisibility() {
	paymentVisibility.value = await fetchPaymentVisibilityContext();
}

async function executeOrderPay(context, methodKey) {
	const orderNo = context?.orderNo;
	const amount = Number(context?.amount ?? 0);
	const ui = resolveUi(context);
	if (!orderNo) {
		uni.showToast({ title: ui.createOrderFail, icon: 'none' });
		return false;
	}

	if (methodKey === 'balance') {
		if (amount > walletBalance.value) {
			uni.showToast({ title: ui.payBalanceInsufficient, icon: 'none' });
			await loadWalletBalance();
			return false;
		}
		const res = await payOrderByWallet(orderNo);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || ui.createOrderFail, icon: 'none' });
			return false;
		}
		return true;
	}

	const channel = mapPayMethodToChannel(methodKey);
	if (!channel) {
		uni.showToast({ title: ui.paySelectMethod, icon: 'none' });
		return false;
	}
	uni.showLoading({ title: ui.payProcessing, mask: true });
	try {
		const payRes = await payOrderByChannel({ orderNo, amount, channel });
		uni.hideLoading();
		if (payRes.code !== 0) {
			uni.showToast({ title: payRes.msg || ui.createOrderFail, icon: 'none' });
			return false;
		}
		const launchResult = launchH5PaymentFromResponse(payRes.data, channel);
		if (isH5PaymentRedirectResult(launchResult)) {
			return false;
		}
		if (launchResult === 'invalid') {
			uni.showToast({ title: '无法拉起支付宝支付，请稍后重试', icon: 'none' });
			return false;
		}
		return true;
	} catch (e) {
		uni.hideLoading();
		uni.showToast({ title: ui.createOrderFail, icon: 'none' });
		return false;
	}
}

export function useGlobalPayPopup() {
	const ui = computed(() => resolveUi(payContext.value));
	const payAmount = computed(() => payContext.value?.amount ?? 0);
	const accentColor = computed(() => payContext.value?.accentColor || '#2979FF');
	const priceColor = computed(() => payContext.value?.priceColor || '#FF4757');
	const paymentMethods = computed(() => buildPaymentMethods(payContext.value));

	function closePayPopup() {
		if (paying.value) return;
		showPayPopup.value = false;
		payContext.value = null;
	}

	function selectPayMethod(method) {
		if (method.disabled || paying.value) return;
		selectedPayMethod.value = method.key;
	}

	async function openPayPopup(options = {}) {
		payContext.value = { ...options };
		await Promise.all([loadWalletBalance(), loadPaymentVisibility()]);
		selectedPayMethod.value = pickDefaultPayMethodKey(buildPaymentMethods(payContext.value));
		showPayPopup.value = true;
		return true;
	}

	async function onConfirmPay() {
		if (paying.value) return;
		const context = payContext.value;
		const uiObj = resolveUi(context);
		if (!selectedPayMethod.value) {
			uni.showToast({ title: uiObj.paySelectMethod, icon: 'none' });
			return;
		}

		paying.value = true;
		try {
			let success = false;
			if (typeof context?.customPay === 'function') {
				success = await context.customPay(selectedPayMethod.value, context);
			} else {
				success = await executeOrderPay(context, selectedPayMethod.value);
			}
			if (!success) return;

			// 先清 paying，否则 closePayPopup 会因防误关直接 return，弹框关不掉
			paying.value = false;
			closePayPopup();
			uni.showToast({
				title: context?.successTitle || uiObj.paySuccess,
				icon: 'success',
				duration: 1200
			});
			context?.onSuccess?.();
		} catch (e) {
			const uiObj = resolveUi(payContext.value);
			uni.showToast({ title: uiObj.createOrderFail, icon: 'none' });
		} finally {
			paying.value = false;
		}
	}

	return {
		showPayPopup,
		selectedPayMethod,
		paying,
		ui,
		payAmount,
		paymentMethods,
		accentColor,
		priceColor,
		openPayPopup,
		closePayPopup,
		selectPayMethod,
		onConfirmPay
	};
}

const payPopupHosts = [];
let payPopupHostToken = 0;

function getActivePayPopupHost() {
	return payPopupHosts.length ? payPopupHosts[payPopupHosts.length - 1] : null;
}

/** 注册全局支付宿主（App.vue / 预订页挂载时调用，支持多层栈） */
export function registerPayPopupHost(host, token) {
	if (host) {
		payPopupHostToken += 1;
		const currentToken = payPopupHostToken;
		payPopupHosts.push({
			token: currentToken,
			openPayPopup: host.openPayPopup
		});
		return currentToken;
	}
	if (token == null) return;
	const index = payPopupHosts.findIndex((item) => item.token === token);
	if (index >= 0) {
		payPopupHosts.splice(index, 1);
	}
}

/** 任意页面唤起支付弹框 */
export function openPayPopup(options = {}) {
	const host = getActivePayPopupHost();
	if (!host?.openPayPopup) {
		return Promise.resolve(false);
	}
	return host.openPayPopup(options);
}

export { BOOKING_UI, PAYMENT_OPTIONS, formatMoney };

/**
 * 商品下单页共用：优惠券、钱包、下单与支付
 */
import { ref, computed, watch } from 'vue';
import { hasToken } from './api/auth.js';
import { createGoodsOrder, resolveCreateOrderResult } from './api/order.js';
import { fetchAvailableCoupons } from './api/coupon.js';
import { fetchWalletSummary } from './api/payment.js';
import { COUPON_SCOPE_GOODS, consumeCouponSelectResult } from './coupon-util.js';
import { goOrderList, ORDER_CATEGORY_GOODS } from './order-ui.js';
import {
	HERITAGE_ORDER_UI,
	HERITAGE_PAYMENT_OPTIONS,
	getHeritagePaymentLabel,
	formatHeritageAmount
} from './heritage-order-ui.js';
import { BOOKING_UI } from './booking-ui.js';
import { openPayPopup as openGlobalPayPopup } from './pay-popup.js';

export function useBookingGoodsPay(options) {
	const {
		goodsAmount,
		shippingFee,
		buildCreateOrderPayload,
		validateForm,
		accentColor = '#B45D2D',
		priceColor = '#B45D2D',
		payUi
	} = options;

	const ui = HERITAGE_ORDER_UI;
	const creatingOrder = ref(false);
	const selectedCoupon = ref(null);
	const availableCouponCount = ref(0);
	const pendingOrder = ref(null);

	const couponDiscount = computed(() => {
		if (!selectedCoupon.value) return 0;
		if (selectedCoupon.value.discount != null) {
			return Number(selectedCoupon.value.discount) || 0;
		}
		const amount = Number(selectedCoupon.value.amount) || 0;
		return Math.min(amount, goodsAmount.value);
	});

	const shippingAmount = computed(() => {
		if (typeof shippingFee === 'function') {
			return Number(shippingFee()) || 0;
		}
		if (shippingFee && typeof shippingFee === 'object' && 'value' in shippingFee) {
			return Number(shippingFee.value) || 0;
		}
		return Number(shippingFee) || 0;
	});

	const totalAmount = computed(() => Math.max(0, goodsAmount.value - couponDiscount.value + shippingAmount.value));

	const payAmountDisplay = computed(() => {
		if (pendingOrder.value && pendingOrder.value.payAmount != null) {
			return Number(pendingOrder.value.payAmount) || 0;
		}
		return totalAmount.value;
	});

	watch(goodsAmount, () => {
		if (selectedCoupon.value && selectedCoupon.value.applicable === false) {
			selectedCoupon.value = null;
		}
		refreshAvailableCouponCount();
	});

	function requireLogin() {
		if (hasToken()) return true;
		uni.showToast({ title: ui.loginTip, icon: 'none' });
		setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
		return false;
	}

	async function refreshAvailableCouponCount() {
		if (!hasToken() || goodsAmount.value <= 0) {
			availableCouponCount.value = 0;
			return;
		}
		try {
			const res = await fetchAvailableCoupons(COUPON_SCOPE_GOODS, goodsAmount.value);
			if (res.code === 0 && Array.isArray(res.data)) {
				availableCouponCount.value = res.data.filter((item) => item.applicable).length;
			} else {
				availableCouponCount.value = 0;
			}
		} catch (e) {
			availableCouponCount.value = 0;
		}
	}

	function openCouponSelect() {
		if (!requireLogin()) return;
		if (goodsAmount.value <= 0) return;
		const selectedId = selectedCoupon.value?.id || '';
		uni.navigateTo({
			url: `/mobile-design/booking/coupon-select?scope=${COUPON_SCOPE_GOODS}&amount=${goodsAmount.value}&selectedId=${selectedId}`
		});
	}

	function applyCouponSelectResult() {
		const result = consumeCouponSelectResult();
		if (!result) return;
		if (!result.couponId) {
			selectedCoupon.value = null;
			refreshAvailableCouponCount();
			return;
		}
		if (result.coupon && result.coupon.applicable !== false) {
			selectedCoupon.value = result.coupon;
		} else {
			selectedCoupon.value = null;
		}
	}

	function finishOrder() {
		pendingOrder.value = null;
		setTimeout(() => goOrderList(ORDER_CATEGORY_GOODS), 800);
	}

	async function openPayPopup() {
		return openGlobalPayPopup({
			amount: payAmountDisplay.value,
			orderNo: pendingOrder.value?.orderNo,
			accentColor,
			priceColor,
			ui: payUi || { ...BOOKING_UI, ...ui },
			paymentOptions: HERITAGE_PAYMENT_OPTIONS,
			getPaymentLabel: (option) => getHeritagePaymentLabel(option),
			successTitle: ui.toastPaySuccess,
			onSuccess: finishOrder
		});
	}

	function handlePayPopupUnavailable() {
		uni.showToast({
			title: BOOKING_UI.orderCreatedGoPay || BOOKING_UI.payPopupUnavailable,
			icon: 'none',
			duration: 2000
		});
		setTimeout(() => goOrderList(ORDER_CATEGORY_GOODS), 1500);
	}

	async function createOrderAndProceed() {
		if (creatingOrder.value) return;
		if (!validateForm()) return;

		creatingOrder.value = true;
		uni.showLoading({ title: ui.orderCreating, mask: true });
		try {
			const res = await createGoodsOrder(buildCreateOrderPayload());
			uni.hideLoading();
			const created = resolveCreateOrderResult(res);
			if (!created) {
				uni.showToast({ title: res?.msg || ui.createOrderFail, icon: 'none' });
				return;
			}
			pendingOrder.value = {
				orderNo: created.orderNo,
				payAmount: created.payAmount
			};
			if (created.payAmount <= 0) {
				uni.showToast({ title: ui.toastPaySuccess, icon: 'success', duration: 1200 });
				finishOrder();
				return;
			}
			const opened = await openPayPopup();
			if (opened === false) {
				handlePayPopupUnavailable();
			}
		} catch (e) {
			uni.hideLoading();
			uni.showToast({ title: ui.createOrderFail, icon: 'none' });
		} finally {
			creatingOrder.value = false;
		}
	}

	function onSubmit() {
		if (!requireLogin()) return;
		createOrderAndProceed();
	}

	function onBookingShow() {
		if (!hasToken()) return;
		fetchWalletSummary().catch(() => {});
		applyCouponSelectResult();
		refreshAvailableCouponCount();
	}

	return {
		ui,
		creatingOrder,
		selectedCoupon,
		availableCouponCount,
		pendingOrder,
		couponDiscount,
		totalAmount,
		payAmountDisplay,
		requireLogin,
		openCouponSelect,
		onSubmit,
		onBookingShow,
		formatHeritageAmount
	};
}

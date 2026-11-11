/**
 * 预订页共用：优惠券、钱包、下单与支付
 */
import { ref, computed, watch } from 'vue';
import { hasToken } from './api/auth.js';
import { createOrder, resolveCreateOrderResult } from './api/order.js';
import { fetchAvailableCoupons } from './api/coupon.js';
import { fetchWalletSummary } from './api/payment.js';
import { consumeCouponSelectResult } from './coupon-util.js';
import { ORDER_CATEGORY_ROUTE, ORDER_CATEGORY_HOTEL, goOrderList } from './order-ui.js';
import {
	BOOKING_UI,
	formatMoney,
	PAYMENT_OPTIONS
} from './booking-ui.js';
import { openPayPopup as openGlobalPayPopup } from './pay-popup.js';

export function useBookingPay(options) {
	const {
		couponScope,
		subtotalAmount,
		orderCategory,
		buildCreateOrderPayload,
		validateForm,
		accentColor = '#2979FF',
		priceColor = '#FF4757',
		payUi,
		paymentOptions = PAYMENT_OPTIONS
	} = options;

	const ui = BOOKING_UI;
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
		return Math.min(amount, subtotalAmount.value);
	});

	const totalAmount = computed(() => Math.max(0, subtotalAmount.value - couponDiscount.value));

	const payAmountDisplay = computed(() => {
		if (pendingOrder.value && pendingOrder.value.payAmount != null) {
			return Number(pendingOrder.value.payAmount) || 0;
		}
		return totalAmount.value;
	});

	watch(subtotalAmount, () => {
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

	async function loadWalletBalance() {
		try {
			await fetchWalletSummary();
		} catch (e) {
			/* ignore */
		}
	}

	async function refreshAvailableCouponCount() {
		if (!hasToken() || subtotalAmount.value <= 0) {
			availableCouponCount.value = 0;
			return;
		}
		try {
			const res = await fetchAvailableCoupons(couponScope.value, subtotalAmount.value);
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
		if (subtotalAmount.value <= 0) {
			uni.showToast({ title: ui.couponNone, icon: 'none' });
			return;
		}
		const selectedId = selectedCoupon.value?.id || '';
		uni.navigateTo({
			url: `/mobile-design/booking/coupon-select?scope=${couponScope.value}&amount=${subtotalAmount.value}&selectedId=${selectedId}`
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

	function finishBooking(successTitle) {
		pendingOrder.value = null;
		if (successTitle) {
			uni.showToast({
				title: successTitle,
				icon: 'success',
				duration: 1200
			});
		}
		setTimeout(() => goOrderList(orderCategory.value), 800);
	}

	async function openPayPopup() {
		return openGlobalPayPopup({
			amount: payAmountDisplay.value,
			orderNo: pendingOrder.value?.orderNo,
			accentColor,
			priceColor,
			ui: payUi || ui,
			paymentOptions,
			successTitle: ui.paySuccess,
			onSuccess: () => finishBooking()
		});
	}

	function handlePayPopupUnavailable() {
		uni.showToast({
			title: ui.orderCreatedGoPay || ui.payPopupUnavailable,
			icon: 'none',
			duration: 2000
		});
		setTimeout(() => goOrderList(orderCategory.value), 1500);
	}

	async function createOrderAndProceed() {
		if (creatingOrder.value) return;
		if (!validateForm()) return;

		creatingOrder.value = true;
		uni.showLoading({ title: ui.orderCreating, mask: true });
		try {
			const res = await createOrder(buildCreateOrderPayload());
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
				finishBooking(ui.submitBook);
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
		loadWalletBalance();
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
		refreshAvailableCouponCount,
		openCouponSelect,
		applyCouponSelectResult,
		onSubmit,
		onBookingShow,
		formatMoney
	};
}

export { ORDER_CATEGORY_HOTEL, ORDER_CATEGORY_ROUTE };

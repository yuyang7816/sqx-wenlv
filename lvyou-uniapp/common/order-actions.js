/**
 * 用户端 / 商户端订单可操作项（列表、详情复用）
 */
import { cancelOrder, postOrderAction, fetchRefundPreview } from './api/order.js';
import { postMerchantOrderAction } from './api/merchant.js';
import { openPayPopup } from './pay-popup.js';
import { openRouteRefundPopup } from './route-refund-popup.js';
import { getOrderPrimaryAction } from './merchant-center.js';
import { getModuleColor } from './theme.js';
import {
	ORDER_UI,
	ORDER_CATEGORY_GOODS,
	ORDER_CATEGORY_HOTEL,
	ORDER_CATEGORY_ROUTE,
	inferOrderCategory,
	isHotelPendingCheckin,
	isRoutePendingVerify,
	canViewOrderLogistics,
	canMerchantViewOrderLogistics,
	openOrderLogistics
} from './order-ui.js';
import { isGoodsFlow } from './order-type.js';
import { isOnlinePayFlow, isRestaurantFlow } from './order-type.js';

function resolvePayAccentColor(record) {
	const category = inferOrderCategory(record);
	if (category === ORDER_CATEGORY_GOODS) return getModuleColor('heritageShop');
	if (category === ORDER_CATEGORY_ROUTE) return getModuleColor('route');
	return getModuleColor('hotel');
}

function resolvePayPriceColor(record) {
	const category = inferOrderCategory(record);
	if (category === ORDER_CATEGORY_GOODS) return '#B45D2D';
	if (category === ORDER_CATEGORY_ROUTE) return '#FF6600';
	return '#FF4757';
}

/** 用户端：当前订单可展示的操作按钮 */
export function getUserOrderActions(record) {
	if (!record) return [];
	const category = inferOrderCategory(record);
	const code = record.statusCode || '';
	const actions = [];

	if (isRestaurantFlow(record.orderType)) {
		if (record.canCancelOrder) {
			actions.push({ key: 'cancel', label: ORDER_UI.btnCancel, tone: 'outline' });
		}
		return actions;
	}

	if (category === ORDER_CATEGORY_ROUTE) {
		if (code === 'pending_pay') {
			if (record.canCancelOrder !== false) {
				actions.push({ key: 'cancel', label: ORDER_UI.btnCancelBooking, tone: 'outline' });
			}
			actions.push({ key: 'pay', label: ORDER_UI.btnPay, tone: 'primary' });
			return actions;
		}
		if (isRoutePendingVerify(record)) {
			actions.unshift({ key: 'verifyCode', label: ORDER_UI.btnVerifyCode, tone: 'primary' });
		}
		if (record.canCancelOrder) {
			actions.push({ key: 'cancel', label: ORDER_UI.btnCancelBooking, tone: 'outline' });
		}
		if (record.canApplyRefund) {
			actions.push({ key: 'refund', label: ORDER_UI.btnRefund, tone: 'outline' });
		}
		if (code === 'completed') {
			if (record.canReview) {
				actions.push({ key: 'review', label: ORDER_UI.btnReview, tone: 'outline' });
			}
			actions.push({ key: 'viewRouteDetail', label: ORDER_UI.btnViewRouteDetail, tone: 'primary' });
		}
		if (code === 'cancelled' || code === 'refunded') {
			actions.push({ key: 'rebook', label: ORDER_UI.goRoute, tone: 'primary' });
		}
		return actions;
	}

	if (code === 'pending_pay') {
		if (record.canCancelOrder !== false) {
			actions.push({ key: 'cancel', label: ORDER_UI.btnCancel, tone: 'outline' });
		}
		actions.push({ key: 'pay', label: ORDER_UI.btnPay, tone: 'primary' });
		return actions;
	}

	if (record.canCancelOrder) {
		actions.push({ key: 'cancel', label: ORDER_UI.btnCancel, tone: 'outline' });
	}

	if (record.canConfirmReceive) {
		actions.push({ key: 'complete', label: ORDER_UI.btnConfirmReceive, tone: 'primary' });
	}
	if (record.canApplyRefund && isOnlinePayFlow(record.orderType || category)) {
		actions.push({ key: 'refund', label: ORDER_UI.btnRefund, tone: 'outline' });
	}

	if (category === ORDER_CATEGORY_HOTEL) {
		if (isHotelPendingCheckin(record)) {
			actions.unshift({ key: 'verifyCode', label: ORDER_UI.btnVerifyCode, tone: 'primary' });
		}
		if (code === 'completed') {
			if (record.canReview) {
				actions.push({ key: 'review', label: ORDER_UI.btnReview, tone: 'outline' });
			}
			actions.push({ key: 'rebook', label: ORDER_UI.btnRebookHotel, tone: 'primary' });
		}
		if (code === 'cancelled' || code === 'refunded') {
			actions.push({ key: 'rebook', label: ORDER_UI.btnRebookHotel, tone: 'primary' });
		}
	}

	if (category === ORDER_CATEGORY_GOODS && code === 'completed' && record.canReview) {
		actions.push({ key: 'review', label: ORDER_UI.btnReview, tone: 'outline' });
	}

	if (category === ORDER_CATEGORY_GOODS && canViewOrderLogistics(record)) {
		actions.unshift({ key: 'viewLogistics', label: ORDER_UI.btnViewLogistics, tone: 'outline' });
	}

	return actions;
}

/** 商户端：列表操作按钮（主操作 + 查看物流） */
export function getMerchantOrderActions(order) {
	const actions = [];
	if (canMerchantViewOrderLogistics(order)) {
		actions.push({ key: 'viewLogistics', label: ORDER_UI.btnViewLogistics, tone: 'outline' });
	}
	const primary = getOrderPrimaryAction(order);
	if (primary) {
		actions.push({
			key: primary.action,
			label: primary.label,
			tone: 'primary'
		});
	}
	return actions;
}

function openHotelList() {
	uni.navigateTo({ url: '/mobile-design/hotel/list' });
}

function openRouteList() {
	uni.navigateTo({ url: '/mobile-design/route/list' });
}

function openRouteDetail(record) {
	const routeId = record?.routeId;
	if (!routeId) {
		openOrderDetail(record);
		return;
	}
	uni.navigateTo({ url: `/mobile-design/route/detail?id=${routeId}` });
}

function openOrderDetail(record) {
	if (!record?.id) return;
	uni.navigateTo({ url: `/pages/order/detail?id=${record.id}` });
}

function openOrderReview(record) {
	if (!record?.id) return;
	uni.navigateTo({ url: `/pages/order/review?id=${record.id}` });
}

function runRouteRefund(record, onChanged) {
	return fetchRefundPreview(record.id).then(async (res) => {
		if (res.code !== 0 || !res.data) {
			uni.showToast({ title: res.msg || ORDER_UI.refundFail, icon: 'none' });
			return false;
		}
		const confirmed = await openRouteRefundPopup({
			preview: res.data,
			onConfirm: async () => {
				try {
					const actionRes = await postOrderAction(record.id, { action: 'refund' });
					if (actionRes.code === 0) {
						uni.showToast({ title: ORDER_UI.refundSuccess, icon: 'success' });
						onChanged?.();
						return true;
					}
					uni.showToast({ title: actionRes.msg || ORDER_UI.refundFail, icon: 'none' });
					return false;
				} catch (e) {
					uni.showToast({ title: ORDER_UI.refundFail, icon: 'none' });
					return false;
				}
			}
		});
		return !!confirmed;
	}).catch(() => {
		uni.showToast({ title: ORDER_UI.refundFail, icon: 'none' });
		return false;
	});
}

/** 用户端执行订单操作 */
export function runUserOrderAction(actionKey, record, { onChanged } = {}) {
	if (!record?.id) return Promise.resolve(false);

	switch (actionKey) {
		case 'cancel':
			return new Promise((resolve) => {
				const isRoute = inferOrderCategory(record) === ORDER_CATEGORY_ROUTE;
				uni.showModal({
					title: isRoute ? ORDER_UI.btnCancelBooking : ORDER_UI.btnCancel,
					content: isRoute ? ORDER_UI.cancelBookingConfirm : ORDER_UI.cancelConfirm,
					success: async (modalRes) => {
						if (!modalRes.confirm) {
							resolve(false);
							return;
						}
						try {
							const res = await cancelOrder(record.id);
							if (res.code === 0) {
								uni.showToast({
									title: isRoute ? ORDER_UI.cancelBookingSuccess : ORDER_UI.cancelSuccess,
									icon: 'success'
								});
								onChanged?.();
								resolve(true);
							} else {
								uni.showToast({ title: res.msg || ORDER_UI.cancelFail, icon: 'none' });
								resolve(false);
							}
						} catch (e) {
							uni.showToast({ title: ORDER_UI.cancelFail, icon: 'none' });
							resolve(false);
						}
					}
				});
			});
		case 'pay':
			return openPayPopup({
				orderNo: record.orderNo,
				amount: record.amount,
				accentColor: resolvePayAccentColor(record),
				priceColor: resolvePayPriceColor(record),
				onSuccess: () => onChanged?.()
			}).then((opened) => {
				if (opened === false && record?.id) {
					openOrderDetail(record);
				}
				return opened !== false;
			});
		case 'refund':
			if (inferOrderCategory(record) === ORDER_CATEGORY_ROUTE) {
				return runRouteRefund(record, onChanged);
			}
			return new Promise((resolve) => {
				uni.showModal({
					title: ORDER_UI.btnRefund,
					content: ORDER_UI.refundConfirm,
					success: async (modalRes) => {
						if (!modalRes.confirm) {
							resolve(false);
							return;
						}
						try {
							const res = await postOrderAction(record.id, { action: 'refund' });
							if (res.code === 0) {
								uni.showToast({ title: ORDER_UI.refundSuccess, icon: 'success' });
								onChanged?.();
								resolve(true);
							} else {
								uni.showToast({ title: res.msg || ORDER_UI.refundFail, icon: 'none' });
								resolve(false);
							}
						} catch (e) {
							uni.showToast({ title: ORDER_UI.refundFail, icon: 'none' });
							resolve(false);
						}
					}
				});
			});
		case 'complete':
			return new Promise((resolve) => {
				uni.showModal({
					title: ORDER_UI.btnConfirmReceive,
					content: ORDER_UI.confirmReceiveContent,
					success: async (modalRes) => {
						if (!modalRes.confirm) {
							resolve(false);
							return;
						}
						try {
							const res = await postOrderAction(record.id, { action: 'complete' });
							if (res.code === 0) {
								uni.showToast({ title: ORDER_UI.confirmReceiveSuccess, icon: 'success' });
								onChanged?.();
								resolve(true);
							} else {
								uni.showToast({ title: res.msg || ORDER_UI.actionFail, icon: 'none' });
								resolve(false);
							}
						} catch (e) {
							uni.showToast({ title: ORDER_UI.actionFail, icon: 'none' });
							resolve(false);
						}
					}
				});
			});
		case 'verifyCode':
			openOrderDetail(record);
			return Promise.resolve(true);
		case 'viewRouteDetail':
			openRouteDetail(record);
			return Promise.resolve(true);
		case 'rebook':
			if (inferOrderCategory(record) === ORDER_CATEGORY_ROUTE) {
				openRouteList();
			} else {
				openHotelList();
			}
			return Promise.resolve(true);
		case 'review':
			openOrderReview(record);
			return Promise.resolve(true);
		case 'viewLogistics':
			openOrderLogistics(record);
			return Promise.resolve(true);
		default:
			return Promise.resolve(false);
	}
}

/** 商户端执行订单操作 */
export async function runMerchantOrderAction(actionKey, order, { onChanged } = {}) {
	if (!order?.id || !actionKey) return false;
	if (actionKey === 'viewLogistics') {
		openOrderLogistics(order, { scope: 'merchant' });
		return true;
	}
	if (actionKey === 'ship') {
		return false;
	}
	try {
		const res = await postMerchantOrderAction(order.id, { action: actionKey });
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '操作失败', icon: 'none' });
			return false;
		}
		uni.showToast({ title: '操作成功', icon: 'success' });
		onChanged?.(res.data);
		return true;
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
		return false;
	}
}

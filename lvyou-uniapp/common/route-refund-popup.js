/**
 * 线路订单退款预览弹框（底部圆角 popup，列表/详情共用）
 */
import { ref, computed } from 'vue';
import { ORDER_UI, formatOrderAmount } from './order-ui.js';

export const showRouteRefundPopup = ref(false);
export const routeRefundPreview = ref(null);
export const routeRefundSubmitting = ref(false);

const refundContext = ref(null);
const refundHosts = [];
let refundHostToken = 0;

function getActiveRouteRefundHost() {
	return refundHosts.length ? refundHosts[refundHosts.length - 1] : null;
}

export function registerRouteRefundHost(host, token) {
	if (host) {
		refundHostToken += 1;
		const currentToken = refundHostToken;
		refundHosts.push({
			token: currentToken,
			openRouteRefundPopup: host.openRouteRefundPopup
		});
		return currentToken;
	}
	if (token == null) return;
	const index = refundHosts.findIndex((item) => item.token === token);
	if (index >= 0) {
		refundHosts.splice(index, 1);
	}
}

export function useGlobalRouteRefundPopup() {
	const ui = ORDER_UI;
	const preview = computed(() => routeRefundPreview.value || {});

	const calcRows = computed(() => {
		const data = preview.value;
		if (!data || data.payAmount == null) return [];
		const deductRatio = Number(data.refundRatio ?? 0);
		const userRefundRatio = data.userRefundRatio != null
			? Number(data.userRefundRatio)
			: Math.max(0, 100 - deductRatio);
		const rows = [
			{ label: '订单实付', value: formatOrderAmount(data.payAmount) },
			{ label: '适用规则', value: data.ruleLabel || '线路退改规则' }
		];
		if (deductRatio > 0) {
			rows.push({
				label: '扣除比例',
				value: `${deductRatio}%（按实付金额扣除后退还余款）`
			});
		}
		rows.push(
			{
				label: '预计退款',
				value: `${formatOrderAmount(data.userRefund)}（约 ${userRefundRatio}%）`,
				highlight: true
			},
			{ label: '到账时间', value: '1-3 个工作日原路返回' }
		);
		return rows;
	});

	const policyText = computed(() => String(preview.value.policySummary || '').trim());

	function closeRouteRefundPopup() {
		if (routeRefundSubmitting.value) return;
		showRouteRefundPopup.value = false;
		routeRefundPreview.value = null;
		refundContext.value = null;
	}

	function openRouteRefundPopup({ preview: data, onConfirm } = {}) {
		routeRefundPreview.value = data || null;
		showRouteRefundPopup.value = true;
		return new Promise((resolve) => {
			refundContext.value = { onConfirm, resolve };
		});
	}

	function onCancelRouteRefund() {
		refundContext.value?.resolve?.(false);
		closeRouteRefundPopup();
	}

	async function onConfirmRouteRefund() {
		if (routeRefundSubmitting.value) return;
		const context = refundContext.value;
		if (typeof context?.onConfirm !== 'function') {
			onCancelRouteRefund();
			return;
		}
		routeRefundSubmitting.value = true;
		try {
			const ok = await context.onConfirm();
			context.resolve?.(!!ok);
			if (ok) {
				routeRefundSubmitting.value = false;
				closeRouteRefundPopup();
			}
		} catch (e) {
			context.resolve?.(false);
		} finally {
			routeRefundSubmitting.value = false;
		}
	}

	return {
		showRouteRefundPopup,
		routeRefundSubmitting,
		ui,
		calcRows,
		policyText,
		openRouteRefundPopup,
		closeRouteRefundPopup,
		onCancelRouteRefund,
		onConfirmRouteRefund
	};
}

/** 任意页面唤起线路退款预览弹框 */
export function openRouteRefundPopup(options = {}) {
	const host = getActiveRouteRefundHost();
	if (!host?.openRouteRefundPopup) {
		uni.showToast({ title: '退款弹窗加载失败，请刷新页面重试', icon: 'none' });
		return Promise.resolve(false);
	}
	return host.openRouteRefundPopup(options);
}

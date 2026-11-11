<template>
	<RouteRefundPopup
		:show="showRouteRefundPopup"
		:ui="ui"
		:calc-rows="calcRows"
		:policy-text="policyText"
		:submitting="routeRefundSubmitting"
		@close="onCancelRouteRefund"
		@confirm="onConfirmRouteRefund"
	/>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import RouteRefundPopup from '@/components/route-refund-popup.vue';
import { registerRouteRefundHost, useGlobalRouteRefundPopup } from '@/common/route-refund-popup.js';

const {
	showRouteRefundPopup,
	routeRefundSubmitting,
	ui,
	calcRows,
	policyText,
	openRouteRefundPopup,
	onCancelRouteRefund,
	onConfirmRouteRefund
} = useGlobalRouteRefundPopup();

let hostToken = 0;

onMounted(() => {
	hostToken = registerRouteRefundHost({ openRouteRefundPopup }, null) || 0;
});

onUnmounted(() => {
	registerRouteRefundHost(null, hostToken);
});
</script>

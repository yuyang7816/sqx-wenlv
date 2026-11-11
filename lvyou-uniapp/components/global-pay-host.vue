<template>
	<BookingPayPopup
		:show="showPayPopup"
		:ui="ui"
		:pay-amount="payAmount"
		:payment-methods="paymentMethods"
		:selected-pay-method="selectedPayMethod"
		:paying="paying"
		:accent-color="accentColor"
		:price-color="priceColor"
		@close="closePayPopup"
		@select="selectPayMethod"
		@confirm="onConfirmPay"
	/>
</template>

<script setup>
import { onMounted, onUnmounted } from 'vue';
import BookingPayPopup from '@/mobile-design/booking/components/booking-pay-popup.vue';
import { registerPayPopupHost, useGlobalPayPopup } from '@/common/pay-popup.js';

const {
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
} = useGlobalPayPopup();

let hostToken = 0;

onMounted(() => {
	hostToken = registerPayPopupHost({ openPayPopup }, null) || 0;
});

onUnmounted(() => {
	registerPayPopupHost(null, hostToken);
});
</script>

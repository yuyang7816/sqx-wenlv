<template>
	<view class="map-picker-page">
		<web-view v-if="pickerUrl" :src="pickerUrl" @message="onMessage" />
		<view v-else class="map-picker-page__empty">
			<text>{{ ui.mapKeyMissing }}</text>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { MERCHANT_UI } from '@/common/merchant-onboarding.js';
import { buildLocationPickerUrl, MAP_LOCATION_PICKED_EVENT } from '@/common/map-config.js';

const ui = MERCHANT_UI;
const pickerUrl = ref('');

onLoad((options) => {
	uni.setNavigationBarTitle({ title: ui.pickOnMap });
	try {
		pickerUrl.value = buildLocationPickerUrl({
			lat: options?.lat,
			lng: options?.lng,
		});
	} catch (error) {
		pickerUrl.value = '';
		uni.showToast({ title: error.message || ui.mapKeyMissing, icon: 'none' });
	}
});

function onMessage(event) {
	const payload = event?.detail?.data ?? event?.data;
	const data = Array.isArray(payload) ? payload[payload.length - 1] : payload;
	if (!data || data.module !== 'locationPicker') return;
	uni.$emit(MAP_LOCATION_PICKED_EVENT, data);
	uni.navigateBack();
}

// #ifdef H5
function onWindowMessage(event) {
	onMessage({ data: event?.data });
}

onMounted(() => {
	window.addEventListener('message', onWindowMessage);
});

onBeforeUnmount(() => {
	window.removeEventListener('message', onWindowMessage);
});
// #endif
</script>

<style lang="scss" scoped>
.map-picker-page,
.map-picker-page web-view {
	width: 100%;
	height: 100vh;
}

.map-picker-page__empty {
	height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
	color: #999999;
	font-size: 28rpx;
}
</style>

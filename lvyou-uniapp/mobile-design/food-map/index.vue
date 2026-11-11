<template>
	<view class="map-redirect-page" />
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { getAppLocationParams } from '@/common/location-params.js';

const pageOptions = ref({});
const redirected = ref(false);

onLoad((options) => {
	pageOptions.value = options || {};
});

onShow(async () => {
	if (redirected.value) {
		return;
	}
	redirected.value = true;
	const locationParams = await getAppLocationParams();
	const queryParts = ['type=food'];
	if (locationParams.lat) {
		queryParts.push(`lat=${locationParams.lat}`);
	}
	if (locationParams.lng) {
		queryParts.push(`lng=${locationParams.lng}`);
	}
	if (pageOptions.value.keyword) {
		queryParts.push(`keyword=${encodeURIComponent(pageOptions.value.keyword)}`);
	}
	uni.redirectTo({ url: `/mobile-design/map/index?${queryParts.join('&')}` });
});
</script>

<style lang="scss" scoped>
.map-redirect-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
}
</style>

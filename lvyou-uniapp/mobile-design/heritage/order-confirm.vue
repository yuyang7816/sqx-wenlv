<template>
	<view class="hoc-redirect">
		<u-loading-icon mode="circle" color="#B45D2D" />
	</view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app';

onLoad((options) => {
	const query = Object.entries(options || {})
		.map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
		.join('&');
	const url = query
		? `/mobile-design/booking/goods-confirm?${query}`
		: '/mobile-design/booking/goods-confirm';
	uni.redirectTo({ url });
});
</script>

<style lang="scss" scoped>
.hoc-redirect {
	min-height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #fff;
}
</style>

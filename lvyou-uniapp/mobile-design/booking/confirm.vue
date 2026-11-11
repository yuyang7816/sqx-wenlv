<template>
	<view class="bc-redirect">
		<u-loading-icon mode="circle" color="#2979FF" />
	</view>
</template>

<script setup>
import { onLoad } from '@dcloudio/uni-app';

const TYPE_PAGE = {
	hotel: 'hotel-confirm',
	route: 'route-confirm',
	goods: 'goods-confirm'
};

onLoad((options) => {
	const type = String(options?.type || 'hotel');
	const page = TYPE_PAGE[type] || 'hotel-confirm';
	const query = Object.entries(options || {})
		.filter(([key]) => key !== 'type')
		.map(([key, value]) => `${key}=${encodeURIComponent(String(value))}`)
		.join('&');
	const url = query
		? `/mobile-design/booking/${page}?${query}`
		: `/mobile-design/booking/${page}`;
	uni.redirectTo({ url });
});
</script>

<style lang="scss" scoped>
.bc-redirect {
	min-height: 100vh;
	display: flex;
	align-items: center;
	justify-content: center;
	background: #fff;
}
</style>

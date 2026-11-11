<script setup>
	import {
		getCurrentInstance
	} from 'vue';
	import GlobalPayHost from '@/components/global-pay-host.vue';
	import GlobalRouteRefundHost from '@/components/global-route-refund-host.vue';
	const {
		proxy
	} = getCurrentInstance();
	import {
		onLaunch,
		onShow,
		onHide
	} from '@dcloudio/uni-app';
	import { loadProductName, getProductName } from '@/common/brand.js';
	import { loadDefaultAvatar } from '@/common/avatar.js';
	import { captureInviteCode } from '@/common/invite-ui.js';

	onLaunch(async (options) => {
		captureInviteCode(options);
		await Promise.all([loadProductName(), loadDefaultAvatar()]);
		uni.setNavigationBarTitle({ title: getProductName() });
		// //企业微信客服链接
		// proxy.$http.get('/app/common/type/274').then(res => {
		// 	if (res.code == 0 && res.data.value) {
		// 		uni.setStorageSync('kefu', res.data.value)
		// 	}
		// })
		// //企业微信客服appid
		// proxy.$http.get('/app/common/type/275').then(res => {
		// 	if (res.code == 0 && res.data.value) {
		// 		uni.setStorageSync('kefuAppId', res.data.value)
		// 	}
		// })
		// //客服类型  328
		// proxy.$http.get('/app/common/type/328').then(res => {
		// 	if (res.code == 0 && res.data.value) {
		// 		uni.setStorageSync('SelKeFu', res.data.value)
		// 	}
		// })
		// //客服电话  329
		// proxy.$http.get('/app/common/type/329').then(res => {
		// 	if (res.code == 0 && res.data.value) {
		// 		uni.setStorageSync('kefuPhone', res.data.value)
		// 	}
		// })
	});
	onShow((options) => {
		captureInviteCode(options);
	});
	onHide(() => {
		console.log('onHide')
	});
</script>

<template>
	<GlobalPayHost />
	<GlobalRouteRefundHost />
</template>

<style lang="scss">
	/*每个页面公共css */
	// 荣e游 Shell Design Tokens（须在 uview 之前引入 CSS 变量）
	@import "@/static/css/shell-tokens.scss";
	//uview-plus公共样式
	@import "@/uni_modules/uview-plus/index.scss";
	//自定义公共样式
	@import "@/static/css/main.css";
	//通用按钮样式
	@import "@/static/css/button.css";

	page {
		background-color: var(--ry-page-bg, #F5F7FF);
		color: var(--ry-text-primary, #333333);
	}

</style>

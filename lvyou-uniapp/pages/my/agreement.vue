<template>
	<view class="agreement-page ry-page">
		<view class="agreement-body">
			<view v-if="pageLoading" class="agreement-loading">
				<u-loading-icon color="#FF6600" />
				<text class="agreement-loading__text">加载中...</text>
			</view>
			<view v-else-if="agreement" class="agreement-card ry-card">
				<text class="agreement-card__title">{{ agreement.title }}</text>
				<rich-text class="agreement-card__content" :nodes="agreement.content" />
			</view>
			<view v-else class="agreement-empty">
				<text class="agreement-empty__text">协议不存在或暂未配置</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { fetchAgreement } from '@/common/api/config.js';
import { getAgreementTitle } from '@/common/agreement.js';

const agreement = ref(null);
const pageLoading = ref(false);

/** onLoad 拉取协议正文（公开接口） */
async function loadAgreement(key) {
	if (!key || pageLoading.value) {
		return;
	}
	pageLoading.value = true;
	try {
		const res = await fetchAgreement(key);
		if (res.code === 0 && res.data?.content) {
			agreement.value = {
				key: res.data.key || key,
				title: getAgreementTitle(res.data.key || key),
				content: res.data.content
			};
			uni.setNavigationBarTitle({ title: agreement.value.title });
		} else {
			agreement.value = null;
			uni.setNavigationBarTitle({ title: '协议详情' });
			uni.showToast({ title: res.msg || '协议不存在', icon: 'none' });
		}
	} catch (e) {
		agreement.value = null;
		uni.setNavigationBarTitle({ title: '协议详情' });
		uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

onLoad((options) => {
	const type = (options?.type || '').trim().toLowerCase();
	if (!type) {
		uni.setNavigationBarTitle({ title: '协议详情' });
		uni.showToast({ title: '协议不存在', icon: 'none' });
		return;
	}
	loadAgreement(type);
});
</script>

<style lang="scss" scoped>
	.agreement-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg);
	}

	.agreement-body {
		padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	}

	.agreement-loading {
		padding: 48rpx 0;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
	}

	.agreement-loading__text {
		font-size: 26rpx;
		color: #999;
	}

	.agreement-empty {
		padding: 48rpx 24rpx;
		text-align: center;
	}

	.agreement-empty__text {
		font-size: 28rpx;
		color: #999;
	}

	.agreement-card {
		padding: 28rpx 24rpx;
	}

	.agreement-card__title {
		display: block;
		font-size: 34rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		line-height: 1.4;
		margin-bottom: 24rpx;
	}

	.agreement-card__content {
		display: block;
		font-size: 28rpx;
		color: var(--ry-text-primary);
		line-height: 1.8;
	}
</style>

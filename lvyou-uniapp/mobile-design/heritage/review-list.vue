<template>
	<view class="review-list-page">
		<view class="review-list-body">
			<view v-if="!loading && reviews.length === 0" class="review-empty">
				<text class="review-empty__text">暂无评价</text>
			</view>
			<view v-for="item in reviews" :key="item.id" class="review-card ry-card">
				<text class="review-card__user">{{ item.user }}</text>
				<text class="review-card__content">{{ item.content }}</text>
				<text class="review-card__time">{{ item.time }}</text>
			</view>
			<view v-if="loading && reviews.length" class="review-loadmore">
				<text class="review-loadmore__text">加载中...</text>
			</view>
			<view v-else-if="!hasMore && reviews.length" class="review-loadmore">
				<text class="review-loadmore__text">没有更多了</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { fetchGoodsReviews, mapReviewItem } from '@/common/api/heritage.js';

const PAGE_SIZE = 10;

const goodsId = ref('');
const reviews = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);

async function loadReviews(reset = false) {
	if (!goodsId.value || loading.value) return;
	if (!reset && !hasMore.value) return;
	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}
	loading.value = true;
	try {
		const res = await fetchGoodsReviews(goodsId.value, { page: page.value, limit: PAGE_SIZE });
		if (res.code === 0 && res.data) {
			const records = (res.data.records || []).map(mapReviewItem);
			reviews.value = reset ? records : [...reviews.value, ...records];
			const total = Number(res.data.total ?? 0);
			hasMore.value = records.length >= PAGE_SIZE && reviews.value.length < total;
		} else {
			if (reset) reviews.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) reviews.value = [];
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

onLoad((options) => {
	goodsId.value = options?.goodsId || '';
	if (options?.title) {
		uni.setNavigationBarTitle({ title: `${decodeURIComponent(options.title)}·评价` });
	}
	loadReviews(true);
});

onPullDownRefresh(async () => {
	await loadReviews(true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasMore.value || loading.value) return;
	page.value += 1;
	loadReviews(false);
});
</script>

<style lang="scss" scoped>
	.review-list-page {
		min-height: 100vh;
		background-color: #FAF6F3;
	}

	.review-list-body {
		padding: 20rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 24rpx;
	}

	.review-empty {
		display: flex;
		justify-content: center;
		padding: 80rpx 48rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;

		&__text {
			font-size: 28rpx;
			font-weight: 600;
			color: #8D6E63;
		}
	}

	.review-loadmore {
		padding: 24rpx 0;
		text-align: center;

		&__text {
			font-size: 24rpx;
			color: #CCCCCC;
		}
	}

	.review-card {
		padding: 28rpx;
		border-radius: var(--ry-radius-lg);
		background-color: #FFFFFF;
		box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);

		&__user {
			display: block;
			font-size: 26rpx;
			font-weight: 600;
			color: #4A3728;
			margin-bottom: 12rpx;
		}

		&__content {
			display: block;
			font-size: 26rpx;
			color: #666666;
			line-height: 1.7;
		}

		&__time {
			display: block;
			margin-top: 12rpx;
			font-size: 22rpx;
			color: #CCCCCC;
		}
	}
</style>

<template>
	<view class="rr-page">
		<view class="rr-body">
			<view v-if="!loading && reviews.length === 0" class="rr-empty">
				<text class="rr-empty__text">{{ ui.reviewEmpty }}</text>
			</view>
			<view v-for="item in reviews" :key="item.id" class="rr-card ry-card">
				<view class="rr-card__head">
					<view class="rr-card__user-row">
						<image class="rr-card__avatar" :src="resolveAvatar(item.avatar)" mode="aspectFill" />
						<text class="rr-card__user">{{ item.user }}</text>
					</view>
					<view class="rr-card__stars">
						<u-icon v-for="index in 5" :key="index" :name="index <= item.rating ? 'star-fill' : 'star'"
							:color="index <= item.rating ? accentColor : '#E5E7EB'" :size="12" />
					</view>
				</view>
				<text class="rr-card__content">{{ item.content }}</text>
				<view v-if="item.images && item.images.length" class="rr-card__images">
					<image v-for="(img, idx) in item.images" :key="idx" class="rr-card__image" :src="img"
						mode="aspectFill" @click="previewImages(item.images, idx)" />
				</view>
				<text class="rr-card__time">{{ item.time }}</text>
			</view>
			<view v-if="loading && reviews.length" class="rr-loadmore">
				<text class="rr-loadmore__text">加载中...</text>
			</view>
			<view v-else-if="!hasMore && reviews.length" class="rr-loadmore">
				<text class="rr-loadmore__text">没有更多了</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { getModuleColor } from '@/common/theme.js';
import { resolveAvatar } from '@/common/avatar.js';
import { fetchRouteStoreReviews, mapRouteReviewItem } from '@/common/api/route.js';
import { ORDER_UI } from '@/common/order-ui.js';

const ui = ORDER_UI;
const accentColor = getModuleColor('route');
const PAGE_SIZE = 10;

const merchantId = ref('');
const reviews = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);

async function loadReviews(reset = false) {
	if (!merchantId.value || loading.value) return;
	if (!reset && !hasMore.value) return;
	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}
	loading.value = true;
	try {
		const res = await fetchRouteStoreReviews(merchantId.value, { page: page.value, limit: PAGE_SIZE });
		if (res.code === 0 && res.data) {
			const records = (res.data.records || []).map(mapRouteReviewItem);
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

function previewImages(images, currentIndex) {
	if (!images || !images.length) return;
	uni.previewImage({ urls: images, current: currentIndex });
}

onLoad((options) => {
	merchantId.value = options?.merchantId || options?.id || '';
	if (options?.title) {
		uni.setNavigationBarTitle({ title: `${decodeURIComponent(options.title)}·评价` });
	} else {
		uni.setNavigationBarTitle({ title: '全部评价' });
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
.rr-page {
	min-height: 100vh;
	background: #fff9f0;
}

.rr-body {
	padding: 20rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.rr-empty {
	display: flex;
	justify-content: center;
	padding: 80rpx 48rpx;
	border-radius: var(--ry-radius-lg);
	background: #fff;

	&__text {
		font-size: 28rpx;
		color: #999;
	}
}

.rr-card {
	padding: 28rpx;

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		margin-bottom: 12rpx;
	}

	&__user-row {
		display: flex;
		align-items: center;
		gap: 12rpx;
		min-width: 0;
		flex: 1;
	}

	&__avatar {
		width: 64rpx;
		height: 64rpx;
		border-radius: 50%;
		background: #f0f0f0;
		flex-shrink: 0;
	}

	&__user {
		font-size: 28rpx;
		font-weight: 600;
		color: #1a1a1a;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__stars {
		display: flex;
		gap: 4rpx;
		flex-shrink: 0;
	}

	&__content {
		display: block;
		font-size: 28rpx;
		line-height: 1.6;
		color: #333;
	}

	&__images {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-top: 16rpx;
	}

	&__image {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		background: #f5f5f5;
		object-fit: cover;
	}

	&__time {
		display: block;
		margin-top: 12rpx;
		font-size: 22rpx;
		color: #aaa;
	}
}

.rr-loadmore {
	padding: 24rpx 0;
	text-align: center;

	&__text {
		font-size: 24rpx;
		color: #ccc;
	}
}
</style>

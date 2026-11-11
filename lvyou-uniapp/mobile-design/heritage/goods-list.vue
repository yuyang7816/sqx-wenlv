<template>
	<view class="goods-list-page">
		<view class="goods-tabs-fixed">
			<view class="goods-tabs">
				<view class="goods-tabs__row">
					<view v-for="(tab, index) in categoryTabs" :key="tab.key" class="goods-tab"
						:class="{ 'goods-tab--active': currentTab === index }" @click="onTabClick(index)">
						<text class="goods-tab__text">{{ tab.name }}</text>
						<view v-if="tab.key === 'price'" class="goods-tab__arrows">
							<u-icon name="arrow-up-fill" :size="8" :color="priceArrowColor(index, 'asc')" />
							<u-icon name="arrow-down-fill" :size="8" :color="priceArrowColor(index, 'desc')" />
						</view>
					</view>
				</view>
			</view>
		</view>
		<view class="goods-tabs-spacer" />

		<view class="goods-list-body">
			<!-- <view class="goods-banner">
				<view class="goods-banner__content">
					<text class="goods-banner__title">非遗好物</text>
					<text class="goods-banner__subtitle">{{ shopName }}</text>
					<view class="goods-banner__count">
						<text class="goods-banner__count-num">{{ displayCount }}</text>
						<text class="goods-banner__count-label">件 · {{ currentTabLabel }}</text>
					</view>
				</view>
			</view> -->
			<view class="goods-list">
				<view v-if="!loading && goodsList.length === 0" class="goods-empty">
					<view class="goods-empty__icon"><u-icon name="bag" color="#D7CCC8" :size="64" /></view>
					<text class="goods-empty__text">暂无商品</text>
				</view>
				<view v-for="item in goodsList" :key="item.id" class="goods-card" @click="onGoodsClick(item)">
					<view class="goods-card__cover">
						<image class="goods-card__img" :src="item.cover || PLACEHOLDER_LOGO" mode="aspectFill" />
					</view>
					<view class="goods-card__body">
						<text class="goods-card__name">{{ item.name }}</text>
						<text class="goods-card__desc">{{ item.desc }}</text>
						<view class="goods-card__foot">
							<view class="goods-card__price">
								<text class="goods-card__price-symbol">¥</text>
								<text class="goods-card__price-value">{{ item.price }}</text>
								<text v-if="item.priceFrom" class="goods-card__price-from">起</text>
							</view>
							<text class="goods-card__sales">已售 {{ item.sales }}</text>
						</view>
					</view>
				</view>
				<view v-if="loading && goodsList.length" class="goods-loadmore">
					<text class="goods-loadmore__text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && goodsList.length" class="goods-loadmore">
					<text class="goods-loadmore__text">没有更多了</text>
				</view>
			</view>
			<text class="goods-footer">非遗 · 匠心好物</text>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onLoad,
		onPullDownRefresh,
		onReachBottom
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO
	} from '@/common/theme.js';
	import {
		fetchGoodsList,
		mapGoodsItem
	} from '@/common/api/heritage.js';

	const PAGE_SIZE = 10;

	const shopName = ref('非遗好物');
	const shopId = ref('');
	const goodsList = ref([]);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);
	const total = ref(0);

	const categoryTabs = [{
			key: 'all',
			name: '全部'
		},
		{
			key: 'price',
			name: '价格'
		},
		{
			key: 'new',
			name: '新品'
		},
		{
			key: 'hot',
			name: '热销'
		}
	];
	const currentTab = ref(0);
	const priceSortAsc = ref(true);

	const displayCount = computed(() => total.value || goodsList.value.length);

	const currentTabLabel = computed(() => {
		const tab = categoryTabs[currentTab.value];
		if (!tab) return '全部';
		if (tab.key === 'price') {
			return priceSortAsc.value ? '价格从低到高' : '价格从高到低';
		}
		return tab.name;
	});

	function priceArrowColor(index, direction) {
		const active = currentTab.value === index;
		const isAsc = direction === 'asc';
		if (!active) {
			return '#BBBBBB';
		}
		if (isAsc) {
			return priceSortAsc.value ? '#FFFFFF' : 'rgba(255,255,255,0.45)';
		}
		return priceSortAsc.value ? 'rgba(255,255,255,0.45)' : '#FFFFFF';
	}

	function buildListParams() {
		const tabKey = categoryTabs[currentTab.value]?.key || 'all';
		const params = {
			page: page.value,
			limit: PAGE_SIZE
		};
		if (shopId.value) {
			params.shopId = shopId.value;
		}
		if (tabKey !== 'all') {
			params.sort = tabKey;
		}
		if (tabKey === 'price') {
			params.priceOrder = priceSortAsc.value ? 'asc' : 'desc';
		}
		return params;
	}

	async function loadGoodsList(reset = false) {
		if (loading.value) return;
		if (!reset && !hasMore.value) return;
		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}
		loading.value = true;
		try {
			const res = await fetchGoodsList(buildListParams());
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapGoodsItem);
				goodsList.value = reset ? records : [...goodsList.value, ...records];
				total.value = Number(res.data.total ?? 0);
				hasMore.value = records.length >= PAGE_SIZE && goodsList.value.length < total.value;
			} else {
				if (reset) {
					goodsList.value = [];
					total.value = 0;
				}
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) {
				goodsList.value = [];
				total.value = 0;
			}
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	function onTabClick(index) {
		const tab = categoryTabs[index];
		if (!tab) return;
		if (tab.key === 'price' && currentTab.value === index) {
			priceSortAsc.value = !priceSortAsc.value;
			loadGoodsList(true);
			return;
		}
		currentTab.value = index;
		if (tab.key === 'price') {
			priceSortAsc.value = true;
		}
		loadGoodsList(true);
	}

	const onGoodsClick = (item) => {
		uni.navigateTo({
			url: `/mobile-design/heritage/goods-detail?id=${item.id}`
		});
	};

	onLoad((options) => {
		if (options?.shopName) shopName.value = decodeURIComponent(options.shopName);
		if (options?.shopId) shopId.value = options.shopId;
		loadGoodsList(true);
	});

	onPullDownRefresh(async () => {
		await loadGoodsList(true);
		uni.stopPullDownRefresh();
	});

	onReachBottom(() => {
		if (!hasMore.value || loading.value) return;
		page.value += 1;
		loadGoodsList(false);
	});
</script>

<style lang="scss" scoped>
	.goods-list-page {
		min-height: 100vh;
		background-color: #faf6f3;
	}

	.goods-tabs-fixed {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 100;
		box-sizing: border-box;
		padding: 12rpx var(--ry-page-padding-x);
		background-color: #faf6f3;
		box-shadow: 0 4rpx 16rpx rgba(74, 55, 40, 0.06);
	}

	.goods-tabs-spacer {
		height: 92rpx;
		flex-shrink: 0;
	}

	.goods-list-body {
		box-sizing: border-box;
		padding: 20rpx var(--ry-page-padding-x) calc(64rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 24rpx;
	}

	.goods-banner {
		position: relative;
		padding: 32rpx 28rpx 28rpx;
		border-radius: var(--ry-radius-lg);
		background: linear-gradient(135deg, #4a3728 0%, #5d4037 50%, #8d6e63 100%);
		overflow: hidden;
		box-shadow: 0 12rpx 40rpx rgba(74, 55, 40, 0.22);

		&__content {
			position: relative;
			z-index: 1;
		}

		&__title {
			display: block;
			font-size: 36rpx;
			font-weight: 800;
			color: #ffffff;
			letter-spacing: 4rpx;
		}

		&__subtitle {
			display: block;
			margin-top: 8rpx;
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.7);
		}

		&__count {
			display: flex;
			align-items: baseline;
			gap: 6rpx;
			margin-top: 20rpx;
			padding-top: 16rpx;
			border-top: 1rpx solid rgba(255, 255, 255, 0.2);
		}

		&__count-num {
			font-size: 40rpx;
			font-weight: 800;
			color: #ffffff;
		}

		&__count-label {
			font-size: 24rpx;
			color: rgba(255, 255, 255, 0.7);
		}
	}

	.goods-tabs {
		width: 100%;
	}

	.goods-tabs__row {
		display: flex;
		width: 100%;
		gap: 12rpx;
	}

	.goods-tab {
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 4rpx;
		height: 68rpx;
		padding: 0 8rpx;
		border-radius: var(--ry-radius-pill);
		background-color: #ffffff;
		border: 1rpx solid #f0ebe6;
		box-sizing: border-box;

		&__text {
			font-size: 26rpx;
			font-weight: 500;
			line-height: 1;
			color: #8d6e63;
			white-space: nowrap;
		}

		&__arrows {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 0;
			line-height: 1;
			margin-left: 2rpx;
		}

		&--active {
			background: linear-gradient(135deg, #8d6e63, #5d4037);
			border-color: transparent;
			box-shadow: 0 6rpx 20rpx rgba(93, 64, 55, 0.28);

			.goods-tab__text {
				color: #ffffff;
				font-weight: 700;
			}
		}
	}

	.goods-list {
		display: grid;
		grid-template-columns: repeat(2, 1fr);
		gap: 16rpx;
	}

	.goods-empty {
		grid-column: 1 / -1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 80rpx 48rpx 100rpx;

		&__icon {
			width: 120rpx;
			height: 120rpx;
			border-radius: 50%;
			background-color: #f5f0eb;
			display: flex;
			align-items: center;
			justify-content: center;
			margin-bottom: 24rpx;
		}

		&__text {
			font-size: 28rpx;
			font-weight: 600;
			color: #8d6e63;
		}
	}

	.goods-loadmore {
		grid-column: 1 / -1;
		padding: 24rpx 0;
		text-align: center;

		&__text {
			font-size: 24rpx;
			color: #cccccc;
		}
	}

	.goods-card {
		border-radius: var(--ry-radius-lg);
		background-color: #ffffff;
		box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.08);
		overflow: hidden;

		&:active {
			opacity: 0.92;
		}

		&__cover {
			width: 100%;
			height: 320rpx;
			background-color: #f5f0eb;
			overflow: hidden;
		}

		&__img {
			width: 100%;
			height: 100%;
			display: block;
		}

		&__body {
			padding: 16rpx 16rpx 20rpx;
		}

		&__name {
			display: block;
			font-size: 26rpx;
			font-weight: 700;
			color: #4a3728;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__desc {
			display: block;
			margin-top: 6rpx;
			font-size: 20rpx;
			color: #999999;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__foot {
			display: flex;
			align-items: flex-end;
			justify-content: space-between;
			margin-top: 12rpx;
		}

		&__price {
			display: flex;
			align-items: baseline;
		}

		&__price-symbol {
			font-size: 20rpx;
			font-weight: 600;
			color: #b45d2d;
		}

		&__price-value {
			margin-left: 2rpx;
			font-size: 32rpx;
			font-weight: 800;
			color: #b45d2d;
		}

		&__price-from {
			margin-left: 4rpx;
			font-size: 20rpx;
			font-weight: 600;
			color: #b45d2d;
		}

		&__sales {
			font-size: 20rpx;
			color: #cccccc;
		}
	}

	.goods-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #cccccc;
		padding-top: 8rpx;
	}
</style>

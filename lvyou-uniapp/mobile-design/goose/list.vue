<template>
	<view class="goose-list-page">
		<view class="goose-list-body">
			<view class="goose-topbar">
				<view class="goose-search">
					<u-icon name="search" color="#FF7A00" :size="18" />
					<input v-model="searchKeyword" class="goose-search__input" type="text" confirm-type="search"
						placeholder="搜索卤鹅门店、地址或特色" placeholder-class="goose-search__placeholder" />
					<u-icon v-if="searchKeyword" name="close-circle-fill" color="#CCCCCC" :size="16"
						@click="searchKeyword = ''" />
				</view>

				<view class="goose-filters">
					<view class="goose-filters__row">
						<view class="goose-filter" :class="{ 'goose-filter--active': sortKey === 'recommend' }"
							@click="onRecommendTap">
							<text class="goose-filter__text">综合推荐</text>
						</view>
						<view class="goose-filter" :class="{ 'goose-filter--active': sortKey === 'sales' }"
							@click="onSalesTap">
							<text class="goose-filter__text">预约量</text>
						</view>
						<view class="goose-filter" :class="{ 'goose-filter--active': sortKey === 'price' }"
							@click="onPriceFilterTap">
							<text class="goose-filter__text">价格</text>
							<view class="goose-filter__arrows">
								<view class="goose-filter__arrow" @click.stop="onPriceSort('asc')">
									<u-icon name="arrow-up-fill" :size="8" :color="sortArrowColor('price', 'asc')" />
								</view>
								<view class="goose-filter__arrow" @click.stop="onPriceSort('desc')">
									<u-icon name="arrow-down-fill" :size="8" :color="sortArrowColor('price', 'desc')" />
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>

			<view class="goose-list">
				<view v-if="!loading && displayList.length === 0" class="goose-list-empty">
					<u-empty mode="list" text="暂无灰鹅美食门店" />
				</view>
				<template v-else>
					<view v-for="shop in displayList" :key="shop.id" class="goose-card">
						<view class="goose-card__cover" @click="onViewDetail(shop)">
							<view class="goose-card__img-wrap">
								<image class="goose-card__img ry-placeholder-img" :src="shop.cover || PLACEHOLDER_LOGO"
									mode="aspectFill" />
							</view>
							<view class="goose-card__pill goose-card__pill--sales">
								<text class="goose-card__pill-text">已预约数 {{ shop.monthlySales ?? 0 }}</text>
							</view>
						</view>

						<view class="goose-card__body">
							<view class="goose-card__head" @click="onViewDetail(shop)">
								<text class="goose-card__name">{{ shop.name }}</text>
								<view v-if="shop.priceFrom != null" class="goose-card__price">
									<text class="goose-card__price-symbol">¥</text>
									<text class="goose-card__price-num">{{ shop.priceFrom }}</text>
									<text class="goose-card__price-unit">起</text>
								</view>
								<text v-else class="goose-card__price goose-card__price--muted">待定</text>
							</view>

							<view class="goose-card__addr-row" @click="onViewDetail(shop)">
								<u-icon name="map" color="#C8C8C8" :size="12" />
								<text class="goose-card__addr">{{ shop.address }}</text>
							</view>

							<view v-if="shop.tags && shop.tags.length" class="goose-card__tags">
								<view v-for="tag in shop.tags" :key="tag" class="goose-card__tag">
									<text class="goose-card__tag-text">{{ tag }}</text>
								</view>
							</view>

							<view class="goose-card__actions"
								:class="{ 'goose-card__actions--with-queue': hasQueueInfo(shop) }">
								<view v-if="hasQueueInfo(shop)" class="goose-card__queue-tag goose-card__queue-tag--busy">
									<text class="goose-card__queue-text">{{ shop.queueDisplayText }}</text>
								</view>
								<view class="goose-card__btn goose-card__btn--ghost" @click="onCanteenReserve(shop)">
									<text class="goose-card__btn-text goose-card__btn-text--ghost">餐饮预约</text>
								</view>
								<view class="goose-card__btn goose-card__btn--primary" @click="onViewDetail(shop)">
									<text class="goose-card__btn-text">查看详情</text>
									<u-icon name="arrow-right" color="#FFFFFF" :size="12" />
								</view>
							</view>
						</view>
					</view>
				</template>
				<view v-if="loading && shopList.length" class="goose-list-loadmore">
					<text class="goose-list-loadmore__text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && shopList.length" class="goose-list-loadmore">
					<text class="goose-list-loadmore__text">没有更多了</text>
				</view>
			</view>

			<text class="goose-footer">非遗美食 · 地道卤味</text>
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
		buildLocationListQuery
	} from '@/common/location-params.js';
	import {
		fetchGooseList
	} from '@/common/api/goose.js';

	const DETAIL_PATH = '/mobile-design/goose/detail';
	const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';
	const PAGE_SIZE = 10;

	const sortKey = ref('recommend');
	const priceSortAsc = ref(true);
	const shopList = ref([]);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);
	const totalCount = ref(0);
	const searchKeyword = ref('');

	function hasQueueInfo(shop) {
		return !!(shop?.queueEnabled && shop.queueTableCount > 0 && shop.queueDisplayText);
	}

	function mapShopItem(record) {
		return {
			id: record.id,
			name: record.name || '',
			address: record.address || '',
			cover: record.cover || PLACEHOLDER_LOGO,
			monthlySales: record.monthlySales ?? 0,
			priceFrom: record.priceFrom != null ? Number(record.priceFrom) : null,
			tags: record.tags || [],
			isOpen: record.isOpen,
			queueEnabled: !!record.queueEnabled,
			queueTableCount: Number(record.queueTableCount ?? 0),
			queueDisplayText: record.queueDisplayText || ''
		};
	}

	function sortArrowColor(filterKey, direction) {
		if (sortKey.value !== filterKey) {
			return '#CCCCCC';
		}
		const isAsc = direction === 'asc';
		if (filterKey === 'price') {
			return priceSortAsc.value === isAsc ? '#FF7A00' : '#CCCCCC';
		}
		return '#CCCCCC';
	}

	const displayList = computed(() => {
		const keyword = searchKeyword.value.trim().toLowerCase();
		const list = shopList.value
			.filter(item => !keyword || [item.name, item.address, ...(item.tags || [])]
				.some(value => String(value || '').toLowerCase().includes(keyword)))
			.map(item => ({
				...item
			}));
		switch (sortKey.value) {
			case 'sales':
				return list.sort((a, b) => Number(b.monthlySales) - Number(a.monthlySales));
			case 'price':
				return list.sort((a, b) => (priceSortAsc.value ? (a.priceFrom ?? 0) - (b.priceFrom ?? 0) : (b.priceFrom ??
					0) - (a.priceFrom ?? 0)));
			default:
				return list;
		}
	});

	/** 分页拉取灰鹅美食商家列表（公开接口） */
	async function loadShopList(reset = false) {
		if (loading.value) {
			return;
		}
		if (!reset && !hasMore.value) {
			return;
		}
		if (reset) {
			page.value = 1;
			hasMore.value = true;
		}

		loading.value = true;
		const params = await buildLocationListQuery({
			page: page.value,
			limit: PAGE_SIZE
		});

		try {
			const res = await fetchGooseList(params);
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapShopItem);
				shopList.value = reset ? records : [...shopList.value, ...records];
				totalCount.value = Number(res.data.total ?? shopList.value.length);
				hasMore.value = records.length >= PAGE_SIZE && shopList.value.length < totalCount.value;
			} else {
				if (reset) {
					shopList.value = [];
					totalCount.value = 0;
				}
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) {
				shopList.value = [];
				totalCount.value = 0;
			}
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	function onRecommendTap() {
		if (sortKey.value === 'recommend') {
			return;
		}
		sortKey.value = 'recommend';
	}

	function onSalesTap() {
		if (sortKey.value === 'sales') {
			return;
		}
		sortKey.value = 'sales';
	}

	function onPriceFilterTap() {
		if (sortKey.value !== 'price') {
			onPriceSort('asc');
			return;
		}
		onPriceSort(priceSortAsc.value ? 'desc' : 'asc');
	}

	function onPriceSort(direction) {
		const asc = direction === 'asc';
		sortKey.value = 'price';
		priceSortAsc.value = asc;
	}

	const navigateSafe = (url) => {
		uni.navigateTo({
			url
		});
	};

	const onViewDetail = (shop) => {
		navigateSafe(`${DETAIL_PATH}?id=${shop.id}&name=${encodeURIComponent(shop.name)}`);
	};

	const onCanteenReserve = (shop) => {
		const parts = [
			`shopId=${shop.id}`,
			`shopName=${encodeURIComponent(shop.name || '')}`
		];
		if (shop.address) {
			parts.push(`shopAddress=${encodeURIComponent(shop.address)}`);
		}
		navigateSafe(`${CANTEEN_RESERVE_PATH}?${parts.join('&')}`);
	};

	onLoad(async () => {
		await loadShopList(true);
	});

	onPullDownRefresh(async () => {
		await loadShopList(true);
		uni.stopPullDownRefresh();
	});

	onReachBottom(() => {
		if (!hasMore.value || loading.value) {
			return;
		}
		page.value += 1;
		loadShopList(false);
	});
</script>

<style lang="scss" scoped>
	.goose-list-page {
		min-height: 100vh;
		background-color: #FFF9F2;
	}

	.goose-list-body {
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
		display: flex;
		flex-direction: column;
		gap: 24rpx;
	}

	/* �?v�*E �?*/
	.goose-legacy {
		display: none;
		position: relative;
		padding: 40rpx 32rpx 36rpx;
		border-radius: var(--ry-radius-lg);
		background: linear-gradient(135deg, #FF8A00 0%, #FF7A00 45%, #FF6A00 100%);
		overflow: hidden;
		box-shadow: 0 12rpx 40rpx rgba(255, 122, 0, 0.28);

		&__glow {
			position: absolute;
			top: -60rpx;
			right: -40rpx;
			width: 240rpx;
			height: 240rpx;
			border-radius: 50%;
			background: radial-gradient(circle, rgba(255, 255, 255, 0.25) 0%, transparent 70%);
		}

		&__badge {
			position: absolute;
			top: 28rpx;
			right: 28rpx;
			padding: 6rpx 18rpx;
			border-radius: var(--ry-radius-pill);
			background-color: rgba(255, 255, 255, 0.22);
			border: 1rpx solid rgba(255, 255, 255, 0.35);
		}

		&__badge-text {
			font-size: 22rpx;
			font-weight: 600;
			color: rgba(255, 255, 255, 0.95);
			letter-spacing: 2rpx;
		}

		&__content {
			position: relative;
			z-index: 1;
		}

		&__title {
			display: block;
			font-size: 48rpx;
			font-weight: 800;
			color: #FFFFFF;
			letter-spacing: 6rpx;
		}

		&__subtitle {
			display: block;
			margin-top: 10rpx;
			font-size: 26rpx;
			color: rgba(255, 255, 255, 0.8);
			letter-spacing: 4rpx;
		}

		&__stats {
			display: flex;
			align-items: center;
			margin-top: 32rpx;
			padding-top: 24rpx;
			border-top: 1rpx solid rgba(255, 255, 255, 0.25);
		}

		&__stat {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 6rpx;
		}

		&__stat-num {
			font-size: 32rpx;
			font-weight: 800;
			color: #FFFFFF;
		}

		&__stat-label {
			font-size: 22rpx;
			color: rgba(255, 255, 255, 0.7);
		}

		&__stat-divider {
			width: 1rpx;
			height: 44rpx;
			background-color: rgba(255, 255, 255, 0.25);
		}
	}

	.goose-topbar {
		position: sticky;
		top: 0;
		z-index: 10;
		padding: 20rpx var(--ry-page-padding-x) 16rpx;
		background-color: #FFFFFF;
		box-shadow: 0 8rpx 24rpx rgba(255, 122, 0, 0.08);
		border-bottom: 1rpx solid rgba(255, 122, 0, 0.06);
	}

	.goose-search {
		display: flex;
		align-items: center;
		gap: 12rpx;
		width: 100%;
		height: 72rpx;
		padding: 0 24rpx;
		border-radius: var(--ry-radius-pill);
		background-color: #FFFFFF;
		box-shadow: 0 4rpx 16rpx rgba(255, 122, 0, 0.12);
		box-sizing: border-box;
	}

	.goose-search__input {
		flex: 1;
		min-width: 0;
		height: 72rpx;
		font-size: 26rpx;
		color: #333333;
	}

	.goose-search__placeholder {
		color: #999999;
	}

	.goose-filters {
		width: 100%;
		margin-top: 16rpx;
		padding: 8rpx 0 4rpx;
	}

	.goose-filters__row {
		display: flex;
		align-items: center;
		justify-content: space-around;
		width: 100%;
	}

	.goose-filter {
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		min-width: 0;
		padding: 8rpx 12rpx;

		&:active {
			opacity: 0.75;
		}

		&__text {
			font-size: 26rpx;
			font-weight: 400;
			color: #666666;
			line-height: 1;
		}

		&__arrows {
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			gap: 0;
			line-height: 0;
			margin-left: 2rpx;
		}

		&__arrow {
			display: flex;
			align-items: center;
			justify-content: center;
			height: 18rpx;
			padding: 0 4rpx;
			overflow: hidden;

			&+& {
				margin-top: -4rpx;
			}
		}

		&--active {
			.goose-filter__text {
				color: #FF7A00;
				font-weight: 400;
			}
		}
	}

	/* �?F�aG �?*/
	.goose-list {
		display: flex;
		flex-direction: column;
		gap: 20rpx;
		padding: 8rpx var(--ry-page-padding-x) 0;
	}

	.goose-list-empty {
		padding: 120rpx 0 80rpx;
	}

	.goose-list-loadmore {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 24rpx 0 8rpx;
	}

	.goose-list-loadmore__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	.goose-card {
		border-radius: 20rpx;
		background-color: #FFFFFF;
		box-shadow: 0 6rpx 24rpx rgba(255, 122, 0, 0.07);
		overflow: hidden;

		&__cover {
			position: relative;
			width: 100%;
			height: 296rpx;
			background-color: #FFF3EB;
		}

		&__img-wrap {
			width: 100%;
			height: 100%;
			overflow: hidden;
			border-radius: 20rpx 20rpx 0 0;
			transform: translateZ(0);
		}

		&__img {
			width: 100%;
			height: 100%;
			display: block;
		}

		&__pill {
			position: absolute;
			z-index: 2;
			display: flex;
			align-items: center;
			gap: 4rpx;
			height: 40rpx;
			padding: 0 14rpx;
			border-radius: 999rpx;
			box-sizing: border-box;

			&--sales {
				right: 20rpx;
				bottom: 20rpx;
				background-color: rgba(0, 0, 0, 0.42);
				backdrop-filter: blur(4px);
			}
		}

		&__pill-text {
			font-size: 20rpx;
			font-weight: 400;
			line-height: 1;
			color: #FFFFFF;
			letter-spacing: 0.5rpx;
		}

		&__body {
			padding: 20rpx 24rpx 24rpx;
		}

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 30rpx;
			font-weight: 700;
			color: #1A1A1A;
			line-height: 1.35;
		}

		&__price {
			flex-shrink: 0;
			display: flex;
			align-items: baseline;
			line-height: 1;
			white-space: nowrap;
		}

		&__price-symbol {
			font-size: 22rpx;
			font-weight: 700;
			color: #FF7A00;
		}

		&__price-num {
			font-size: 34rpx;
			font-weight: 800;
			color: #FF7A00;
			letter-spacing: -0.5rpx;
		}

		&__price-unit {
			margin-left: 2rpx;
			font-size: 20rpx;
			font-weight: 500;
			color: #FF9A40;
		}

		&__price--muted {
			flex-shrink: 0;
			font-size: 22rpx;
			font-weight: 500;
			color: #BBBBBB;
		}

		&__addr-row {
			display: flex;
			align-items: center;
			gap: 6rpx;
			margin-top: 10rpx;
		}

		&__queue-tag {
			flex-shrink: 0;
			display: inline-flex;
			align-items: center;
			max-width: 168rpx;
			height: 56rpx;
			padding: 0 12rpx;
			border-radius: 999rpx;
			background: #E8F8EF;
			box-sizing: border-box;

			&--busy {
				font-size: 28rpx !important;
				background: transparent;
			}
		}

		&__queue-text {
			font-size: 20rpx;
			font-weight: 600;
			color: #2EAF6A;
			line-height: 1.2;
			white-space: nowrap;
			overflow: hidden;
			text-overflow: ellipsis;
		}

		&__queue-tag--busy &__queue-text {
			color: #FF7A00;
		}

		&__addr {
			flex: 1;
			min-width: 0;
			font-size: 22rpx;
			color: #999999;
			line-height: 1.4;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__tags {
			display: flex;
			flex-wrap: wrap;
			gap: 8rpx;
			margin-top: 12rpx;
		}

		&__tag {
			display: flex;
			align-items: center;
			justify-content: center;
			height: 36rpx;
			padding: 0 12rpx;
			border-radius: 8rpx;
			background-color: #FFF6EF;
			border: 1rpx solid #FFE8D6;
		}

		&__tag-text {
			font-size: 20rpx;
			line-height: 1;
			color: #E86A00;
		}

		&__actions {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-top: 18rpx;
			padding-top: 18rpx;
			border-top: 1rpx solid #F5F0EB;

			&--with-queue {
				.goose-card__btn {
					flex: 1;
					min-width: 0;
					height: 56rpx;
					padding: 0 8rpx;
				}

				.goose-card__btn-text {
					font-size: 22rpx;
				}
			}
		}

		&__btn {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 4rpx;
			height: 64rpx;
			border-radius: 999rpx;
			box-sizing: border-box;

			&--ghost {
				border: 1rpx solid #FFD4B8;
				background-color: #FFFBF7;
			}

			&--primary {
				background: linear-gradient(135deg, #FF9A40, #FF7A00);
				box-shadow: 0 4rpx 16rpx rgba(255, 122, 0, 0.22);
			}

			&:active {
				opacity: 0.88;
				transform: scale(0.98);
			}
		}

		&__btn-text {
			font-size: 24rpx;
			font-weight: 600;
			color: #FFFFFF;

			&--ghost {
				color: #FF7A00;
			}
		}
	}

	.goose-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding-top: 8rpx;
	}
</style>

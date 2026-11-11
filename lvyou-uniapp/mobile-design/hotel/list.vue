<template>
	<view class="hotel-list-page">
		<view class="hotel-list-body">
			<view class="hotel-list-topbar">
				<view class="hotel-list-search" @click="onSearchClick">
					<u-icon name="search" color="#FF6600" :size="18" />
					<text class="hotel-list-search__placeholder">搜索酒店名称、地址</text>
				</view>

				<view class="hotel-list-filters">
					<view class="hotel-list-filters__row">
						<view class="hotel-list-filter" :class="{ 'hotel-list-filter--active': sortKey === 'featured' }"
							@click="onFeaturedTap">
							<text class="hotel-list-filter__text">精选</text>
						</view>
						<view class="hotel-list-filter" :class="{ 'hotel-list-filter--active': sortKey === 'distance' }"
							@click="onDistanceFilterTap">
							<text class="hotel-list-filter__text">距离</text>
							<view class="hotel-list-filter__arrows">
								<view class="hotel-list-filter__arrow" @click.stop="onDistanceSort('asc')">
									<u-icon name="arrow-up-fill" :size="8" :color="sortArrowColor('distance', 'asc')" />
								</view>
								<view class="hotel-list-filter__arrow" @click.stop="onDistanceSort('desc')">
									<u-icon name="arrow-down-fill" :size="8" :color="sortArrowColor('distance', 'desc')" />
								</view>
							</view>
						</view>
						<view class="hotel-list-filter" :class="{ 'hotel-list-filter--active': sortKey === 'price' }"
							@click="onPriceFilterTap">
							<text class="hotel-list-filter__text">价格</text>
							<view class="hotel-list-filter__arrows">
								<view class="hotel-list-filter__arrow" @click.stop="onPriceSort('asc')">
									<u-icon name="arrow-up-fill" :size="8" :color="sortArrowColor('price', 'asc')" />
								</view>
								<view class="hotel-list-filter__arrow" @click.stop="onPriceSort('desc')">
									<u-icon name="arrow-down-fill" :size="8" :color="sortArrowColor('price', 'desc')" />
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>

			<view class="hotel-list-content">
				<view v-if="!loading && hotelList.length === 0" class="hotel-list-empty">
					<u-empty mode="list" text="暂无相关酒店" />
				</view>
				<template v-else>
					<view v-for="hotel in hotelList" :key="hotel.id" class="hotel-list-card ry-card" @click="onViewDetail(hotel)">
						<view class="hotel-list-card__thumb-wrap">
							<image class="hotel-list-card__thumb ry-placeholder-img" :src="hotel.coverImage || PLACEHOLDER_LOGO"
								mode="aspectFill" />
							<view v-if="hotel.distance != null" class="hotel-list-card__distance">
								<u-icon name="map-fill" color="#FFFFFF" :size="11" />
								<text class="hotel-list-card__distance-text">{{ hotel.distance }}km</text>
							</view>
						</view>
						<view class="hotel-list-card__info">
							<view class="hotel-list-card__main">
								<text class="hotel-list-card__name">{{ hotel.name }}</text>
								<view v-if="hotel.address" class="hotel-list-card__addr-row">
									<u-icon name="map-fill" color="#B0B0B8" :size="13" />
									<text class="hotel-list-card__addr">{{ hotel.address }}</text>
								</view>
								<view class="hotel-list-card__stars">
									<u-rate :model-value="hotel.starLevel" :count="5" :readonly="true" :allow-half="false"
										:active-color="starActiveColor" inactive-color="#E8E8E8" :size="12" :gutter="2" />
								</view>
							</view>
							<view class="hotel-list-card__footer">
								<view class="hotel-list-card__tag-wrap">
									<text v-if="hotel.score" class="hotel-list-card__score">{{ formatScore(hotel.score) }}分</text>
									<text v-if="hotel.score && hotel.tag" class="hotel-list-card__tag-divider">|</text>
									<text v-if="hotel.tag" class="hotel-list-card__tag">"{{ hotel.tag }}"</text>
								</view>
								<view class="hotel-list-card__price-row">
									<text class="hotel-list-card__symbol">¥</text>
									<text class="hotel-list-card__price">{{ hotel.priceFrom }}</text>
									<text class="hotel-list-card__suffix">起</text>
								</view>
							</view>
						</view>
					</view>
				</template>
				<view v-if="loading && hotelList.length" class="hotel-list-loadmore">
					<text class="hotel-list-loadmore__text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && hotelList.length" class="hotel-list-loadmore">
					<text class="hotel-list-loadmore__text">没有更多了</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref
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
		fetchHotelList
	} from '@/common/api/hotel.js';

	const starActiveColor = '#FFB800';
	const PAGE_SIZE = 10;

	const DETAIL_PATH = '/mobile-design/hotel/detail';
	const SEARCH_PATH = '/mobile-design/search/index';

	const sortKey = ref('featured');
	const distanceSortAsc = ref(true);
	const priceSortAsc = ref(true);
	const hotelList = ref([]);
	const page = ref(1);
	const hasMore = ref(true);
	const loading = ref(false);

	const formatScore = (score) => Number(score).toFixed(1);

	function sortArrowColor(filterKey, direction) {
		if (sortKey.value !== filterKey) {
			return '#CCCCCC';
		}
		const isAsc = direction === 'asc';
		if (filterKey === 'distance') {
			return distanceSortAsc.value === isAsc ? '#FF6600' : '#CCCCCC';
		}
		if (filterKey === 'price') {
			return priceSortAsc.value === isAsc ? '#FF6600' : '#CCCCCC';
		}
		return '#CCCCCC';
	}

	function mapHotelItem(record) {
		return {
			id: record.id,
			name: record.name || '',
			category: record.category,
			starLevel: record.starLevel || 0,
			address: record.address || '',
			score: record.score != null ? Number(record.score) : 0,
			tag: record.tag || record.categoryLabel || '',
			priceFrom: record.priceFrom != null ? Number(record.priceFrom) : 0,
			coverImage: record.coverImage || PLACEHOLDER_LOGO,
			distance: record.distance != null ? Number(record.distance) : null
		};
	}

	function buildListParams() {
		const params = {
			page: page.value,
			limit: PAGE_SIZE
		};
		if (sortKey.value === 'distance') {
			params.sort = 'distance';
			params.distanceOrder = distanceSortAsc.value ? 'asc' : 'desc';
		} else if (sortKey.value === 'price') {
			params.sort = 'price';
			params.priceOrder = priceSortAsc.value ? 'asc' : 'desc';
		}
		return params;
	}

	async function loadHotelList(reset = false, forceRefreshLocation = false) {
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
		const params = await buildLocationListQuery(buildListParams(), forceRefreshLocation);

		try {
			const res = await fetchHotelList(params);
			if (res.code === 0 && res.data) {
				const records = (res.data.records || []).map(mapHotelItem);
				hotelList.value = reset ? records : [...hotelList.value, ...records];
				const total = Number(res.data.total ?? 0);
				hasMore.value = records.length >= PAGE_SIZE && hotelList.value.length < total;
			} else {
				if (reset) {
					hotelList.value = [];
				}
				uni.showToast({
					title: res.msg || '加载失败',
					icon: 'none'
				});
			}
		} catch (e) {
			if (reset) {
				hotelList.value = [];
			}
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			loading.value = false;
		}
	}

	function onFeaturedTap() {
		if (sortKey.value === 'featured') {
			return;
		}
		sortKey.value = 'featured';
		loadHotelList(true);
	}

	function onDistanceSort(direction) {
		const asc = direction === 'asc';
		const changed = sortKey.value !== 'distance' || distanceSortAsc.value !== asc;
		sortKey.value = 'distance';
		distanceSortAsc.value = asc;
		if (changed) {
			loadHotelList(true);
		}
	}

	function onDistanceFilterTap() {
		if (sortKey.value !== 'distance') {
			onDistanceSort('asc');
			return;
		}
		onDistanceSort(distanceSortAsc.value ? 'desc' : 'asc');
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
		const changed = sortKey.value !== 'price' || priceSortAsc.value !== asc;
		sortKey.value = 'price';
		priceSortAsc.value = asc;
		if (changed) {
			loadHotelList(true);
		}
	}

	const navigateSafe = (url) => {
		uni.navigateTo({
			url
		});
	};

	const onViewDetail = (hotel) => {
		const query = `id=${hotel.id}&name=${encodeURIComponent(hotel.name)}`;
		navigateSafe(`${DETAIL_PATH}?${query}`);
	};

	const onSearchClick = () => {
		uni.navigateTo({
			url: `${SEARCH_PATH}?type=hotel`
		});
	};

	onLoad(async () => {
		await loadHotelList(true, true);
	});

	onPullDownRefresh(async () => {
		await loadHotelList(true, true);
		uni.stopPullDownRefresh();
	});

	onReachBottom(() => {
		if (!hasMore.value || loading.value) {
			return;
		}
		page.value += 1;
		loadHotelList(false);
	});
</script>

<style lang="scss" scoped>
	.hotel-list-page {
		min-height: 100vh;
		background-color: #FFF9F2;
		--ry-module-hotel: #FF6600;
	}

	.hotel-list-body {
		padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
	}

	.hotel-list-topbar {
		position: sticky;
		top: 0;
		z-index: 10;
		padding: 20rpx var(--ry-page-padding-x) 16rpx;
		background-color: #FFFFFF;
		box-shadow: 0 8rpx 24rpx rgba(255, 102, 0, 0.08);
		border-bottom: 1rpx solid rgba(255, 102, 0, 0.06);
	}

	.hotel-list-search {
		display: flex;
		align-items: center;
		gap: 12rpx;
		width: 100%;
		height: 72rpx;
		padding: 0 24rpx;
		border-radius: var(--ry-radius-pill);
		background-color: #FFFFFF;
		box-shadow: 0 4rpx 16rpx rgba(255, 102, 0, 0.12);
		box-sizing: border-box;

		&:active {
			opacity: 0.88;
		}
	}

	.hotel-list-search__placeholder {
		flex: 1;
		min-width: 0;
		font-size: 26rpx;
		color: #999999;
	}

	.hotel-list-filters {
		width: 100%;
		margin-top: 16rpx;
		padding: 8rpx 0 4rpx;
	}

	.hotel-list-filters__row {
		display: flex;
		align-items: center;
		justify-content: space-around;
		width: 100%;
	}

	.hotel-list-filter {
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
			.hotel-list-filter__text {
				color: #FF6600;
				font-weight: 400;
			}
		}
	}

	.hotel-list-content {
		padding: 8rpx var(--ry-page-padding-x) 0;
	}

	.hotel-list-empty {
		padding: 120rpx 0 80rpx;
	}

	.hotel-list-loadmore {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 24rpx 0 8rpx;
	}

	.hotel-list-loadmore__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	.hotel-list-content {
		display: flex;
		flex-direction: column;
		gap: var(--ry-card-gap);
	}

	.hotel-list-card {
		display: flex;
		flex-direction: row;
		align-items: flex-start;
		padding: 24rpx;
		gap: 20rpx;
		transition: opacity 0.15s ease;

		&:active {
			opacity: 0.92;
		}
	}

	.hotel-list-card__thumb-wrap {
		position: relative;
		flex-shrink: 0;
	}

	.hotel-list-card__thumb {
		width: 176rpx;
		height: 176rpx;
		border-radius: var(--ry-radius-md);
		background-color: #F0F0F0;
		overflow: hidden;
	}

	.hotel-list-card__distance {
		position: absolute;
		top: 12rpx;
		left: 12rpx;
		display: flex;
		align-items: center;
		gap: 4rpx;
		height: 36rpx;
		padding: 0 12rpx;
		border-radius: var(--ry-radius-pill);
		background-color: rgba(0, 0, 0, 0.35);
	}

	.hotel-list-card__distance-text {
		font-size: 20rpx;
		color: #FFFFFF;
	}

	.hotel-list-card__info {
		flex: 1;
		min-width: 0;
		min-height: 176rpx;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.hotel-list-card__main {
		display: flex;
		flex-direction: column;
		gap: 10rpx;
		flex: 1;
		min-width: 0;
	}

	.hotel-list-card__name {
		font-size: 32rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.hotel-list-card__addr-row {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 6rpx;
		margin-top: 4rpx;
		min-width: 0;
	}

	.hotel-list-card__addr {
		flex: 1;
		min-width: 0;
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.hotel-list-card__stars {
		display: flex;
		flex-direction: row;
		align-items: center;
		min-height: 28rpx;
		margin-top: 8rpx;
	}

	.hotel-list-card__footer {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		margin-top: 12rpx;
		min-height: 40rpx;
	}

	.hotel-list-card__tag-wrap {
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
		gap: 8rpx;
		overflow: hidden;
	}

	.hotel-list-card__score {
		flex-shrink: 0;
		font-size: 24rpx;
		font-weight: 600;
		color: var(--ry-module-hotel, #FF6600);
		line-height: 1.4;
	}

	.hotel-list-card__tag-divider {
		flex-shrink: 0;
		font-size: 22rpx;
		color: #DDDDDD;
		line-height: 1;
	}

	.hotel-list-card__tag {
		flex: 1;
		min-width: 0;
		font-size: 24rpx;
		font-weight: 500;
		color: var(--ry-module-hotel, #FF6600);
		line-height: 1.4;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	.hotel-list-card__price-row {
		display: flex;
		flex-direction: row;
		align-items: baseline;
		flex-shrink: 0;
	}

	.hotel-list-card__symbol {
		font-size: 24rpx;
		font-weight: 600;
		color: var(--ry-price-color, #FF6600);
		line-height: 1;
	}

	.hotel-list-card__price {
		font-size: 36rpx;
		font-weight: 700;
		color: var(--ry-price-color, #FF6600);
		line-height: 1;
	}

	.hotel-list-card__suffix {
		margin-left: 4rpx;
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1;
	}
</style>

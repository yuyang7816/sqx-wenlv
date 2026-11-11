<template>
	<view class="asset-page ry-page">
		<view v-if="currentTab === 0" class="asset-filter-sticky">
			<scroll-view class="asset-filter-scroll" scroll-x :show-scrollbar="false">
				<view class="asset-filter-row">
					<view
						v-for="filter in favoriteFilters"
						:key="filter.key"
						class="asset-filter-pill"
						:class="{ 'asset-filter-pill--active': favoriteFilter === filter.key }"
						@click="onFavoriteFilterChange(filter.key)"
					>
						<text class="asset-filter-pill__text">{{ filter.label }}</text>
					</view>
				</view>
			</scroll-view>
		</view>
		<view v-if="currentTab === 0" class="asset-filter-spacer" />

		<view v-if="currentTab === 0" class="asset-body">
			<view v-if="!loading && displayFavorites.length === 0" class="asset-empty">
				<u-empty mode="list" :text="favoriteEmptyText" />
				<u-button :text="favoriteEmptyAction" color="#FF6600" plain @click="goFavoriteExplore" />
			</view>
			<view
				v-for="item in displayFavorites"
				:key="item.moduleType + '-' + item.id"
				class="asset-card ry-card"
				@click="openItem(item)"
			>
				<view class="asset-card__head">
					<view class="asset-card__type">
						<u-icon :name="getAssetModuleIcon(item.moduleType)" :color="getModuleColor(getAssetModuleColorKey(item.moduleType))" :size="14" />
						<text class="asset-card__type-text">{{ moduleLabel(item.moduleType) }}</text>
					</view>
					<view class="asset-card__head-right">
						<text v-if="item.favoritedAt" class="asset-card__time">收藏于 {{ formatAssetTime(item.favoritedAt) }}</text>
						<view class="asset-card__unfav" @click.stop="onToggleFavorite(item)">
							<u-icon name="heart-fill" color="#FF4757" :size="18" />
						</view>
					</view>
				</view>

				<view class="asset-card__main">
					<view class="asset-card__cover-wrap">
						<image class="asset-card__cover ry-placeholder-img" :src="item.cover" mode="aspectFill" />
						<view v-if="item.duration" class="asset-card__cover-badge">
							<text class="asset-card__cover-badge-text">{{ item.duration }}</text>
						</view>
					</view>
					<view class="asset-card__info">
						<text class="asset-card__name">{{ item.name }}</text>
						<text v-if="item.desc" class="asset-card__desc">{{ item.desc }}</text>

						<view v-if="item.tags?.length" class="asset-card__tags">
							<view v-for="tag in item.tags" :key="tag.text" class="asset-card__tag">
								<text class="asset-card__tag-text">{{ tag.text }}</text>
							</view>
						</view>

						<view v-if="item.spots?.length" class="asset-card__meta">
							<u-icon name="map-fill" :color="getModuleColor('route')" :size="13" />
							<text class="asset-card__meta-text">{{ item.spots.join(' · ') }}</text>
						</view>

						<view v-if="item.moduleType === 'hotel' && item.score" class="asset-card__meta">
							<text class="asset-card__score">{{ item.score }}分</text>
							<text v-if="item.tag" class="asset-card__meta-divider">|</text>
							<text v-if="item.tag" class="asset-card__meta-text">"{{ item.tag }}"</text>
						</view>

						<view v-if="item.moduleType === 'goods'" class="asset-card__meta">
							<text v-if="item.shopName" class="asset-card__meta-text">{{ item.shopName }}</text>
							<text v-if="item.sales" class="asset-card__meta-divider">|</text>
							<text v-if="item.sales" class="asset-card__meta-text">已售 {{ item.sales }}</text>
							<text v-if="item.rating" class="asset-card__meta-divider">|</text>
							<text v-if="item.rating" class="asset-card__meta-text">好评 {{ item.rating }}</text>
						</view>

						<view v-if="item.moduleType === 'scenic'" class="asset-card__meta">
							<text v-if="item.levelLabel" class="asset-card__meta-text">{{ item.levelLabel }}</text>
							<text v-if="item.ticketPrice" class="asset-card__meta-divider">|</text>
							<text v-if="item.ticketPrice" class="asset-card__meta-text">门票 {{ item.ticketPrice }}</text>
							<text v-if="item.openTime" class="asset-card__meta-divider">|</text>
							<text v-if="item.openTime" class="asset-card__meta-text">{{ item.openTime }}</text>
						</view>
					</view>
				</view>

				<view v-if="item.location" class="asset-card__location">
					<u-icon name="map-fill" color="#999999" :size="13" />
					<text class="asset-card__location-text">{{ item.location }}</text>
				</view>

				<view class="asset-card__foot">
					<view v-if="formatAssetPrice(item)" class="asset-card__price">
						<text v-if="assetPriceLabel(item.moduleType)" class="asset-card__price-label">{{ assetPriceLabel(item.moduleType) }}</text>
						<text class="asset-card__symbol">¥</text>
						<text class="asset-card__amount">{{ formatAssetPrice(item) }}</text>
						<text v-if="assetPriceSuffix(item.moduleType)" class="asset-card__suffix">{{ assetPriceSuffix(item.moduleType) }}</text>
					</view>
					<view class="asset-card__action">
						<text class="asset-card__action-text">查看详情</text>
						<u-icon name="arrow-right" color="#FF6600" :size="14" />
					</view>
				</view>
			</view>
			<view v-if="loading && displayFavorites.length" class="asset-loadmore">
				<text class="asset-loadmore__text">加载中...</text>
			</view>
			<view v-else-if="!hasMore && displayFavorites.length" class="asset-loadmore">
				<text class="asset-loadmore__text">没有更多了</text>
			</view>
		</view>

		<view v-else-if="currentTab === 1" class="asset-body">
			<view v-if="!loading && displayFootprints.length === 0" class="asset-empty">
				<u-empty mode="list" text="暂无足迹" />
				<u-button text="去探索" color="#FF6600" plain @click="goIndex" />
			</view>
			<view
				v-for="item in displayFootprints"
				:key="item.moduleType + '-' + item.id"
				class="asset-card ry-card"
				@click="openItem(item)"
			>
				<view class="asset-card__head">
					<view class="asset-card__type">
						<u-icon :name="getAssetModuleIcon(item.moduleType)" :color="getModuleColor(getAssetModuleColorKey(item.moduleType))" :size="14" />
						<text class="asset-card__type-text">{{ moduleLabel(item.moduleType) }}</text>
					</view>
					<text v-if="item.visitedAt" class="asset-card__time">{{ formatAssetTime(item.visitedAt) }}浏览</text>
				</view>

				<view class="asset-card__main">
					<view class="asset-card__cover-wrap">
						<image class="asset-card__cover ry-placeholder-img" :src="item.cover" mode="aspectFill" />
						<view v-if="item.duration" class="asset-card__cover-badge">
							<text class="asset-card__cover-badge-text">{{ item.duration }}</text>
						</view>
					</view>
					<view class="asset-card__info">
						<text class="asset-card__name">{{ item.name }}</text>
						<text v-if="item.desc" class="asset-card__desc">{{ item.desc }}</text>

						<view v-if="item.spots?.length" class="asset-card__meta">
							<u-icon name="map-fill" :color="getModuleColor('route')" :size="13" />
							<text class="asset-card__meta-text">{{ item.spots.join(' · ') }}</text>
						</view>

						<view v-if="item.moduleType === 'hotel' && item.score" class="asset-card__meta">
							<text class="asset-card__score">{{ item.score }}分</text>
							<text v-if="item.tag" class="asset-card__meta-divider">|</text>
							<text v-if="item.tag" class="asset-card__meta-text">"{{ item.tag }}"</text>
						</view>

						<view v-if="item.moduleType === 'goods'" class="asset-card__meta">
							<text v-if="item.shopName" class="asset-card__meta-text">{{ item.shopName }}</text>
							<text v-if="item.sales" class="asset-card__meta-divider">|</text>
							<text v-if="item.sales" class="asset-card__meta-text">已售 {{ item.sales }}</text>
						</view>

						<view v-if="item.moduleType === 'scenic'" class="asset-card__meta">
							<text v-if="item.levelLabel" class="asset-card__meta-text">{{ item.levelLabel }}</text>
							<text v-if="item.ticketPrice" class="asset-card__meta-divider">|</text>
							<text v-if="item.ticketPrice" class="asset-card__meta-text">门票 {{ item.ticketPrice }}</text>
						</view>
					</view>
				</view>

				<view v-if="item.location" class="asset-card__location">
					<u-icon name="map-fill" color="#999999" :size="13" />
					<text class="asset-card__location-text">{{ item.location }}</text>
				</view>

				<view class="asset-card__foot">
					<view v-if="formatAssetPrice(item)" class="asset-card__price">
						<text v-if="assetPriceLabel(item.moduleType)" class="asset-card__price-label">{{ assetPriceLabel(item.moduleType) }}</text>
						<text class="asset-card__symbol">¥</text>
						<text class="asset-card__amount">{{ formatAssetPrice(item) }}</text>
						<text v-if="assetPriceSuffix(item.moduleType)" class="asset-card__suffix">{{ assetPriceSuffix(item.moduleType) }}</text>
					</view>
					<view class="asset-card__action">
						<text class="asset-card__action-text">再看一次</text>
						<u-icon name="arrow-right" color="#FF6600" :size="14" />
					</view>
				</view>
			</view>
			<view v-if="loading && displayFootprints.length" class="asset-loadmore">
				<text class="asset-loadmore__text">加载中...</text>
			</view>
			<view v-else-if="!hasMore && displayFootprints.length" class="asset-loadmore">
				<text class="asset-loadmore__text">没有更多了</text>
			</view>
		</view>

		<view v-else class="asset-body">
			<view v-if="coupons.length === 0" class="asset-empty">
				<u-empty mode="list" text="暂无可用优惠券" />
				<text class="asset-empty__tip">参与活动或关注公告，领取更多优惠</text>
			</view>
			<view v-for="c in coupons" :key="c.id" class="asset-coupon ry-card">
				<view class="asset-coupon__left">
					<text class="asset-coupon__symbol">¥</text>
					<text class="asset-coupon__amount">{{ c.amount }}</text>
					<text class="asset-coupon__limit">{{ c.limit }}</text>
				</view>
				<view class="asset-coupon__right">
					<text class="asset-coupon__name">{{ c.name }}</text>
					<text class="asset-coupon__scope">{{ c.scopeLabel }}</text>
					<text class="asset-coupon__time">{{ c.time }}</text>
					<text v-if="c.desc" class="asset-coupon__desc">{{ c.desc }}</text>
					<view class="asset-coupon__status-tag">
						<text class="asset-coupon__status">{{ c.status }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow, onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { hasToken } from '@/common/api/auth.js';
import { goLogin, openPage, openTab, PAGES } from '@/common/navigation.js';
import {
	fetchFavoriteList,
	fetchFootprintList,
	toggleFavorite,
	resolveAssetPath,
	ASSET_PAGE_SIZE
} from '@/common/api/asset.js';
import { fetchMyCoupons } from '@/common/api/coupon.js';
import {
	COUPON_STATUS_AVAILABLE,
	formatCouponLimit,
	formatCouponScope
} from '@/common/coupon-util.js';
import { getModuleColor } from '@/common/theme.js';
import {
	enrichAssetList,
	formatAssetTime,
	formatAssetPrice,
	assetPriceLabel,
	assetPriceSuffix,
	getAssetModuleIcon,
	getAssetModuleColorKey
} from '@/common/asset-enrich.js';

const FAVORITE_FILTER_ALL = 'all';
const FAVORITE_EXPLORE_PATH = {
	all: '/pages/index/index',
	hotel: '/mobile-design/hotel/list',
	goods: '/mobile-design/heritage/goods-list',
	route: '/mobile-design/route/list'
};

const TAB_TITLES = { favorite: '我的收藏', footprint: '我的足迹', coupon: '优惠券' };
const favoriteFilters = [
	{ key: FAVORITE_FILTER_ALL, label: '全部' },
	{ key: 'hotel', label: '酒店' },
	{ key: 'goods', label: '商品' },
	{ key: 'route', label: '旅游线路' }
];
const currentTab = ref(0);
const favoriteFilter = ref(FAVORITE_FILTER_ALL);
const favorites = ref([]);
const footprints = ref([]);
const coupons = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
const MODULE_LABEL = { scenic: '景区', hotel: '酒店', goods: '商品', goose: '餐饮', activity: '活动', route: '旅游线路', heritage: '非遗' };

const displayFavorites = computed(() => enrichAssetList(favorites.value));
const displayFootprints = computed(() => enrichAssetList(footprints.value));
const favoriteEmptyText = computed(() => {
	const map = {
		all: '暂无收藏',
		hotel: '暂无酒店收藏',
		goods: '暂无商品收藏',
		route: '暂无旅游线路收藏'
	};
	return map[favoriteFilter.value] || '暂无收藏';
});
const favoriteEmptyAction = computed(() => {
	const map = {
		all: '去逛逛',
		hotel: '去看看酒店',
		goods: '去逛逛商品',
		route: '去看看线路'
	};
	return map[favoriteFilter.value] || '去逛逛';
});

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: '请先登录', icon: 'none' });
	goLogin({ delay: 400 });
	return false;
}

function favoriteTargetType() {
	return favoriteFilter.value === FAVORITE_FILTER_ALL ? undefined : favoriteFilter.value;
}

async function loadFavorites(reset = false) {
	if (loading.value) return;
	if (!reset && !hasMore.value) return;

	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loading.value = true;
	try {
		const res = await fetchFavoriteList({
			page: page.value,
			limit: ASSET_PAGE_SIZE,
			targetType: favoriteTargetType()
		});
		if (res.code === 0 && res.data) {
			const list = res.data.records || [];
			favorites.value = reset ? list : [...favorites.value, ...list];
			const total = Number(res.data.total ?? 0);
			hasMore.value = list.length >= ASSET_PAGE_SIZE && favorites.value.length < total;
		} else {
			if (reset) favorites.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) favorites.value = [];
		uni.showToast({ title: '加载失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

async function loadFootprints(reset = false) {
	if (loading.value) return;
	if (!reset && !hasMore.value) return;

	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loading.value = true;
	try {
		const res = await fetchFootprintList({
			page: page.value,
			limit: ASSET_PAGE_SIZE
		});
		if (res.code === 0 && res.data) {
			const list = res.data.records || [];
			footprints.value = reset ? list : [...footprints.value, ...list];
			const total = Number(res.data.total ?? 0);
			hasMore.value = list.length >= ASSET_PAGE_SIZE && footprints.value.length < total;
		} else {
			if (reset) footprints.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) footprints.value = [];
		uni.showToast({ title: '加载失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

async function loadCoupons() {
	loading.value = true;
	try {
		const res = await fetchMyCoupons(COUPON_STATUS_AVAILABLE);
		if (res.code === 0) {
			const list = Array.isArray(res.data) ? res.data : [];
			coupons.value = list.map((item) => ({
				id: item.id,
				amount: item.amount,
				limit: formatCouponLimit(item),
				name: item.name,
				scopeLabel: formatCouponScope(item.scope),
				time: item.validTo ? `有效期至 ${item.validTo}` : '',
				desc: item.desc || '',
				status: '未使用'
			}));
		} else {
			coupons.value = [];
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		coupons.value = [];
		uni.showToast({ title: '加载失败', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function loadCurrentTab(reset = true) {
	if (currentTab.value === 0) return loadFavorites(reset);
	if (currentTab.value === 1) return loadFootprints(reset);
	return loadCoupons();
}

function onFavoriteFilterChange(key) {
	if (favoriteFilter.value === key) return;
	favoriteFilter.value = key;
	if (!hasToken()) return;
	loadFavorites(true);
}

async function onToggleFavorite(item) {
	const targetType = item.moduleType || item.targetType;
	const targetId = item.id || item.targetId;
	if (!targetType || targetId == null) return;

	try {
		const res = await toggleFavorite({ targetType, targetId });
		if (res.code === 0) {
			uni.showToast({ title: res.data?.favorited ? '已收藏' : '已取消收藏', icon: 'none' });
			loadFavorites(true);
			return;
		}
		uni.showToast({ title: res.msg || '操作失败', icon: 'none' });
	} catch (e) {
		uni.showToast({ title: '操作失败', icon: 'none' });
	}
}

function setNavTitle(tabKey) {
	const title = TAB_TITLES[tabKey] || '资产中心';
	uni.setNavigationBarTitle({ title });
}
function moduleLabel(key) { return MODULE_LABEL[key] || '其他'; }
function openItem(item) {
	const path = resolveAssetPath(item);
	if (path) {
		openPage(path);
		return;
	}
	uni.showToast({ title: '暂无法打开', icon: 'none' });
}
function goIndex() {
	openTab(PAGES.INDEX);
}
function goFavoriteExplore() {
	const path = FAVORITE_EXPLORE_PATH[favoriteFilter.value] || FAVORITE_EXPLORE_PATH.all;
	openPage(path);
}

onLoad((options) => {
	const map = { favorite: 0, footprint: 1, coupon: 2 };
	let tabKey = 'favorite';
	if (options?.tab && map[options.tab] !== undefined) {
		currentTab.value = map[options.tab];
		tabKey = options.tab;
	}
	setNavTitle(tabKey);
	const filterKeys = favoriteFilters.map((item) => item.key);
	if (options?.type && filterKeys.includes(options.type)) favoriteFilter.value = options.type;
});

onShow(() => {
	if (!requireLogin()) return;
	loadCurrentTab(true);
});

onPullDownRefresh(async () => {
	if (!hasToken()) {
		uni.stopPullDownRefresh();
		return;
	}
	await loadCurrentTab(true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasToken() || loading.value || !hasMore.value) return;
	if (currentTab.value === 2) return;
	page.value += 1;
	loadCurrentTab(false);
});
</script>

<style lang="scss" scoped>
	.asset-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg);
		padding-bottom: calc(24rpx + env(safe-area-inset-bottom));
	}

	.asset-filter-sticky {
		position: fixed;
		top: 0;
		left: 0;
		right: 0;
		z-index: 100;
		background-color: #ffffff;
		padding: 20rpx var(--ry-page-padding-x) 16rpx;
		box-shadow: 0 4rpx 16rpx rgba(0, 0, 0, 0.04);
	}

	.asset-filter-spacer {
		height: 96rpx;
		flex-shrink: 0;
	}

	.asset-filter-scroll {
		width: 100%;
		white-space: nowrap;
	}

	.asset-filter-row {
		display: inline-flex;
		gap: 16rpx;
		padding: 4rpx 0;
	}

	.asset-filter-pill {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 14rpx 32rpx;
		border-radius: var(--ry-radius-pill);
		background: #f5f6fa;
		border: 2rpx solid transparent;
		transition: all 0.2s ease;

		&--active {
			background: var(--ry-brand-light);
			border-color: var(--ry-brand);
		}
	}

	.asset-filter-pill__text {
		font-size: 26rpx;
		color: var(--ry-text-secondary);
		line-height: 1;
	}

	.asset-filter-pill--active .asset-filter-pill__text {
		color: var(--ry-brand);
		font-weight: 600;
	}

	.asset-body {
		padding: 24rpx var(--ry-page-padding-x);
		display: flex;
		flex-direction: column;
		gap: var(--ry-card-gap);
	}

	.asset-card {
		padding: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.asset-card__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	.asset-card__type {
		display: flex;
		align-items: center;
		gap: 8rpx;
	}

	.asset-card__type-text {
		font-size: 24rpx;
		color: var(--ry-text-secondary);
		font-weight: 500;
	}

	.asset-card__head-right {
		display: flex;
		align-items: center;
		gap: 12rpx;
		flex-shrink: 0;
	}

	.asset-card__time {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	.asset-card__unfav {
		padding: 4rpx;
	}

	.asset-loadmore {
		padding: 24rpx 0 8rpx;
		text-align: center;
	}

	.asset-loadmore__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	.asset-card__main {
		display: flex;
		gap: 20rpx;
	}

	.asset-card__cover-wrap {
		position: relative;
		width: 200rpx;
		height: 200rpx;
		flex-shrink: 0;
		border-radius: var(--ry-radius-md);
		overflow: hidden;
	}

	.asset-card__cover {
		width: 100%;
		height: 100%;
		display: block;
	}

	.asset-card__cover-badge {
		position: absolute;
		left: 0;
		bottom: 0;
		padding: 6rpx 14rpx;
		background: rgba(0, 0, 0, 0.55);
		border-top-right-radius: var(--ry-radius-sm);
	}

	.asset-card__cover-badge-text {
		font-size: 20rpx;
		color: #ffffff;
		line-height: 1;
	}

	.asset-card__info {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}

	.asset-card__name {
		font-size: 30rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		line-height: 1.4;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	.asset-card__desc {
		font-size: 24rpx;
		color: var(--ry-text-secondary);
		line-height: 1.5;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	.asset-card__tags {
		display: flex;
		flex-wrap: wrap;
		gap: 8rpx;
	}

	.asset-card__tag {
		padding: 4rpx 12rpx;
		border-radius: var(--ry-radius-xs);
		background: var(--ry-brand-light);
	}

	.asset-card__tag-text {
		font-size: 20rpx;
		color: var(--ry-brand);
		line-height: 1;
	}

	.asset-card__meta {
		display: flex;
		align-items: center;
		flex-wrap: wrap;
		gap: 6rpx;
	}

	.asset-card__meta-text {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}

	.asset-card__meta-divider {
		font-size: 22rpx;
		color: #dddddd;
	}

	.asset-card__score {
		font-size: 22rpx;
		color: #ffb800;
		font-weight: 600;
	}

	.asset-card__location {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 12rpx 16rpx;
		border-radius: var(--ry-radius-sm);
		background: #f8f9fc;
	}

	.asset-card__location-text {
		flex: 1;
		font-size: 24rpx;
		color: var(--ry-text-secondary);
		line-height: 1.5;
	}

	.asset-card__foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-top: 12rpx;
		border-top: 1rpx solid #f0f2f5;
	}

	.asset-card__price {
		display: flex;
		align-items: baseline;
		gap: 4rpx;
	}

	.asset-card__price-label {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		margin-right: 4rpx;
	}

	.asset-card__symbol {
		font-size: 24rpx;
		font-weight: 700;
		color: var(--ry-price-color);
	}

	.asset-card__amount {
		font-size: 36rpx;
		font-weight: 700;
		color: var(--ry-price-color);
		line-height: 1;
	}

	.asset-card__suffix {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	.asset-card__action {
		display: flex;
		align-items: center;
		gap: 4rpx;
	}

	.asset-card__action-text {
		font-size: 24rpx;
		color: var(--ry-brand);
		font-weight: 500;
	}

	.asset-empty {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 24rpx;
		padding: 100rpx 0 40rpx;
	}

	.asset-empty__tip {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		text-align: center;
	}

	.asset-coupon {
		display: flex;
		flex-direction: row;
		align-items: stretch;
		overflow: hidden;
		padding: 0;
	}

	.asset-coupon__left {
		width: 200rpx;
		background: linear-gradient(160deg, #fff8f2 0%, var(--ry-brand-light) 100%);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 32rpx 0;
		position: relative;

		&::after {
			content: '';
			position: absolute;
			right: -8rpx;
			top: 50%;
			transform: translateY(-50%);
			width: 16rpx;
			height: 16rpx;
			border-radius: 50%;
			background: var(--ry-page-bg);
			box-shadow: 0 -20rpx 0 var(--ry-page-bg), 0 20rpx 0 var(--ry-page-bg);
		}
	}

	.asset-coupon__symbol {
		font-size: 24rpx;
		font-weight: 700;
		color: var(--ry-brand);
		line-height: 1;
	}

	.asset-coupon__amount {
		font-size: 48rpx;
		font-weight: 700;
		color: var(--ry-brand);
		line-height: 1.1;
	}

	.asset-coupon__limit {
		margin-top: 8rpx;
		font-size: 22rpx;
		color: #aa7755;
	}

	.asset-coupon__right {
		flex: 1;
		padding: 28rpx 24rpx;
		display: flex;
		flex-direction: column;
		justify-content: center;
		gap: 8rpx;
	}

	.asset-coupon__name {
		font-size: 30rpx;
		color: var(--ry-text-primary);
		font-weight: 600;
	}

	.asset-coupon__scope {
		font-size: 24rpx;
		color: var(--ry-brand);
	}

	.asset-coupon__time {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	.asset-coupon__desc {
		font-size: 22rpx;
		color: var(--ry-text-secondary);
		line-height: 1.4;
	}

	.asset-coupon__status-tag {
		align-self: flex-start;
		margin-top: 4rpx;
		padding: 4rpx 14rpx;
		border-radius: var(--ry-radius-xs);
		background: var(--ry-brand-light);
	}

	.asset-coupon__status {
		font-size: 22rpx;
		color: var(--ry-brand);
		font-weight: 500;
	}
</style>

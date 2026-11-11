<template>
	<view class="heritage-shop-list-page">
		<view class="heritage-shop-list-tabs-fixed">
			<scroll-view class="heritage-shop-list-tabs" scroll-x :show-scrollbar="false">
				<view class="heritage-shop-list-tabs__track">
					<view
						v-for="(tab, index) in categoryTabs"
						:key="tab.key"
						class="heritage-shop-list-tabs__item"
						:class="{ 'heritage-shop-list-tabs__item--active': currentTab === index }"
						@click="onTabClick(index)"
					>
						<text class="heritage-shop-list-tabs__text">{{ tab.label }}</text>
						<view v-if="currentTab === index" class="heritage-shop-list-tabs__line" />
					</view>
				</view>
			</scroll-view>
		</view>
		<view class="heritage-shop-list-tabs-spacer" />

		<view class="heritage-shop-list-body">
			<!-- 门店列表 -->
			<view class="heritage-shop-list-cards">
				<view v-if="!loading && shopList.length === 0" class="heritage-shop-list-empty">
					<text class="heritage-shop-list-empty__text">暂无相关门店</text>
				</view>
				<view
					v-for="shop in shopList"
					:key="shop.id"
					class="heritage-shop-list-card"
					@click="openShop(shop)"
				>
					<view class="heritage-shop-list-card__thumb">
						<image class="heritage-shop-list-card__thumb-img ry-placeholder-img" :src="shop.cover" mode="aspectFill" />
					</view>
					<view class="heritage-shop-list-card__info">
						<view class="heritage-shop-list-card__head">
							<text class="heritage-shop-list-card__name">{{ shop.name }}</text>
							<view class="heritage-shop-list-card__rating">
								<u-icon name="star-fill" color="#FF9800" :size="12" />
								<text class="heritage-shop-list-card__rating-text">{{ shop.rating }}</text>
							</view>
						</view>
						<text class="heritage-shop-list-card__desc">{{ shop.desc }}</text>
						<view class="heritage-shop-list-card__meta">
							<u-icon name="map" color="#BBBBBB" :size="12" />
							<text class="heritage-shop-list-card__addr">{{ shop.address }}</text>
						</view>
						<view class="heritage-shop-list-card__foot">
							<view class="heritage-shop-list-card__tags">
								<view v-for="tag in shop.tags" :key="tag" class="heritage-shop-list-card__tag">
									<text class="heritage-shop-list-card__tag-text">{{ tag }}</text>
								</view>
							</view>
							<view class="heritage-shop-list-card__btn">
								<text class="heritage-shop-list-card__btn-text">{{ ui.btnEnter }}</text>
								<u-icon name="arrow-right" color="#B45D2D" :size="12" />
							</view>
						</view>
					</view>
				</view>
				<view v-if="loading && shopList.length" class="heritage-shop-list-loadmore">
					<text class="heritage-shop-list-loadmore__text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && shopList.length" class="heritage-shop-list-loadmore">
					<text class="heritage-shop-list-loadmore__text">没有更多了</text>
				</view>
			</view>

			<text class="shop-footer">{{ ui.footerShop }}</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { buildLocationListQuery } from '@/common/location-params.js';
import { fetchShopList, fetchShopCategoryOptions, mapShopItem } from '@/common/api/heritage.js';
import { HERITAGE_UI, SHOP_CATEGORY_TABS } from '@/common/heritage-ui.js';

const ui = HERITAGE_UI;
const PAGE_SIZE = 10;

const shopList = ref([]);
const categoryTabs = ref([{ key: 'all', label: ui.tabAll }]);
const currentTab = ref(0);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
const total = ref(0);

/** 好店分类 Tab：首项固定「全部」，其余来自管理后台启用分类 */
async function loadCategoryTabs() {
	const allTab = { key: 'all', label: ui.tabAll };
	try {
		const res = await fetchShopCategoryOptions();
		if (res.code === 0 && Array.isArray(res.data) && res.data.length) {
			categoryTabs.value = [
				allTab,
				...res.data.map((item) => ({
					key: item.code,
					label: item.name || item.code
				}))
			];
			return;
		}
	} catch (e) {
		// 接口异常时使用本地兜底 Tab
	}
	categoryTabs.value = SHOP_CATEGORY_TABS;
}

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
	const tabKey = categoryTabs.value[currentTab.value]?.key || 'all';
	const base = { page: page.value, limit: PAGE_SIZE };
	if (tabKey !== 'all') {
		base.category = tabKey;
	}
	const params = await buildLocationListQuery(base);
	try {
		const res = await fetchShopList(params);
		if (res.code === 0 && res.data) {
			const records = (res.data.records || []).map(mapShopItem);
			shopList.value = reset ? records : [...shopList.value, ...records];
			total.value = Number(res.data.total ?? 0);
			hasMore.value = records.length >= PAGE_SIZE && shopList.value.length < total.value;
		} else {
			if (reset) {
				shopList.value = [];
				total.value = 0;
			}
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) {
			shopList.value = [];
			total.value = 0;
		}
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function onTabClick(index) {
	if (currentTab.value === index) {
		return;
	}
	currentTab.value = index;
	loadShopList(true);
}

function openShop(shop) {
	uni.navigateTo({ url: `/mobile-design/heritage/shop-detail?id=${shop.id}` });
}

onLoad(async () => {
	await loadCategoryTabs();
	if (currentTab.value >= categoryTabs.value.length) {
		currentTab.value = 0;
	}
	await loadShopList(true);
});

onPullDownRefresh(async () => {
	await loadCategoryTabs();
	if (currentTab.value >= categoryTabs.value.length) {
		currentTab.value = 0;
	}
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
.heritage-shop-list-page {
	min-height: 100vh;
	box-sizing: border-box;
	padding-bottom: constant(safe-area-inset-bottom);
	padding-bottom: env(safe-area-inset-bottom);
	background-color: #faf6f3;
}

.heritage-shop-list-tabs-fixed {
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

.heritage-shop-list-tabs-spacer {
	/* 与 fixed 区高度一致：上下 padding 24rpx + tab 80rpx */
	height: 104rpx;
	flex-shrink: 0;
}

.heritage-shop-list-body {
	width: calc(100% - 60rpx);
	margin: auto;
	box-sizing: border-box;
	padding: 20rpx var(--ry-page-padding-x) calc(80rpx + constant(safe-area-inset-bottom));
	padding-bottom: calc(80rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.heritage-shop-list-tabs {
	width: 100%;
	background-color: #ffffff;
	border-radius: var(--ry-radius-lg);
	box-shadow: 0 4rpx 16rpx rgba(74, 55, 40, 0.06);
}

.heritage-shop-list-tabs__track {
	display: inline-flex;
	flex-direction: row;
	padding: 0 8rpx;
	min-width: 100%;
}

.heritage-shop-list-tabs__item {
	position: relative;
	flex: 1;
	flex-shrink: 0;
	height: 80rpx;
	padding: 0 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
}

.heritage-shop-list-tabs__text {
	font-size: 28rpx;
	color: #666666;
	white-space: nowrap;
}

.heritage-shop-list-tabs__item--active .heritage-shop-list-tabs__text {
	font-weight: 600;
	color: var(--ry-module-heritage-shop);
}

.heritage-shop-list-tabs__line {
	position: absolute;
	left: 50%;
	bottom: 8rpx;
	width: 48rpx;
	height: 6rpx;
	border-radius: 3rpx;
	background-color: var(--ry-module-heritage-shop);
	transform: translateX(-50%);
}

.heritage-shop-list-cards {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.heritage-shop-list-card {
	display: flex;
	flex-direction: column;
	border-radius: var(--ry-radius-lg);
	background-color: #ffffff;
	box-shadow: 0 8rpx 28rpx rgba(74, 55, 40, 0.08);
	overflow: hidden;

	&:active {
		opacity: 0.92;
	}

	&__thumb {
		width: 100%;
		height: 320rpx;
		overflow: hidden;
	}

	&__thumb-img {
		width: 100%;
		height: 100%;
		display: block;
	}

	&__info {
		padding: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 12rpx;
	}

	&__name {
		flex: 1;
		font-size: 30rpx;
		font-weight: 700;
		color: #4a3728;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__rating {
		display: flex;
		align-items: center;
		gap: 4rpx;
		flex-shrink: 0;
	}

	&__rating-text {
		font-size: 22rpx;
		font-weight: 600;
		color: #ff9800;
	}

	&__desc {
		font-size: 22rpx;
		color: #999999;
		line-height: 1.4;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 1;
		overflow: hidden;
	}

	&__meta {
		display: flex;
		align-items: center;
		gap: 6rpx;
	}

	&__addr {
		flex: 1;
		font-size: 22rpx;
		color: #bbbbbb;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__distance {
		font-size: 22rpx;
		color: #b45d2d;
		flex-shrink: 0;
	}

	&__foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: auto;
		padding-top: 8rpx;
		gap: 12rpx;
	}

	&__tags {
		display: flex;
		flex-direction: row;
		flex-wrap: wrap;
		gap: 8rpx;
		flex: 1;
		min-width: 0;
	}

	&__tag {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 4rpx 12rpx;
		border-radius: var(--ry-radius-pill);
		background-color: #f5e8df;
	}

	&__tag-text {
		font-size: 20rpx;
		color: var(--ry-module-heritage-shop);
		text-align: center;
	}

	&__btn {
		display: flex;
		align-items: center;
		gap: 2rpx;
		flex-shrink: 0;
	}

	&__btn-text {
		font-size: 22rpx;
		font-weight: 600;
		color: var(--ry-module-heritage-shop);
	}
}

.heritage-shop-list-empty {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 80rpx 0;
}

.heritage-shop-list-empty__text {
	font-size: 28rpx;
	color: var(--ry-text-hint);
}

.heritage-shop-list-loadmore {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 24rpx 0 8rpx;
}

.heritage-shop-list-loadmore__text {
	font-size: 24rpx;
	color: var(--ry-text-hint);
}

.shop-footer {
	display: block;
	text-align: center;
	font-size: 22rpx;
	color: #cccccc;
	padding: 8rpx 0 16rpx;
}
</style>

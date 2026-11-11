<template>
	<view class="heritage-catalog-page">
		<view class="catalog-tabs-fixed">
			<scroll-view class="catalog-tabs" scroll-x :show-scrollbar="false">
				<view class="catalog-tabs__row">
					<view
						v-for="(tab, index) in levelTabs"
						:key="tab.key"
						class="catalog-tab"
						:class="{ 'catalog-tab--active': currentTab === index }"
						@click="onTabClick(index)"
					>
						<text class="catalog-tab__text">{{ tab.label }}</text>
					</view>
				</view>
			</scroll-view>
		</view>
		<view class="catalog-tabs-spacer" />

		<view class="heritage-catalog-body">
			<!-- 项目列表 -->
			<view class="catalog-list">
				<view v-if="!loading && filteredList.length === 0" class="catalog-empty">
					<view class="catalog-empty__icon">
						<u-icon name="list" color="#D7CCC8" :size="64" />
					</view>
					<text class="catalog-empty__text">{{ ui.emptyCatalog }}</text>
					<text class="catalog-empty__hint">{{ ui.emptyHint }}</text>
				</view>

				<view
					v-for="craft in filteredList"
					:key="craft.id"
					class="catalog-card"
					@click="openCraft(craft)"
				>
					<view class="catalog-card__cover">
						<image class="catalog-card__img ry-placeholder-img" :src="craft.cover || PLACEHOLDER_LOGO" mode="aspectFill" />
						<view class="catalog-card__level" :class="levelClass(craft.level)">
							<text class="catalog-card__level-text">{{ craft.level }}</text>
						</view>
					</view>
					<view class="catalog-card__body">
						<text class="catalog-card__name">{{ craft.name }}</text>
						<text class="catalog-card__intro">{{ craft.desc }}</text>
						<view class="catalog-card__foot">
							<text class="catalog-card__tag">{{ craft.tag }}</text>
							<view class="catalog-card__btn">
								<text class="catalog-card__btn-text">{{ ui.btnDetail }}</text>
								<u-icon name="arrow-right" color="#FFFFFF" :size="12" />
							</view>
						</view>
					</view>
				</view>

				<view v-if="loading && craftList.length" class="catalog-loadmore">
					<text class="catalog-loadmore__text">加载中...</text>
				</view>
				<view v-else-if="!hasMore && craftList.length" class="catalog-loadmore">
					<text class="catalog-loadmore__text">没有更多了</text>
				</view>
			</view>

			<text class="catalog-footer">{{ ui.footerCatalog }}</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { buildLocationListQuery } from '@/common/location-params.js';
import { fetchCraftList, mapCraftItem } from '@/common/api/heritage.js';
import { HERITAGE_UI } from '@/common/heritage-ui.js';
import { HERITAGE_CRAFT_LEVEL_TABS, resolveCraftLevelClass } from '@/common/heritage-craft-level.js';

const ui = HERITAGE_UI;
const PAGE_SIZE = 10;

const craftList = ref([]);
const currentTab = ref(0);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
const total = ref(0);
const levelTabs = [
	{ key: 'all', label: ui.tabAll },
	...HERITAGE_CRAFT_LEVEL_TABS
];

/** 级别 Tab 客户端筛选（server 无 level 参数） */
const filteredList = computed(() => {
	const tab = levelTabs[currentTab.value];
	if (!tab || tab.key === 'all') {
		return craftList.value;
	}
	return craftList.value.filter((item) => item.level === tab.label);
});

function levelClass(level) {
	return resolveCraftLevelClass(level);
}

async function loadCraftList(reset = false) {
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
	const params = await buildLocationListQuery({ page: page.value, limit: PAGE_SIZE });
	try {
		const res = await fetchCraftList(params);
		if (res.code === 0 && res.data) {
			const records = (res.data.records || []).map(mapCraftItem);
			craftList.value = reset ? records : [...craftList.value, ...records];
			total.value = Number(res.data.total ?? 0);
			hasMore.value = records.length >= PAGE_SIZE && craftList.value.length < total.value;
		} else {
			if (reset) {
				craftList.value = [];
				total.value = 0;
			}
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		if (reset) {
			craftList.value = [];
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
}

function openCraft(craft) {
	uni.navigateTo({ url: `/mobile-design/heritage/craft-intro?id=${craft.id}` });
}

onLoad(async () => {
	await loadCraftList(true);
});

onPullDownRefresh(async () => {
	await loadCraftList(true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasMore.value || loading.value) {
		return;
	}
	page.value += 1;
	loadCraftList(false);
});
</script>

<style lang="scss" scoped>
.heritage-catalog-page {
	min-height: 100vh;
	background-color: #faf6f3;
}

.catalog-tabs-fixed {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	z-index: 100;
	padding: 12rpx var(--ry-page-padding-x);
	background-color: #faf6f3;
	box-shadow: 0 4rpx 16rpx rgba(74, 55, 40, 0.06);
}

.catalog-tabs-spacer {
	height: 92rpx;
	flex-shrink: 0;
}

.heritage-catalog-body {
	padding: 20rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 24rpx;
}

.catalog-tabs {
	width: 100%;
	white-space: nowrap;
}

.catalog-tabs__row {
	display: inline-flex;
	gap: 16rpx;
	padding: 4rpx;
}

.catalog-tab {
	flex-shrink: 0;
	display: flex;
	align-items: center;
	justify-content: center;
	height: 68rpx;
	padding: 0 32rpx;
	border-radius: var(--ry-radius-pill);
	background-color: #ffffff;
	border: 1rpx solid #f0ebe6;
	box-sizing: border-box;

	&__text {
		font-size: 26rpx;
		font-weight: 500;
		line-height: 1;
		color: #8d6e63;
	}

	&--active {
		background: linear-gradient(135deg, #8d6e63, #5d4037);
		border-color: transparent;
		box-shadow: 0 6rpx 20rpx rgba(93, 64, 55, 0.28);

		.catalog-tab__text {
			color: #ffffff;
			font-weight: 700;
		}
	}
}

.catalog-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.catalog-loadmore {
	display: flex;
	align-items: center;
	justify-content: center;
	padding: 24rpx 0 8rpx;
}

.catalog-loadmore__text {
	font-size: 24rpx;
	color: var(--ry-text-hint);
}

.catalog-empty {
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

	&__hint {
		margin-top: 8rpx;
		font-size: 24rpx;
		color: #bbbbbb;
	}
}

.catalog-card {
	border-radius: var(--ry-radius-lg);
	background-color: #ffffff;
	box-shadow: 0 8rpx 28rpx rgba(74, 55, 40, 0.08);
	overflow: hidden;

	&:active {
		opacity: 0.92;
		transform: scale(0.995);
	}

	&__cover {
		position: relative;
		width: 100%;
		height: 280rpx;
		overflow: hidden;
	}

	&__img {
		width: 100%;
		height: 100%;
		display: block;
	}

	&__level {
		position: absolute;
		top: 16rpx;
		left: 16rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 40rpx;
		padding: 0 16rpx;
		border-radius: var(--ry-radius-pill);
		background-color: rgba(255, 255, 255, 0.92);
		z-index: 2;

		&--world .catalog-card__level-text {
			color: #4a3728;
		}

		&--national .catalog-card__level-text {
			color: #5d4037;
		}

		&--provincial .catalog-card__level-text {
			color: #6d4c41;
		}

		&--municipal .catalog-card__level-text {
			color: #795548;
		}

		&--county .catalog-card__level-text {
			color: #8d6e63;
		}

		&--craft .catalog-card__level-text {
			color: #b45d2d;
		}
	}

	&__level-text {
		font-size: 20rpx;
		font-weight: 600;
		line-height: 1;
	}

	&__body {
		padding: 24rpx 28rpx 28rpx;
	}

	&__name {
		display: block;
		font-size: 32rpx;
		font-weight: 700;
		color: #4a3728;
		letter-spacing: 2rpx;
	}

	&__intro {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #666666;
		line-height: 1.7;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	&__foot {
		display: flex;
		align-items: center;
		justify-content: space-between;
		margin-top: 20rpx;
		padding-top: 20rpx;
		border-top: 1rpx solid #f0ebe6;
	}

	&__tag {
		font-size: 22rpx;
		color: #b45d2d;
	}

	&__btn {
		display: flex;
		align-items: center;
		gap: 4rpx;
		padding: 10rpx 22rpx;
		border-radius: var(--ry-radius-pill);
		background: linear-gradient(135deg, #8d6e63, #5d4037);
		box-shadow: 0 4rpx 12rpx rgba(93, 64, 55, 0.22);
	}

	&__btn-text {
		font-size: 22rpx;
		font-weight: 600;
		color: #ffffff;
	}
}

.catalog-footer {
	display: block;
	text-align: center;
	font-size: 22rpx;
	color: #cccccc;
	padding-top: 8rpx;
}
</style>

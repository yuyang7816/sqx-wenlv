<template>
	<view class="ry-page index-page">
		<!-- 顶部搜索 -->
		<view class="home-search-row">
			<view class="home-dest" @click="openDestinationSelect">
				<u-icon name="map-fill" color="#FF7A00" :size="15" />
				<text class="home-dest__name">{{ locationLabel }}</text>
				<u-icon name="arrow-down" color="#C25E00" :size="12" />
			</view>
			<view class="home-search" @click="onSearchClick">
				<u-icon name="search" color="#B7B7C4" :size="19" />
				<text class="home-search__placeholder">搜索景点、酒店、美食...</text>
				<view class="home-search__btn">
					<text class="home-search__btn-text">搜索</text>
				</view>
			</view>
		</view>

		<!-- 轮播 Banner -->
		<view class="index-banner">
			<u-swiper :list="bannerList" key-name="image" height="160" radius="18" bg-color="transparent" :indicator="true"
				indicator-mode="dot" :indicator-style="{ right: '24rpx', bottom: '22rpx' }" indicator-active-color="#FFFFFF"
				indicator-inactive-color="rgba(255,255,255,0.45)" :circular="true" :autoplay="true" :interval="4000"
				@click="onBannerClick">
				<template #default="{ item }">
					<view class="index-banner__slide">
						<image class="index-banner__img" :src="item.image" mode="aspectFill" />
						<!-- <view class="index-banner__veil"></view>
						<view class="index-banner__overlay">
							<view class="index-banner__chip">精选推荐</view>
							<text class="index-banner__title">{{ item.title }}</text>
						</view> -->
					</view>
				</template>
			</u-swiper>
		</view>

		<!-- 快捷入口 -->
		<view class="home-grid">
			<view class="home-grid__head">
				<text class="home-grid__title">快捷入口</text>
			</view>
			<view class="home-grid__panel ry-card">
				<u-grid :col="5" :border="false">
					<u-grid-item v-for="item in gridList" :key="item.id" @click="onGridClick(item)">
						<view class="home-grid__item">
							<view class="home-grid__icon">
								<image class="home-grid__icon-img" :src="item.iconImage" mode="aspectFit" />
							</view>
							<text class="home-grid__label">{{ item.label }}</text>
						</view>
					</u-grid-item>
				</u-grid>
			</view>
		</view>

		<!-- 公告栏 -->
		<view class="index-notice ry-card" @click="onNoticeMore">
			<view class="index-notice__tag">
				<u-icon name="volume" color="#FFFFFF" :size="13" />
				<text class="index-notice__tag-text">公告</text>
			</view>
			<view class="index-notice__body">
				<u-notice-bar :text="noticeText" direction="row" :icon="''" color="#6B6B78" bg-color="transparent"
					:speed="60" />
			</view>
			<view class="index-notice__more">
				<text class="index-notice__more-text">更多</text>
				<u-icon name="arrow-right" color="#B7B7C4" :size="12" />
			</view>
		</view>

		<!-- 分类 Tab -->
		<view class="home-tabs">
			<view v-for="(tab, index) in tabList" :key="tab.key || tab.name" class="home-tab"
				:class="{ 'home-tab--active': currentTab === index }" @click="onTabClick(index)">
				<text class="home-tab__text">{{ tab.name }}</text>
			</view>
		</view>

		<!-- 推荐列表 -->
		<view class="index-list">
			<view v-if="filteredRecommendList.length === 0" class="index-list__empty">
				<u-empty mode="list" text="暂无推荐" />
			</view>
			<view v-for="item in filteredRecommendList" :key="`${item.itemType}-${item.id}`" class="home-card ry-card" @click="onCardClick(item)">
				<view class="home-card__cover">
					<image class="home-card__img" :src="item.cover" mode="aspectFill" />
				</view>
				<view class="home-card__info">
					<view class="home-card__name-row">
						<text class="home-card__name">{{ item.name }}</text>
					</view>
					<view v-if="item.address" class="home-card__addr">
						<u-icon name="map-fill" color="#FF6600" :size="13" />
						<text class="home-card__addr-text">{{ item.address }}</text>
					</view>
					<view class="home-card__tags">
						<view v-for="tag in item.tags" :key="tag" class="home-card__tag">
							<text class="home-card__tag-text">{{ tag }}</text>
						</view>
					</view>
					<view class="home-card__foot">
						<view v-if="item.price > 0" class="home-card__price">
							<text class="home-card__price-symbol">¥</text>
							<text class="home-card__price-value">{{ item.price }}</text>
							<text class="home-card__price-unit">起</text>
						</view>
						<view v-else class="home-card__price home-card__price--free">
							<text class="home-card__price-value">免费</text>
						</view>
						<view class="home-card__arrow">
							<text class="home-card__arrow-text">查看详情</text>
							<u-icon name="arrow-right" color="#FF6600" :size="13" />
						</view>
					</view>
				</view>
			</view>
		</view>

		<view class="index-list__tail">
			<text class="index-list__tail-text">— 已经到底啦 —</text>
		</view>

		<!-- 美食地图悬浮按钮 -->
		<view class="home-fab home-fab--food" @click="onFoodMapFab">
			<view class="home-fab__bg"></view>
			<u-icon name="map-fill" color="#FF6600" :size="24" />
		</view>

		<!-- AI 助手悬浮按钮 -->
		<view class="home-fab home-fab--ai" @click="onAiFab">
			<view class="home-fab__bg"></view>
			<view class="home-fab__ring"></view>
			<u-icon name="chat-fill" color="#FF6600" :size="28" />
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onShow,
		onPullDownRefresh
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO
	} from '@/common/theme.js';
	import {
		fetchBannerPage,
		fetchHomeModules,
		fetchHomeRecommend,
		fetchNoticeMarquee
	} from '@/common/api/home.js';
	import {
		readCachedCityLabel,
		resolveCurrentCityLabel
	} from '@/common/location.js';
	import {
		getAppLocationParams
	} from '@/common/location-params.js';
	import {
		openPage
	} from '@/common/navigation.js';
	import {
		browseDestinationLabel,
		readBrowseDestination
	} from '@/common/browse-destination.js';

	const locationLabel = ref('定位中');

	const TAB_DETAIL_PATH = {
		hotel: '/mobile-design/hotel/detail',
		route: '/mobile-design/route/detail',
		activity: '/mobile-design/activity/detail',
		scenic: '/mobile-design/scenic/detail',
		heritage: '/mobile-design/heritage/goods-detail',
		goods: '/mobile-design/heritage/goods-detail',
		culture: '/mobile-design/heritage/goods-detail'
	};

	function toastHomeApiError(label, res, fallback) {
		if (res && res.code === 0) return;
		uni.showToast({
			title: (res && res.msg) || fallback || `${label}加载失败`,
			icon: 'none'
		});
	}

	const bannerList = ref([]);

	const gridList = ref([]);

	const noticeText = ref('');
	const tabList = ref([]);
	const recommendTabs = ref([]);
	const currentTab = ref(0);
	const homeLoading = ref(false);

	const currentTabKey = computed(() => recommendTabs.value[currentTab.value]?.key || 'recommend');

	const filteredRecommendList = computed(() => {
		const tab = recommendTabs.value[currentTab.value];
		if (!tab || !Array.isArray(tab.items)) {
			return [];
		}
		return tab.items.map(mapRecommendItem);
	});

	function mapBannerItem(record) {
		return {
			image: record.imageUrl || PLACEHOLDER_LOGO,
			title: record.name || record.description || '',
			linkPath: record.url || ''
		};
	}

	function mapRecommendItem(item) {
		return {
			id: item.id,
			name: item.title || '',
			address: item.address || '',
			price: item.price != null ? Number(item.price) : 0,
			tags: item.tag ? [item.tag] : [],
			cover: item.cover || PLACEHOLDER_LOGO,
			itemType: item.itemType || '',
			linkPath: item.linkPath || ''
		};
	}

	function isGridIconImage(value) {
		if (!value) return false;
		const text = String(value).trim();
		return /^https?:\/\//.test(text) || text.startsWith('/') || /^data:image\//.test(text);
	}

	function mapGridItem(record) {
		const icon = record.icon || '';
		return {
			id: record.key,
			label: record.label || '',
			iconImage: isGridIconImage(icon) ? icon : PLACEHOLDER_LOGO,
			path: record.path || ''
		};
	}

	const navigateSafe = (url) => openPage(url);

	const onGridClick = (item) => {
		if (item.path) {
			navigateSafe(item.path);
		}
	};

	const onTabClick = (index) => {
		if (!recommendTabs.value[index]) {
			return;
		}
		currentTab.value = index;
	};

	const onBannerClick = (index) => {
		const banner = bannerList.value[index];
		if (banner?.linkPath) {
			navigateSafe(banner.linkPath);
		}
	};

	const onNoticeMore = () => {
		navigateSafe('/mobile-design/notice/list');
	};

	const onCardClick = (item) => {
		if (item.linkPath) {
			navigateSafe(item.linkPath);
			return;
		}
		const tabKey = item.itemType || currentTabKey.value;
		if (tabKey === 'recommend' || tabKey === 'merchant') {
			return;
		}
		const path = TAB_DETAIL_PATH[tabKey];
		if (!path || !item.id) {
			return;
		}
		navigateSafe(`${path}?id=${item.id}`);
	};

	const onFoodMapFab = () => {
		navigateSafe('/mobile-design/map/index?type=food');
	};

	const onAiFab = () => {
		navigateSafe('/mobile-design/ai/chat');
	};

	const onSearchClick = () => {
		navigateSafe('/mobile-design/search/index');
	};

	const openDestinationSelect = () => {
		navigateSafe('/pages/destination/select');
	};

	/** 手动选择的浏览城市优先，否则用定位城市 */
	async function loadLocationCity() {
		const selectedLabel = browseDestinationLabel(readBrowseDestination());
		if (selectedLabel) {
			locationLabel.value = selectedLabel;
			return;
		}
		locationLabel.value = await resolveCurrentCityLabel();
	}

	function applyLocationFallback() {
		locationLabel.value = readCachedCityLabel() || '定位失败';
	}

	/** 首页 onShow 并行拉取轮播 / 推荐 / 公告（公开接口，按需加载） */
	async function loadHomeContent() {
		let locationParams = {};
		try {
			locationParams = await getAppLocationParams();
		} catch (e) {
			// 定位失败时仍请求全平台快捷入口等内容
		}
		const selectedDestination = readBrowseDestination();
		if (selectedDestination?.id) {
			locationParams.destinationId = selectedDestination.id;
		}
		homeLoading.value = true;
		try {
			const [bannerRes, gridRes, recommendRes, noticeRes] = await Promise.all([
				fetchBannerPage({
					page: 1,
					limit: 10,
					classify: 1,
					state: 1
				}),
				fetchHomeModules('grid', locationParams),
				fetchHomeRecommend(locationParams),
				fetchNoticeMarquee(locationParams)
			]);

			if (bannerRes.code === 0 && bannerRes.data?.records) {
				bannerList.value = bannerRes.data.records.map(mapBannerItem);
			} else {
				bannerList.value = [];
				toastHomeApiError('轮播图', bannerRes);
			}

			if (gridRes.code === 0 && Array.isArray(gridRes.data)) {
				gridList.value = gridRes.data.map(mapGridItem);
			} else {
				gridList.value = [];
				toastHomeApiError('快捷入口', gridRes);
			}

			if (recommendRes.code === 0 && recommendRes.data?.tabs) {
				recommendTabs.value = recommendRes.data.tabs;
				tabList.value = recommendRes.data.tabs.map((tab) => ({
					name: tab.title,
					key: tab.key
				}));
				if (currentTab.value >= tabList.value.length) {
					currentTab.value = 0;
				}
			} else {
				recommendTabs.value = [];
				tabList.value = [];
				toastHomeApiError('推荐内容', recommendRes);
			}

			if (noticeRes.code === 0 && noticeRes.data) {
				noticeText.value = noticeRes.data.text || '';
			} else {
				noticeText.value = '';
				toastHomeApiError('公告', noticeRes);
			}
		} catch (e) {
			uni.showToast({
				title: (e && e.msg) || (e && e.message) || '首页内容加载失败',
				icon: 'none'
			});
		} finally {
			homeLoading.value = false;
		}
	}

	onShow(async () => {
		let locationOk = false;
		const locationTask = loadLocationCity()
			.then(() => {
				locationOk = true;
			})
			.catch(() => {});
		await locationTask;
		if (!locationOk) {
			applyLocationFallback();
		}
		loadHomeContent();
	});

	onPullDownRefresh(async () => {
		let locationOk = false;
		const locationTask = loadLocationCity()
			.then(() => {
				locationOk = true;
			})
			.catch(() => {});
		await locationTask;
		if (!locationOk) {
			applyLocationFallback();
		}
		await loadHomeContent();
		uni.stopPullDownRefresh();
	});
</script>

<style lang="scss" scoped>
	.index-page {
		padding: 0 var(--ry-page-padding-x) var(--ry-page-padding-y);
		background: linear-gradient(180deg, #FFF6EE 0%, #F4F6FF 560rpx);

		/* #ifdef H5 || APP-PLUS */
		padding-bottom: calc(180rpx + env(safe-area-inset-bottom));
		/* #endif */
		/* #ifdef MP-WEIXIN */
		padding-bottom: 180rpx;
		/* #endif */
	}

	/* ============ 顶部搜索 ============ */
	.home-search-row {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-top: 8rpx;
	}

	.home-dest {
		flex-shrink: 0;
		display: flex;
		align-items: center;
		gap: 6rpx;
		cursor: pointer;
		padding: 0 16rpx;
		height: 80rpx;
		border-radius: 999rpx;
		background-color: rgba(255, 255, 255, 0.9);
		box-shadow: 0 6rpx 18rpx rgba(199, 102, 0, 0.12);

		&__name {
			max-width: 140rpx;
			font-size: 25rpx;
			font-weight: 500;
			color: #C25E00;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}
	}

	.home-search {
		position: relative;
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
		padding: 12rpx 12rpx 12rpx 28rpx;
		height: 56rpx;
		border-radius: 999rpx;
		background-color: #FFFFFF;
		box-shadow: 0 10rpx 34rpx rgba(224, 110, 0, 0.14);

		&__placeholder {
			flex: 1;
			min-width: 0;
			margin-left: 16rpx;
			font-size: 26rpx;
			color: #A59A90;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__btn {
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
			height: 56rpx;
			padding: 0 34rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF8A3D, #FF6600);
			box-shadow: 0 8rpx 20rpx rgba(255, 102, 0, 0.28);

			&-text {
				font-size: 26rpx;
				font-weight: 600;
				color: #FFFFFF;
				letter-spacing: 2rpx;
			}
		}
	}

	/* ============ 轮播 Banner ============ */
	.index-banner {
		margin-top: 24rpx;
		// margin-left: calc(0rpx - var(--ry-page-padding-x));
		// margin-right: calc(0rpx - var(--ry-page-padding-x));
		// width: calc(100% + var(--ry-page-padding-x) * 2);

		&__slide {
			position: relative;
			width: 100%;
			height: 320rpx;
			overflow: hidden;
		}

		&__img {
			width: 100%;
			height: 320rpx;
			display: block;
			background-color: transparent;
		}

		&__veil {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 210rpx;
			background: linear-gradient(transparent, rgba(0, 0, 0, 0.52));
		}

		&__overlay {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			display: flex;
			flex-direction: column;
			align-items: flex-start;
			padding: 0 30rpx 56rpx;
		}

		&__chip {
			padding: 6rpx 18rpx;
			border-radius: 999rpx;
			background-color: rgba(255, 255, 255, 0.22);
			border: 1rpx solid rgba(255, 255, 255, 0.55);
			font-size: 20rpx;
			color: #FFFFFF;
			line-height: 1.4;
			backdrop-filter: blur(4rpx);
		}

		&__title {
			margin-top: 12rpx;
			font-size: 30rpx;
			font-weight: 800;
			color: #FFFFFF;
			line-height: 1.3;
		}
	}

	/* ============ 区块 ============ */
	.home-grid {
		margin-top: 24rpx;

		&__head {
			display: flex;
			align-items: baseline;
			padding: 0 6rpx;
		}

		&__title {
			font-size: 34rpx;
			font-weight: 800;
			color: #26221E;
		}

		&__sub {
			font-size: 22rpx;
			color: #B0A8A0;
		}

		&__panel {
			margin-top: 16rpx;
			padding: 20rpx 8rpx 22rpx;
		}

		&__item {
			display: flex;
			flex-direction: column;
			align-items: center;
			padding: 12rpx 0 16rpx;
		}

		&__icon {
			width: 88rpx;
			height: 88rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			background: transparent;
		}

		&__icon-img {
			width: 100%;
			height: 100%;
			display: block;
		}

		&__label {
			margin-top: 12rpx;
			font-size: 22rpx;
			color: #4A453F;
			line-height: 1.3;
			text-align: center;
			max-width: 150rpx;
		}
	}

	/* ============ lJ�?============ */
	.index-notice {
		margin-top: 24rpx;
		padding: 16rpx 16rpx 16rpx 20rpx;
		display: flex;
		align-items: center;
		overflow: hidden;

		&__tag {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 6rpx;
			padding: 10rpx 18rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF8A3D, #FF6600);
			box-shadow: 0 6rpx 16rpx rgba(255, 102, 0, 0.22);

			&-text {
				font-size: 22rpx;
				font-weight: 600;
				color: #FFFFFF;
				line-height: 1.4;
			}
		}

		&__body {
			flex: 1;
			min-width: 0;
			margin: 0 20rpx;
			overflow: hidden;

			:deep(.u-notice-bar) {
				padding: 0 !important;
			}

			:deep(.u-notice-bar__content) {
				font-size: 24rpx;
				color: #6B6B78;
			}
		}

		&__more {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 4rpx;
			font-size: 22rpx;
			color: #B0A8A0;
			line-height: 1.4;
		}
	}

	/* ============ { Tab ============ */
	.home-tabs {
		margin-top: 32rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		width: 100%;
		overflow-x: auto;
		-webkit-overflow-scrolling: touch;
		-ms-overflow-style: none;
		scrollbar-width: none;

		&::-webkit-scrollbar {
			display: none;
		}
	}

	.home-tab {
		flex: 1 0 calc(100% / 5);
		min-width: calc(100% / 5);
		box-sizing: border-box;
		position: relative;
		padding: 8rpx 0 16rpx;
		text-align: center;
		transition: color 0.2s ease;

		&__text {
			display: block;
			font-size: 28rpx;
			font-weight: 500;
			color: #8A8278;
			line-height: 1.3;
			white-space: nowrap;
		}

		&--active {
			.home-tab__text {
				color: #26221E;
				font-weight: 700;
			}

			&::after {
				content: '';
				position: absolute;
				left: 50%;
				bottom: 0;
				width: 40rpx;
				height: 6rpx;
				border-radius: 999rpx;
				background: linear-gradient(135deg, #FF8A3D, #FF6600);
				transform: translateX(-50%);
			}
		}
	}

	/* ============ 区块 ============ */
	.index-list {
		margin-top: 24rpx;
		display: flex;
		flex-direction: column;
		gap: 24rpx;

		&__empty {
			padding: 60rpx 0;
		}

		&__tail {
			display: flex;
			justify-content: center;
			align-items: center;
			padding: 34rpx 0 12rpx;

			&-text {
				font-size: 22rpx;
				color: #C0B8AF;
				letter-spacing: 2rpx;
			}
		}
	}

	.home-card {
		overflow: hidden;

		&__cover {
			position: relative;
			height: 300rpx;
			overflow: hidden;
			background-color: transparent;
		}

		&__img {
			width: 100%;
			height: 300rpx;
			display: block;
			background-color: transparent;
			transform: scale(1.48);
			transform-origin: center center;
		}

		&__info {
			padding: 26rpx 26rpx 28rpx;
		}

		&__name-row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 31rpx;
			font-weight: 700;
			color: #26221E;
			line-height: 1.35;
		}

		&__addr {
			display: flex;
			align-items: baseline;
			gap: 8rpx;
			margin-top: 10rpx;
			min-height: 34rpx;
		}

		&__addr-text {
			flex: 1;
			min-width: 0;
			font-size: 23rpx;
			color: #A0A0A0;
			overflow: hidden;
			text-overflow: ellipsis;
			white-space: nowrap;
		}

		&__tags {
			display: flex;
			flex-wrap: wrap;
			gap: 12rpx;
			margin-top: 14rpx;
		}

		&__tag {
			padding: 6rpx 16rpx;
			background-color: #F5F6F8;
			border-radius: 8rpx;

			&-text {
				font-size: 21rpx;
				color: #6B6B73;
				line-height: 1.4;
			}
		}

		&__foot {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-top: 8rpx;
		}

		&__price {
			display: flex;
			align-items: baseline;

			&-symbol {
				font-size: 22rpx;
				font-weight: 600;
				color: #FF6600;
			}

			&-value {
				margin-left: 4rpx;
				font-size: 40rpx;
				font-weight: 800;
				color: #FF6600;
				line-height: 1;
			}

			&-unit {
				margin-left: 6rpx;
				font-size: 20rpx;
				color: #B0A8A0;
			}

			&--free {
				.home-card__price-value {
					font-size: 30rpx;
					font-weight: 700;
					color: #2EAF6A;
				}
			}
		}

		&__arrow {
			display: flex;
			align-items: center;
			gap: 4rpx;
			padding: 12rpx 22rpx;
			border-radius: 999rpx;
			background-color: #FFF3EB;

			&-text {
				font-size: 24rpx;
				font-weight: 600;
				color: #E8670C;
				line-height: 1.3;
			}
		}
	}

	/* ============ 轮播 Banner ============ */
	.home-fab {
		position: fixed;
		right: 22rpx;
		z-index: 98;
		display: flex;
		align-items: center;
		justify-content: center;
		width: 96rpx;
		height: 96rpx;

		&__bg {
			position: absolute;
			top: 0;
			left: 0;
			width: 96rpx;
			height: 96rpx;
			border-radius: 50%;
			background-color: #FFFFFF;
			box-shadow: 0 8rpx 24rpx rgba(0, 0, 0, 0.15);
		}

		&--food {
			bottom: calc(358rpx + env(safe-area-inset-bottom));
		}

		&--ai {
			bottom: calc(236rpx + env(safe-area-inset-bottom));
		}

		&__ring {
			position: absolute;
			top: 50%;
			left: 50%;
			transform: translate(-50%, -50%);
			width: 66rpx;
			height: 66rpx;
			border-radius: 50%;
			border: 2rpx dashed rgba(255, 102, 0, 0.5);
			pointer-events: none;
			z-index: 1;
		}
	}

	/* #ifdef MP-WEIXIN */
	.home-fab--food {
		bottom: 358rpx;
	}

	.home-fab--ai {
		bottom: 266rpx;
		margin-top: 30rpx;
	}

	/* #endif */
</style>

<template>
	<view class="hsd-page">
		<!-- 顶部轮播 -->
		<view class="hsd-hero">
			<swiper
				class="hsd-hero__swiper"
				:indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)"
				indicator-active-color="#FFFFFF"
				:circular="bannerList.length > 1"
				@change="onBannerChange"
			>
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="hsd-hero__item">
					<image
						class="hsd-hero__img ry-placeholder-img"
						:src="item || PLACEHOLDER_LOGO"
						mode="aspectFill"
						@click="previewBannerImages(index)"
					/>
				</swiper-item>
			</swiper>
			<view class="hsd-hero__mask" />
			<view v-if="bannerList.length > 1" class="hsd-hero__count">
				<text class="hsd-hero__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text>
			</view>
		</view>

		<view class="hsd-body">
			<!-- 门店主信息 -->
			<view class="hsd-header">
				<text class="hsd-header__name">{{ shop.name }}</text>
				<view class="hsd-header__meta">
					<view class="hsd-header__rating">
						<u-icon name="star-fill" color="#FF9800" :size="14" />
						<text class="hsd-header__rating-text">{{ shop.rating }}</text>
					</view>
					<text class="hsd-header__reviews">{{ shop.reviewCount }}{{ ui.reviewCountSuffix }}</text>
				</view>
				<view class="hsd-header__tags">
					<view v-for="tag in shop.tags" :key="tag" class="hsd-header__tag">
						<text class="hsd-header__tag-text">{{ tag }}</text>
					</view>
				</view>
			</view>

			<!-- 营业 / 电话 / 地址 -->
			<view class="hsd-info">
				<view class="hsd-info__row">
					<view class="hsd-info__icon" style="background-color: #F5E8DF;">
						<u-icon name="clock-fill" color="#B45D2D" :size="16" />
					</view>
					<view class="hsd-info__content">
						<text class="hsd-info__label">{{ ui.shopDetailHours }}</text>
						<text class="hsd-info__value">{{ shop.hours }}</text>
					</view>
				</view>
				<view class="hsd-info__row" @click="onCall">
					<view class="hsd-info__icon" style="background-color: #E8F5E9;">
						<u-icon name="phone-fill" color="#2EAF6A" :size="16" />
					</view>
					<view class="hsd-info__content">
						<text class="hsd-info__label">{{ ui.shopDetailPhone }}</text>
						<text class="hsd-info__value">{{ shop.phone }}</text>
					</view>
					<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
				</view>
				<view class="hsd-info__row" @click="onNavigate">
					<view class="hsd-info__icon" style="background-color: #E3F2FD;">
						<u-icon name="map-fill" color="#2979FF" :size="16" />
					</view>
					<view class="hsd-info__content">
						<text class="hsd-info__label">{{ ui.shopDetailAddr }}</text>
						<text class="hsd-info__value">{{ shop.address }}</text>
					</view>
					<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
				</view>
			</view>

			<!-- 门店介绍 -->
			<view class="hsd-section">
				<view class="hsd-section__head">
					<view class="hsd-section__deco" />
					<text class="hsd-section__title">{{ ui.shopDetailIntro }}</text>
				</view>
				<text class="hsd-section__text">{{ shop.introDetail || shop.intro }}</text>
			</view>

			<!-- 特色服务 -->
			<view class="hsd-section">
				<view class="hsd-section__head">
					<view class="hsd-section__deco" />
					<text class="hsd-section__title">{{ ui.shopDetailService }}</text>
				</view>
				<view class="hsd-services">
					<view v-for="service in shop.services" :key="service" class="hsd-services__item">
						<u-icon name="checkmark-circle-fill" color="#B45D2D" :size="14" />
						<text class="hsd-services__text">{{ service }}</text>
					</view>
				</view>
			</view>

			<!-- 门店好物 -->
			<view v-if="featuredGoods.length" class="hsd-section">
				<view class="hsd-section__head hsd-section__head--row">
					<view class="hsd-section__head-left">
						<view class="hsd-section__deco" />
						<text class="hsd-section__title">{{ ui.shopDetailGoods }}</text>
					</view>
					<view class="hsd-section__more" @click="goGoods">
						<text class="hsd-section__more-text">{{ ui.btnAllGoods }}</text>
						<u-icon name="arrow-right" color="#8D6E63" :size="12" />
					</view>
				</view>
				<scroll-view class="hsd-goods-scroll" scroll-x :show-scrollbar="false">
					<view class="hsd-goods-row">
						<view
							v-for="item in featuredGoods"
							:key="item.id"
							class="hsd-goods-card"
							@click="openGoods(item)"
						>
							<image class="hsd-goods-card__cover ry-placeholder-img" :src="item.cover" mode="aspectFill" />
							<text class="hsd-goods-card__name">{{ item.name }}</text>
							<text class="hsd-goods-card__price">¥{{ item.price }}</text>
						</view>
					</view>
				</scroll-view>
			</view>

			<text class="hsd-footer">{{ ui.footerShopDetail }}</text>
		</view>

		<!-- 底部操作栏 -->
		<view class="hsd-bar">
			<view class="hsd-bar__btn hsd-bar__btn--ghost" @click="onCall">
				<u-icon name="phone-fill" color="#8D6E63" :size="18" />
				<text class="hsd-bar__btn-text hsd-bar__btn-text--ghost">{{ ui.btnContact }}</text>
			</view>
			<view class="hsd-bar__btn hsd-bar__btn--primary" @click="goGoods">
				<u-icon name="bag-fill" color="#FFFFFF" :size="18" />
				<text class="hsd-bar__btn-text">{{ ui.btnAllGoods }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import {
	fetchShopDetail,
	fetchShopGoodsPreview,
	mapShopItem,
	mapGoodsPreviewItem
} from '@/common/api/heritage.js';
import { HERITAGE_UI } from '@/common/heritage-ui.js';
import { openMapNavigation } from '@/common/location.js';

const ui = HERITAGE_UI;
const shopId = ref('');
const shop = ref({
	name: '',
	cover: PLACEHOLDER_LOGO,
	coverFrom: '#B45D2D',
	coverTo: '#8D4E2A',
	bannerImages: [],
	tags: [],
	services: [],
	rating: '',
	reviewCount: 0,
	hours: '',
	phone: '',
	address: '',
	latitude: null,
	longitude: null,
	intro: '',
	introDetail: ''
});
const featuredGoods = ref([]);
const loading = ref(false);
const bannerCurrent = ref(0);

const bannerList = computed(() => {
	const images = (shop.value.bannerImages || []).filter(Boolean);
	if (images.length) {
		return images;
	}
	return shop.value.cover ? [shop.value.cover] : [PLACEHOLDER_LOGO];
});

function onBannerChange(event) {
	bannerCurrent.value = Number(event?.detail?.current ?? 0);
}

function previewBannerImages(index) {
	const urls = bannerList.value.filter(Boolean);
	if (!urls.length) {
		return;
	}
	uni.previewImage({
		urls,
		current: urls[index] || urls[0]
	});
}

function onCall() {
	if (!shop.value.phone) return;
	uni.makePhoneCall({ phoneNumber: shop.value.phone });
}

function onNavigate() {
	const { latitude, longitude, address, name } = shop.value;
	openMapNavigation({
		latitude,
		longitude,
		name: name || '店铺位置',
		address: address || '',
		scale: 18
	});
}

function goGoods() {
	if (!shop.value.id) return;
	uni.navigateTo({
		url: `/mobile-design/heritage/goods-list?shopId=${shop.value.id}&shopName=${encodeURIComponent(shop.value.name)}`
	});
}

function openGoods(item) {
	uni.navigateTo({ url: `/mobile-design/heritage/goods-detail?id=${item.id}` });
}

async function loadShopDetail() {
	if (!shopId.value || loading.value) {
		return;
	}
	loading.value = true;
	try {
		const res = await fetchShopDetail(shopId.value);
		if (res.code === 0 && res.data) {
			shop.value = mapShopItem(res.data);
			bannerCurrent.value = 0;
			uni.setNavigationBarTitle({ title: shop.value.name || '好店详情' });
		} else {
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		loading.value = false;
	}
}

async function loadFeaturedGoods() {
	if (!shopId.value) {
		return;
	}
	try {
		const res = await fetchShopGoodsPreview(shopId.value, 5);
		if (res.code === 0 && res.data) {
			featuredGoods.value = (res.data.records || []).map(mapGoodsPreviewItem);
		} else {
			featuredGoods.value = [];
		}
	} catch (e) {
		featuredGoods.value = [];
	}
}

onLoad((options) => {
	shopId.value = options?.id || '';
});

onShow(async () => {
	if (!shopId.value) {
		return;
	}
	await loadShopDetail();
	await loadFeaturedGoods();
});
</script>

<style lang="scss" scoped>
.hsd-page {
	min-height: 100vh;
	background-color: #faf6f3;
	padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.hsd-hero {
	position: relative;
	height: 520rpx;
	overflow: hidden;

	&__swiper,
	&__item {
		width: 100%;
		height: 100%;
	}

	&__img {
		width: 100%;
		height: 100%;
		display: block;
	}

	&__mask {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		height: 120rpx;
		background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.18));
		pointer-events: none;
		z-index: 1;
	}

	&__count {
		position: absolute;
		right: 32rpx;
		bottom: 32rpx;
		padding: 6rpx 16rpx;
		border-radius: var(--ry-radius-pill);
		background-color: rgba(0, 0, 0, 0.35);
		z-index: 2;
	}

	&__count-text {
		font-size: 22rpx;
		line-height: 1;
		color: #ffffff;
	}
}

.hsd-body {
	position: relative;
	margin-top: -32rpx;
	padding: 0 var(--ry-page-padding-x);
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	z-index: 3;
}

.hsd-header {
	padding: 28rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 12rpx 40rpx rgba(74, 55, 40, 0.1);

	&__name {
		display: block;
		font-size: 40rpx;
		font-weight: 800;
		color: #4a3728;
		line-height: 1.3;
	}

	&__meta {
		display: flex;
		align-items: center;
		gap: 16rpx;
		margin-top: 12rpx;
	}

	&__rating {
		display: flex;
		align-items: center;
		gap: 4rpx;
	}

	&__rating-text {
		font-size: 26rpx;
		font-weight: 700;
		color: #ff9800;
	}

	&__reviews,
	&__distance {
		font-size: 22rpx;
		color: #999999;
	}

	&__tags {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
		margin-top: 16rpx;
	}

	&__tag {
		display: flex;
		align-items: center;
		justify-content: center;
		padding: 6rpx 16rpx;
		border-radius: 999rpx;
		background-color: #f5e8df;
	}

	&__tag-text {
		font-size: 22rpx;
		line-height: 1;
		color: #b45d2d;
		text-align: center;
	}
}

.hsd-info {
	padding: 8rpx 24rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);

	&__row {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 20rpx 0;
		border-bottom: 1rpx solid #f5f0eb;

		&:last-child {
			border-bottom: none;
		}

		&:active {
			opacity: 0.88;
		}
	}

	&__icon {
		width: 48rpx;
		height: 48rpx;
		border-radius: 12rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	&__content {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	&__label {
		font-size: 22rpx;
		color: #999999;
	}

	&__value {
		font-size: 26rpx;
		color: #4a3728;
		line-height: 1.4;
	}
}

.hsd-section {
	padding: 24rpx 28rpx;
	border-radius: 24rpx;
	background-color: #ffffff;
	box-shadow: 0 4rpx 20rpx rgba(74, 55, 40, 0.06);

	&__head {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 16rpx;

		&--row {
			justify-content: space-between;
		}
	}

	&__head-left {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	&__deco {
		width: 6rpx;
		height: 28rpx;
		border-radius: 3rpx;
		background: linear-gradient(180deg, #b45d2d, #5d4037);
	}

	&__title {
		font-size: 30rpx;
		font-weight: 700;
		color: #4a3728;
	}

	&__more {
		display: flex;
		align-items: center;
		gap: 4rpx;
	}

	&__more-text {
		font-size: 24rpx;
		color: #8d6e63;
	}

	&__text {
		font-size: 26rpx;
		color: #666666;
		line-height: 1.8;
		text-align: justify;
	}
}

.hsd-services {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16rpx;

	&__item {
		display: flex;
		align-items: center;
		gap: 8rpx;
		padding: 12rpx 16rpx;
		border-radius: 12rpx;
		background-color: #faf6f3;
	}

	&__text {
		font-size: 24rpx;
		color: #666666;
	}
}

.hsd-goods-scroll {
	width: 100%;
	white-space: nowrap;
	margin: 0 -8rpx;
}

.hsd-goods-row {
	display: inline-flex;
	gap: 16rpx;
	padding: 4rpx 8rpx 8rpx;
}

.hsd-goods-card {
	display: inline-flex;
	flex-direction: column;
	width: 200rpx;
	border-radius: 16rpx;
	background-color: #faf6f3;
	overflow: hidden;

	&:active {
		opacity: 0.9;
	}

	&__cover {
		width: 200rpx;
		height: 200rpx;
		background-color: #f5f0eb;
	}

	&__name {
		padding: 12rpx 12rpx 0;
		font-size: 24rpx;
		font-weight: 600;
		color: #4a3728;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__price {
		padding: 6rpx 12rpx 12rpx;
		font-size: 26rpx;
		font-weight: 700;
		color: #b45d2d;
	}
}

.hsd-footer {
	display: block;
	text-align: center;
	font-size: 22rpx;
	color: #cccccc;
	padding: 8rpx 0 16rpx;
}

.hsd-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	z-index: 99;
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 20rpx var(--ry-page-padding-x);
	padding-bottom: calc(20rpx + env(safe-area-inset-bottom));
	background-color: #ffffff;
	border-top: 1rpx solid #f0ebe6;
	box-shadow: 0 -4rpx 16rpx rgba(74, 55, 40, 0.06);

	&__btn {
		flex: 1;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 8rpx;
		height: 80rpx;
		border-radius: 999rpx;

		&--ghost {
			border: 1rpx solid #e8ddd4;
			background-color: #faf6f3;
		}

		&--primary {
			background: linear-gradient(135deg, #8d6e63, #5d4037);
			box-shadow: 0 8rpx 24rpx rgba(93, 64, 55, 0.3);
		}

		&:active {
			opacity: 0.9;
		}
	}

	&__btn-text {
		font-size: 28rpx;
		font-weight: 600;
		color: #ffffff;

		&--ghost {
			color: #8d6e63;
		}
	}
}
</style>

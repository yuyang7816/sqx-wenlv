<template>
	<view class="gd-page">
		<view class="gd-cover">
			<swiper
				class="gd-cover__swiper"
				:indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)"
				indicator-active-color="#FFFFFF"
				:autoplay="bannerList.length > 1"
				:circular="true"
				:interval="4000"
				duration="300"
				@change="onBannerChange"
			>
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="gd-cover__item">
					<image
						class="gd-cover__img ry-placeholder-img"
						:src="item"
						mode="aspectFill"
						@click="previewBanner(index)"
					/>
				</swiper-item>
			</swiper>
			<view class="gd-cover__mask" />
			<view v-if="bannerList.length > 1" class="gd-cover__count">
				<text class="gd-cover__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text>
			</view>
		</view>

		<view class="gd-body">
			<view class="gd-info ry-card">
				<view class="gd-info__price-row">
					<text class="gd-info__symbol">¥</text>
					<text class="gd-info__price">{{ dish.price }}</text>
					<text class="gd-info__sold">月售 {{ dish.sold ?? 0 }}</text>
				</view>
				<text class="gd-info__name">{{ dish.name }}</text>
				<text v-if="dish.desc" class="gd-info__desc">{{ dish.desc }}</text>
			</view>

			<view class="gd-shop ry-card" @click="goShop">
				<view v-if="shopCoverImage" class="gd-shop__banner">
					<image
						class="gd-shop__banner-img"
						:src="shopCoverImage"
						mode="aspectFill"
					/>
				</view>
				<view class="gd-shop__body">
					<view class="gd-shop__head">
						<view class="gd-shop__icon">
							<u-icon name="home-fill" :color="accentColor" :size="16" />
						</view>
						<view class="gd-shop__meta">
							<text class="gd-shop__label">所属门店</text>
							<text class="gd-shop__name">{{ dish.shopName || '门店信息' }}</text>
						</view>
						<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
					</view>
					<view v-if="dish.shopAddress" class="gd-shop__addr">
						<view class="gd-shop__addr-icon">
							<u-icon name="map-fill" :color="accentColor" :size="13" />
						</view>
						<text class="gd-shop__addr-text">{{ dish.shopAddress }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { fetchGooseDishDetail } from '@/common/api/goose.js';

const accentColor = getModuleColor('goose');
const GOOSE_DETAIL_PATH = '/mobile-design/goose/detail';

const dishId = ref('');
const pageLoading = ref(false);
const bannerCurrent = ref(0);
const dish = ref({
	id: '',
	name: '',
	desc: '',
	price: '',
	sold: 0,
	cover: '',
	banners: [],
	shopId: '',
	shopName: '',
	shopCover: '',
	shopAddress: ''
});

const bannerList = computed(() => {
	const images = (dish.value.banners || []).filter(Boolean);
	if (images.length) return images;
	if (dish.value.cover) return [dish.value.cover];
	return [PLACEHOLDER_LOGO];
});

const shopCoverImage = computed(() => dish.value.shopCover || '');

function mapDishData(data) {
	return {
		id: data.id,
		name: data.name || '',
		desc: data.desc || '',
		price: data.price || '',
		sold: data.sold ?? 0,
		cover: data.cover || '',
		banners: Array.isArray(data.banners) ? data.banners.filter(Boolean) : [],
		shopId: data.shopId,
		shopName: data.shopName || '',
		shopCover: data.shopCover || '',
		shopAddress: data.shopAddress || ''
	};
}

async function loadDishDetail() {
	if (!dishId.value || pageLoading.value) return;
	pageLoading.value = true;
	try {
		const res = await fetchGooseDishDetail(dishId.value);
		if (res.code !== 0 || !res.data) {
			uni.showToast({ title: res.msg || '菜品加载失败', icon: 'none' });
			return;
		}
		dish.value = mapDishData(res.data);
		if (dish.value.name) {
			uni.setNavigationBarTitle({ title: dish.value.name });
		}
	} catch (e) {
		uni.showToast({ title: '加载失败，请稍后重试', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

function onBannerChange(e) {
	bannerCurrent.value = e.detail.current;
}

function previewBanner(index) {
	const urls = bannerList.value.filter(Boolean);
	if (!urls.length) return;
	uni.previewImage({
		urls,
		current: urls[index] || urls[0]
	});
}

function goShop() {
	if (!dish.value.shopId) {
		uni.showToast({ title: '门店信息缺失', icon: 'none' });
		return;
	}
	uni.navigateTo({ url: `${GOOSE_DETAIL_PATH}?id=${dish.value.shopId}` });
}

onLoad((options) => {
	dishId.value = options?.id ? String(options.id) : '';
	if (options?.name) {
		uni.setNavigationBarTitle({ title: decodeURIComponent(options.name) });
	}
});

onShow(() => {
	if (dishId.value) {
		loadDishDetail();
	}
});
</script>

<style lang="scss" scoped>
.gd-page {
	min-height: 100vh;
	background-color: #FFF9F2;
	padding-bottom: calc(32rpx + env(safe-area-inset-bottom));
}

.gd-cover {
	position: relative;
	width: 100%;
	height: 420rpx;
	overflow: hidden;
	background-color: #FFF3EB;

	&__swiper,
	&__item,
	&__img {
		width: 100%;
		height: 420rpx;
	}

	&__img {
		display: block;
	}

	&__mask {
		position: absolute;
		left: 0;
		right: 0;
		bottom: 0;
		height: 120rpx;
		background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.28));
		pointer-events: none;
		z-index: 2;
	}

	&__count {
		position: absolute;
		right: 24rpx;
		bottom: 20rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		height: 36rpx;
		padding: 0 16rpx;
		border-radius: var(--ry-radius-pill);
		background-color: rgba(0, 0, 0, 0.38);
		z-index: 3;
	}

	&__count-text {
		font-size: 20rpx;
		color: #FFFFFF;
	}
}

.gd-body {
	margin-top: -32rpx;
	padding: 0 var(--ry-page-padding-x) var(--ry-page-padding-y);
	position: relative;
	z-index: 4;
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.gd-info {
	padding: 32rpx 28rpx;
	border-radius: 32rpx 32rpx var(--ry-radius-card) var(--ry-radius-card);
	box-shadow: 0 12rpx 40rpx rgba(255, 122, 0, 0.1);

	&__price-row {
		display: flex;
		align-items: baseline;
		gap: 4rpx;
		margin-bottom: 16rpx;
	}

	&__symbol {
		font-size: 28rpx;
		font-weight: 700;
		color: #FF7A00;
	}

	&__price {
		font-size: 48rpx;
		font-weight: 800;
		color: #FF7A00;
		line-height: 1;
	}

	&__sold {
		margin-left: auto;
		font-size: 22rpx;
		color: #BBBBBB;
	}

	&__name {
		display: block;
		font-size: 36rpx;
		font-weight: 800;
		color: #333333;
		line-height: 1.4;
	}

	&__desc {
		display: block;
		margin-top: 16rpx;
		font-size: 26rpx;
		color: #666666;
		line-height: 1.65;
	}
}

.gd-shop {
	overflow: hidden;

	&:active {
		background-color: #FFFBF7;
	}

	&__banner {
		width: 100%;
		height: 180rpx;
		background-color: #FFF3EB;
	}

	&__banner-img {
		width: 100%;
		height: 100%;
		display: block;
	}

	&__body {
		padding: 24rpx;
	}

	&__head {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	&__icon {
		flex-shrink: 0;
		width: 48rpx;
		height: 48rpx;
		border-radius: 12rpx;
		background-color: #FFF6EF;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__meta {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__label {
		font-size: 22rpx;
		color: #999999;
		line-height: 1;
	}

	&__name {
		font-size: 30rpx;
		font-weight: 700;
		color: #1A1A1A;
		line-height: 1.35;
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;
	}

	&__addr {
		display: flex;
		align-items: center;
		gap: 8rpx;
		margin-top: 16rpx;
		padding-top: 16rpx;
		border-top: 1rpx solid #FFF0E6;
	}

	&__addr-icon {
		flex-shrink: 0;
		width: 28rpx;
		height: 28rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__addr-text {
		flex: 1;
		min-width: 0;
		font-size: 24rpx;
		color: #666666;
		line-height: 1.5;
		overflow: hidden;
		text-overflow: ellipsis;
		display: -webkit-box;
		-webkit-line-clamp: 2;
		-webkit-box-orient: vertical;
	}
}
</style>

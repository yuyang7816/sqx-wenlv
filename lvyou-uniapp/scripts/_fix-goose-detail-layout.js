const fs = require('fs');
const path = require('path');

const COVER_HEIGHT = 420;
const target = path.join(__dirname, '../mobile-design/goose/detail.vue');

const content = `<template>
	<view class="goose-detail-page">
		<view class="goose-cover">
			<swiper
				class="goose-cover__swiper"
				:style="{ height: coverHeight + 'rpx' }"
				:indicator-dots="bannerList.length > 1"
				indicator-color="rgba(255,255,255,0.45)"
				indicator-active-color="#FFFFFF"
				:autoplay="bannerList.length > 1"
				:circular="true"
				:interval="4000"
				duration="300"
				@change="onBannerChange"
			>
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="goose-cover__item">
					<image
						class="goose-cover__img ry-placeholder-img"
						:src="item"
						mode="aspectFill"
						@click="previewBanner(index)"
					/>
				</swiper-item>
			</swiper>
			<view class="goose-cover__mask" />
			<view v-if="shop.badgeText" class="goose-cover__badge">
				<text class="goose-cover__badge-text">{{ shop.badgeText }}</text>
			</view>
			<view v-if="bannerList.length > 1" class="goose-cover__count">
				<text class="goose-cover__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text>
			</view>
		</view>

		<view class="goose-detail-body">
			<view class="goose-header ry-card">
				<view class="goose-header__head">
					<text class="goose-header__name">{{ shop.name }}</text>
					<view v-if="priceFrom !== '--'" class="goose-header__price">
						<text class="goose-header__price-symbol">¥</text>
						<text class="goose-header__price-num">{{ priceFrom }}</text>
						<text class="goose-header__price-unit">起</text>
					</view>
					<text v-else class="goose-header__price goose-header__price--muted">待定</text>
				</view>

				<view class="goose-header__meta">
					<view class="goose-header__meta-item">
						<u-icon name="star-fill" :color="accentColor" :size="11" />
						<text class="goose-header__meta-text">{{ shop.rating || '--' }}</text>
					</view>
					<text class="goose-header__meta-dot">·</text>
					<text class="goose-header__meta-text">月售 {{ shop.monthlySales ?? 0 }}</text>
				</view>

				<view v-if="shop.tags && shop.tags.length" class="goose-header__tags">
					<view v-for="tag in shop.tags" :key="tag" class="goose-header__tag">
						<text class="goose-header__tag-text">{{ tag }}</text>
					</view>
				</view>

				<view class="goose-header__address" @click="onAddressClick">
					<u-icon name="map" color="#C8C8C8" :size="13" />
					<text class="goose-header__address-text">{{ shop.address }}</text>
					<u-icon name="arrow-right" :size="12" color="#CCCCCC" />
				</view>

				<view class="goose-header__quick">
					<view class="goose-header__quick-item" @click="onPhoneCall">
						<u-icon name="phone-fill" :color="accentColor" :size="18" />
						<text class="goose-header__quick-text">电话</text>
					</view>
					<view class="goose-header__quick-item" @click="onAddressClick">
						<u-icon name="map-fill" :color="accentColor" :size="18" />
						<text class="goose-header__quick-text">导航</text>
					</view>
					<view class="goose-header__quick-item" @click="onCanteenReserve">
						<u-icon name="calendar-fill" :color="accentColor" :size="18" />
						<text class="goose-header__quick-text">预约</text>
					</view>
				</view>
			</view>

			<view class="goose-section ry-card">
				<view class="goose-section__head">
					<view class="goose-section__icon">
						<u-icon name="file-text-fill" :color="accentColor" :size="16" />
					</view>
					<text class="goose-section__title">店铺介绍</text>
				</view>
				<text class="goose-section__para">{{ shop.intro }}</text>
			</view>

			<view class="goose-section ry-card">
				<view class="goose-section__head goose-section__head--split">
					<view class="goose-section__head-left">
						<view class="goose-section__icon">
							<u-icon name="gift-fill" :color="accentColor" :size="16" />
						</view>
						<text class="goose-section__title">招牌菜品</text>
					</view>
					<view class="goose-section__more" @click="onViewAllMenu">
						<text class="goose-section__more-text">查看全部</text>
						<u-icon name="arrow-right" :color="accentColor" :size="12" />
					</view>
				</view>
				<view class="goose-specialty">
					<view
						v-for="item in previewSpecialtyList"
						:key="item.id"
						class="goose-specialty__item"
						@click="onSpecialtyClick(item)"
					>
						<view class="goose-specialty__thumb-wrap">
							<image class="goose-specialty__thumb ry-placeholder-img" :src="item.cover || PLACEHOLDER_LOGO" mode="aspectFill" />
						</view>
						<view class="goose-specialty__info">
							<text class="goose-specialty__name">{{ item.name }}</text>
							<text class="goose-specialty__desc">{{ item.desc }}</text>
							<view class="goose-specialty__foot">
								<view class="goose-specialty__price-wrap">
									<text class="goose-specialty__symbol">¥</text>
									<text class="goose-specialty__price">{{ item.price }}</text>
								</view>
								<text class="goose-specialty__sales">月售 {{ item.sold }} 份</text>
							</view>
						</view>
						<view class="goose-specialty__action">
							<text class="goose-specialty__action-text">预约</text>
						</view>
					</view>
				</view>
			</view>

			<view class="goose-section ry-card">
				<view class="goose-section__head">
					<view class="goose-section__icon">
						<u-icon name="clock-fill" :color="accentColor" :size="16" />
					</view>
					<text class="goose-section__title">营业时间</text>
				</view>
				<view class="goose-hours">
					<view v-for="row in shop.businessHours" :key="row.id" class="goose-hours__row">
						<text class="goose-hours__label">{{ row.label }}</text>
						<text class="goose-hours__value">{{ row.time }}</text>
					</view>
				</view>
			</view>

			<text class="goose-footer">荣昌非遗美食 · 地道卤味</text>
		</view>

		<view class="goose-bar">
			<view class="goose-bar__phone" @click="onPhoneCall">
				<u-icon name="phone-fill" :color="accentColor" :size="22" />
				<text class="goose-bar__phone-text">电话咨询</text>
			</view>
			<view class="goose-bar__submit" @click="onCanteenReserve">
				<text class="goose-bar__submit-text">餐饮预约</text>
				<u-icon name="arrow-right" color="#FFFFFF" :size="14" />
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { fetchGooseDetail } from '@/common/api/goose.js';

const accentColor = getModuleColor('goose');
const coverHeight = ${COVER_HEIGHT};
const CANTEEN_RESERVE_PATH = '/mobile-design/canteen/reserve';
const GOOSE_MENU_PATH = '/mobile-design/goose/menu';
const GOOSE_DISH_DETAIL_PATH = '/mobile-design/goose/dish-detail';

const emptyShop = () => ({
	id: '',
	name: '',
	badgeText: '',
	rating: '',
	monthlySales: 0,
	address: '',
	latitude: 0,
	longitude: 0,
	phone: '',
	tags: [],
	intro: '',
	bannerList: [],
	specialtyList: [],
	businessHours: []
});

const shopId = ref('');
const shop = ref(emptyShop());
const bannerCurrent = ref(0);
const pageLoading = ref(false);

const bannerList = computed(() => {
	const images = (shop.value.bannerList || []).filter(Boolean);
	return images.length ? images : [PLACEHOLDER_LOGO];
});

const priceFrom = computed(() => {
	if (shop.value.priceFrom != null && shop.value.priceFrom !== '') {
		return shop.value.priceFrom;
	}
	const list = shop.value.specialtyList || [];
	if (!list.length) return '--';
	return Math.min(...list.map(item => Number(item.price)));
});

const previewSpecialtyList = computed(() => (shop.value.specialtyList || []).slice(0, 3));

function mapDetailData(data) {
	return {
		id: data.id,
		name: data.name || '',
		badgeText: data.badgeText || '',
		rating: data.rating || '',
		monthlySales: data.monthlySales ?? 0,
		address: data.address || '',
		latitude: data.latitude != null ? Number(data.latitude) : 0,
		longitude: data.longitude != null ? Number(data.longitude) : 0,
		phone: data.phone || '',
		tags: data.tags || [],
		intro: data.intro || '',
		priceFrom: data.priceFrom != null ? Number(data.priceFrom) : null,
		bannerList: data.bannerList || [],
		specialtyList: (data.specialtyList || []).map(item => ({
			id: item.id,
			name: item.name || '',
			desc: item.desc || '',
			price: item.price || '',
			sold: item.sold ?? 0,
			cover: item.cover || ''
		})),
		businessHours: data.businessHours || []
	};
}

/** onShow 拉取商家详情（公开接口） */
async function loadShopDetail() {
	if (!shopId.value || pageLoading.value) {
		return;
	}
	pageLoading.value = true;
	try {
		const res = await fetchGooseDetail(shopId.value);
		if (res.code !== 0 || !res.data) {
			uni.showToast({ title: res.msg || '商家详情加载失败', icon: 'none' });
			return;
		}
		shop.value = mapDetailData(res.data);
		if (shop.value.name) {
			uni.setNavigationBarTitle({ title: shop.value.name });
		}
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		pageLoading.value = false;
	}
}

const navigateSafe = (url) => uni.navigateTo({ url });
const onBannerChange = (e) => { bannerCurrent.value = e.detail.current; };

function previewBanner(index) {
	const urls = bannerList.value.filter(Boolean);
	if (!urls.length) return;
	uni.previewImage({
		urls,
		current: urls[index] || urls[0]
	});
}

onLoad((options) => {
	shopId.value = options?.id ? String(options.id) : '';
	if (options?.name) {
		uni.setNavigationBarTitle({ title: decodeURIComponent(options.name) });
	}
});

onShow(() => {
	if (shopId.value) {
		loadShopDetail();
	}
});

function onAddressClick() {
	const { latitude, longitude, address, name } = shop.value;
	if (!latitude || !longitude) {
		uni.showToast({ title: address || '暂无地址信息', icon: 'none' });
		return;
	}
	uni.openLocation({
		latitude,
		longitude,
		name,
		address,
		fail: () => uni.showToast({ title: address, icon: 'none' })
	});
}

function onPhoneCall() {
	const { phone } = shop.value;
	if (!phone) {
		uni.showToast({ title: '暂无联系电话', icon: 'none' });
		return;
	}
	/* #ifdef H5 */
	uni.showToast({ title: \`请拨打 \${phone}\`, icon: 'none' });
	return;
	/* #endif */
	uni.makePhoneCall({
		phoneNumber: phone.replace(/-/g, ''),
		fail: () => uni.showToast({ title: \`请拨打 \${phone}\`, icon: 'none' })
	});
}

const buildCanteenQuery = (dishName = '') => {
	const { id, name, address } = shop.value;
	const parts = [\`shopId=\${id}\`, \`shopName=\${encodeURIComponent(name)}\`, \`shopAddress=\${encodeURIComponent(address)}\`];
	if (dishName) parts.push(\`dishName=\${encodeURIComponent(dishName)}\`);
	return parts.join('&');
};

function onSpecialtyClick(item) {
	if (item?.id) {
		navigateSafe(\`\${GOOSE_DISH_DETAIL_PATH}?id=\${item.id}&name=\${encodeURIComponent(item.name || '')}\`);
		return;
	}
	navigateSafe(\`\${CANTEEN_RESERVE_PATH}?\${buildCanteenQuery(item.name)}\`);
}

function onCanteenReserve() {
	navigateSafe(\`\${CANTEEN_RESERVE_PATH}?\${buildCanteenQuery()}\`);
}

function onViewAllMenu() {
	navigateSafe(\`\${GOOSE_MENU_PATH}?id=\${shop.value.id}&shopName=\${encodeURIComponent(shop.value.name)}\`);
}
</script>

<style lang="scss" scoped>
	.goose-detail-page {
		min-height: 100vh;
		background-color: #FFF9F2;
		padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
	}

	.goose-cover {
		position: relative;
		width: 100%;
		overflow: hidden;
		background-color: #FFF3EB;

		&__swiper,
		&__item,
		&__img {
			width: 100%;
			height: ${COVER_HEIGHT}rpx;
			display: block;
		}

		&__mask {
			position: absolute;
			left: 0;
			right: 0;
			bottom: 0;
			height: 100rpx;
			background: linear-gradient(180deg, transparent, rgba(0, 0, 0, 0.18));
			pointer-events: none;
			z-index: 2;
		}

		&__badge {
			position: absolute;
			top: 20rpx;
			left: 24rpx;
			display: flex;
			align-items: center;
			justify-content: center;
			height: 40rpx;
			padding: 0 18rpx;
			border-radius: 999rpx;
			background-color: rgba(255, 122, 0, 0.88);
			z-index: 3;
		}

		&__badge-text {
			font-size: 22rpx;
			font-weight: 600;
			line-height: 1;
			color: #FFFFFF;
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
			border-radius: 999rpx;
			background-color: rgba(0, 0, 0, 0.38);
			z-index: 3;
		}

		&__count-text {
			font-size: 20rpx;
			line-height: 1;
			color: #FFFFFF;
		}
	}

	.goose-detail-body {
		margin-top: -24rpx;
		padding: 0 var(--ry-page-padding-x) var(--ry-page-padding-y);
		position: relative;
		z-index: 4;
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	.goose-header {
		padding: 24rpx 24rpx 20rpx;
		border-radius: 24rpx;
		box-shadow: 0 6rpx 24rpx rgba(255, 122, 0, 0.06);

		&__head {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
		}

		&__name {
			flex: 1;
			min-width: 0;
			font-size: 34rpx;
			font-weight: 800;
			color: #1A1A1A;
			line-height: 1.35;
		}

		&__price {
			flex-shrink: 0;
			display: flex;
			align-items: baseline;
			line-height: 1;
		}

		&__price-symbol {
			font-size: 22rpx;
			font-weight: 700;
			color: #FF7A00;
		}

		&__price-num {
			font-size: 36rpx;
			font-weight: 800;
			color: #FF7A00;
		}

		&__price-unit {
			margin-left: 2rpx;
			font-size: 20rpx;
			font-weight: 500;
			color: #FF9A40;
		}

		&__price--muted {
			font-size: 22rpx;
			color: #BBBBBB;
		}

		&__meta {
			display: flex;
			align-items: center;
			gap: 8rpx;
			margin-top: 10rpx;
		}

		&__meta-item {
			display: flex;
			align-items: center;
			gap: 4rpx;
		}

		&__meta-text {
			font-size: 22rpx;
			line-height: 1.2;
			color: #999999;
		}

		&__meta-dot {
			font-size: 22rpx;
			color: #DDDDDD;
		}

		&__tags {
			display: flex;
			flex-wrap: wrap;
			margin-top: 14rpx;
			gap: 8rpx;
		}

		&__tag {
			display: inline-flex;
			align-items: center;
			height: 36rpx;
			padding: 0 12rpx;
			border-radius: 8rpx;
			background-color: #FFF6EF;
		}

		&__tag-text {
			font-size: 20rpx;
			line-height: 1;
			color: #E86A00;
		}

		&__address {
			display: flex;
			align-items: center;
			gap: 8rpx;
			margin-top: 14rpx;
			padding: 0 2rpx;

			&:active {
				opacity: 0.75;
			}
		}

		&__address-text {
			flex: 1;
			min-width: 0;
			font-size: 24rpx;
			color: #666666;
			line-height: 1.5;
		}

		&__quick {
			display: flex;
			align-items: center;
			justify-content: space-around;
			margin-top: 20rpx;
			padding-top: 18rpx;
		}

		&__quick-item {
			flex: 1;
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 6rpx;

			&:active {
				opacity: 0.75;
			}
		}

		&__quick-text {
			font-size: 22rpx;
			color: #666666;
		}
	}

	.goose-section {
		padding: 22rpx 24rpx;

		&__head {
			display: flex;
			align-items: center;
			gap: 10rpx;
			margin-bottom: 16rpx;

			&--split {
				justify-content: space-between;
			}
		}

		&__head-left {
			display: flex;
			align-items: center;
			gap: 10rpx;
			min-width: 0;
		}

		&__more {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			gap: 4rpx;

			&:active {
				opacity: 0.75;
			}
		}

		&__more-text {
			font-size: 24rpx;
			line-height: 1;
			color: #FF7A00;
		}

		&__icon {
			width: 44rpx;
			height: 44rpx;
			border-radius: 12rpx;
			background-color: #FFF6EF;
			display: flex;
			align-items: center;
			justify-content: center;
			flex-shrink: 0;
		}

		&__title {
			font-size: 28rpx;
			font-weight: 700;
			line-height: 1;
			color: #333333;
		}

		&__para {
			display: block;
			font-size: 26rpx;
			color: #666666;
			line-height: 1.8;
		}
	}

	.goose-specialty {
		display: flex;
		flex-direction: column;
		gap: 12rpx;

		&__item {
			display: flex;
			align-items: center;
			gap: 14rpx;
			padding: 12rpx;
			border-radius: 16rpx;
			background-color: #FFFBF7;

			&:active {
				background-color: #FFF6EF;
			}
		}

		&__thumb-wrap {
			flex-shrink: 0;
			width: 132rpx;
			height: 132rpx;
			border-radius: 12rpx;
			overflow: hidden;
		}

		&__thumb {
			width: 132rpx;
			height: 132rpx;
			display: block;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 4rpx;
		}

		&__name {
			font-size: 28rpx;
			font-weight: 700;
			color: #333333;
			line-height: 1.35;
		}

		&__desc {
			font-size: 22rpx;
			color: #999999;
			line-height: 1.45;
			overflow: hidden;
			text-overflow: ellipsis;
			display: -webkit-box;
			-webkit-line-clamp: 2;
			-webkit-box-orient: vertical;
		}

		&__foot {
			display: flex;
			align-items: center;
			justify-content: space-between;
			margin-top: 4rpx;
		}

		&__price-wrap {
			display: flex;
			align-items: baseline;
			gap: 2rpx;
		}

		&__symbol {
			font-size: 20rpx;
			font-weight: 700;
			color: #FF7A00;
		}

		&__price {
			font-size: 30rpx;
			font-weight: 800;
			color: #FF7A00;
			line-height: 1;
		}

		&__sales {
			font-size: 20rpx;
			color: #BBBBBB;
		}

		&__action {
			flex-shrink: 0;
			display: flex;
			align-items: center;
			justify-content: center;
			min-width: 100rpx;
			height: 52rpx;
			padding: 0 22rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
		}

		&__action-text {
			font-size: 22rpx;
			font-weight: 600;
			line-height: 1;
			color: #FFFFFF;
		}
	}

	.goose-hours {
		display: flex;
		flex-direction: column;
		gap: 8rpx;

		&__row {
			display: flex;
			align-items: center;
			justify-content: space-between;
			gap: 16rpx;
			padding: 6rpx 0;
		}

		&__label {
			font-size: 26rpx;
			color: #666666;
		}

		&__value {
			font-size: 26rpx;
			font-weight: 600;
			color: #333333;
		}
	}

	.goose-footer {
		display: block;
		text-align: center;
		font-size: 22rpx;
		color: #CCCCCC;
		padding-top: 4rpx;
	}

	.goose-bar {
		position: fixed;
		left: 0;
		right: 0;
		bottom: 0;
		z-index: 100;
		display: flex;
		align-items: center;
		gap: 20rpx;
		padding: 16rpx var(--ry-page-padding-x);
		padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
		background-color: #FFFFFF;
		box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
		box-sizing: border-box;

		&__phone {
			flex-shrink: 0;
			display: flex;
			flex-direction: column;
			align-items: center;
			justify-content: center;
			width: 100rpx;
			gap: 4rpx;

			&:active {
				opacity: 0.75;
			}
		}

		&__phone-text {
			font-size: 20rpx;
			color: #666666;
		}

		&__submit {
			flex: 1;
			display: flex;
			align-items: center;
			justify-content: center;
			gap: 8rpx;
			height: 84rpx;
			border-radius: 999rpx;
			background: linear-gradient(135deg, #FF9A40, #FF7A00);
			box-shadow: 0 6rpx 20rpx rgba(255, 122, 0, 0.24);

			&:active {
				opacity: 0.9;
			}
		}

		&__submit-text {
			font-size: 30rpx;
			font-weight: 700;
			line-height: 1;
			color: #FFFFFF;
		}
	}
</style>
`;

fs.writeFileSync(target, content, 'utf8');
console.log('goose detail layout updated');

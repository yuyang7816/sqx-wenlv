const fs = require('fs');
const path = require('path');

const target = path.join(__dirname, '../mobile-design/goose/detail.vue');
const style = fs.readFileSync(target, 'utf8').split('<style lang="scss" scoped>')[1];

const content = `<template>
	<view class="goose-detail-page">
		<view class="goose-cover">
			<swiper class="goose-cover__swiper" :indicator-dots="bannerList.length > 1" indicator-color="rgba(255,255,255,0.45)" indicator-active-color="#FFFFFF" :autoplay="bannerList.length > 1" :circular="true" :interval="4000" duration="300" @change="onBannerChange">
				<swiper-item v-for="(item, index) in bannerList" :key="index" class="goose-cover__item">
					<image class="goose-cover__img ry-placeholder-img" :src="item" mode="aspectFill" />
				</swiper-item>
			</swiper>
			<view class="goose-cover__mask" />
			<view v-if="shop.badgeText" class="goose-cover__badge"><text class="goose-cover__badge-text">{{ shop.badgeText }}</text></view>
			<view v-if="bannerList.length > 1" class="goose-cover__count"><text class="goose-cover__count-text">{{ bannerCurrent + 1 }}/{{ bannerList.length }}</text></view>
		</view>
		<view class="goose-detail-body">
			<view class="goose-header ry-card">
				<text class="goose-header__name">{{ shop.name }}</text>
				<view class="goose-header__stats">
					<view class="goose-header__stat">
						<view class="goose-header__stat-top"><u-icon name="star-fill" :color="accentColor" :size="14" /><text class="goose-header__stat-num">{{ shop.rating }}</text></view>
						<text class="goose-header__stat-label">评分</text>
					</view>
					<view class="goose-header__stat-divider" />
					<view class="goose-header__stat"><text class="goose-header__stat-num">{{ shop.monthlySales }}</text><text class="goose-header__stat-label">月售</text></view>
					<view class="goose-header__stat-divider" />
					<view class="goose-header__stat"><text class="goose-header__stat-num goose-header__stat-num--price">¥{{ priceFrom }}</text><text class="goose-header__stat-label">起</text></view>
				</view>
				<view v-if="shop.tags && shop.tags.length" class="goose-header__tags">
					<view v-for="tag in shop.tags" :key="tag" class="goose-header__tag"><text class="goose-header__tag-text">{{ tag }}</text></view>
				</view>
				<view class="goose-header__address" @click="onAddressClick">
					<view class="goose-header__address-icon"><u-icon name="map-fill" :color="accentColor" :size="14" /></view>
					<text class="goose-header__address-text">{{ shop.address }}</text>
					<u-icon name="arrow-right" :size="12" color="#CCCCCC" />
				</view>
				<view class="goose-header__quick">
					<view class="goose-header__quick-item" @click="onPhoneCall"><u-icon name="phone-fill" :color="accentColor" :size="18" /><text class="goose-header__quick-text">电话</text></view>
					<view class="goose-header__quick-divider" />
					<view class="goose-header__quick-item" @click="onAddressClick"><u-icon name="map-fill" :color="accentColor" :size="18" /><text class="goose-header__quick-text">导航</text></view>
					<view class="goose-header__quick-divider" />
					<view class="goose-header__quick-item" @click="onCanteenReserve"><u-icon name="calendar-fill" :color="accentColor" :size="18" /><text class="goose-header__quick-text">预约</text></view>
				</view>
			</view>
			<view class="goose-section ry-card">
				<view class="goose-section__head"><view class="goose-section__icon"><u-icon name="file-text-fill" :color="accentColor" :size="16" /></view><text class="goose-section__title">店铺介绍</text></view>
				<text class="goose-section__para">{{ shop.intro }}</text>
			</view>
			<view class="goose-section ry-card">
				<view class="goose-section__head goose-section__head--split">
					<view class="goose-section__head-left"><view class="goose-section__icon"><u-icon name="gift-fill" :color="accentColor" :size="16" /></view><text class="goose-section__title">招牌菜品</text></view>
					<view class="goose-section__more" @click="onViewAllMenu"><text class="goose-section__more-text">查看全部</text><u-icon name="arrow-right" :color="accentColor" :size="12" /></view>
				</view>
				<view class="goose-specialty">
					<view v-for="item in previewSpecialtyList" :key="item.id" class="goose-specialty__item" @click="onSpecialtyClick(item)">
						<view class="goose-specialty__thumb-wrap"><image class="goose-specialty__thumb ry-placeholder-img" :src="item.cover || PLACEHOLDER_LOGO" mode="aspectFill" /></view>
						<view class="goose-specialty__info">
							<text class="goose-specialty__name">{{ item.name }}</text>
							<text class="goose-specialty__desc">{{ item.desc }}</text>
							<view class="goose-specialty__foot">
								<view class="goose-specialty__price-wrap"><text class="goose-specialty__symbol">¥</text><text class="goose-specialty__price">{{ item.price }}</text></view>
								<text class="goose-specialty__sales">月售 {{ item.sold }} 份</text>
							</view>
						</view>
						<view class="goose-specialty__action"><text class="goose-specialty__action-text">预约</text></view>
					</view>
				</view>
			</view>
			<view class="goose-section ry-card">
				<view class="goose-section__head"><view class="goose-section__icon"><u-icon name="clock-fill" :color="accentColor" :size="16" /></view><text class="goose-section__title">营业时间</text></view>
				<view class="goose-hours">
					<view v-for="row in shop.businessHours" :key="row.id" class="goose-hours__row">
						<view class="goose-hours__dot" /><text class="goose-hours__label">{{ row.label }}</text><text class="goose-hours__value">{{ row.time }}</text>
					</view>
				</view>
			</view>
			<text class="goose-footer">荣昌非遗美食 · 地道卤味</text>
		</view>
		<view class="goose-bar">
			<view class="goose-bar__phone" @click="onPhoneCall"><u-icon name="phone-fill" :color="accentColor" :size="22" /><text class="goose-bar__phone-text">电话咨询</text></view>
			<view class="goose-bar__submit" @click="onCanteenReserve"><text class="goose-bar__submit-text">餐饮预约</text><u-icon name="arrow-right" color="#FFFFFF" :size="14" /></view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO, getModuleColor } from '@/common/theme.js';
import { fetchGooseDetail } from '@/common/api/goose.js';

const accentColor = getModuleColor('goose');
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

<style lang="scss" scoped>${style}`;

fs.writeFileSync(target, content, { encoding: 'utf8' });
console.log('fixed goose detail utf8 v2');

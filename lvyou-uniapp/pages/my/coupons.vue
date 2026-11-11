<template>
	<view class="mc-page">
		<view class="mc-tabs">
			<view
				v-for="(tab, index) in tabs"
				:key="tab.key"
				class="mc-tabs__item"
				:class="{ 'mc-tabs__item--active': currentTab === index }"
				@click="onTabChange(index)"
			>
				<text class="mc-tabs__text">{{ tab.label }}</text>
			</view>
		</view>

		<view class="mc-tip">
			<text class="mc-tip__text">{{ ui.myTip }}</text>
		</view>

		<view v-if="loading" class="mc-loading">
			<u-loading-icon mode="circle" color="#FF6600" />
		</view>

		<view v-else-if="displayList.length" class="mc-list">
			<view
				v-for="item in displayList"
				:key="item.id"
				class="mc-card ry-card"
				:class="`mc-card--${item.status}`"
			>
				<view class="mc-card__left">
					<text class="mc-card__symbol">¥</text>
					<text class="mc-card__amount">{{ item.amount }}</text>
					<text class="mc-card__limit">{{ formatCouponLimit(item) }}</text>
				</view>
				<view class="mc-card__body">
					<text class="mc-card__name">{{ item.name }}</text>
					<text class="mc-card__scope">{{ formatCouponScope(item.scope) }}</text>
					<text class="mc-card__time">{{ ui.validUntil }} {{ item.validTo }}</text>
					<text v-if="item.desc" class="mc-card__desc">{{ item.desc }}</text>
				</view>
				<view v-if="item.status === COUPON_STATUS_AVAILABLE" class="mc-card__status mc-card__status--ok">
					<text>{{ ui.tabAvailable }}</text>
				</view>
				<view v-else-if="item.status === COUPON_STATUS_USED" class="mc-card__status">
					<text>{{ ui.statusUsed }}</text>
				</view>
				<view v-else class="mc-card__status">
					<text>{{ ui.statusExpired }}</text>
				</view>
			</view>
		</view>

		<view v-else class="mc-empty">
			<u-empty mode="coupon" :text="ui.emptyMy" />
			<view class="mc-empty__btn" @click="goCenter">
				<text>{{ ui.goCenter }}</text>
			</view>
		</view>

		<view class="mc-footer">
			<view class="mc-footer__btn" @click="goCenter">
				<text>{{ ui.goCenter }}</text>
				<u-icon name="arrow-right" color="#FF6600" :size="14" />
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { hasToken } from '@/common/api/auth.js';
import { fetchMyCoupons } from '@/common/api/coupon.js';
import {
	COUPON_UI,
	COUPON_STATUS_AVAILABLE,
	COUPON_STATUS_USED,
	COUPON_STATUS_EXPIRED,
	formatCouponLimit,
	formatCouponScope
} from '@/common/coupon-util.js';

const ui = COUPON_UI;
const currentTab = ref(0);
const loading = ref(false);
const listMap = ref({ available: [], used: [], expired: [] });

const tabs = [
	{ key: COUPON_STATUS_AVAILABLE, label: ui.tabAvailable },
	{ key: COUPON_STATUS_USED, label: ui.tabUsed },
	{ key: COUPON_STATUS_EXPIRED, label: ui.tabExpired }
];

const displayList = computed(() => {
	const key = tabs[currentTab.value].key;
	return listMap.value[key] || [];
});

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: '请先登录', icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

async function loadTab(status) {
	if (!requireLogin()) return;
	loading.value = true;
	try {
		const res = await fetchMyCoupons(status);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
			listMap.value = { ...listMap.value, [status]: [] };
			return;
		}
		listMap.value = {
			...listMap.value,
			[status]: Array.isArray(res.data) ? res.data : []
		};
	} catch (e) {
		uni.showToast({ title: '加载失败', icon: 'none' });
		listMap.value = { ...listMap.value, [status]: [] };
	} finally {
		loading.value = false;
	}
}

async function loadCurrentTab() {
	const status = tabs[currentTab.value].key;
	await loadTab(status);
}

function onTabChange(index) {
	if (currentTab.value === index) return;
	currentTab.value = index;
	loadCurrentTab();
}

function goCenter() {
	uni.navigateTo({ url: '/pages/my/coupon-center' });
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.myTitle });
});

onShow(() => {
	loadCurrentTab();
});
</script>

<style lang="scss" scoped>
.mc-page {
	min-height: 100vh;
	background: #f5f6fa;
	padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
}

.mc-tabs {
	display: flex;
	background: #fff;
	padding: 0 var(--ry-page-padding-x);
	border-bottom: 1rpx solid #f0f0f0;
}

.mc-tabs__item {
	flex: 1;
	height: 88rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	position: relative;
}

.mc-tabs__text {
	font-size: 28rpx;
	color: #666;
}

.mc-tabs__item--active .mc-tabs__text {
	color: #ff6600;
	font-weight: 600;
}

.mc-tabs__item--active::after {
	content: '';
	position: absolute;
	left: 50%;
	bottom: 0;
	width: 48rpx;
	height: 6rpx;
	border-radius: 3rpx;
	background: #ff6600;
	transform: translateX(-50%);
}

.mc-tip {
	padding: 20rpx var(--ry-page-padding-x);
}

.mc-tip__text {
	font-size: 24rpx;
	color: #999;
	line-height: 1.5;
}

.mc-loading {
	display: flex;
	justify-content: center;
	padding: 120rpx 0;
}

.mc-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
	padding: 0 var(--ry-page-padding-x) 24rpx;
}

.mc-card {
	display: flex;
	align-items: stretch;
	overflow: hidden;
	padding: 0;
	position: relative;

	&--used,
	&--expired {
		opacity: 0.72;

		.mc-card__left {
			background: #ccc;
		}
	}
}

.mc-card__left {
	width: 180rpx;
	flex-shrink: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	gap: 6rpx;
	padding: 24rpx 12rpx;
	background: linear-gradient(135deg, #ff8a3d, #ff6600);
}

.mc-card__symbol {
	font-size: 24rpx;
	font-weight: 700;
	color: rgba(255, 255, 255, 0.9);
}

.mc-card__amount {
	font-size: 52rpx;
	font-weight: 800;
	color: #fff;
	line-height: 1;
}

.mc-card__limit {
	font-size: 20rpx;
	color: rgba(255, 255, 255, 0.88);
	text-align: center;
}

.mc-card__body {
	flex: 1;
	min-width: 0;
	padding: 24rpx 20rpx;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.mc-card__name {
	font-size: 28rpx;
	font-weight: 700;
	color: #1a1a1a;
}

.mc-card__scope {
	align-self: flex-start;
	padding: 4rpx 12rpx;
	border-radius: 999rpx;
	background: #fff3eb;
	font-size: 20rpx;
	color: #ff6600;
}

.mc-card__time,
.mc-card__desc {
	font-size: 22rpx;
	color: #999;
	line-height: 1.45;
}

.mc-card__status {
	position: absolute;
	top: 16rpx;
	right: 16rpx;
	padding: 4rpx 12rpx;
	border-radius: 999rpx;
	background: #f5f5f5;

	text {
		font-size: 20rpx;
		color: #999;
	}

	&--ok {
		background: #e8f8ef;

		text {
			color: #2eaf6a;
		}
	}
}

.mc-empty {
	padding: 100rpx 0 40rpx;
}

.mc-empty__btn {
	margin: 24rpx auto 0;
	width: 280rpx;
	height: 80rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #ff8a3d, #ff6600);
	display: flex;
	align-items: center;
	justify-content: center;

	text {
		font-size: 28rpx;
		font-weight: 600;
		color: #fff;
	}
}

.mc-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.mc-footer__btn {
	height: 88rpx;
	border-radius: 999rpx;
	border: 2rpx solid #ff6600;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 8rpx;

	text {
		font-size: 28rpx;
		font-weight: 600;
		color: #ff6600;
	}
}
</style>

<template>
	<view class="my-page">
		<view class="my-header">
			<view class="my-header__bg">
				<view class="my-header__circle my-header__circle--1" />
				<view class="my-header__circle my-header__circle--2" />
			</view>
		</view>

		<view class="my-body">
			<view class="my-profile ry-card">
				<view class="my-profile__msg" @click.stop="goMsg">
					<u-icon name="bell-fill" color="#FF6600" :size="22" />
					<view v-if="msgUnread > 0" class="my-profile__msg-badge">
						<text class="my-profile__msg-badge-text">{{ msgUnread > 99 ? '99+' : msgUnread }}</text>
					</view>
				</view>
				<view class="my-profile__main" @click="onProfileTap">
					<image v-if="loggedIn" :src="userInfo.avatar" mode="aspectFill" class="my-profile__avatar" />
					<view v-else class="my-profile__logo">
						<image class="my-profile__logo-img ry-placeholder-img" :src="brandLogo" mode="aspectFit" />
					</view>
					<view class="my-profile__info">
						<text class="my-profile__name">{{ userInfo.nickname }}</text>
						<text v-if="loggedIn && userInfo.phone" class="my-profile__phone">{{ userInfo.phone }}</text>
					</view>
				</view>
			</view>

			<view class="my-grid ry-card">
				<view v-for="item in quickList" :key="item.id" class="my-grid__item" @click="onItemClick(item)">
					<view class="my-grid__icon" :style="{ backgroundColor: item.bgColor }">
						<u-icon :name="item.icon" :color="item.iconColor" :size="24" />
						<view v-if="item.badge > 0" class="my-grid__badge">
							<text class="my-grid__badge-text">{{ item.badge > 99 ? '99+' : item.badge }}</text>
						</view>
					</view>
					<text class="my-grid__label">{{ item.label }}</text>
				</view>
			</view>

			<view class="my-menu ry-card">
				<view
					v-for="(item, index) in menuList"
					:key="item.id"
					class="my-menu__item"
					:class="{ 'my-menu__item--border': index < menuList.length - 1 }"
					@click="onItemClick(item)"
				>
					<view class="my-menu__icon" :style="{ backgroundColor: item.bgColor }">
						<u-icon :name="item.icon" :color="item.iconColor" :size="20" />
					</view>
					<text class="my-menu__title">{{ item.title }}</text>
					<!-- <view class="my-menu__right">
						<text v-if="item.badge > 0" class="my-menu__value">{{ item.badge > 99 ? '99+' : item.badge }}</text>
						<text v-else-if="item.value" class="my-menu__value">{{ item.value }}</text>
						<u-icon name="arrow-right" :size="14" color="#CCCCCC" />
					</view> -->
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { getDefaultAvatar, resolveAvatar } from '@/common/avatar.js';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { hasToken, getUserInfo } from '@/common/api/auth.js';
import { fetchFavoriteList } from '@/common/api/asset.js';
import { fetchCouponCount } from '@/common/api/coupon.js';
import { fetchCanteenPendingCount } from '@/common/api/canteen.js';
import { fetchMessageUnreadCount } from '@/common/api/messages.js';
import { goOrderList } from '@/common/order-ui.js';
import { buildMerchantEntry, fetchMerchantStatus } from '@/common/api/merchant.js';
import { goLogin, openPage, PAGES } from '@/common/navigation.js';
import { openKefuCenter } from '@/common/kefu-action.js';

const loggedIn = ref(hasToken());
const msgUnread = ref(0);
const brandLogo = PLACEHOLDER_LOGO;

const userInfo = ref({
	avatar: getDefaultAvatar(),
	nickname: '去登录',
	phone: ''
});

const quickList = ref([
	{ id: 'wallet', label: '我的钱包', icon: 'rmb-circle-fill', bgColor: '#FFF3EB', iconColor: '#FF6600', path: '/pages/my/wallet', badge: 0 },
	{ id: 'order', label: '我的订单', icon: 'file-text-fill', bgColor: '#FFF8E6', iconColor: '#FFB800', action: 'order', badge: 0 },
	{ id: 'reservation', label: '餐饮预约', icon: 'calendar-fill', bgColor: '#FFF3EB', iconColor: '#FF6600', path: '/mobile-design/canteen/records', badge: 0 },
	{ id: 'my-coupon', label: '优惠券', icon: 'coupon-fill', bgColor: '#FFF3EB', iconColor: '#FF6600', path: '/pages/my/coupons', badge: 0 }
]);

const menuList = ref([]);

const baseMenuList = [
	{ id: 'collect', title: '我的收藏', icon: 'star-fill', bgColor: '#FFF8E6', iconColor: '#FFB800', path: '/pages/my/asset?tab=favorite', badge: 0 },
	{ id: 'invite', title: '邀请好友', icon: 'share-fill', bgColor: '#FFF3EB', iconColor: '#FF6600', path: '/pages/my/invite' },
	{ id: 'team', title: '我的团队', icon: 'account-fill', bgColor: '#E8F8EF', iconColor: '#2EAF6A', path: '/pages/my/team' },
	{ id: 'coupon-center', title: '领券中心', icon: 'gift-fill', bgColor: '#FFF8E6', iconColor: '#FFB800', path: '/pages/my/coupon-center' },
	{ id: 'feedback', title: '意见反馈', icon: 'edit-pen', bgColor: '#FFF3EB', iconColor: '#FF6600', path: '/mobile-design/feedback/index' },
	{ id: 'service', title: '客服中心', icon: 'chat-fill', bgColor: '#E8F8EF', iconColor: '#2EAF6A', action: 'kefu' },
	{ id: 'setting', title: '设置中心', icon: 'setting-fill', bgColor: '#EAF2FF', iconColor: '#2979FF', path: '/pages/my/settings' }
];

const PRIVATE_PREFIXES = ['/pages/my/asset', '/pages/my/coupons', '/pages/my/coupon-center', '/pages/my/invite', '/pages/my/team', '/pages/my/commission', '/pages/my/wallet', '/pages/order/', '/merchant-portal/', '/mobile-design/canteen/'];

onShow(() => {
	loadPageSummary();
});

function maskPhone(phone) {
	const text = String(phone || '');
	if (text.length < 7) return text;
	return `${text.slice(0, 3)}****${text.slice(-4)}`;
}

function setGuestProfile() {
	loggedIn.value = false;
	userInfo.value = { avatar: getDefaultAvatar(), nickname: '去登录', phone: '' };
}

function resetGuestBadges() {
	msgUnread.value = 0;
	const reservationItem = quickList.value.find((item) => item.id === 'reservation');
	const couponQuickItem = quickList.value.find((item) => item.id === 'my-coupon');
	if (reservationItem) reservationItem.badge = 0;
	if (couponQuickItem) couponQuickItem.badge = 0;
}

async function loadUserInfo() {
	if (!hasToken()) {
		setGuestProfile();
		return;
	}
	try {
		const res = await getUserInfo();
		if (res.code === 0 && res.data) {
			const user = res.data;
			loggedIn.value = true;
			userInfo.value = {
				avatar: resolveAvatar(user.avatar),
				nickname: user.nickname || user.userName || user.name || '用户',
				phone: maskPhone(user.phone || user.phoneNo || '')
			};
		} else {
			setGuestProfile();
		}
	} catch (e) {
		setGuestProfile();
	}
}

async function loadPageSummary() {
	if (!hasToken()) {
		setGuestProfile();
		resetGuestBadges();
		await loadMerchantEntry();
		return;
	}
	await Promise.all([
		loadUserInfo(),
		loadMerchantEntry(),
		loadMessageUnreadCount(),
		loadCouponCount(),
		loadCanteenPendingCount()
	]);
}

async function loadCanteenPendingCount() {
	const reservationItem = quickList.value.find((item) => item.id === 'reservation');
	if (!reservationItem) return;
	if (!hasToken()) {
		reservationItem.badge = 0;
		return;
	}
	try {
		const res = await fetchCanteenPendingCount();
		if (res.code === 0 && res.data) {
			reservationItem.badge = Number(res.data.count) || 0;
		} else {
			reservationItem.badge = 0;
		}
	} catch (e) {
		reservationItem.badge = 0;
	}
}

async function loadMerchantEntry() {
	let merchantEntry = buildMerchantEntry(null);
	if (hasToken()) {
		try {
			const res = await fetchMerchantStatus();
			if (res.code === 0 && res.data) {
				merchantEntry = buildMerchantEntry(res.data);
			}
		} catch (e) { /* 未登录或接口失败时使用默认「商户入驻」 */ }
	}
	menuList.value = [
		{
			id: merchantEntry.id,
			title: merchantEntry.title,
			icon: merchantEntry.icon,
			bgColor: merchantEntry.bgColor,
			iconColor: merchantEntry.iconColor,
			path: merchantEntry.path,
			badge: merchantEntry.badge || 0
		},
		...baseMenuList
	];
	await loadFavoriteCount();
}

async function loadFavoriteCount() {
	const collectMenuItem = menuList.value.find((item) => item.id === 'collect');
	if (!collectMenuItem) return;
	if (!hasToken()) {
		collectMenuItem.badge = 0;
		return;
	}
	try {
		const res = await fetchFavoriteList({ page: 1, limit: 1 });
		if (res.code === 0 && res.data) {
			collectMenuItem.badge = Number(res.data.total ?? 0);
		} else {
			collectMenuItem.badge = 0;
		}
	} catch (e) {
		collectMenuItem.badge = 0;
	}
}

async function loadMessageUnreadCount() {
	if (!hasToken()) {
		msgUnread.value = 0;
		return;
	}
	try {
		const res = await fetchMessageUnreadCount();
		if (res.code === 0 && res.data) {
			msgUnread.value = Number(res.data.count) || 0;
		} else {
			msgUnread.value = 0;
		}
	} catch (e) {
		msgUnread.value = 0;
	}
}

async function loadCouponCount() {
	const couponQuickItem = quickList.value.find((item) => item.id === 'my-coupon');
	if (!couponQuickItem) return;
	if (!hasToken()) {
		couponQuickItem.badge = 0;
		return;
	}
	try {
		const res = await fetchCouponCount();
		if (res.code === 0 && res.data) {
			couponQuickItem.badge = Number(res.data.count) || 0;
		} else {
			couponQuickItem.badge = 0;
		}
	} catch (e) {
		couponQuickItem.badge = 0;
	}
}

function requireLogin() {
	if (hasToken()) return true;
	goLogin();
	return false;
}

function onProfileTap() {
	if (!loggedIn.value) {
		goLogin();
		return;
	}
	openPage(PAGES.PROFILE);
}

function goMsg() {
	openPage(PAGES.MSG);
}

function onItemClick(item) {
	if (item.action === 'order') {
		if (!requireLogin()) return;
		goOrderList();
		return;
	}
	if (item.action === 'kefu') {
		openKefuCenter();
		return;
	}
	if (item.path) {
		if (PRIVATE_PREFIXES.some((prefix) => item.path.startsWith(prefix)) && !requireLogin()) return;
		openPage(item.path);
	}
}
</script>

<style lang="scss" scoped>
	.my-page {
		min-height: 100vh;
		background-color: var(--ry-page-bg);
		padding-bottom: calc(120rpx + env(safe-area-inset-bottom));
	}

	/* #ifdef MP-WEIXIN */
	.my-page {
		padding-bottom: 120rpx;
	}
	/* #endif */

	.my-header {
		position: relative;
		height: 260rpx;
		overflow: hidden;
	}

	.my-header__bg {
		position: absolute;
		inset: 0;
		background: linear-gradient(135deg, #FF6600 0%, #FF8A3D 55%, #FFB366 100%);
	}

	.my-header__circle {
		position: absolute;
		border-radius: 50%;
		background-color: rgba(255, 255, 255, 0.12);
	}

	.my-header__circle--1 {
		width: 240rpx;
		height: 240rpx;
		top: -80rpx;
		right: -60rpx;
	}

	.my-header__circle--2 {
		width: 140rpx;
		height: 140rpx;
		bottom: 20rpx;
		left: -40rpx;
	}

	.my-body {
		position: relative;
		margin-top: -180rpx;
		padding: 0 var(--ry-page-padding-x) 32rpx;
		display: flex;
		flex-direction: column;
		gap: 20rpx;
	}

	.my-profile {
		position: relative;
		padding: 28rpx;

		&__msg {
			position: absolute;
			top: 24rpx;
			right: 24rpx;
			z-index: 2;
			display: flex;
			align-items: center;
			justify-content: center;
			width: 64rpx;
			height: 64rpx;
			border-radius: 50%;
			background-color: #FFF3EB;
		}

		&__msg-badge {
			position: absolute;
			top: 0;
			right: 0;
			min-width: 28rpx;
			height: 28rpx;
			padding: 0 6rpx;
			border-radius: 999rpx;
			background-color: #FF4D4F;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__msg-badge-text {
			font-size: 18rpx;
			color: #FFFFFF;
			line-height: 1;
		}

		&__main {
			display: flex;
			align-items: center;
			gap: 20rpx;
			padding-right: 72rpx;
			padding-bottom: 24rpx;

			&:active {
				opacity: 0.9;
			}
		}

		&__avatar {
			width: 88rpx;
			height: 88rpx;
			border-radius: 50%;
			flex-shrink: 0;
		}

		&__logo {
			width: 88rpx;
			height: 88rpx;
			border-radius: 22rpx;
			flex-shrink: 0;
			overflow: hidden;
			background: #FFFFFF;
			box-shadow: 0 8rpx 20rpx rgba(255, 102, 0, 0.12);
		}

		&__logo-img {
			width: 100%;
			height: 100%;
		}

		&__info {
			flex: 1;
			min-width: 0;
			display: flex;
			flex-direction: column;
			gap: 8rpx;
		}

		&__name {
			font-size: 32rpx;
			font-weight: 600;
			color: var(--ry-text-primary);
		}

		&__phone {
			font-size: 24rpx;
			color: var(--ry-text-secondary);
		}
	}

	.my-grid {
		display: grid;
		grid-template-columns: repeat(4, 1fr);
		gap: 8rpx;
		padding: 28rpx 16rpx;

		&__item {
			display: flex;
			flex-direction: column;
			align-items: center;
			gap: 12rpx;

			&:active {
				opacity: 0.8;
			}
		}

		&__icon {
			position: relative;
			width: 88rpx;
			height: 88rpx;
			border-radius: 22rpx;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__badge {
			position: absolute;
			top: -6rpx;
			right: -6rpx;
			min-width: 28rpx;
			height: 28rpx;
			padding: 0 6rpx;
			border-radius: 999rpx;
			background-color: #FF4D4F;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__badge-text {
			font-size: 18rpx;
			color: #FFFFFF;
			line-height: 1;
		}

		&__label {
			font-size: 24rpx;
			color: var(--ry-text-primary);
			text-align: center;
		}
	}

	.my-menu {
		padding: 0 24rpx;
		overflow: hidden;

		&__item {
			display: flex;
			align-items: center;
			gap: 16rpx;
			padding: 18rpx 0;
			min-height: 72rpx;

			&--border {
				border-bottom: 1rpx solid #F5F5F5;
			}

			&:active {
				opacity: 0.88;
			}
		}

		&__icon {
			flex-shrink: 0;
			width: 52rpx;
			height: 52rpx;
			border-radius: 14rpx;
			display: flex;
			align-items: center;
			justify-content: center;
		}

		&__title {
			flex: 1;
			font-size: 28rpx;
			color: var(--ry-text-primary);
		}

		&__right {
			display: flex;
			align-items: center;
			gap: 8rpx;
			flex-shrink: 0;
		}

		&__value {
			font-size: 26rpx;
			color: #FF6600;
			font-weight: 500;
		}
	}
</style>

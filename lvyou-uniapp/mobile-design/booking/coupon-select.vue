<template>
	<view class="cs-page">
		<view class="cs-summary">
			<text class="cs-summary__label">订单金额</text>
			<text class="cs-summary__value">¥{{ orderAmount }}</text>
		</view>

		<view v-if="loading" class="cs-loading">
			<u-loading-icon mode="circle" color="#FF6600" />
		</view>

		<view v-else-if="couponList.length" class="cs-list">
			<view
				class="cs-item cs-item--none"
				:class="{ 'cs-item--active': selectedId === '' }"
				@click="selectedId = ''"
			>
				<text class="cs-item__name">{{ ui.notUseCoupon }}</text>
				<view class="cs-item__radio" :class="{ 'cs-item__radio--checked': selectedId === '' }">
					<u-icon v-if="selectedId === ''" name="checkmark" color="#fff" :size="12" />
				</view>
			</view>

			<view
				v-for="item in couponList"
				:key="item.id"
				class="cs-item"
				:class="{
					'cs-item--active': selectedId === String(item.id),
					'cs-item--disabled': !item.applicable
				}"
				@click="selectCoupon(item)"
			>
				<view class="cs-item__left">
					<text class="cs-item__amount">¥{{ item.amount }}</text>
					<text class="cs-item__limit">{{ formatCouponLimit(item) }}</text>
				</view>
				<view class="cs-item__body">
					<text class="cs-item__name">{{ item.name }}</text>
					<text class="cs-item__scope">{{ formatCouponScope(item.scope) }}</text>
					<text class="cs-item__time">{{ ui.validUntil }} {{ item.validTo }}</text>
					<text v-if="!item.applicable" class="cs-item__warn">{{ ui.inapplicable }}</text>
				</view>
				<view class="cs-item__radio" :class="{ 'cs-item__radio--checked': selectedId === String(item.id) }">
					<u-icon v-if="selectedId === String(item.id)" name="checkmark" color="#fff" :size="12" />
				</view>
			</view>
		</view>

		<view v-else class="cs-empty">
			<u-empty mode="coupon" :text="ui.emptySelect" />
			<view class="cs-empty__btn" @click="goCenter">
				<text>{{ ui.goCenter }}</text>
			</view>
		</view>

		<view class="cs-bar">
			<view class="cs-bar__link" @click="goCenter">
				<text>{{ ui.goCenter }}</text>
			</view>
			<view class="cs-bar__btn" @click="confirmSelect">
				<text>{{ ui.confirmSelect }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { hasToken } from '@/common/api/auth.js';
import { fetchAvailableCoupons } from '@/common/api/coupon.js';
import {
	COUPON_UI,
	setCouponSelectResult,
	formatCouponLimit,
	formatCouponScope
} from '@/common/coupon-util.js';

const ui = COUPON_UI;
const scope = ref('all');
const orderAmount = ref(0);
const selectedId = ref('');
const couponList = ref([]);
const loading = ref(false);

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: '请先登录', icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

async function loadCoupons() {
	if (!requireLogin()) return;
	loading.value = true;
	try {
		const res = await fetchAvailableCoupons(scope.value, orderAmount.value);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '加载优惠券失败', icon: 'none' });
			couponList.value = [];
			return;
		}
		couponList.value = Array.isArray(res.data) ? res.data : [];
	} catch (e) {
		uni.showToast({ title: '加载优惠券失败', icon: 'none' });
		couponList.value = [];
	} finally {
		loading.value = false;
	}
}

function selectCoupon(item) {
	if (!item.applicable) {
		uni.showToast({ title: ui.inapplicable, icon: 'none' });
		return;
	}
	selectedId.value = String(item.id);
}

function confirmSelect() {
	const coupon = selectedId.value
		? couponList.value.find((item) => String(item.id) === selectedId.value) || null
		: null;
	if (selectedId.value && coupon && !coupon.applicable) {
		uni.showToast({ title: ui.inapplicable, icon: 'none' });
		return;
	}
	setCouponSelectResult({ couponId: selectedId.value || '', coupon });
	uni.navigateBack();
}

function goCenter() {
	uni.navigateTo({ url: '/pages/my/coupon-center' });
}

onLoad((options) => {
	scope.value = String(options?.scope || 'all');
	orderAmount.value = Number(options?.amount) || 0;
	selectedId.value = String(options?.selectedId || '');
	uni.setNavigationBarTitle({ title: ui.selectTitle });
});

/** 登录返回或领券中心返回后刷新可用券列表 */
onShow(() => {
	if (!hasToken()) {
		requireLogin();
		return;
	}
	loadCoupons();
});
</script>

<style lang="scss" scoped>
.cs-page {
	min-height: 100vh;
	background: #f5f6fa;
	padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.cs-loading {
	display: flex;
	justify-content: center;
	padding: 120rpx 0;
}

.cs-summary {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin: 24rpx var(--ry-page-padding-x);
	padding: 24rpx;
	border-radius: 16rpx;
	background: #fff;
}

.cs-summary__label {
	font-size: 28rpx;
	color: #666;
}

.cs-summary__value {
	font-size: 36rpx;
	font-weight: 700;
	color: #ff6600;
}

.cs-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
	padding: 0 var(--ry-page-padding-x);
}

.cs-item {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 24rpx;
	border-radius: 16rpx;
	background: #fff;
	border: 2rpx solid transparent;

	&--none {
		padding: 28rpx 24rpx;
	}

	&--active {
		border-color: #ff6600;
		background: #fffaf5;
	}

	&--disabled {
		opacity: 0.55;
	}
}

.cs-item__left {
	width: 120rpx;
	flex-shrink: 0;
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 4rpx;
}

.cs-item__amount {
	font-size: 40rpx;
	font-weight: 800;
	color: #ff6600;
	line-height: 1;
}

.cs-item__limit {
	font-size: 20rpx;
	color: #999;
	text-align: center;
}

.cs-item__body {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.cs-item__name {
	font-size: 28rpx;
	font-weight: 600;
	color: #1a1a1a;
}

.cs-item__scope,
.cs-item__time {
	font-size: 22rpx;
	color: #999;
}

.cs-item__warn {
	font-size: 22rpx;
	color: #ff6600;
}

.cs-item__radio {
	width: 36rpx;
	height: 36rpx;
	border-radius: 50%;
	border: 2rpx solid #ddd;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;

	&--checked {
		background: #ff6600;
		border-color: #ff6600;
	}
}

.cs-empty {
	padding: 120rpx 0 40rpx;
}

.cs-empty__btn {
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

.cs-bar {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	display: flex;
	align-items: center;
	gap: 20rpx;
	padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.cs-bar__link {
	flex-shrink: 0;
	padding: 0 12rpx;

	text {
		font-size: 26rpx;
		color: #ff6600;
	}
}

.cs-bar__btn {
	flex: 1;
	height: 88rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #ff8a3d, #ff6600);
	display: flex;
	align-items: center;
	justify-content: center;

	text {
		font-size: 30rpx;
		font-weight: 600;
		color: #fff;
	}
}
</style>

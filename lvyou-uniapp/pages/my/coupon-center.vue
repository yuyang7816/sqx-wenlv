<template>
	<view class="cc-page">
		<view class="cc-tip">
			<u-icon name="coupon-fill" color="#FF6600" :size="16" />
			<text class="cc-tip__text">{{ ui.centerTip }}</text>
		</view>

		<view v-if="loading" class="cc-loading">
			<u-loading-icon mode="circle" color="#FF6600" />
		</view>

		<view v-else-if="centerList.length" class="cc-list">
			<view v-for="item in centerList" :key="item.templateId" class="cc-card ry-card">
				<view class="cc-card__left">
					<text class="cc-card__symbol">¥</text>
					<text class="cc-card__amount">{{ item.amount }}</text>
					<text class="cc-card__limit">{{ formatCouponLimit(item) }}</text>
				</view>
				<view class="cc-card__body">
					<text class="cc-card__name">{{ item.name }}</text>
					<text class="cc-card__scope">{{ formatCouponScope(item.scope) }}</text>
					<text class="cc-card__desc">{{ item.desc }}</text>
					<text class="cc-card__days">领取后 {{ item.validDays }} 天内有效</text>
				</view>
				<view
					class="cc-card__btn"
					:class="{ 'cc-card__btn--disabled': item.claimed || claimingId === item.templateId }"
					@click="onClaim(item)"
				>
					<text>{{ item.claimed ? ui.claimedBtn : ui.claimBtn }}</text>
				</view>
			</view>
		</view>

		<view v-else class="cc-empty">
			<u-empty mode="coupon" :text="ui.emptyCenter" />
		</view>

		<view class="cc-footer">
			<view class="cc-footer__btn" @click="goMyCoupons">
				<text>{{ ui.goMyCoupons }}</text>
				<u-icon name="arrow-right" color="#FF6600" :size="14" />
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onShow, onLoad } from '@dcloudio/uni-app';
import { hasToken } from '@/common/api/auth.js';
import { fetchCouponCenter, claimCoupon } from '@/common/api/coupon.js';
import { COUPON_UI, formatCouponLimit, formatCouponScope } from '@/common/coupon-util.js';

const ui = COUPON_UI;
const centerList = ref([]);
const loading = ref(false);
const claimingId = ref('');

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: '请先登录', icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

async function loadList() {
	if (!requireLogin()) return;
	loading.value = true;
	try {
		const res = await fetchCouponCenter();
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '加载失败', icon: 'none' });
			centerList.value = [];
			return;
		}
		centerList.value = Array.isArray(res.data) ? res.data : [];
	} catch (e) {
		uni.showToast({ title: '加载失败', icon: 'none' });
		centerList.value = [];
	} finally {
		loading.value = false;
	}
}

async function onClaim(item) {
	if (item.claimed || claimingId.value) return;
	if (!requireLogin()) return;
	claimingId.value = item.templateId;
	try {
		const res = await claimCoupon(item.templateId);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || ui.claimFail, icon: 'none' });
			await loadList();
			return;
		}
		uni.showToast({ title: ui.claimSuccess, icon: 'success' });
		await loadList();
	} catch (e) {
		uni.showToast({ title: ui.claimFail, icon: 'none' });
	} finally {
		claimingId.value = '';
	}
}

function goMyCoupons() {
	uni.navigateTo({ url: '/pages/my/coupons' });
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.centerTitle });
});

onShow(() => {
	loadList();
});
</script>

<style lang="scss" scoped>
.cc-page {
	min-height: 100vh;
	background: #f5f6fa;
	padding: 24rpx var(--ry-page-padding-x) calc(120rpx + env(safe-area-inset-bottom));
	box-sizing: border-box;
}

.cc-tip {
	display: flex;
	align-items: center;
	gap: 10rpx;
	padding: 16rpx 20rpx;
	margin-bottom: 20rpx;
	border-radius: 12rpx;
	background: #fff3eb;
}

.cc-tip__text {
	font-size: 24rpx;
	color: #b87333;
	line-height: 1.5;
}

.cc-loading {
	display: flex;
	justify-content: center;
	padding: 120rpx 0;
}

.cc-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.cc-card {
	display: flex;
	align-items: stretch;
	overflow: hidden;
	padding: 0;
}

.cc-card__left {
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

.cc-card__symbol {
	font-size: 24rpx;
	font-weight: 700;
	color: rgba(255, 255, 255, 0.9);
	line-height: 1;
}

.cc-card__amount {
	font-size: 52rpx;
	font-weight: 800;
	color: #ffffff;
	line-height: 1;
}

.cc-card__limit {
	font-size: 20rpx;
	color: rgba(255, 255, 255, 0.88);
	text-align: center;
	line-height: 1.3;
}

.cc-card__body {
	flex: 1;
	min-width: 0;
	padding: 24rpx 16rpx 24rpx 20rpx;
	display: flex;
	flex-direction: column;
	gap: 8rpx;
}

.cc-card__name {
	font-size: 28rpx;
	font-weight: 700;
	color: #1a1a1a;
	line-height: 1.35;
}

.cc-card__scope {
	align-self: flex-start;
	padding: 4rpx 12rpx;
	border-radius: 999rpx;
	background: #fff3eb;
	font-size: 20rpx;
	color: #ff6600;
	line-height: 1;
}

.cc-card__desc,
.cc-card__days {
	font-size: 22rpx;
	color: #999;
	line-height: 1.45;
}

.cc-card__btn {
	flex-shrink: 0;
	align-self: center;
	margin-right: 20rpx;
	padding: 14rpx 24rpx;
	border-radius: 999rpx;
	background: linear-gradient(135deg, #ff8a3d, #ff6600);

	text {
		font-size: 24rpx;
		font-weight: 600;
		color: #fff;
		white-space: nowrap;
	}

	&--disabled {
		background: #f0f0f0;

		text {
			color: #999;
			font-weight: 500;
		}
	}
}

.cc-empty {
	padding: 120rpx 0;
}

.cc-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
	background: #fff;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.06);
}

.cc-footer__btn {
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

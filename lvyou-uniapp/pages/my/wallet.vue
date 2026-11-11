<template>
	<view class="wallet-page">
		<view v-if="loading && !loaded" class="wallet-loading">
			<u-loading-icon mode="circle" color="#FF6600" />
		</view>
		<template v-else>
			<view class="wallet-hero">
				<view class="wallet-hero__circle wallet-hero__circle--1" />
				<view class="wallet-hero__circle wallet-hero__circle--2" />
				<text class="wallet-hero__label">{{ ui.balanceLabel }}</text>
				<text class="wallet-hero__balance">{{ ui.currencySymbol }}{{ summary.balance }}</text>
				<view class="wallet-hero__actions">
					<view class="wallet-hero__btn wallet-hero__btn--ghost" @click="goWithdraw">
						<text>{{ ui.withdrawBtn }}</text>
					</view>
					<view class="wallet-hero__btn wallet-hero__btn--primary" @click="goRecharge">
						<text>{{ ui.rechargeBtn }}</text>
					</view>
				</view>
			</view>

			<view class="wallet-overview ry-card">
				<view class="wallet-overview__item">
					<text class="wallet-overview__value">{{ ui.currencySymbol }}{{ summary.totalRecharge }}</text>
					<text class="wallet-overview__label">{{ ui.totalRechargeLabel }}</text>
				</view>
				<view class="wallet-overview__item">
					<text class="wallet-overview__value">{{ ui.currencySymbol }}{{ summary.totalConsume }}</text>
					<text class="wallet-overview__label">{{ ui.totalConsumeLabel }}</text>
				</view>
				<view class="wallet-overview__item">
					<text class="wallet-overview__value">{{ ui.currencySymbol }}{{ summary.totalWithdraw }}</text>
					<text class="wallet-overview__label">{{ ui.totalWithdrawLabel }}</text>
				</view>
			</view>

			<view class="wallet-menu ry-card">
				<view class="wallet-menu__item" @click="goTransactions">
					<view class="wallet-menu__icon wallet-menu__icon--blue">
						<u-icon name="list-dot" color="#2979FF" :size="22" />
					</view>
					<text class="wallet-menu__title">{{ ui.transactionsLink }}</text>
					<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
				</view>
				<view class="wallet-menu__item wallet-menu__item--border" @click="goWithdrawRecords">
					<view class="wallet-menu__icon wallet-menu__icon--orange">
						<u-icon name="file-text-fill" color="#FF6600" :size="22" />
					</view>
					<text class="wallet-menu__title">{{ ui.withdrawRecordsLink }}</text>
					<view class="wallet-menu__right">
						<text v-if="summary.withdrawCount > 0" class="wallet-menu__badge">{{ summary.withdrawCount }}笔</text>
						<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
					</view>
				</view>
				<view class="wallet-menu__item wallet-menu__item--border" @click="goAccountBind">
					<view class="wallet-menu__icon wallet-menu__icon--green">
						<u-icon name="account-fill" color="#2EAF6A" :size="22" />
					</view>
					<text class="wallet-menu__title">{{ ui.accountBindLink }}</text>
					<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
				</view>
			</view>
		</template>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { WALLET_UI, EMPTY_WALLET_SUMMARY } from '@/common/wallet-ui.js';
import { hasToken } from '@/common/api/auth.js';
import { fetchWalletSummary } from '@/common/api/wallet.js';
import { goLogin, openPage, PAGES } from '@/common/navigation.js';

const ui = WALLET_UI;
const summary = ref({ ...EMPTY_WALLET_SUMMARY });
const loading = ref(false);
const loaded = ref(false);

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: ui.loginTip, icon: 'none' });
	goLogin({ delay: 400 });
	return false;
}

async function reloadData() {
	loading.value = true;
	try {
		const res = await fetchWalletSummary();
		if (res.code === 0 && res.data) {
			summary.value = { ...EMPTY_WALLET_SUMMARY, ...res.data };
			loaded.value = true;
		} else {
			uni.showToast({ title: res.msg || ui.loadFail, icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

function goRecharge() {
	openPage(PAGES.WALLET_RECHARGE);
}

function goWithdraw() {
	openPage(PAGES.WALLET_WITHDRAW);
}

function goTransactions() {
	openPage(PAGES.WALLET_TRANSACTIONS);
}

function goWithdrawRecords() {
	openPage(PAGES.WALLET_WITHDRAW_RECORDS);
}

function goAccountBind() {
	openPage(PAGES.WALLET_WITHDRAW_ACCOUNT);
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.walletTitle });
});

onShow(() => {
	if (!requireLogin()) return;
	reloadData();
});
</script>

<style lang="scss" scoped>
.wallet-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.wallet-loading {
	flex: 1;
	display: flex;
	align-items: center;
	justify-content: center;
	min-height: 400rpx;
}

.wallet-hero {
	position: relative;
	overflow: hidden;
	padding: 40rpx 32rpx 36rpx;
	border-radius: var(--ry-radius-lg);
	background: linear-gradient(135deg, #FF6600 0%, #FF8A3D 55%, #FFB366 100%);
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 12rpx;

	&__circle {
		position: absolute;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.12);
	}

	&__circle--1 {
		width: 200rpx;
		height: 200rpx;
		top: -60rpx;
		right: -40rpx;
	}

	&__circle--2 {
		width: 120rpx;
		height: 120rpx;
		bottom: -30rpx;
		left: -20rpx;
	}

	&__label {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.9);
	}

	&__balance {
		font-size: 64rpx;
		font-weight: 700;
		color: #FFFFFF;
		line-height: 1.2;
	}

	&__actions {
		display: flex;
		gap: 24rpx;
		margin-top: 20rpx;
		width: 100%;
	}

	&__btn {
		flex: 1;
		height: 80rpx;
		border-radius: var(--ry-radius-pill);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		font-weight: 600;

		&--ghost {
			background: rgba(255, 255, 255, 0.2);
			color: #FFFFFF;
			border: 1rpx solid rgba(255, 255, 255, 0.5);
		}

		&--primary {
			background: #FFFFFF;
			color: #FF6600;
		}

		&:active {
			opacity: 0.88;
		}
	}
}

.wallet-overview {
	display: flex;
	padding: 28rpx 8rpx;

	&__item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
	}

	&__value {
		font-size: 28rpx;
		font-weight: 600;
		color: #FF6600;
	}

	&__label {
		font-size: 22rpx;
		color: var(--ry-text-secondary);
		text-align: center;
	}
}

.wallet-menu {
	padding: 0 24rpx;

	&__item {
		display: flex;
		align-items: center;
		gap: 16rpx;
		padding: 28rpx 0;
		min-height: 72rpx;

		&--border {
			border-top: 1rpx solid #F5F5F5;
		}

		&:active {
			opacity: 0.88;
		}
	}

	&__icon {
		width: 52rpx;
		height: 52rpx;
		border-radius: 14rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		&--blue {
			background: #EAF2FF;
		}

		&--orange {
			background: #FFF3EB;
		}

		&--green {
			background: #E8F8EF;
		}
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
	}

	&__badge {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}
}
</style>

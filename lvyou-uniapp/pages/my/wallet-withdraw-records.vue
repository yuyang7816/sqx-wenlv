<template>
	<view class="wwr-page">
		<view v-if="records.length" class="wwr-list">
			<view v-for="item in records" :key="item.id" class="wwr-item ry-card">
				<view class="wwr-item__head">
					<text class="wwr-item__amount">-{{ ui.currencySymbol }}{{ formatMoney(item.amount) }}</text>
					<text
						class="wwr-item__status"
						:class="{
							'wwr-item__status--pending': item.status === 'pending',
							'wwr-item__status--failed': item.status === 'failed'
						}"
					>
						{{ getStatusText(item.status) }}
					</text>
				</view>
				<view class="wwr-item__body">
					<view class="wwr-item__row">
						<text class="wwr-item__label">{{ ui.methodLabel }}</text>
						<text class="wwr-item__value">{{ item.methodLabel || item.method }}</text>
					</view>
					<view class="wwr-item__row">
						<text class="wwr-item__label">{{ ui.accountLabel }}</text>
						<text class="wwr-item__value">{{ formatAccount(item) }}</text>
					</view>
					<view v-if="item.realName" class="wwr-item__row">
						<text class="wwr-item__label">{{ ui.realNameLabel }}</text>
						<text class="wwr-item__value">{{ item.realName }}</text>
					</view>
					<view class="wwr-item__row">
						<text class="wwr-item__label">申请时间</text>
						<text class="wwr-item__value">{{ item.time }}</text>
					</view>
					<view v-if="item.remark" class="wwr-item__row">
						<text class="wwr-item__label">备注</text>
						<text class="wwr-item__value wwr-item__value--hint">{{ item.remark }}</text>
					</view>
				</view>
			</view>
		</view>

		<view v-else-if="!loading" class="wwr-empty">
			<u-empty mode="list" :text="ui.emptyWithdrawRecords" />
		</view>

		<view v-if="loading && records.length" class="wwr-loadmore">
			<text class="wwr-loadmore__text">加载中...</text>
		</view>
		<view v-else-if="!hasMore && records.length" class="wwr-loadmore">
			<text class="wwr-loadmore__text">没有更多了</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import {
	WALLET_UI,
	formatMoney,
	getWithdrawStatusText,
	formatWithdrawAccountText
} from '@/common/wallet-ui.js';
import { hasToken } from '@/common/api/auth.js';
import { fetchWalletWithdrawRecords, WALLET_PAGE_SIZE } from '@/common/api/wallet.js';

const ui = WALLET_UI;
const records = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: ui.loginTip, icon: 'none' });
	setTimeout(() => uni.navigateTo({ url: '/pages/my/login' }), 400);
	return false;
}

function getStatusText(status) {
	return getWithdrawStatusText(status);
}

function formatAccount(item) {
	return formatWithdrawAccountText(item);
}

async function loadRecords(reset = false) {
	if (loading.value) return;
	if (!reset && !hasMore.value) return;

	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loading.value = true;
	try {
		const res = await fetchWalletWithdrawRecords({
			page: page.value,
			limit: WALLET_PAGE_SIZE
		});
		if (res.code === 0 && res.data) {
			const list = res.data.records || [];
			records.value = reset ? list : [...records.value, ...list];
			const total = Number(res.data.total ?? 0);
			hasMore.value = list.length >= WALLET_PAGE_SIZE && records.value.length < total;
		} else {
			if (reset) records.value = [];
			uni.showToast({ title: res.msg || ui.loadFail, icon: 'none' });
		}
	} catch (e) {
		if (reset) records.value = [];
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		loading.value = false;
	}
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.withdrawRecordsTitle });
});

onShow(() => {
	if (!requireLogin()) return;
	loadRecords(true);
});

onPullDownRefresh(async () => {
	if (!hasToken()) {
		uni.stopPullDownRefresh();
		return;
	}
	await loadRecords(true);
	uni.stopPullDownRefresh();
});

onReachBottom(() => {
	if (!hasToken() || loading.value || !hasMore.value) return;
	page.value += 1;
	loadRecords(false);
});
</script>

<style lang="scss" scoped>
.wwr-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
}

.wwr-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.wwr-item {
	padding: 28rpx 24rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-bottom: 16rpx;
		border-bottom: 1rpx solid #F5F5F5;
	}

	&__amount {
		font-size: 36rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
	}

	&__status {
		font-size: 24rpx;
		color: #2EAF6A;
		padding: 4rpx 16rpx;
		border-radius: var(--ry-radius-pill);
		background: #E8F8EF;

		&--pending {
			color: #FF6600;
			background: #FFF3EB;
		}

		&--failed {
			color: #FF4D4F;
			background: #FFF1F0;
		}
	}

	&__body {
		display: flex;
		flex-direction: column;
		gap: 12rpx;
	}

	&__row {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
	}

	&__label {
		width: 140rpx;
		flex-shrink: 0;
		font-size: 26rpx;
		color: var(--ry-text-hint);
	}

	&__value {
		flex: 1;
		font-size: 26rpx;
		color: var(--ry-text-primary);
		line-height: 1.5;

		&--hint {
			color: var(--ry-text-secondary);
		}
	}
}

.wwr-empty {
	padding: 80rpx 0;
}

.wwr-loadmore {
	padding: 24rpx 0;
	text-align: center;

	&__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}
}
</style>

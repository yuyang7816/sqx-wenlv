<template>
	<view class="wt-page">
		<view class="wt-tabs">
			<view
				v-for="tab in tabs"
				:key="tab.key"
				class="wt-tabs__item"
				:class="{ 'wt-tabs__item--active': activeTab === tab.key }"
				@click="switchTab(tab.key)"
			>
				<text>{{ tab.label }}</text>
			</view>
		</view>

		<view v-if="records.length" class="wt-list">
			<view v-for="item in records" :key="item.id" class="wt-item ry-card">
				<view class="wt-item__main">
					<text class="wt-item__title">{{ item.title }}</text>
					<text class="wt-item__time">{{ item.time }}</text>
				</view>
				<text
					class="wt-item__amount"
					:class="{
						'wt-item__amount--income': Number(item.amount) > 0,
						'wt-item__amount--expense': Number(item.amount) < 0
					}"
				>
					{{ formatAmount(item.amount) }}
				</text>
			</view>
		</view>

		<view v-else-if="!loading" class="wt-empty">
			<u-empty mode="list" :text="ui.emptyTransactions" />
		</view>

		<view v-if="loading && records.length" class="wt-loadmore">
			<text class="wt-loadmore__text">加载中...</text>
		</view>
		<view v-else-if="!hasMore && records.length" class="wt-loadmore">
			<text class="wt-loadmore__text">没有更多了</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { WALLET_UI, formatTransactionAmount } from '@/common/wallet-ui.js';
import { hasToken } from '@/common/api/auth.js';
import { fetchWalletTransactions, WALLET_PAGE_SIZE } from '@/common/api/wallet.js';

const ui = WALLET_UI;
const tabs = [
	{ key: 'all', label: ui.allTab },
	{ key: 'income', label: ui.incomeTab },
	{ key: 'expense', label: ui.expenseTab }
];
const activeTab = ref('all');
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

function formatAmount(amount) {
	return formatTransactionAmount(amount);
}

function switchTab(key) {
	if (activeTab.value === key) return;
	activeTab.value = key;
	loadRecords(true);
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
		const res = await fetchWalletTransactions({
			page: page.value,
			limit: WALLET_PAGE_SIZE,
			filter: activeTab.value
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
	uni.setNavigationBarTitle({ title: ui.transactionsTitle });
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
.wt-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.wt-tabs {
	display: flex;
	padding: 8rpx;
	border-radius: var(--ry-radius-pill);
	background: #FFFFFF;
	box-shadow: var(--ry-card-shadow);

	&__item {
		flex: 1;
		height: 72rpx;
		border-radius: var(--ry-radius-pill);
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
		color: var(--ry-text-secondary);

		&--active {
			background: #FF6600;
			color: #FFFFFF;
			font-weight: 600;
		}
	}
}

.wt-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.wt-item {
	display: flex;
	align-items: center;
	justify-content: space-between;
	padding: 28rpx 24rpx;
	gap: 20rpx;

	&__main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	&__title {
		font-size: 28rpx;
		color: var(--ry-text-primary);
		font-weight: 500;
	}

	&__time {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	&__amount {
		flex-shrink: 0;
		font-size: 32rpx;
		font-weight: 600;
		color: var(--ry-text-primary);

		&--income {
			color: #FF6600;
		}

		&--expense {
			color: var(--ry-text-primary);
		}
	}
}

.wt-empty {
	padding: 80rpx 0;
}

.wt-loadmore {
	padding: 24rpx 0;
	text-align: center;

	&__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}
}
</style>

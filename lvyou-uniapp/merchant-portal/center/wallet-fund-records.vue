<template>
	<view class="mwr-page" :style="pageStyle">
		<view v-if="records.length" class="mwr-list">
			<view v-for="item in records" :key="item.id" class="mwr-item ry-card">
				<view class="mwr-item__main">
					<text class="mwr-item__title">{{ item.title }}</text>
					<text class="mwr-item__time">{{ item.time }}</text>
					<text v-if="item.orderNo" class="mwr-item__order">关联订单 {{ item.orderNo }}</text>
				</view>
				<view class="mwr-item__right">
					<text class="mwr-item__amount" :class="{ 'mwr-item__amount--in': item.amount > 0 }">
						{{ item.amount > 0 ? '+' : '' }}{{ formatMoney(Math.abs(item.amount)) }}
					</text>
					<text class="mwr-item__balance">余额 {{ formatMoney(item.balance) }}</text>
				</view>
			</view>
		</view>
		<view v-else-if="!loading" class="mwr-empty">
			<u-empty mode="list" :text="ui.emptyFundRecords" />
		</view>
		<view v-if="loading && records.length" class="mwr-loadmore">
			<text class="mwr-loadmore__text">加载中...</text>
		</view>
		<view v-else-if="!hasMore && records.length" class="mwr-loadmore">
			<text class="mwr-loadmore__text">没有更多了</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { requireMerchantApproved, MERCHANT_BUSINESS_UI, formatMoney } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { fetchMerchantFundRecords, MERCHANT_WALLET_PAGE_SIZE } from '@/common/api/merchant.js';

const { pageStyle, initTheme, applyNavBarColor } = useMerchantTheme();
const ui = MERCHANT_BUSINESS_UI;
const records = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);

async function loadRecords(reset = false) {
	if (loading.value) return;
	if (!reset && !hasMore.value) return;

	if (reset) {
		page.value = 1;
		hasMore.value = true;
	}

	loading.value = true;
	try {
		const res = await fetchMerchantFundRecords({
			page: page.value,
			limit: MERCHANT_WALLET_PAGE_SIZE
		});
		if (res.code === 0 && res.data) {
			const list = res.data.records || [];
			records.value = reset ? list : [...records.value, ...list];
			const total = Number(res.data.total ?? 0);
			hasMore.value = list.length >= MERCHANT_WALLET_PAGE_SIZE && records.value.length < total;
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

onLoad(async () => {
	await initTheme();
	uni.setNavigationBarTitle({ title: ui.fundRecordsTitle });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantApproved())) return;
	await initTheme();
	applyNavBarColor();
	await loadRecords(true);
});

onPullDownRefresh(async () => {
	if (!(await requireMerchantApproved())) {
		uni.stopPullDownRefresh();
		return;
	}
	await loadRecords(true);
	uni.stopPullDownRefresh();
});

onReachBottom(async () => {
	if (!(await requireMerchantApproved()) || loading.value || !hasMore.value) return;
	page.value += 1;
	await loadRecords(false);
});
</script>

<style lang="scss" scoped>
.mwr-page {
	min-height: 100vh;
	background: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(32rpx + env(safe-area-inset-bottom));
}

.mwr-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.mwr-item {
	display: flex;
	align-items: flex-start;
	justify-content: space-between;
	gap: 20rpx;
	padding: 28rpx 24rpx;

	&__main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__time {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	&__order {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	&__right {
		display: flex;
		flex-direction: column;
		align-items: flex-end;
		gap: 6rpx;
		flex-shrink: 0;
	}

	&__amount {
		font-size: 32rpx;
		font-weight: 700;
		color: var(--mc-primary, #FF6600);

		&--in { color: #2EAF6A; }
	}

	&__balance {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}
}

.mwr-empty { padding: 80rpx 0; }

.mwr-loadmore {
	padding: 24rpx 0;
	text-align: center;

	&__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}
}
</style>

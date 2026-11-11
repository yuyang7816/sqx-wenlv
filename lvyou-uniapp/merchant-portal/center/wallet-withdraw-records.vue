<template>
	<view class="mwr-page" :style="pageStyle">
		<view v-if="records.length" class="mwr-list">
			<view v-for="item in records" :key="item.id" class="mwr-item ry-card">
				<view class="mwr-item__head">
					<text class="mwr-item__amount">-{{ formatMoney(item.amount) }}</text>
					<text
						class="mwr-item__status"
						:class="{
							'mwr-item__status--pending': item.status === 'pending',
							'mwr-item__status--failed': item.status === 'failed',
							'mwr-item__status--ok': item.status === 'success'
						}"
					>
						{{ item.statusLabel }}
					</text>
				</view>
				<view class="mwr-item__body">
					<view class="mwr-item__row">
						<text class="mwr-item__label">提现单号</text>
						<text class="mwr-item__value">{{ item.id || '-' }}</text>
					</view>
					<view class="mwr-item__row">
						<text class="mwr-item__label">提现方式</text>
						<text class="mwr-item__value">{{ item.methodLabel }}</text>
					</view>
					<view class="mwr-item__row">
						<text class="mwr-item__label">收款账户</text>
						<text class="mwr-item__value">{{ item.accountText }}</text>
					</view>
					<view v-if="item.realName" class="mwr-item__row">
						<text class="mwr-item__label">收款姓名</text>
						<text class="mwr-item__value">{{ item.realName }}</text>
					</view>
					<view class="mwr-item__row">
						<text class="mwr-item__label">提现金额</text>
						<text class="mwr-item__value">{{ formatMoney(item.amount) }}</text>
					</view>
					<view class="mwr-item__row">
						<text class="mwr-item__label">手续费</text>
						<text class="mwr-item__value">{{ formatMoney(item.feeAmount) }}</text>
					</view>
					<view class="mwr-item__row">
						<text class="mwr-item__label">实际到账</text>
						<text class="mwr-item__value mwr-item__value--em">{{ formatMoney(item.actualAmount) }}</text>
					</view>
					<view class="mwr-item__row">
						<text class="mwr-item__label">申请时间</text>
						<text class="mwr-item__value">{{ item.time || '-' }}</text>
					</view>
					<view v-if="item.remark" class="mwr-item__row">
						<text class="mwr-item__label">备注</text>
						<text class="mwr-item__value mwr-item__value--hint">{{ item.remark }}</text>
					</view>
				</view>
			</view>
		</view>
		<view v-else-if="!loading" class="mwr-empty">
			<u-empty mode="list" :text="ui.emptyWithdrawRecords" />
		</view>
		<view v-if="loading && records.length" class="mwr-loadmore">
			<text class="mwr-loadmore__text">加载中...</text>
		</view>
		<view v-else-if="!hasMore && records.length" class="mwr-loadmore">
			<text class="mwr-loadmore__text">没有更多了</text>
		</view>

		<view class="mwr-footer">
			<u-button text="申请提现" block :color="theme.primary" :custom-style="btnStyle" @click="goWithdraw" />
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue';
import { onLoad, onShow, onPullDownRefresh, onReachBottom } from '@dcloudio/uni-app';
import { requireMerchantApproved, MERCHANT_BUSINESS_UI, formatMoney } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { fetchMerchantWithdrawRecords, MERCHANT_WALLET_PAGE_SIZE } from '@/common/api/merchant.js';
import { getWithdrawStatusText } from '@/common/wallet-ui.js';

const { theme, pageStyle, initTheme, applyNavBarColor } = useMerchantTheme();
const ui = MERCHANT_BUSINESS_UI;
const records = ref([]);
const page = ref(1);
const hasMore = ref(true);
const loading = ref(false);
const btnStyle = { height: '48px', borderRadius: '24px', fontWeight: '600' };

function goWithdraw() {
	uni.navigateTo({ url: '/merchant-portal/center/wallet-withdraw' });
}

function resolveMethodLabel(method, methodLabel) {
	if (methodLabel) return methodLabel;
	if (method === 'alipay') return '支付宝';
	if (method === 'wechat') return '微信';
	return method || '-';
}

/** 微信不展示二维码/图片链接，仅展示文案 */
function resolveAccountText(item) {
	const method = item.method || item.withdrawMethod || '';
	if (method === 'wechat') {
		const account = String(item.account || '').trim();
		if (!account || /^https?:\/\//i.test(account)) {
			return '微信收款码';
		}
		return account;
	}
	return item.account || item.alipayAccount || '-';
}

function mapRecord(item) {
	const method = item.method || item.withdrawMethod || 'alipay';
	const status = item.status || 'pending';
	const amount = Number(item.amount) || 0;
	const feeAmount = Number(item.feeAmount ?? 0);
	const actualAmount = Number(item.actualAmount ?? amount - feeAmount);
	return {
		...item,
		method,
		status,
		amount,
		feeAmount,
		actualAmount,
		methodLabel: resolveMethodLabel(method, item.methodLabel),
		accountText: resolveAccountText({ ...item, method }),
		realName: item.realName || item.alipayName || '',
		time: item.time || item.createTime || '',
		remark: item.remark || '',
		statusLabel: getWithdrawStatusText(status)
	};
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
		const res = await fetchMerchantWithdrawRecords({
			page: page.value,
			limit: MERCHANT_WALLET_PAGE_SIZE
		});
		if (res.code === 0 && res.data) {
			const list = (res.data.records || []).map(mapRecord);
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
	uni.setNavigationBarTitle({ title: ui.withdrawRecordsTitle });
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
	padding: 24rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
}

.mwr-list {
	display: flex;
	flex-direction: column;
	gap: 16rpx;
}

.mwr-item {
	padding: 28rpx 24rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		padding-bottom: 16rpx;
		border-bottom: 1rpx solid #F5F5F5;
	}

	&__amount {
		font-size: 36rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		line-height: 1.2;
	}

	&__status {
		flex-shrink: 0;
		font-size: 24rpx;
		line-height: 1.4;
		padding: 6rpx 16rpx;
		border-radius: 999rpx;
		background: #E8F8EF;
		color: #2EAF6A;

		&--pending {
			background: #FFF3EB;
			color: #FF6600;
		}

		&--failed {
			background: #FFF1F0;
			color: #FF4D4F;
		}

		&--ok {
			background: #E8F8EF;
			color: #2EAF6A;
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
		line-height: 1.5;
	}

	&__value {
		flex: 1;
		min-width: 0;
		font-size: 26rpx;
		color: var(--ry-text-primary);
		line-height: 1.5;
		word-break: break-all;

		&--em {
			font-weight: 600;
			color: var(--ry-text-primary);
		}

		&--hint {
			color: var(--ry-text-secondary);
		}
	}
}

.mwr-empty {
	padding: 80rpx 0;
}

.mwr-loadmore {
	padding: 24rpx 0;
	text-align: center;

	&__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}
}

.mwr-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255, 255, 255, 0.96);
	box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.04);
}
</style>

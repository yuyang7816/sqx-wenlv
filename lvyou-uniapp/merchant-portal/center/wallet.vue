<template>
	<view class="mw-page" :style="pageStyle">
		<view class="mw-top">
			<view class="mw-top__glow" />
			<view class="mw-wallet">
				<view class="mw-wallet__mesh" />
				<view class="mw-wallet__head">
					<view class="mw-wallet__brand">
						<u-icon name="rmb-circle-fill" color="rgba(255,255,255,0.92)" :size="18" />
						<text class="mw-wallet__brand-text">商户资金账户</text>
					</view>
					<text class="mw-wallet__label">{{ ui.balanceLabel }}</text>
				</view>
				<text class="mw-wallet__balance">{{ formatMoney(summary.balance) }}</text>
				<view class="mw-wallet__actions">
					<view class="mw-wallet__action" @click="goWithdraw">
						<view class="mw-wallet__action-icon mw-wallet__action-icon--light">
							<u-icon name="arrow-upward" color="#FFFFFF" :size="20" />
						</view>
						<text class="mw-wallet__action-text">{{ ui.withdrawBtn }}</text>
					</view>
					<view class="mw-wallet__divider" />
					<view class="mw-wallet__action" @click="goDeposit">
						<view class="mw-wallet__action-icon mw-wallet__action-icon--solid">
							<u-icon name="lock-fill" :color="theme.primary" :size="20" />
						</view>
						<text class="mw-wallet__action-text">{{ ui.depositBtn }}</text>
					</view>
				</view>
			</view>
		</view>

		<view class="mw-body">
			<view class="mw-metrics ry-card">
				<view class="mw-metrics__item">
					<view class="mw-metrics__icon mw-metrics__icon--blue">
						<u-icon name="clock-fill" :color="theme.primary" :size="18" />
					</view>
					<text class="mw-metrics__value">{{ formatMoney(summary.pendingSettle) }}</text>
					<text class="mw-metrics__label">{{ ui.pendingSettleLabel }}</text>
				</view>
				<view class="mw-metrics__sep" />
				<view class="mw-metrics__item">
					<view class="mw-metrics__icon mw-metrics__icon--gold">
						<u-icon name="rmb-circle-fill" color="#C9A227" :size="18" />
					</view>
					<text class="mw-metrics__value">{{ formatMoney(summary.totalIncome) }}</text>
					<text class="mw-metrics__label">{{ ui.totalIncomeLabel }}</text>
				</view>
				<view class="mw-metrics__sep" />
				<view class="mw-metrics__item">
					<view class="mw-metrics__icon mw-metrics__icon--green">
						<u-icon name="lock-fill" color="#2EAF6A" :size="18" />
					</view>
					<text class="mw-metrics__value">{{ formatMoney(summary.depositPaid) }}</text>
					<text class="mw-metrics__label">{{ ui.depositLabel }}</text>
				</view>
			</view>

			<view class="mw-deposit ry-card" @click="goDeposit">
				<view class="mw-deposit__head">
					<view class="mw-deposit__title-wrap">
						<text class="mw-deposit__title">保证金</text>
						<text
							class="mw-deposit__tag"
							:class="{
								'mw-deposit__tag--ok': summary.depositStatus === 'paid' && !summary.pendingDepositRefund && !summary.rejectedDepositRefund,
								'mw-deposit__tag--pending': summary.pendingDepositRefund,
								'mw-deposit__tag--reject': summary.rejectedDepositRefund
							}"
						>
							{{ depositStatusText }}
						</text>
					</view>
					<u-icon name="arrow-right" color="#C8C4BE" :size="14" />
				</view>

				<view class="mw-deposit__amounts">
					<view class="mw-deposit__amount-item">
						<text class="mw-deposit__amount-label">已缴</text>
						<text class="mw-deposit__amount-value">{{ formatMoney(summary.depositPaid) }}</text>
					</view>
					<view class="mw-deposit__amount-divider" />
					<view class="mw-deposit__amount-item">
						<text class="mw-deposit__amount-label">应缴</text>
						<text class="mw-deposit__amount-value">{{ formatMoney(summary.depositRequired) }}</text>
					</view>
				</view>

				<view class="mw-deposit__progress">
					<view class="mw-deposit__bar">
						<view class="mw-deposit__bar-inner" :style="{ width: depositPercent + '%' }" />
					</view>
					<text class="mw-deposit__progress-text">{{ depositProgressText }}</text>
				</view>

				<view v-if="summary.pendingDepositRefund" class="mw-deposit__tip mw-deposit__tip--pending">
					<u-icon name="clock-fill" :color="theme.primary" :size="14" />
					<text>{{ ui.depositRefundPending }} · {{ formatMoney(summary.pendingDepositRefund.amount) }}</text>
				</view>

				<view v-else-if="summary.rejectedDepositRefund" class="mw-deposit__tip mw-deposit__tip--reject">
					<u-icon name="close-circle-fill" color="#E34D59" :size="14" />
					<text>{{ ui.depositRefundRejected }}：{{ rejectReasonText }}</text>
				</view>

				<view v-else-if="summary.depositStatus !== 'paid'" class="mw-deposit__tip">
					<u-icon name="info-circle" :color="theme.primary" :size="14" />
					<text>还需缴纳 {{ formatMoney(depositNeed) }}，缴足后可营业接单</text>
				</view>

				<view v-else-if="summary.canApplyDepositRefund" class="mw-deposit__tip mw-deposit__tip--refund">
					<u-icon name="reload" :color="theme.primary" :size="14" />
					<text>点击进入保证金页面申请退保</text>
				</view>
			</view>

			<view class="mw-section">
				<text class="mw-section__title">资金服务</text>
				<view class="mw-services ry-card">
					<view class="mw-services__item" @click="goFundRecords">
						<view class="mw-services__left">
							<view class="mw-services__icon">
								<u-icon name="list-dot" :color="theme.primary" :size="22" />
							</view>
							<view class="mw-services__main">
								<text class="mw-services__name">{{ ui.fundRecordsLink }}</text>
								<text class="mw-services__desc">查看收入、支出与保证金流水</text>
							</view>
						</view>
						<u-icon name="arrow-right" color="#C8C4BE" :size="14" />
					</view>
					<view class="mw-services__line" />
					<view class="mw-services__item" @click="goWithdrawRecords">
						<view class="mw-services__left">
							<view class="mw-services__icon mw-services__icon--alt">
								<u-icon name="file-text-fill" :color="theme.primary" :size="22" />
							</view>
							<view class="mw-services__main">
								<text class="mw-services__name">{{ ui.withdrawRecordsLink }}</text>
								<text class="mw-services__desc">
									{{ summary.withdrawCount > 0 ? `共 ${summary.withdrawCount} 笔提现记录` : '查看提现进度与到账状态' }}
								</text>
							</view>
						</view>
						<u-icon name="arrow-right" color="#C8C4BE" :size="14" />
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { requireMerchantApproved, MERCHANT_BUSINESS_UI, formatMoney } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { fetchMerchantWalletSummary } from '@/common/api/merchant.js';

const { theme, pageStyle, initTheme, applyNavBarColor } = useMerchantTheme();
const ui = MERCHANT_BUSINESS_UI;
const summary = ref({
	balance: 0,
	pendingSettle: 0,
	totalIncome: 0,
	depositRequired: 0,
	depositPaid: 0,
	depositStatus: 'unpaid',
	depositStatusLabel: ui.depositUnpaid,
	withdrawCount: 0,
	canApplyDepositRefund: false,
	pendingDepositRefund: null,
	rejectedDepositRefund: null
});

const depositNeed = computed(() => {
	const required = Number(summary.value.depositRequired) || 0;
	const paid = Number(summary.value.depositPaid) || 0;
	return Math.max(0, required - paid);
});

const rejectReasonText = computed(() => {
	const remark = summary.value.rejectedDepositRefund?.auditRemark;
	return String(remark || '').trim() || ui.depositRefundRejectDefault;
});

const depositPercent = computed(() => {
	const required = Number(summary.value.depositRequired) || 0;
	if (!required) return 100;
	return Math.min(100, Math.round(((Number(summary.value.depositPaid) || 0) / required) * 100));
});

const depositStatusText = computed(() => {
	if (summary.value.pendingDepositRefund) return ui.depositStatusRefunding;
	if (summary.value.rejectedDepositRefund) return ui.depositRefundRejected;
	if (summary.value.depositStatus === 'paid') return summary.value.depositStatusLabel || ui.depositStatusPaid;
	return summary.value.depositStatusLabel || ui.depositStatusUnpaid;
});

const depositProgressText = computed(() => {
	if (summary.value.pendingDepositRefund) return '退保审核中，保证金暂不可用于新订单';
	if (summary.value.rejectedDepositRefund) return rejectReasonText.value;
	if (summary.value.depositStatus === 'paid') return '保证金已缴足，账户可正常营业';
	return `完成度 ${depositPercent.value}% · 还需 ${formatMoney(depositNeed.value)}`;
});

async function reload() {
	try {
		const res = await fetchMerchantWalletSummary();
		if (res.code === 0 && res.data) {
			summary.value = res.data;
		}
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	}
}

function goWithdraw() {
	uni.navigateTo({ url: '/merchant-portal/center/wallet-withdraw' });
}

function goDeposit() {
	uni.navigateTo({ url: '/merchant-portal/center/wallet-deposit' });
}

function goFundRecords() {
	uni.navigateTo({ url: '/merchant-portal/center/wallet-fund-records' });
}

function goWithdrawRecords() {
	uni.navigateTo({ url: '/merchant-portal/center/wallet-withdraw-records' });
}

onLoad(async () => {
	await initTheme();
	uni.setNavigationBarTitle({ title: ui.walletTitle });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantApproved())) return;
	await initTheme();
	applyNavBarColor();
	await reload();
});
</script>

<style lang="scss" scoped>
.mw-page {
	min-height: 100vh;
	background: #F5F3F0;
}

.mw-top {
	position: relative;
	padding: 24rpx var(--ry-page-padding-x) 0;
}

.mw-top__glow {
	position: absolute;
	top: -80rpx;
	left: 50%;
	transform: translateX(-50%);
	width: 520rpx;
	height: 260rpx;
	background: radial-gradient(ellipse, rgba(255, 102, 0, 0.18) 0%, rgba(255, 102, 0, 0) 70%);
	pointer-events: none;
}

.mw-wallet {
	position: relative;
	overflow: hidden;
	padding: 36rpx 32rpx 28rpx;
	border-radius: 28rpx;
	background: var(--mc-wallet-gradient, linear-gradient(145deg, #1A1512 0%, #2D2218 38%, #FF6600 118%));
	box-shadow: 0 24rpx 48rpx rgba(26, 21, 18, 0.18);

	&__mesh {
		position: absolute;
		inset: 0;
		background:
			radial-gradient(circle at 85% 15%, rgba(255, 255, 255, 0.14) 0%, transparent 42%),
			radial-gradient(circle at 10% 90%, rgba(255, 138, 61, 0.22) 0%, transparent 48%);
		pointer-events: none;
	}

	&__head {
		position: relative;
		z-index: 1;
		display: flex;
		flex-direction: column;
		gap: 18rpx;
	}

	&__brand {
		display: inline-flex;
		align-items: center;
		gap: 10rpx;
		align-self: flex-start;
		padding: 8rpx 18rpx;
		border-radius: 999rpx;
		background: rgba(255, 255, 255, 0.1);
		border: 1rpx solid rgba(255, 255, 255, 0.12);
	}

	&__brand-text {
		font-size: 22rpx;
		color: rgba(255, 255, 255, 0.88);
		letter-spacing: 1rpx;
	}

	&__label {
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.72);
		letter-spacing: 0.5rpx;
	}

	&__balance {
		position: relative;
		z-index: 1;
		margin-top: 8rpx;
		font-size: 72rpx;
		font-weight: 700;
		color: #FFFFFF;
		line-height: 1.15;
		letter-spacing: -1rpx;
		font-variant-numeric: tabular-nums;
	}

	&__actions {
		position: relative;
		z-index: 1;
		margin-top: 32rpx;
		padding-top: 24rpx;
		border-top: 1rpx solid rgba(255, 255, 255, 0.12);
		display: flex;
		align-items: center;
	}

	&__action {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 12rpx;

		&:active { opacity: 0.88; }
	}

	&__action-icon {
		width: 72rpx;
		height: 72rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;

		&--light {
			background: rgba(255, 255, 255, 0.14);
			border: 1rpx solid rgba(255, 255, 255, 0.16);
		}

		&--solid {
			background: #FFFFFF;
			box-shadow: 0 8rpx 20rpx rgba(0, 0, 0, 0.12);
		}
	}

	&__action-text {
		font-size: 26rpx;
		font-weight: 600;
		color: rgba(255, 255, 255, 0.94);
	}

	&__divider {
		width: 1rpx;
		height: 72rpx;
		background: rgba(255, 255, 255, 0.12);
	}
}

.mw-body {
	padding: 24rpx var(--ry-page-padding-x) calc(40rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.mw-metrics {
	display: flex;
	align-items: stretch;
	padding: 28rpx 12rpx;
	box-shadow: 0 8rpx 32rpx rgba(26, 21, 18, 0.04);

	&__item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 10rpx;
	}

	&__sep {
		width: 1rpx;
		background: linear-gradient(180deg, transparent, #EBE8E4 20%, #EBE8E4 80%, transparent);
	}

	&__icon {
		width: 48rpx;
		height: 48rpx;
		border-radius: 14rpx;
		display: flex;
		align-items: center;
		justify-content: center;

		&--blue { background: var(--mc-primary-light, #FFF3EB); }
		&--gold { background: #FBF6E8; }
		&--green { background: #EAF7EF; }
	}

	&__value {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		font-variant-numeric: tabular-nums;
	}

	&__label {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		text-align: center;
		line-height: 1.4;
	}
}

.mw-deposit {
	padding: 28rpx;
	box-shadow: 0 8rpx 32rpx rgba(26, 21, 18, 0.04);

	&:active { opacity: 0.96; }

	&__head {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
	}

	&__title-wrap {
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	&__title {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
	}

	&__tag {
		font-size: 20rpx;
		padding: 4rpx 14rpx;
		border-radius: 999rpx;
		background: #FFF3EB;
		color: var(--mc-primary, #FF6600);
		font-weight: 600;

		&--ok {
			background: #EAF7EF;
			color: #2EAF6A;
		}

		&--pending {
			background: #FFF8E6;
			color: #D48806;
		}

		&--reject {
			background: #FDECEE;
			color: #E34D59;
		}
	}

	&__amounts {
		display: flex;
		align-items: center;
		margin-top: 24rpx;
		padding: 20rpx 24rpx;
		border-radius: 18rpx;
		background: linear-gradient(180deg, #FAFAF8 0%, #F5F3F0 100%);
	}

	&__amount-item {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__amount-divider {
		width: 1rpx;
		height: 48rpx;
		background: #E8E4DF;
		margin: 0 24rpx;
	}

	&__amount-label {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	&__amount-value {
		font-size: 32rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		font-variant-numeric: tabular-nums;
	}

	&__progress {
		margin-top: 20rpx;
		display: flex;
		flex-direction: column;
		gap: 10rpx;
	}

	&__bar {
		height: 8rpx;
		border-radius: 999rpx;
		background: #EDEAE6;
		overflow: hidden;
	}

	&__bar-inner {
		height: 100%;
		border-radius: 999rpx;
		background: linear-gradient(90deg, var(--mc-primary, #FF6600), #FFB366);
		transition: width 0.35s ease;
	}

	&__progress-text {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}

	&__tip {
		display: flex;
		align-items: flex-start;
		gap: 8rpx;
		margin-top: 18rpx;
		padding: 16rpx 18rpx;
		border-radius: 14rpx;
		background: var(--mc-primary-light, #FFF8F3);
		font-size: 24rpx;
		color: var(--mc-primary, #FF6600);
		line-height: 1.5;

		&--pending {
			background: #FFFBF0;
			color: #B88207;
		}

		&--reject {
			background: #FDECEE;
			color: #E34D59;
		}

		&--refund {
			background: #F7F7F5;
			color: var(--ry-text-secondary);
		}
	}
}

.mw-section {
	display: flex;
	flex-direction: column;
	gap: 16rpx;

	&__title {
		padding: 0 8rpx;
		font-size: 28rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
		letter-spacing: 0.5rpx;
	}
}

.mw-services {
	padding: 8rpx 24rpx;
	box-shadow: 0 8rpx 32rpx rgba(26, 21, 18, 0.04);

	&__item {
		display: flex;
		align-items: center;
		justify-content: space-between;
		gap: 16rpx;
		padding: 24rpx 0;

		&:active { opacity: 0.88; }
	}

	&__line {
		height: 1rpx;
		background: #F0EDE8;
	}

	&__left {
		flex: 1;
		min-width: 0;
		display: flex;
		align-items: center;
		gap: 18rpx;
	}

	&__icon {
		width: 56rpx;
		height: 56rpx;
		border-radius: 16rpx;
		background: var(--mc-primary-light, #FFF3EB);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;

		&--alt { background: #F7F4F0; }
	}

	&__main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__name {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__desc {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		line-height: 1.4;
	}
}
</style>

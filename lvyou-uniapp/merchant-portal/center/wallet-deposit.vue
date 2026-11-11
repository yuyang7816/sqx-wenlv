<template>
	<view class="mwd-page" :style="pageStyle">
		<!-- 顶部状态卡片 -->
		<view class="mwd-hero">
			<view class="mwd-hero__circle mwd-hero__circle--1" />
			<view class="mwd-hero__circle mwd-hero__circle--2" />
			<view class="mwd-hero__badge" :class="statusBadgeClass">
				<u-icon :name="statusIcon" color="#FFF" :size="14" />
				<text>{{ statusLabel }}</text>
			</view>
			<view class="mwd-hero__shield">
				<u-icon name="lock-fill" color="#FFFFFF" :size="36" />
			</view>
			<text class="mwd-hero__title">{{ ui.depositRequired }}</text>
			<text class="mwd-hero__amount">{{ formatMoney(summary.depositRequired) }}</text>
		</view>

		<!-- 金额明细 -->
		<view class="mwd-stats ry-card">
			<view class="mwd-stats__item">
				<text class="mwd-stats__value">{{ formatMoney(summary.depositPaid) }}</text>
				<text class="mwd-stats__label">{{ ui.depositPaidLabel }}</text>
			</view>
			<view class="mwd-stats__divider" />
			<view class="mwd-stats__item">
				<text class="mwd-stats__value mwd-stats__value--accent">{{ formatMoney(needPay) }}</text>
				<text class="mwd-stats__label">{{ ui.depositNeedPayLabel }}</text>
			</view>
			<view class="mwd-stats__divider" />
			<view class="mwd-stats__item">
				<text class="mwd-stats__value">{{ formatMoney(summary.depositRequired) }}</text>
				<text class="mwd-stats__label">{{ ui.depositRequired }}</text>
			</view>
		</view>

		<!-- 待缴提示 -->
		<view v-if="needPay > 0 && !summary.pendingDepositRefund" class="mwd-pay ry-card">
			<view class="mwd-pay__head">
				<view class="mwd-pay__icon">
					<u-icon name="rmb-circle-fill" :color="theme.primary" :size="22" />
				</view>
				<view class="mwd-pay__main">
					<text class="mwd-pay__title">{{ ui.depositAmountLabel }}</text>
					<text class="mwd-pay__sub">{{ ui.depositPayTip }}</text>
				</view>
			</view>
			<view class="mwd-pay__amount">
				<text class="mwd-pay__prefix">¥</text>
				<text class="mwd-pay__value">{{ needPay }}</text>
			</view>
		</view>

		<!-- 退保区域 -->
		<view v-if="showRefundSection" class="mwd-refund ry-card">
			<view class="mwd-refund__head">
				<text class="mwd-refund__title">{{ ui.depositRefundSectionTitle }}</text>
				<text class="mwd-refund__rule">{{ ui.depositRefundRule }}</text>
			</view>

			<view v-if="summary.pendingDepositRefund" class="mwd-refund__pending">
				<view class="mwd-refund__pending-icon">
					<u-icon name="clock-fill" :color="theme.primary" :size="24" />
				</view>
				<view class="mwd-refund__pending-main">
					<text class="mwd-refund__pending-title">{{ ui.depositRefundPending }}</text>
					<text class="mwd-refund__pending-amount">申请金额 {{ formatMoney(summary.pendingDepositRefund.amount) }}</text>
					<text class="mwd-refund__pending-tip">平台审核中，请耐心等待</text>
				</view>
			</view>

			<view v-else-if="summary.rejectedDepositRefund" class="mwd-refund__rejected">
				<view class="mwd-refund__rejected-head">
					<u-icon name="close-circle-fill" color="#E34D59" :size="22" />
					<text class="mwd-refund__rejected-title">{{ ui.depositRefundRejected }}</text>
				</view>
				<text class="mwd-refund__rejected-amount">申请金额 {{ formatMoney(summary.rejectedDepositRefund.amount) }}</text>
				<view class="mwd-refund__rejected-reason">
					<text class="mwd-refund__rejected-label">{{ ui.depositRefundRejectReason }}</text>
					<text class="mwd-refund__rejected-text">{{ rejectReasonText }}</text>
				</view>
				<u-button
					v-if="summary.canApplyDepositRefund"
					:text="ui.depositRefundBtn"
					block
					plain
					:color="theme.primary"
					:custom-style="refundBtnStyle"
					:loading="refunding"
					@click="onApplyDepositRefund"
				/>
			</view>

			<view v-else-if="summary.canApplyDepositRefund" class="mwd-refund__action">
				<view class="mwd-refund__summary">
					<text class="mwd-refund__summary-label">可退金额</text>
					<text class="mwd-refund__summary-value">{{ formatMoney(summary.depositPaid) }}</text>
				</view>
				<u-button
					:text="ui.depositRefundBtn"
					block
					plain
					:color="theme.primary"
					:custom-style="refundBtnStyle"
					:loading="refunding"
					@click="onApplyDepositRefund"
				/>
			</view>

			<view v-else-if="summary.depositPaid > 0" class="mwd-refund__blocked">
				<u-icon name="info-circle" color="#999" :size="16" />
				<text>{{ ui.depositRefundBlockedTip }}</text>
			</view>
		</view>

		<!-- 说明 -->
		<view class="mwd-rules ry-card">
			<text class="mwd-rules__title">保证金说明</text>
			<view class="mwd-rules__item">
				<view class="mwd-rules__dot" />
				<text>保证金用于保障用户交易安全，缴足后方可正常营业接单</text>
			</view>
			<view class="mwd-rules__item">
				<view class="mwd-rules__dot" />
				<text>应缴金额根据入驻类型确定，平台调整后将同步更新</text>
			</view>
			<view class="mwd-rules__item">
				<view class="mwd-rules__dot" />
				<text>{{ ui.depositTip }}</text>
			</view>
		</view>

		<!-- 底部操作 -->
		<view v-if="needPay > 0 && !summary.pendingDepositRefund" class="mwd-footer">
			<u-button
				:text="ui.submitDeposit"
				block
				:color="theme.primary"
				:custom-style="btnStyle"
				:loading="submitting"
				@click="onSubmit"
			/>
		</view>

		<GlobalPayHost />
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { requireMerchantApproved, MERCHANT_BUSINESS_UI, formatMoney } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import {
	applyMerchantDepositRefund,
	createMerchantDepositPayOrder,
	fetchMerchantWalletSummary,
	payMerchantDepositByBalance
} from '@/common/api/merchant.js';
import { fetchWalletSummary } from '@/common/api/wallet.js';
import { payMerchantDeposit, mapPayMethodToChannel } from '@/common/api/payment.js';
import { isH5PaymentRedirectResult, launchH5PaymentFromResponse } from '@/common/h5-payment-launch.js';
import { openPayPopup } from '@/common/pay-popup.js';
import { BOOKING_UI } from '@/common/booking-ui.js';
import GlobalPayHost from '@/components/global-pay-host.vue';

const { theme, pageStyle, initTheme, applyNavBarColor } = useMerchantTheme();
const ui = MERCHANT_BUSINESS_UI;
const payUi = BOOKING_UI;
const summary = ref({
	balance: 0,
	depositRequired: 0,
	depositPaid: 0,
	depositStatus: 'unpaid',
	canApplyDepositRefund: false,
	pendingDepositRefund: null,
	rejectedDepositRefund: null
});
const submitting = ref(false);
const refunding = ref(false);
const btnStyle = { height: '48px', borderRadius: '24px', fontWeight: '600' };
const refundBtnStyle = { height: '44px', borderRadius: '22px', fontWeight: '600' };

const needPay = computed(() => {
	const required = Number(summary.value.depositRequired) || 0;
	const paid = Number(summary.value.depositPaid) || 0;
	return Math.max(0, required - paid);
});

const rejectReasonText = computed(() => {
	const remark = summary.value.rejectedDepositRefund?.auditRemark;
	return String(remark || '').trim() || ui.depositRefundRejectDefault;
});

const statusLabel = computed(() => {
	if (summary.value.pendingDepositRefund) return ui.depositStatusRefunding;
	if (summary.value.rejectedDepositRefund) return ui.depositRefundRejected;
	if (summary.value.depositStatus === 'paid') return ui.depositStatusPaid;
	return ui.depositStatusUnpaid;
});

const statusBadgeClass = computed(() => {
	if (summary.value.pendingDepositRefund) return 'mwd-hero__badge--pending';
	if (summary.value.rejectedDepositRefund) return 'mwd-hero__badge--reject';
	if (summary.value.depositStatus === 'paid') return 'mwd-hero__badge--ok';
	return 'mwd-hero__badge--warn';
});

const statusIcon = computed(() => {
	if (summary.value.pendingDepositRefund) return 'clock-fill';
	if (summary.value.rejectedDepositRefund) return 'close-circle-fill';
	if (summary.value.depositStatus === 'paid') return 'checkmark-circle-fill';
	return 'info-circle-fill';
});

const showRefundSection = computed(() => {
	return summary.value.depositPaid > 0
		|| summary.value.pendingDepositRefund
		|| summary.value.rejectedDepositRefund
		|| summary.value.canApplyDepositRefund;
});

async function reload() {
	try {
		const res = await fetchMerchantWalletSummary();
		if (res.code === 0 && res.data) summary.value = res.data;
	} catch (e) { /* ignore */ }
}

async function loadUserWalletBalance() {
	try {
		const res = await fetchWalletSummary();
		if (res.code === 0 && res.data) {
			return Number(res.data.balance) || 0;
		}
	} catch (e) { /* ignore */ }
	return 0;
}

function onApplyDepositRefund() {
	uni.showModal({
		title: ui.depositRefundBtn,
		content: ui.depositRefundConfirm,
		success: async (res) => {
			if (!res.confirm || refunding.value) return;
			refunding.value = true;
			try {
				const result = await applyMerchantDepositRefund({});
				if (result.code !== 0) {
					uni.showToast({ title: result.msg || ui.networkError, icon: 'none' });
					return;
				}
				uni.showToast({ title: ui.depositRefundSuccess, icon: 'success' });
				await reload();
			} catch (e) {
				uni.showToast({ title: ui.networkError, icon: 'none' });
			} finally {
				refunding.value = false;
			}
		}
	});
}

async function onSubmit() {
	if (needPay.value <= 0 || submitting.value) return;
	submitting.value = true;
	try {
		const orderRes = await createMerchantDepositPayOrder({ amount: needPay.value });
		if (orderRes.code !== 0 || !orderRes.data?.payNo) {
			uni.showToast({ title: orderRes.msg || '创建支付单失败', icon: 'none' });
			return;
		}

		const { payNo, amount } = orderRes.data;
		const opened = await openPayPopup({
			amount,
			accentColor: theme.value.primary,
			priceColor: theme.value.primary,
			ui: payUi,
			successTitle: ui.depositSuccess,
			loadWalletBalance: loadUserWalletBalance,
			customPay: async (methodKey) => {
				uni.showLoading({ title: payUi.payProcessing, mask: true });
				try {
					if (methodKey === 'balance') {
						const payRes = await payMerchantDepositByBalance({ payNo });
						uni.hideLoading();
						if (payRes.code !== 0) {
							uni.showToast({ title: payRes.msg || '支付失败', icon: 'none' });
							return false;
						}
						return true;
					}
					const payRes = await payMerchantDeposit({ payNo, amount, methodKey });
					uni.hideLoading();
					if (payRes.code !== 0) {
						uni.showToast({ title: payRes.msg || '支付失败', icon: 'none' });
						return false;
					}
					const launchResult = launchH5PaymentFromResponse(payRes.data, mapPayMethodToChannel(methodKey));
					if (isH5PaymentRedirectResult(launchResult)) {
						return false;
					}
					if (launchResult === 'invalid') {
						uni.showToast({ title: '无法拉起支付宝支付，请稍后重试', icon: 'none' });
						return false;
					}
					return true;
				} catch (e) {
					uni.hideLoading();
					uni.showToast({ title: ui.networkError, icon: 'none' });
					return false;
				}
			},
			onSuccess: async () => {
				await reload();
			}
		});
		if (opened === false) {
			uni.showToast({ title: payUi.payPopupUnavailable || '支付窗口未就绪', icon: 'none' });
		}
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		submitting.value = false;
	}
}

onLoad(async () => {
	await initTheme();
	uni.setNavigationBarTitle({ title: ui.depositTitle });
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
.mwd-page {
	min-height: 100vh;
	background: linear-gradient(180deg, #FAFAF8 0%, #F3F2EF 100%);
	padding: 24rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.mwd-hero {
	position: relative;
	overflow: hidden;
	padding: 40rpx 32rpx 36rpx;
	border-radius: var(--ry-radius-lg);
	background: var(--mc-wallet-gradient, linear-gradient(135deg, #FF6600 0%, #FF8A3D 55%, #FFB366 100%));
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 10rpx;

	&__circle {
		position: absolute;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.12);
		pointer-events: none;
	}

	&__circle--1 { width: 220rpx; height: 220rpx; top: -70rpx; right: -50rpx; }
	&__circle--2 { width: 140rpx; height: 140rpx; bottom: -40rpx; left: -30rpx; }

	&__badge {
		position: relative;
		z-index: 1;
		display: inline-flex;
		align-items: center;
		gap: 8rpx;
		padding: 6rpx 20rpx;
		border-radius: 999rpx;
		font-size: 22rpx;
		font-weight: 600;
		color: #FFF;
		background: rgba(255, 255, 255, 0.22);

		&--ok { background: rgba(46, 175, 106, 0.35); }
		&--warn { background: rgba(255, 255, 255, 0.22); }
		&--pending { background: rgba(255, 193, 7, 0.35); }
		&--reject { background: rgba(227, 77, 89, 0.35); }
	}

	&__shield {
		position: relative;
		z-index: 1;
		width: 88rpx;
		height: 88rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.18);
		display: flex;
		align-items: center;
		justify-content: center;
		margin-top: 8rpx;
	}

	&__title {
		position: relative;
		z-index: 1;
		font-size: 26rpx;
		color: rgba(255, 255, 255, 0.88);
		margin-top: 4rpx;
	}

	&__amount {
		position: relative;
		z-index: 1;
		font-size: 60rpx;
		font-weight: 700;
		color: #FFF;
		line-height: 1.2;
	}

}

.mwd-stats {
	display: flex;
	align-items: stretch;
	padding: 28rpx 12rpx;

	&__item {
		flex: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 8rpx;
	}

	&__divider {
		width: 1rpx;
		background: #F0EFEC;
		margin: 8rpx 0;
	}

	&__value {
		font-size: 30rpx;
		font-weight: 700;
		color: var(--ry-text-primary);

		&--accent { color: var(--mc-primary, #FF6600); }
	}

	&__label {
		font-size: 22rpx;
		color: var(--ry-text-hint);
		text-align: center;
	}
}

.mwd-pay {
	padding: 28rpx;

	&__head {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
	}

	&__icon {
		width: 52rpx;
		height: 52rpx;
		border-radius: 14rpx;
		background: var(--mc-primary-light, #FFF3EB);
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	&__main { flex: 1; min-width: 0; }

	&__title {
		display: block;
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__sub {
		display: block;
		margin-top: 6rpx;
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}

	&__amount {
		display: flex;
		align-items: baseline;
		justify-content: center;
		gap: 8rpx;
		margin-top: 24rpx;
		padding: 28rpx;
		border-radius: 16rpx;
		background: linear-gradient(180deg, #FFF8F3 0%, #FFF3EB 100%);
	}

	&__prefix {
		font-size: 36rpx;
		font-weight: 600;
		color: var(--mc-primary, #FF6600);
	}

	&__value {
		font-size: 56rpx;
		font-weight: 700;
		color: var(--mc-primary, #FF6600);
	}
}

.mwd-refund {
	padding: 28rpx;
	display: flex;
	flex-direction: column;
	gap: 20rpx;

	&__head {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__rule {
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.6;
	}

	&__pending {
		display: flex;
		align-items: flex-start;
		gap: 16rpx;
		padding: 24rpx;
		border-radius: 16rpx;
		background: var(--mc-primary-light, #FFF8F3);
	}

	&__pending-icon {
		width: 52rpx;
		height: 52rpx;
		border-radius: 50%;
		background: #FFF;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	&__pending-main {
		flex: 1;
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	&__pending-title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__pending-amount {
		font-size: 26rpx;
		color: var(--mc-primary, #FF6600);
		font-weight: 600;
	}

	&__pending-tip {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	&__action {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
	}

	&__summary {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 20rpx 24rpx;
		border-radius: 14rpx;
		background: #F8F7F5;
	}

	&__summary-label {
		font-size: 26rpx;
		color: var(--ry-text-secondary);
	}

	&__summary-value {
		font-size: 32rpx;
		font-weight: 700;
		color: var(--ry-text-primary);
	}

	&__blocked {
		display: flex;
		align-items: flex-start;
		gap: 8rpx;
		padding: 20rpx;
		border-radius: 14rpx;
		background: #F8F7F5;
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.5;
	}

	&__rejected {
		display: flex;
		flex-direction: column;
		gap: 16rpx;
		padding: 24rpx;
		border-radius: 16rpx;
		background: #FFF5F5;
		border: 1rpx solid #FAD4D8;
	}

	&__rejected-head {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	&__rejected-title {
		font-size: 28rpx;
		font-weight: 600;
		color: #E34D59;
	}

	&__rejected-amount {
		font-size: 26rpx;
		color: var(--ry-text-secondary);
	}

	&__rejected-reason {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
		padding: 18rpx 20rpx;
		border-radius: 12rpx;
		background: #FFFFFF;
	}

	&__rejected-label {
		font-size: 22rpx;
		color: var(--ry-text-hint);
	}

	&__rejected-text {
		font-size: 26rpx;
		color: var(--ry-text-primary);
		line-height: 1.6;
		word-break: break-all;
	}
}

.mwd-rules {
	padding: 28rpx;
	display: flex;
	flex-direction: column;
	gap: 16rpx;

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__item {
		display: flex;
		align-items: flex-start;
		gap: 12rpx;
		font-size: 24rpx;
		color: var(--ry-text-hint);
		line-height: 1.6;
	}

	&__dot {
		width: 10rpx;
		height: 10rpx;
		border-radius: 50%;
		background: var(--mc-primary, #FF6600);
		margin-top: 12rpx;
		flex-shrink: 0;
		opacity: 0.7;
	}
}

.mwd-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 16rpx var(--ry-page-padding-x) calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255, 255, 255, 0.96);
	box-shadow: 0 -4rpx 24rpx rgba(0, 0, 0, 0.04);
}
</style>

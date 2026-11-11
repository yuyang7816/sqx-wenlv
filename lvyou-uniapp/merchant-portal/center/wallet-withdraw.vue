<template>
	<view class="mww-page" :style="pageStyle">
		<!-- 余额头图 -->
		<view class="mww-hero">
			<text class="mww-hero__label">{{ ui.balanceLabel }}</text>
			<view class="mww-hero__amount">
				<text class="mww-hero__yen">¥</text>
				<text class="mww-hero__num">{{ formatMoney(summary.balance, '') }}</text>
			</view>
		</view>

		<view class="mww-panel">
			<!-- 金额直输 -->
			<view class="mww-amount">
				<view class="mww-amount__head">
					<text class="mww-amount__title">{{ ui.withdrawAmountLabel }}</text>
					<text class="mww-amount__all" @click="fillAll">{{ ui.withdrawAllBtn }}</text>
				</view>
				<view class="mww-amount__box">
					<text class="mww-amount__prefix">¥</text>
					<input
						v-model="amountText"
						class="mww-amount__input"
						type="digit"
						:placeholder="ui.withdrawAmountPlaceholder"
						placeholder-class="mww-amount__ph"
						:focus="amountFocus"
					/>
				</view>
			</view>

			<!-- 收款账户：跳转独立页绑定 -->
			<view class="mww-account" @click="goAccountBind">
				<view class="mww-account__left">
					<view class="mww-account__icon" :style="{ background: methodMeta.bg }">
						<u-icon :name="methodMeta.icon" :color="methodMeta.color" :size="20" />
					</view>
					<view class="mww-account__main">
						<text class="mww-account__label">{{ ui.withdrawAccountLabel }}</text>
						<text
							class="mww-account__value"
							:class="{ 'mww-account__value--warn': !accountReady }"
						>{{ accountSummary }}</text>
					</view>
				</view>
				<u-icon name="arrow-right" color="#C9C3BB" :size="14" />
			</view>

			<!-- 提现方式 -->
			<view class="mww-method">
				<text class="mww-method__title">{{ ui.withdrawMethodLabel }}</text>
				<view class="mww-method__list">
					<view
						v-for="item in methods"
						:key="item.key"
						class="mww-method__item"
						:class="{ 'mww-method__item--on': selectedMethod === item.key }"
						@click="selectedMethod = item.key"
					>
						<u-icon :name="item.icon" :color="item.color" :size="22" />
						<text class="mww-method__name">{{ item.label }}</text>
						<view v-if="selectedMethod === item.key" class="mww-method__check">
							<u-icon name="checkmark" color="#fff" :size="10" />
						</view>
					</view>
				</view>
			</view>

			<!-- 费用明细 -->
			<view v-if="withdrawBreakdown.applyAmount > 0" class="mww-fee">
				<view class="mww-fee__row">
					<text class="mww-fee__k">{{ withdrawUi.withdrawFeeLabel }}</text>
					<text class="mww-fee__v">{{ formatMoney(withdrawBreakdown.feeAmount) }}</text>
				</view>
				<view class="mww-fee__row mww-fee__row--em">
					<text class="mww-fee__k">{{ withdrawUi.withdrawActualLabel }}</text>
					<text class="mww-fee__v mww-fee__v--em">{{ formatMoney(withdrawBreakdown.actualAmount) }}</text>
				</view>
			</view>
		</view>

		<view class="mww-tip">
			<text>{{ withdrawRulesTip }}</text>
		</view>

		<view class="mww-dock">
			<view
				class="mww-dock__btn"
				:class="{ 'mww-dock__btn--loading': submitting }"
				:style="{ background: theme.primary }"
				@click="onSubmit"
			>
				<text class="mww-dock__btn-text">{{ ui.submitWithdraw }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { requireMerchantApproved, MERCHANT_BUSINESS_UI, formatMoney } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { fetchMerchantWalletSummary, postMerchantWithdraw } from '@/common/api/merchant.js';
import {
	WALLET_UI,
	WITHDRAW_METHODS,
	DEFAULT_WITHDRAW_RULES,
	getWithdrawRules,
	calcWithdrawBreakdown,
	buildWithdrawRulesTip,
	validateWithdrawAmount,
	validateWithdrawAccount,
	getWithdrawAccountSummary,
	getMerchantWithdrawAccount
} from '@/common/wallet-ui.js';

const { theme, pageStyle, initTheme, applyNavBarColor } = useMerchantTheme();
const ui = MERCHANT_BUSINESS_UI;
const withdrawUi = WALLET_UI;
const methods = WITHDRAW_METHODS;

const summary = ref({ balance: 0, withdrawRules: { ...DEFAULT_WITHDRAW_RULES } });
const amountText = ref('');
const amountFocus = ref(false);
const selectedMethod = ref('alipay');
const withdrawAccount = ref(getMerchantWithdrawAccount());
const submitting = ref(false);

const withdrawAmount = computed(() => {
	const n = Number(amountText.value);
	return Number.isFinite(n) ? n : 0;
});

const withdrawRules = computed(() => getWithdrawRules(summary.value));
const withdrawRulesTip = computed(() => buildWithdrawRulesTip(withdrawRules.value, withdrawUi));
const withdrawBreakdown = computed(() => calcWithdrawBreakdown(withdrawAmount.value, withdrawRules.value));

const accountReady = computed(() => {
	return validateWithdrawAccount(selectedMethod.value, withdrawAccount.value).ok;
});

const accountSummary = computed(() => {
	return getWithdrawAccountSummary(selectedMethod.value, withdrawAccount.value);
});

const methodMeta = computed(() => {
	const item = methods.find((m) => m.key === selectedMethod.value) || methods[0];
	return {
		icon: item.icon,
		color: item.color,
		bg: item.key === 'alipay' ? 'rgba(22, 119, 255, 0.1)' : 'rgba(7, 193, 96, 0.1)'
	};
});

function fillAll() {
	const bal = Number(summary.value.balance);
	if (!Number.isFinite(bal) || bal <= 0) {
		uni.showToast({ title: withdrawUi.balanceInsufficient, icon: 'none' });
		return;
	}
	amountText.value = String(bal);
}

function goAccountBind() {
	uni.navigateTo({
		url: `/merchant-portal/center/wallet-withdraw-account?method=${selectedMethod.value}`
	});
}

function showWithdrawConfirmModal(amount) {
	const methodLabel = methods.find((m) => m.key === selectedMethod.value)?.label || '';
	return new Promise((resolve) => {
		uni.showModal({
			title: ui.withdrawConfirmTitle,
			content: `${ui.withdrawConfirmContent} ${formatMoney(amount)}至${methodLabel}，${withdrawUi.withdrawActualLabel}${formatMoney(calcWithdrawBreakdown(amount, withdrawRules.value).actualAmount)}？`,
			confirmText: '确定',
			cancelText: '取消',
			success: (res) => resolve(Boolean(res.confirm))
		});
	});
}

async function onSubmit() {
	if (submitting.value) return;
	const amount = withdrawAmount.value;
	const amountCheck = validateWithdrawAmount(amount, withdrawRules.value, summary.value.balance);
	if (!amountCheck.ok) {
		uni.showToast({ title: amountCheck.message, icon: 'none' });
		return;
	}
	const accountCheck = validateWithdrawAccount(selectedMethod.value, withdrawAccount.value);
	if (!accountCheck.ok) {
		uni.showToast({ title: ui.withdrawAccountNotBound, icon: 'none' });
		setTimeout(() => goAccountBind(), 400);
		return;
	}
	const confirmed = await showWithdrawConfirmModal(amount);
	if (!confirmed) return;

	submitting.value = true;
	try {
		const payload = {
			amount,
			method: selectedMethod.value
		};
		if (selectedMethod.value === 'alipay') {
			payload.alipayAccount = String(withdrawAccount.value.alipayAccount || '').trim();
			payload.alipayName = String(withdrawAccount.value.alipayName || '').trim();
		} else {
			payload.wechatQrCode = String(withdrawAccount.value.wechatQrCode || '').trim();
		}
		const res = await postMerchantWithdraw(payload);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '提现失败', icon: 'none' });
			return;
		}
		uni.showToast({ title: ui.withdrawSuccess, icon: 'success' });
		setTimeout(() => {
			uni.redirectTo({ url: '/merchant-portal/center/wallet-withdraw-records' });
		}, 600);
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		submitting.value = false;
	}
}

onLoad(async () => {
	await initTheme();
	uni.setNavigationBarTitle({ title: ui.withdrawTitle });
	applyNavBarColor();
	amountFocus.value = true;
});

onShow(async () => {
	if (!(await requireMerchantApproved())) return;
	await initTheme();
	applyNavBarColor();
	withdrawAccount.value = getMerchantWithdrawAccount();
	// 仅在当前所选方式未绑定时，自动切到已绑定的方式（不必两种都绑）
	const account = withdrawAccount.value;
	const currentOk = validateWithdrawAccount(selectedMethod.value, account).ok;
	if (!currentOk) {
		if (validateWithdrawAccount('alipay', account).ok) {
			selectedMethod.value = 'alipay';
		} else if (validateWithdrawAccount('wechat', account).ok) {
			selectedMethod.value = 'wechat';
		} else if (account.preferredMethod === 'wechat' || account.preferredMethod === 'alipay') {
			selectedMethod.value = account.preferredMethod;
		}
	}
	try {
		const res = await fetchMerchantWalletSummary();
		if (res.code === 0 && res.data) {
			summary.value = {
				...res.data,
				withdrawRules: {
					...DEFAULT_WITHDRAW_RULES,
					...(res.data.withdrawRules || {})
				}
			};
		}
	} catch (e) { /* ignore */ }
});
</script>

<style lang="scss" scoped>
.mww-page {
	min-height: 100vh;
	background: var(--ry-page-bg, #f5f3f0);
	padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.mww-hero {
	padding: 48rpx 40rpx 72rpx;
	background: var(--mc-primary, #ff6600);
}

.mww-hero__label {
	display: block;
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.78);
	letter-spacing: 1rpx;
}

.mww-hero__amount {
	display: flex;
	align-items: baseline;
	gap: 6rpx;
	margin-top: 16rpx;
}

.mww-hero__yen {
	font-size: 32rpx;
	font-weight: 700;
	color: #fff;
}

.mww-hero__num {
	font-size: 64rpx;
	font-weight: 800;
	color: #fff;
	letter-spacing: -2rpx;
	line-height: 1;
}

.mww-panel {
	margin: -36rpx 28rpx 0;
	padding: 8rpx 0 12rpx;
	border-radius: 28rpx;
	background: #fff;
	box-shadow: 0 16rpx 48rpx rgba(40, 30, 20, 0.1);
	overflow: hidden;
}

.mww-amount {
	padding: 32rpx 32rpx 28rpx;
}

.mww-amount__head {
	display: flex;
	align-items: center;
	justify-content: space-between;
	margin-bottom: 20rpx;
}

.mww-amount__title {
	font-size: 26rpx;
	font-weight: 600;
	color: #1a1a1a;
}

.mww-amount__all {
	font-size: 24rpx;
	font-weight: 600;
	color: var(--mc-primary, #ff6600);
	padding: 8rpx 0 8rpx 16rpx;
}

.mww-amount__box {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding-bottom: 16rpx;
	border-bottom: 2rpx solid rgba(0, 0, 0, 0.06);
}

.mww-amount__prefix {
	font-size: 44rpx;
	font-weight: 700;
	color: #1a1a1a;
	line-height: 1;
}

.mww-amount__input {
	flex: 1;
	height: 72rpx;
	font-size: 56rpx;
	font-weight: 700;
	color: #1a1a1a;
	letter-spacing: -1rpx;
}

.mww-amount__ph {
	font-size: 32rpx;
	font-weight: 500;
	color: #c9c3bb;
}

.mww-account {
	display: flex;
	align-items: center;
	justify-content: space-between;
	gap: 16rpx;
	margin: 0 20rpx;
	padding: 28rpx 24rpx;
	border-radius: 20rpx;
	background: #faf8f5;

	&:active {
		opacity: 0.85;
	}
}

.mww-account__left {
	flex: 1;
	min-width: 0;
	display: flex;
	align-items: center;
	gap: 18rpx;
}

.mww-account__icon {
	width: 72rpx;
	height: 72rpx;
	border-radius: 20rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	flex-shrink: 0;
}

.mww-account__main {
	flex: 1;
	min-width: 0;
	display: flex;
	flex-direction: column;
	gap: 6rpx;
}

.mww-account__label {
	font-size: 22rpx;
	color: #999;
}

.mww-account__value {
	font-size: 26rpx;
	font-weight: 600;
	color: #1a1a1a;
	overflow: hidden;
	text-overflow: ellipsis;
	white-space: nowrap;

	&--warn {
		color: var(--mc-primary, #ff6600);
		font-weight: 500;
	}
}

.mww-method {
	padding: 28rpx 32rpx 20rpx;
}

.mww-method__title {
	display: block;
	margin-bottom: 16rpx;
	font-size: 26rpx;
	font-weight: 600;
	color: #1a1a1a;
}

.mww-method__list {
	display: flex;
	gap: 16rpx;
}

.mww-method__item {
	position: relative;
	flex: 1;
	height: 96rpx;
	border-radius: 20rpx;
	background: #faf8f5;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;

	&--on {
		background: var(--mc-primary-light, #fff3eb);
		box-shadow: inset 0 0 0 2rpx var(--mc-primary, #ff6600);
	}
}

.mww-method__name {
	font-size: 26rpx;
	font-weight: 600;
	color: #333;
}

.mww-method__check {
	position: absolute;
	right: 0;
	bottom: 0;
	width: 36rpx;
	height: 28rpx;
	border-radius: 16rpx 0 18rpx 0;
	background: var(--mc-primary, #ff6600);
	display: flex;
	align-items: center;
	justify-content: center;
}

.mww-fee {
	margin: 8rpx 32rpx 24rpx;
	padding: 24rpx;
	border-radius: 18rpx;
	background: #faf8f5;
	display: flex;
	flex-direction: column;
	gap: 14rpx;
}

.mww-fee__row {
	display: flex;
	align-items: center;
	justify-content: space-between;
}

.mww-fee__k {
	font-size: 24rpx;
	color: #999;
}

.mww-fee__v {
	font-size: 26rpx;
	color: #666;
	font-weight: 500;

	&--em {
		font-size: 32rpx;
		font-weight: 700;
		color: var(--mc-primary, #ff6600);
	}
}

.mww-tip {
	padding: 28rpx 40rpx 0;
	font-size: 22rpx;
	color: #9a948c;
	line-height: 1.55;
}

.mww-dock {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255, 255, 255, 0.94);
	backdrop-filter: blur(20rpx);
}

.mww-dock__btn {
	height: 96rpx;
	border-radius: 28rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	box-shadow: 0 12rpx 28rpx rgba(255, 102, 0, 0.28);

	&:active {
		opacity: 0.9;
		transform: scale(0.985);
	}

	&--loading {
		opacity: 0.7;
		pointer-events: none;
	}
}

.mww-dock__btn-text {
	font-size: 30rpx;
	font-weight: 700;
	color: #fff;
	letter-spacing: 2rpx;
}
</style>

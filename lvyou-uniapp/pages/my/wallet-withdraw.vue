<template>
	<view class="ww-page">
		<view class="ww-balance ry-card">
			<text class="ww-balance__label">{{ ui.balanceLabel }}</text>
			<text class="ww-balance__value">{{ ui.currencySymbol }}{{ summary.balance }}</text>
		</view>

		<view class="ww-section ry-card">
			<text class="ww-section__title">{{ ui.withdrawAmountLabel }}</text>
			<view class="ww-amounts">
				<view
					v-for="item in amounts"
					:key="item"
					class="ww-amounts__item"
					:class="{ 'ww-amounts__item--active': selectedAmount === item && !customAmount }"
					@click="selectAmount(item)"
				>
					<text>{{ ui.currencySymbol }}{{ item }}</text>
				</view>
			</view>
			<view class="ww-custom">
				<text class="ww-custom__prefix">{{ ui.currencySymbol }}</text>
				<input
					v-model="customAmount"
					class="ww-custom__input"
					type="digit"
					:placeholder="ui.customAmountPlaceholder"
					placeholder-class="ww-custom__placeholder"
					@input="onCustomInput"
				/>
			</view>
		</view>

		<view class="ww-section ry-card">
			<text class="ww-section__title">{{ ui.withdrawMethodLabel }}</text>
			<view class="ww-methods">
				<view
					v-for="item in methods"
					:key="item.key"
					class="ww-methods__item"
					:class="{ 'ww-methods__item--active': selectedMethod === item.key }"
					@click="selectedMethod = item.key"
				>
					<u-icon :name="item.icon" :color="item.color" :size="22" />
					<text>{{ item.label }}</text>
				</view>
			</view>
		</view>

		<view class="ww-account ry-card" @click="goAccountBind">
			<view class="ww-account__main">
				<text class="ww-account__title">{{ ui.accountBindLink }}</text>
				<text class="ww-account__desc" :class="{ 'ww-account__desc--warn': !accountReady }">
					{{ accountSummary }}
				</text>
			</view>
			<u-icon name="arrow-right" color="#CCCCCC" :size="14" />
		</view>

		<view v-if="withdrawBreakdown.applyAmount > 0" class="ww-fee ry-card">
			<view class="ww-fee__row">
				<text>{{ ui.withdrawFeeLabel }}</text>
				<text>{{ ui.currencySymbol }}{{ formatMoney(withdrawBreakdown.feeAmount) }}</text>
			</view>
			<view class="ww-fee__row ww-fee__row--strong">
				<text>{{ ui.withdrawActualLabel }}</text>
				<text>{{ ui.currencySymbol }}{{ formatMoney(withdrawBreakdown.actualAmount) }}</text>
			</view>
		</view>

		<view class="ww-tip">
			<text>{{ withdrawRulesTip }}</text>
			<text class="ww-tip__sub">{{ ui.withdrawPendingTip }}</text>
		</view>

		<view class="ww-footer">
			<u-button
				:text="ui.confirmWithdraw"
				block
				color="#FF6600"
				:custom-style="submitStyle"
				:loading="submitting"
				@click="onSubmit"
			/>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import {
	WALLET_UI,
	WITHDRAW_AMOUNTS,
	WITHDRAW_METHODS,
	EMPTY_WALLET_SUMMARY,
	getWithdrawAccount,
	validateWithdrawAccount,
	formatMoney,
	getWithdrawAccountSummary,
	getWithdrawRules,
	calcWithdrawBreakdown,
	buildWithdrawRulesTip,
	validateWithdrawAmount
} from '@/common/wallet-ui.js';
import { hasToken } from '@/common/api/auth.js';
import { goLogin, goBackDelayed, openPage, PAGES } from '@/common/navigation.js';
import {
	fetchWalletSummary,
	createWalletWithdraw
} from '@/common/api/wallet.js';

const ui = WALLET_UI;
const amounts = WITHDRAW_AMOUNTS;
const methods = WITHDRAW_METHODS;
const summary = ref({ ...EMPTY_WALLET_SUMMARY });
const selectedAmount = ref(100);
const customAmount = ref('');
const selectedMethod = ref('alipay');
const submitting = ref(false);
const submitStyle = { height: '44px', borderRadius: '22px' };
const withdrawAccount = ref(getWithdrawAccount());

const finalAmount = computed(() => {
	if (customAmount.value) return Number(customAmount.value);
	return selectedAmount.value;
});

const accountReady = computed(() => {
	return validateWithdrawAccount(selectedMethod.value, withdrawAccount.value).ok;
});

const accountSummary = computed(() => {
	return getWithdrawAccountSummary(selectedMethod.value, withdrawAccount.value);
});

const withdrawRules = computed(() => getWithdrawRules(summary.value));

const withdrawRulesTip = computed(() => buildWithdrawRulesTip(withdrawRules.value, ui));

const withdrawBreakdown = computed(() => {
	return calcWithdrawBreakdown(finalAmount.value, withdrawRules.value);
});

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: ui.loginTip, icon: 'none' });
	goLogin({ delay: 400 });
	return false;
}

async function reloadData() {
	try {
		const res = await fetchWalletSummary();
		if (res.code === 0 && res.data) {
			summary.value = {
				...EMPTY_WALLET_SUMMARY,
				...res.data,
				withdrawRules: {
					...EMPTY_WALLET_SUMMARY.withdrawRules,
					...(res.data.withdrawRules || {})
				}
			};
		}
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	}
	withdrawAccount.value = getWithdrawAccount();
}

function selectAmount(value) {
	selectedAmount.value = value;
	customAmount.value = '';
}

function onCustomInput() {
	if (customAmount.value) selectedAmount.value = 0;
}

function goAccountBind() {
	openPage(PAGES.WALLET_WITHDRAW_ACCOUNT);
}

function showWithdrawConfirmModal(amount) {
	return new Promise((resolve) => {
		const methodLabel = methods.find((m) => m.key === selectedMethod.value)?.label || '';
		uni.showModal({
			title: ui.withdrawConfirmTitle,
			content: `${ui.withdrawConfirmContent} ${ui.currencySymbol}${formatMoney(amount)}至${methodLabel}，${ui.withdrawActualLabel}${ui.currencySymbol}${formatMoney(calcWithdrawBreakdown(amount, withdrawRules.value).actualAmount)}？`,
			confirmText: '确定',
			cancelText: '取消',
			success: (res) => resolve(Boolean(res.confirm))
		});
	});
}

async function onSubmit() {
	if (submitting.value) return;
	const amount = finalAmount.value;
	const amountCheck = validateWithdrawAmount(
		amount,
		withdrawRules.value,
		summary.value.available || summary.value.balance
	);
	if (!amountCheck.ok) {
		uni.showToast({ title: amountCheck.message, icon: 'none' });
		return;
	}

	const accountInfo = withdrawAccount.value;
	const accountCheck = validateWithdrawAccount(selectedMethod.value, accountInfo);
	if (!accountCheck.ok) {
		uni.showModal({
			title: ui.accountBindLink,
			content: ui.accountNotBoundTip,
			confirmText: '去绑定',
			confirmColor: '#FF6600',
			success: (res) => {
				if (res.confirm) goAccountBind();
			}
		});
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
			payload.alipayAccount = String(accountInfo.alipayAccount || '').trim();
			payload.alipayName = String(accountInfo.alipayName || '').trim();
		} else {
			payload.wechatQrCode = accountInfo.wechatQrCode;
		}

		const res = await createWalletWithdraw(payload);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || ui.loadFail, icon: 'none' });
			return;
		}
		await reloadData();
		uni.showToast({ title: ui.withdrawSuccess, icon: 'success' });
		goBackDelayed(800);
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		submitting.value = false;
	}
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.withdrawTitle });
});

onShow(() => {
	if (!requireLogin()) return;
	reloadData();
});
</script>

<style lang="scss" scoped>
.ww-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.ww-balance {
	padding: 32rpx 28rpx;
	display: flex;
	flex-direction: column;
	gap: 8rpx;

	&__label {
		font-size: 26rpx;
		color: var(--ry-text-secondary);
	}

	&__value {
		font-size: 48rpx;
		font-weight: 700;
		color: #FF6600;
	}
}

.ww-section {
	padding: 28rpx;

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		margin-bottom: 20rpx;
		display: block;
	}
}

.ww-amounts {
	display: grid;
	grid-template-columns: repeat(4, 1fr);
	gap: 16rpx;
	margin-bottom: 24rpx;

	&__item {
		height: 72rpx;
		border-radius: var(--ry-radius-sm);
		border: 1rpx solid #EEEEEE;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 26rpx;
		color: var(--ry-text-primary);
		background: #FAFAFA;

		&--active {
			border-color: #FF6600;
			background: #FFF3EB;
			color: #FF6600;
			font-weight: 600;
		}
	}
}

.ww-custom {
	display: flex;
	align-items: center;
	gap: 8rpx;
	padding: 0 20rpx;
	height: 88rpx;
	border-radius: var(--ry-radius-sm);
	background: #FAFAFA;
	border: 1rpx solid #EEEEEE;

	&__prefix {
		font-size: 32rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__input {
		flex: 1;
		font-size: 32rpx;
		color: var(--ry-text-primary);
	}

	&__placeholder {
		color: #CCCCCC;
		font-size: 28rpx;
	}
}

.ww-methods {
	display: flex;
	gap: 16rpx;

	&__item {
		flex: 1;
		height: 80rpx;
		border-radius: var(--ry-radius-sm);
		border: 1rpx solid #EEEEEE;
		background: #FAFAFA;
		display: flex;
		align-items: center;
		justify-content: center;
		gap: 10rpx;
		font-size: 28rpx;
		color: var(--ry-text-primary);

		&--active {
			border-color: #FF6600;
			background: #FFF3EB;
			color: #FF6600;
			font-weight: 600;
		}
	}
}

.ww-account {
	padding: 28rpx;
	display: flex;
	align-items: center;
	gap: 16rpx;

	&:active {
		opacity: 0.88;
	}

	&__main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
	}

	&__desc {
		font-size: 24rpx;
		color: var(--ry-text-secondary);
		overflow: hidden;
		text-overflow: ellipsis;
		white-space: nowrap;

		&--warn {
			color: #FF6600;
		}
	}
}

.ww-fee {
	padding: 24rpx 28rpx;
	display: flex;
	flex-direction: column;
	gap: 12rpx;

	&__row {
		display: flex;
		align-items: center;
		justify-content: space-between;
		font-size: 26rpx;
		color: var(--ry-text-secondary);

		&--strong {
			font-size: 28rpx;
			font-weight: 600;
			color: #FF6600;
		}
	}
}

.ww-tip {
	padding: 0 8rpx;
	font-size: 24rpx;
	color: var(--ry-text-hint);
	line-height: 1.5;
	display: flex;
	flex-direction: column;
	gap: 8rpx;

	&__sub {
		color: var(--ry-text-hint);
	}
}

.ww-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
	background: #FFFFFF;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
}
</style>

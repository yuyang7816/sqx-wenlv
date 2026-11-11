<template>
	<view class="wr-page">
		<view class="wr-balance ry-card">
			<text class="wr-balance__label">{{ ui.balanceLabel }}</text>
			<text class="wr-balance__value">{{ ui.currencySymbol }}{{ balance }}</text>
		</view>

		<view class="wr-section ry-card">
			<text class="wr-section__title">{{ ui.rechargeAmountLabel }}</text>
			<view class="wr-amounts">
				<view
					v-for="item in amounts"
					:key="item"
					class="wr-amounts__item"
					:class="{ 'wr-amounts__item--active': selectedAmount === item && !customAmount }"
					@click="selectAmount(item)"
				>
					<text>{{ ui.currencySymbol }}{{ item }}</text>
				</view>
			</view>
			<view class="wr-custom">
				<text class="wr-custom__prefix">{{ ui.currencySymbol }}</text>
				<input
					v-model="customAmount"
					class="wr-custom__input"
					type="digit"
					:placeholder="ui.customAmountPlaceholder"
					placeholder-class="wr-custom__placeholder"
					@input="onCustomInput"
				/>
			</view>
		</view>

		<view class="wr-section ry-card">
			<text class="wr-section__title">{{ ui.rechargeMethodLabel }}</text>
			<view v-if="paymentMethods.length" class="wr-methods">
				<view
					v-for="method in paymentMethods"
					:key="method.key"
					class="wr-method"
					:class="{ 'wr-method--active': selectedPayMethod === method.key }"
					:style="selectedPayMethod === method.key ? { borderColor: method.color } : {}"
					@click="selectPayMethod(method)"
				>
					<view class="wr-method__icon" :style="{ backgroundColor: `${method.color}14` }">
						<u-icon :name="method.icon" :size="20" :color="method.color" />
					</view>
					<view class="wr-method__main">
						<view class="wr-method__title-row">
							<text class="wr-method__title">{{ method.label }}</text>
							<view v-if="method.recommend" class="wr-method__tag">
								<text>{{ payUi.payRecommend }}</text>
							</view>
						</view>
						<text v-if="method.desc" class="wr-method__desc">{{ method.desc }}</text>
					</view>
					<view
						class="wr-method__radio"
						:class="{ 'wr-method__radio--checked': selectedPayMethod === method.key }"
						:style="selectedPayMethod === method.key ? { borderColor: method.color, backgroundColor: method.color } : {}"
					>
						<u-icon v-if="selectedPayMethod === method.key" name="checkmark" :size="12" color="#fff" />
					</view>
				</view>
			</view>
			<text v-else class="wr-methods__empty">{{ payUi.paySelectMethod }}</text>
		</view>

		<view class="wr-footer">
			<u-button
				:text="ui.confirmRecharge"
				block
				color="#FF6600"
				:custom-style="submitStyle"
				:loading="submitting"
				:disabled="!paymentMethods.length"
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
	RECHARGE_AMOUNTS,
	formatMoney
} from '@/common/wallet-ui.js';
import { BOOKING_UI } from '@/common/booking-ui.js';
import {
	buildRechargePaymentMethods,
	fetchPaymentVisibilityContext,
	resolvePayMethodSelection
} from '@/common/payment-options.js';
import { hasToken } from '@/common/api/auth.js';
import { fetchWalletSummary, createWalletRecharge } from '@/common/api/wallet.js';
import { payWalletRecharge, mapPayMethodToChannel } from '@/common/api/payment.js';
import { isH5PaymentRedirectResult, launchH5PaymentFromResponse } from '@/common/h5-payment-launch.js';
import { goLogin, goBackDelayed } from '@/common/navigation.js';

const ui = WALLET_UI;
const payUi = BOOKING_UI;
const amounts = RECHARGE_AMOUNTS;
const balance = ref('0.00');
const selectedAmount = ref(100);
const customAmount = ref('');
const submitting = ref(false);
const submitStyle = { height: '44px', borderRadius: '22px' };
const paymentVisibility = ref({
	wechatMpEnabled: true,
	isWechatMpUser: false
});
const selectedPayMethod = ref('alipay');

const paymentMethods = computed(() => buildRechargePaymentMethods(paymentVisibility.value, payUi));

const finalAmount = computed(() => {
	if (customAmount.value) return Number(customAmount.value);
	return selectedAmount.value;
});

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: ui.loginTip, icon: 'none' });
	goLogin({ delay: 400 });
	return false;
}

async function reloadBalance() {
	try {
		const res = await fetchWalletSummary();
		if (res.code === 0 && res.data) {
			balance.value = formatMoney(res.data.balance);
		}
	} catch (e) {
		/* 余额展示失败不阻断充值 */
	}
}

async function loadPaymentMethods() {
	paymentVisibility.value = await fetchPaymentVisibilityContext();
	selectedPayMethod.value = resolvePayMethodSelection(paymentMethods.value, selectedPayMethod.value);
}

function selectAmount(value) {
	selectedAmount.value = value;
	customAmount.value = '';
}

function onCustomInput() {
	if (customAmount.value) selectedAmount.value = 0;
}

function selectPayMethod(method) {
	selectedPayMethod.value = method.key;
}

async function executeRechargePay(rechargeNo, amount, methodKey) {
	uni.showLoading({ title: payUi.payProcessing, mask: true });
	try {
		const payRes = await payWalletRecharge({
			rechargeNo,
			amount,
			methodKey
		});
		uni.hideLoading();
		if (payRes.code !== 0) {
			uni.showToast({ title: payRes.msg || ui.loadFail, icon: 'none' });
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
}

async function onSubmit() {
	if (submitting.value) return;
	const amount = finalAmount.value;
	if (!Number.isFinite(amount) || amount <= 0) {
		uni.showToast({ title: ui.amountInvalid, icon: 'none' });
		return;
	}
	if (amount < 1) {
		uni.showToast({ title: ui.amountMinRecharge, icon: 'none' });
		return;
	}
	if (!paymentMethods.value.length || !selectedPayMethod.value) {
		uni.showToast({ title: payUi.paySelectMethod, icon: 'none' });
		return;
	}
	if (!paymentMethods.value.some((item) => item.key === selectedPayMethod.value)) {
		selectedPayMethod.value = resolvePayMethodSelection(paymentMethods.value, '');
		if (!selectedPayMethod.value) {
			uni.showToast({ title: payUi.paySelectMethod, icon: 'none' });
			return;
		}
	}

	submitting.value = true;
	try {
		const rechargeRes = await createWalletRecharge(amount);
		if (rechargeRes.code !== 0 || !rechargeRes.data?.rechargeNo) {
			uni.showToast({ title: rechargeRes.msg || ui.loadFail, icon: 'none' });
			return;
		}

		const success = await executeRechargePay(
			rechargeRes.data.rechargeNo,
			amount,
			selectedPayMethod.value
		);
		if (!success) return;

		uni.showToast({ title: ui.rechargeSuccess, icon: 'success' });
		await reloadBalance();
		goBackDelayed(800);
	} catch (e) {
		uni.showToast({ title: ui.networkError, icon: 'none' });
	} finally {
		submitting.value = false;
	}
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.rechargeTitle });
});

onShow(async () => {
	if (!requireLogin()) return;
	await Promise.all([reloadBalance(), loadPaymentMethods()]);
});
</script>

<style lang="scss" scoped>
.wr-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.wr-balance {
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

.wr-section {
	padding: 28rpx;

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		margin-bottom: 20rpx;
		display: block;
	}
}

.wr-amounts {
	display: grid;
	grid-template-columns: repeat(3, 1fr);
	gap: 16rpx;
	margin-bottom: 24rpx;

	&__item {
		height: 80rpx;
		border-radius: var(--ry-radius-sm);
		border: 1rpx solid #EEEEEE;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 28rpx;
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

.wr-custom {
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

.wr-methods {
	display: flex;
	flex-direction: column;
	gap: 12rpx;

	&__empty {
		font-size: 24rpx;
		color: #999999;
		line-height: 1.5;
	}
}

.wr-method {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 18rpx 20rpx;
	min-height: 88rpx;
	border-radius: 16rpx;
	background: #FAFAFA;
	border: 2rpx solid transparent;
	box-sizing: border-box;

	&--active {
		background: #FFFFFF;
	}

	&__icon {
		flex-shrink: 0;
		width: 56rpx;
		height: 56rpx;
		border-radius: 14rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	&__main {
		flex: 1;
		min-width: 0;
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	&__title-row {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	&__title {
		font-size: 28rpx;
		color: #333333;
	}

	&__tag {
		display: inline-flex;
		align-items: center;
		justify-content: center;
		padding: 2rpx 10rpx;
		border-radius: 6rpx;
		background: #FFF3EB;

		text {
			font-size: 18rpx;
			color: #FF6600;
			line-height: 1;
		}
	}

	&__desc {
		font-size: 22rpx;
		color: #999999;
	}

	&__radio {
		flex-shrink: 0;
		width: 36rpx;
		height: 36rpx;
		border-radius: 50%;
		border: 2rpx solid #D9D9D9;
		display: flex;
		align-items: center;
		justify-content: center;
		box-sizing: border-box;
	}
}

.wr-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
	background: #FFFFFF;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
}
</style>

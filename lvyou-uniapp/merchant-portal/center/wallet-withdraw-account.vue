<template>
	<view class="mwa-page" :style="pageStyle">
		<view class="mwa-hero">
			<text class="mwa-hero__title">收款方式</text>
			<text class="mwa-hero__desc">绑定后用于商户提现到账，请确保信息真实有效</text>
		</view>

		<view class="mwa-sheet">
			<view class="mwa-seg">
				<view
					v-for="item in methods"
					:key="item.key"
					class="mwa-seg__item"
					:class="{ 'mwa-seg__item--on': selectedMethod === item.key }"
					@click="selectedMethod = item.key"
				>
					<u-icon :name="item.icon" :color="selectedMethod === item.key ? '#fff' : item.color" :size="18" />
					<text class="mwa-seg__text">{{ item.label }}</text>
				</view>
			</view>

			<template v-if="selectedMethod === 'alipay'">
				<view class="mwa-field">
					<text class="mwa-field__label">{{ withdrawUi.alipayAccountLabel }}</text>
					<input
						v-model="accountForm.alipayAccount"
						class="mwa-field__input"
						type="text"
						:placeholder="withdrawUi.alipayAccountPlaceholder"
						placeholder-class="mwa-field__ph"
					/>
				</view>
				<view class="mwa-field">
					<text class="mwa-field__label">{{ withdrawUi.alipayNameLabel }}</text>
					<input
						v-model="accountForm.alipayName"
						class="mwa-field__input"
						type="text"
						:placeholder="withdrawUi.alipayNamePlaceholder"
						placeholder-class="mwa-field__ph"
					/>
				</view>
			</template>

			<template v-else>
				<text class="mwa-tip-inline">{{ withdrawUi.wechatQrTip }}</text>
				<view class="mwa-qr" @click="chooseQrCode">
					<image
						v-if="accountForm.wechatQrCode"
						class="mwa-qr__img"
						:src="accountForm.wechatQrCode"
						mode="aspectFill"
					/>
					<view v-else class="mwa-qr__empty">
						<u-icon name="camera-fill" :color="theme.primary" :size="36" />
						<text class="mwa-qr__text">{{ withdrawUi.wechatQrUpload }}</text>
					</view>
				</view>
				<text v-if="accountForm.wechatQrCode" class="mwa-qr__change" @click="chooseQrCode">
					{{ withdrawUi.wechatQrChange }}
				</text>
			</template>
		</view>

		<view class="mwa-note">
			<u-icon name="info-circle" color="#9A948C" :size="14" />
			<text class="mwa-note__text">{{ withdrawUi.accountBindTip }}</text>
		</view>

		<view class="mwa-dock">
			<view
				class="mwa-dock__btn"
				:class="{ 'mwa-dock__btn--loading': saving }"
				:style="{ background: theme.primary }"
				@click="onSave"
			>
				<text class="mwa-dock__btn-text">{{ withdrawUi.accountBindSave }}</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import { requireMerchantApproved, MERCHANT_BUSINESS_UI } from '@/common/merchant-center.js';
import { useMerchantTheme } from '@/common/useMerchantTheme.js';
import { uploadMerchantFile, resolveUploadUrl } from '@/common/api/merchant.js';
import {
	WALLET_UI,
	WITHDRAW_METHODS,
	getMerchantWithdrawAccount,
	saveMerchantWithdrawAccount,
	validateWithdrawAccount
} from '@/common/wallet-ui.js';

const { theme, pageStyle, initTheme, applyNavBarColor } = useMerchantTheme();
const ui = MERCHANT_BUSINESS_UI;
const withdrawUi = WALLET_UI;
const methods = WITHDRAW_METHODS;
const selectedMethod = ref('alipay');
const methodFromQuery = ref('');
const saving = ref(false);
const uploading = ref(false);

const accountForm = reactive({
	alipayAccount: '',
	alipayName: '',
	wechatQrCode: ''
});

function loadAccount() {
	const saved = getMerchantWithdrawAccount();
	accountForm.alipayAccount = saved.alipayAccount || '';
	accountForm.alipayName = saved.alipayName || '';
	accountForm.wechatQrCode = saved.wechatQrCode || '';
	// 优先使用提现页传入的方式，只绑定当前要用的一种即可
	if (methodFromQuery.value === 'wechat' || methodFromQuery.value === 'alipay') {
		selectedMethod.value = methodFromQuery.value;
	} else if (saved.preferredMethod === 'wechat' || saved.preferredMethod === 'alipay') {
		selectedMethod.value = saved.preferredMethod;
	} else if (saved.wechatQrCode && !saved.alipayAccount) {
		selectedMethod.value = 'wechat';
	} else {
		selectedMethod.value = 'alipay';
	}
}

function chooseQrCode() {
	if (uploading.value) return;
	uni.chooseImage({
		count: 1,
		sizeType: ['compressed'],
		sourceType: ['album', 'camera'],
		success: async (res) => {
			const tempPath = res.tempFilePaths[0];
			if (!tempPath) return;
			uploading.value = true;
			uni.showLoading({ title: '上传中...', mask: true });
			try {
				const uploadRes = await uploadMerchantFile(tempPath);
				const url = resolveUploadUrl(uploadRes);
				if (!url) {
					uni.showToast({ title: withdrawUi.uploadFail, icon: 'none' });
					return;
				}
				accountForm.wechatQrCode = url;
			} catch (e) {
				uni.showToast({ title: withdrawUi.uploadFail, icon: 'none' });
			} finally {
				uploading.value = false;
				uni.hideLoading();
			}
		}
	});
}

function onSave() {
	if (saving.value) return;
	// 只校验并保存当前选中的提现方式，另一种可不绑
	const draft = {
		alipayAccount: accountForm.alipayAccount.trim(),
		alipayName: accountForm.alipayName.trim(),
		wechatQrCode: accountForm.wechatQrCode
	};
	const accountCheck = validateWithdrawAccount(selectedMethod.value, draft);
	if (!accountCheck.ok) {
		uni.showToast({ title: accountCheck.message, icon: 'none' });
		return;
	}
	const patch = { preferredMethod: selectedMethod.value };
	if (selectedMethod.value === 'alipay') {
		patch.alipayAccount = draft.alipayAccount;
		patch.alipayName = draft.alipayName;
	} else {
		patch.wechatQrCode = draft.wechatQrCode;
	}
	saving.value = true;
	try {
		saveMerchantWithdrawAccount(patch);
		uni.showToast({ title: withdrawUi.accountBindSaved, icon: 'success' });
		setTimeout(() => uni.navigateBack({ fail: () => {} }), 500);
	} finally {
		saving.value = false;
	}
}

onLoad(async (options) => {
	if (options?.method === 'wechat' || options?.method === 'alipay') {
		methodFromQuery.value = options.method;
	}
	await initTheme();
	uni.setNavigationBarTitle({ title: ui.withdrawAccountBindTitle });
	applyNavBarColor();
});

onShow(async () => {
	if (!(await requireMerchantApproved())) return;
	await initTheme();
	applyNavBarColor();
	loadAccount();
});
</script>

<style lang="scss" scoped>
.mwa-page {
	min-height: 100vh;
	background: var(--ry-page-bg, #f5f3f0);
	padding-bottom: calc(140rpx + env(safe-area-inset-bottom));
}

.mwa-hero {
	padding: 40rpx 36rpx 48rpx;
	background: var(--mc-primary, #ff6600);
}

.mwa-hero__title {
	display: block;
	font-size: 40rpx;
	font-weight: 800;
	color: #fff;
	letter-spacing: -0.5rpx;
}

.mwa-hero__desc {
	display: block;
	margin-top: 12rpx;
	font-size: 24rpx;
	color: rgba(255, 255, 255, 0.82);
	line-height: 1.5;
}

.mwa-sheet {
	margin: -24rpx 28rpx 0;
	padding: 32rpx 28rpx 36rpx;
	border-radius: 28rpx;
	background: #fff;
	box-shadow: 0 12rpx 40rpx rgba(40, 30, 20, 0.08);
}

.mwa-seg {
	display: flex;
	gap: 16rpx;
	margin-bottom: 32rpx;
	padding: 6rpx;
	border-radius: 20rpx;
	background: #f5f3f0;
}

.mwa-seg__item {
	flex: 1;
	height: 72rpx;
	border-radius: 16rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 10rpx;

	&--on {
		background: var(--mc-primary, #ff6600);
		box-shadow: 0 6rpx 16rpx rgba(255, 102, 0, 0.28);

		.mwa-seg__text {
			color: #fff;
			font-weight: 700;
		}
	}
}

.mwa-seg__text {
	font-size: 26rpx;
	color: #666;
	font-weight: 500;
}

.mwa-field {
	padding: 24rpx 0;
	border-bottom: 1rpx solid rgba(0, 0, 0, 0.05);

	&:last-child {
		border-bottom: none;
		padding-bottom: 0;
	}
}

.mwa-field__label {
	display: block;
	margin-bottom: 12rpx;
	font-size: 24rpx;
	color: #999;
}

.mwa-field__input {
	width: 100%;
	height: 48rpx;
	font-size: 30rpx;
	font-weight: 600;
	color: #1a1a1a;
}

.mwa-field__ph {
	color: #c8c4be;
	font-weight: 400;
}

.mwa-tip-inline {
	display: block;
	margin-bottom: 20rpx;
	font-size: 24rpx;
	color: #999;
	line-height: 1.5;
}

.mwa-qr {
	width: 280rpx;
	height: 280rpx;
	border-radius: 24rpx;
	background: #f7f5f2;
	overflow: hidden;
	margin: 0 auto;

	&__img {
		width: 100%;
		height: 100%;
	}

	&__empty {
		width: 100%;
		height: 100%;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		gap: 16rpx;
	}

	&__text {
		font-size: 24rpx;
		color: #999;
	}

	&__change {
		display: block;
		margin-top: 20rpx;
		text-align: center;
		font-size: 26rpx;
		font-weight: 600;
		color: var(--mc-primary, #ff6600);
	}
}

.mwa-note {
	display: flex;
	align-items: flex-start;
	gap: 10rpx;
	margin: 28rpx 36rpx 0;
}

.mwa-note__text {
	flex: 1;
	font-size: 22rpx;
	color: #9a948c;
	line-height: 1.55;
}

.mwa-dock {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 16rpx 28rpx calc(16rpx + env(safe-area-inset-bottom));
	background: rgba(255, 255, 255, 0.94);
	backdrop-filter: blur(20rpx);
}

.mwa-dock__btn {
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

.mwa-dock__btn-text {
	font-size: 30rpx;
	font-weight: 700;
	color: #fff;
	letter-spacing: 2rpx;
}
</style>

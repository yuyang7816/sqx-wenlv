<template>
	<view class="wba-page">
		<view class="wba-section ry-card">
			<text class="wba-section__title">{{ ui.accountBindMethodLabel }}</text>
			<view class="wba-methods">
				<view
					v-for="item in methods"
					:key="item.key"
					class="wba-methods__item"
					:class="{ 'wba-methods__item--active': selectedMethod === item.key }"
					@click="selectedMethod = item.key"
				>
					<u-icon :name="item.icon" :color="item.color" :size="22" />
					<text>{{ item.label }}</text>
				</view>
			</view>
		</view>

		<view v-if="selectedMethod === 'alipay'" class="wba-section ry-card">
			<text class="wba-section__title">{{ ui.alipayBindTitle }}</text>
			<text class="wba-section__tip">{{ ui.accountBindAlipayTip }}</text>
			<view class="wba-field">
				<text class="wba-field__label">{{ ui.alipayAccountLabel }}</text>
				<input
					v-model="accountForm.alipayAccount"
					class="wba-field__input"
					type="text"
					:placeholder="ui.alipayAccountPlaceholder"
					placeholder-class="wba-field__placeholder"
				/>
			</view>
			<view class="wba-field wba-field--last">
				<text class="wba-field__label">{{ ui.alipayNameLabel }}</text>
				<input
					v-model="accountForm.alipayName"
					class="wba-field__input"
					type="text"
					:placeholder="ui.alipayNamePlaceholder"
					placeholder-class="wba-field__placeholder"
				/>
			</view>
		</view>

		<view v-else class="wba-section ry-card">
			<text class="wba-section__title">{{ ui.wechatQrLabel }}</text>
			<text class="wba-section__tip">{{ ui.wechatQrTip }}</text>
			<view class="wba-qrcode" @click="chooseQrCode">
				<image
					v-if="accountForm.wechatQrCode"
					class="wba-qrcode__img"
					:src="accountForm.wechatQrCode"
					mode="aspectFill"
				/>
				<view v-else class="wba-qrcode__empty">
					<u-icon name="camera-fill" color="#CCCCCC" :size="40" />
					<text class="wba-qrcode__text">{{ ui.wechatQrUpload }}</text>
				</view>
			</view>
			<text v-if="accountForm.wechatQrCode" class="wba-qrcode__change">{{ ui.wechatQrChange }}</text>
		</view>

		<view class="wba-tip">
			<text>{{ ui.accountBindTip }}</text>
		</view>

		<view class="wba-footer">
			<u-button
				:text="ui.accountBindSave"
				block
				color="#FF6600"
				:custom-style="submitStyle"
				:loading="saving"
				@click="onSave"
			/>
		</view>
	</view>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { onLoad, onShow } from '@dcloudio/uni-app';
import {
	WALLET_UI,
	WITHDRAW_METHODS,
	getWithdrawAccount,
	saveWithdrawAccount,
	validateWithdrawAccount
} from '@/common/wallet-ui.js';
import { hasToken } from '@/common/api/auth.js';
import { uploadWalletFile, resolveUploadUrl } from '@/common/api/wallet.js';
import { goLogin, goBackDelayed } from '@/common/navigation.js';

const ui = WALLET_UI;
const methods = WITHDRAW_METHODS;
const selectedMethod = ref('alipay');
const saving = ref(false);
const uploading = ref(false);
const submitStyle = { height: '44px', borderRadius: '22px' };

const accountForm = reactive({
	alipayAccount: '',
	alipayName: '',
	wechatQrCode: ''
});

function requireLogin() {
	if (hasToken()) return true;
	uni.showToast({ title: ui.loginTip, icon: 'none' });
	goLogin({ delay: 400 });
	return false;
}

function loadAccount() {
	const saved = getWithdrawAccount();
	accountForm.alipayAccount = saved.alipayAccount || '';
	accountForm.alipayName = saved.alipayName || '';
	accountForm.wechatQrCode = saved.wechatQrCode || '';
	if (saved.wechatQrCode && !saved.alipayAccount) {
		selectedMethod.value = 'wechat';
	} else if (saved.alipayAccount) {
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
				const uploadRes = await uploadWalletFile(tempPath);
				const url = resolveUploadUrl(uploadRes);
				if (!url) {
					uni.showToast({ title: ui.uploadFail, icon: 'none' });
					return;
				}
				accountForm.wechatQrCode = url;
			} catch (e) {
				uni.showToast({ title: ui.uploadFail, icon: 'none' });
			} finally {
				uploading.value = false;
				uni.hideLoading();
			}
		}
	});
}

function onSave() {
	if (saving.value) return;
	const accountInfo = {
		alipayAccount: accountForm.alipayAccount.trim(),
		alipayName: accountForm.alipayName.trim(),
		wechatQrCode: accountForm.wechatQrCode
	};
	const accountCheck = validateWithdrawAccount(selectedMethod.value, accountInfo);
	if (!accountCheck.ok) {
		uni.showToast({ title: accountCheck.message, icon: 'none' });
		return;
	}

	saving.value = true;
	try {
		saveWithdrawAccount(accountInfo);
		uni.showToast({ title: ui.accountBindSaved, icon: 'success' });
		goBackDelayed(600);
	} finally {
		saving.value = false;
	}
}

onLoad(() => {
	uni.setNavigationBarTitle({ title: ui.accountBindTitle });
});

onShow(() => {
	if (!requireLogin()) return;
	loadAccount();
});
</script>

<style lang="scss" scoped>
.wba-page {
	min-height: 100vh;
	background-color: var(--ry-page-bg);
	padding: 24rpx var(--ry-page-padding-x) calc(140rpx + env(safe-area-inset-bottom));
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.wba-section {
	padding: 28rpx;

	&__title {
		font-size: 28rpx;
		font-weight: 600;
		color: var(--ry-text-primary);
		margin-bottom: 20rpx;
		display: block;
	}

	&__tip {
		display: block;
		font-size: 24rpx;
		color: var(--ry-text-hint);
		margin-bottom: 20rpx;
		line-height: 1.5;
	}
}

.wba-methods {
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

.wba-field {
	display: flex;
	align-items: center;
	gap: 16rpx;
	padding: 24rpx 0;
	border-bottom: 1rpx solid #F5F5F5;

	&--last {
		border-bottom: none;
		padding-bottom: 0;
	}

	&__label {
		width: 160rpx;
		flex-shrink: 0;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	&__input {
		flex: 1;
		font-size: 28rpx;
		color: var(--ry-text-primary);
	}

	&__placeholder {
		color: #CCCCCC;
		font-size: 28rpx;
	}
}

.wba-qrcode {
	width: 240rpx;
	height: 240rpx;
	border-radius: var(--ry-radius-sm);
	border: 1rpx dashed #DDDDDD;
	background: #FAFAFA;
	overflow: hidden;

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
		gap: 12rpx;
	}

	&__text {
		font-size: 24rpx;
		color: var(--ry-text-hint);
	}

	&__change {
		display: block;
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #FF6600;
	}
}

.wba-tip {
	padding: 0 8rpx;
	font-size: 24rpx;
	color: var(--ry-text-hint);
	line-height: 1.5;
}

.wba-footer {
	position: fixed;
	left: 0;
	right: 0;
	bottom: 0;
	padding: 20rpx var(--ry-page-padding-x) calc(20rpx + env(safe-area-inset-bottom));
	background: #FFFFFF;
	box-shadow: 0 -4rpx 20rpx rgba(0, 0, 0, 0.04);
}
</style>

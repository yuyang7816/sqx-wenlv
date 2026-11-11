<template>
	<view class="reg-page">
		<view class="reg-orb reg-orb--one"></view>
		<view class="reg-orb reg-orb--two"></view>
		<view class="reg-head">
			<view class="reg-head__logo">
				<image class="reg-head__logo-img ry-placeholder-img" :src="brandLogo" mode="aspectFit" />
			</view>
			<text class="reg-head__title">注册账号</text>
			<text class="reg-head__sub">加入{{ productName }}，开启文旅之旅</text>
		</view>
		<view class="reg-panel">
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="phone-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号"
						border="none" color="#2B2B33" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.code" type="number" maxlength="6" placeholder="请输入验证码"
						border="none" color="#2B2B33" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
				<view class="reg-code" :class="{ 'reg-code--disabled': countDown > 0 }" @click="sendCode"><text
						class="reg-code__text">{{ codeText }}</text></view>
			</view>
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.password" :password="true" maxlength="20"
						placeholder="设置密码（6-20位）" border="none" color="#2B2B33" :clearable="false" placeholder-style="color:#B7B7C7;"
						:custom-style="inputStyle" /></view>
			</view>
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.confirm" :password="true" maxlength="20"
						placeholder="请再次输入密码" border="none" color="#2B2B33" :clearable="false" placeholder-style="color:#B7B7C7;"
						:custom-style="inputStyle" /></view>
			</view>
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="gift-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.inviteCode" maxlength="20" placeholder="邀请码（选填）"
						border="none" color="#2B2B33" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="reg-agree" @click="agree = !agree">
				<view class="reg-agree__box" :class="{ 'reg-agree__box--checked': agree }"><u-icon v-if="agree" name="checkmark"
						color="#FFFFFF" :size="14" /></view>
				<text class="reg-agree__text">我已阅读并同意<text class="reg-agree__link"
						@click.stop="onUserAgreement">《用户协议》</text>和<text class="reg-agree__link"
						@click.stop="onPrivacyPolicy">《隐私政策》</text></text>
			</view>
			<view class="reg-submit" @click="onRegister">
				<text class="reg-submit__text">完成注册</text>
				<view class="reg-submit__arrow"><u-icon name="arrow-right" color="#FFFFFF" :size="18" /></view>
			</view>
			<view class="reg-switch" @click="onGoLogin">
				<text class="reg-switch__text">已有账号？</text>
				<text class="reg-switch__link">去登录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
	import {
		ref,
		computed
	} from 'vue';
	import {
		onLoad
	} from '@dcloudio/uni-app';
	import {
		PLACEHOLDER_LOGO
	} from '@/common/theme.js';
	import {
		AGREEMENT_KEYS,
		openAgreement
	} from '@/common/agreement.js';
	import {
		sendPhoneCode,
		phoneRegister,
		saveLoginSession,
		VERIFY_BIZ
	} from '@/common/api/auth.js';
	import {
		getProductName,
		loadProductName
	} from '@/common/brand.js';
	import {
		afterLogin,
		goBack
	} from '@/common/navigation.js';
	import {
		resolveInviteCodeFromOptions
	} from '@/common/invite-ui.js';

	const productName = ref(getProductName());
	const brandLogo = PLACEHOLDER_LOGO;
	const form = ref({
		phone: '',
		code: '',
		password: '',
		confirm: '',
		inviteCode: ''
	});
	const agree = ref(false);
	const countDown = ref(0);
	const submitting = ref(false);
	const inputStyle = {
		padding: '0',
		height: '100rpx',
		fontSize: '30rpx',
		color: '#2B2B33'
	};
	const codeText = computed(() => countDown.value > 0 ? `${countDown.value}s 后重发` : '获取验证码');

	onLoad(async (options) => {
		productName.value = await loadProductName();
		if (options?.phone) form.value.phone = String(options.phone);
		const inviteCode = resolveInviteCodeFromOptions(options);
		if (inviteCode) form.value.inviteCode = inviteCode;
	});

	function onUserAgreement() {
		openAgreement(AGREEMENT_KEYS.USER);
	}

	function onPrivacyPolicy() {
		openAgreement(AGREEMENT_KEYS.PRIVACY);
	}

	function onGoLogin() {
		goBack();
	}

	function startCountDown() {
		countDown.value = 60;
		const timer = setInterval(() => {
			countDown.value -= 1;
			if (countDown.value <= 0) {
				clearInterval(timer);
				countDown.value = 0;
			}
		}, 1000);
	}
	async function sendCode() {
		if (!/^1\d{10}$/.test(form.value.phone)) {
			uni.showToast({
				title: '请输入正确手机号',
				icon: 'none'
			});
			return;
		}
		if (countDown.value > 0) return;
		try {
			const res = await sendPhoneCode(form.value.phone, VERIFY_BIZ.REGISTER);
			if (res.code !== 0) {
				uni.showToast({
					title: res.msg || '验证码发送失败',
					icon: 'none'
				});
				return;
			}
			startCountDown();
			uni.showToast({
				title: '验证码已发送',
				icon: 'none'
			});
		} catch (e) {
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		}
	}
	async function onRegister() {
		if (submitting.value) return;
		if (!agree.value) {
			uni.showToast({
				title: '请先同意协议',
				icon: 'none'
			});
			return;
		}
		if (!/^1\d{10}$/.test(form.value.phone)) {
			uni.showToast({
				title: '请输入正确手机号',
				icon: 'none'
			});
			return;
		}
		if (!/^\d{6}$/.test(form.value.code)) {
			uni.showToast({
				title: '请输入6位验证码',
				icon: 'none'
			});
			return;
		}
		if (String(form.value.password).length < 6) {
			uni.showToast({
				title: '密码至少6位',
				icon: 'none'
			});
			return;
		}
		if (String(form.value.password) !== String(form.value.confirm)) {
			uni.showToast({
				title: '两次密码不一致',
				icon: 'none'
			});
			return;
		}
		submitting.value = true;
		try {
			const payload = {
				phoneNo: form.value.phone,
				code: form.value.code,
				password: form.value.password
			};
			const inviteCode = String(form.value.inviteCode || '').trim();
			if (inviteCode) payload.superiorInvitationCode = inviteCode;
			const res = await phoneRegister(payload);
			if (res.code !== 0) {
				const msg = res.msg || '注册失败';
				if (msg.includes('已注册') || msg.includes('存在')) {
					uni.showModal({
						title: '手机号已注册',
						content: '该手机号已注册，是否前往登录？',
						confirmText: '去登录',
						confirmColor: '#FF6600',
						success: (r) => {
							if (r.confirm) goBack();
						}
					});
				} else {
					uni.showToast({
						title: msg,
						icon: 'none'
					});
				}
				return;
			}
			saveLoginSession(res.data);
			uni.showToast({
				title: '注册成功',
				icon: 'success'
			});
			setTimeout(() => afterLogin(), 400);
		} catch (e) {
			uni.showToast({
				title: '网络异常，请稍后重试',
				icon: 'none'
			});
		} finally {
			submitting.value = false;
		}
	}
</script>

<style lang="scss" scoped>
	.reg-page {
		position: relative;
		min-height: 100vh;
		padding: 0 48rpx;
		background: linear-gradient(180deg, #FFF6EE 0%, #FFF3E8 26%, #F6F8FF 26%, #F6F8FF 100%);
		overflow: hidden;
	}

	.reg-orb {
		position: absolute;
		border-radius: 50%;
		pointer-events: none;
	}

	.reg-orb--one {
		top: -140rpx;
		right: -120rpx;
		width: 400rpx;
		height: 400rpx;
		background: radial-gradient(circle at 30% 30%, rgba(255, 122, 42, 0.16), rgba(255, 122, 42, 0));
	}

	.reg-orb--two {
		top: 240rpx;
		left: -160rpx;
		width: 340rpx;
		height: 340rpx;
		background: radial-gradient(circle at 60% 60%, rgba(255, 166, 99, 0.12), rgba(255, 166, 99, 0));
	}

	/* 4� */
	.reg-head {
		position: relative;
		z-index: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 88rpx 0 48rpx;
	}

	.reg-head__logo {
		width: 112rpx;
		height: 112rpx;
		border-radius: 32rpx;
		overflow: hidden;
		background: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 16rpx 40rpx rgba(255, 102, 0, 0.16);
	}

	.reg-head__logo-img {
		width: 100%;
		height: 100%;
		border-radius: 32rpx;
	}

	.reg-head__title {
		margin-top: 28rpx;
		font-size: 44rpx;
		font-weight: 800;
		color: #2B2B33;
		letter-spacing: 2rpx;
	}

	.reg-head__sub {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #9A8B7C;
	}

	/* b */
	.reg-panel {
		position: relative;
		z-index: 2;
		padding: 36rpx;
		border-radius: 40rpx;
		background: rgba(255, 255, 255, 0.98);
		box-shadow: 0 24rpx 80rpx rgba(40, 46, 92, 0.08);
	}

	.reg-field {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 22rpx;
		height: 100rpx;
		margin-top: 20rpx;
		padding: 0 30rpx;
		border-radius: 26rpx;
		background: #F6F7FB;
	}

	.reg-field:first-child {
		margin-top: 0;
	}

	.reg-field__icon {
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: rgba(255, 102, 0, 0.08);
	}

	.reg-field__body {
		flex: 1;
		min-width: 0;
		height: 100rpx;
	}

	.reg-code {
		flex-shrink: 0;
		padding: 14rpx 22rpx;
		border-radius: 30rpx;
		background: rgba(255, 102, 0, 0.1);
	}

	.reg-code--disabled {
		background: rgba(150, 150, 160, 0.12);
	}

	.reg-code__text {
		font-size: 24rpx;
		font-weight: 500;
		color: #FF6600;
	}

	.reg-code--disabled .reg-code__text {
		color: #9B9BA9;
	}

	.reg-agree {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		gap: 14rpx;
		margin-top: 28rpx;
	}

	.reg-agree__box {
		width: 32rpx;
		height: 32rpx;
		border-radius: 10rpx;
		border: 2rpx solid #D5D5E0;
		background: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		transition: all 0.2s ease;
	}

	.reg-agree__box--checked {
		border-color: #FF6600;
		background: linear-gradient(135deg, #FF8A3D, #FF6600);
	}

	.reg-agree__text {
		font-size: 23rpx;
		color: #8C8C9C;
	}

	.reg-agree__link {
		color: #FF6600;
	}

	.reg-submit {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		gap: 12rpx;
		height: 96rpx;
		margin-top: 30rpx;
		border-radius: 48rpx;
		background: linear-gradient(135deg, #FF8A3D 0%, #FF6600 100%);
		box-shadow: 0 16rpx 40rpx rgba(255, 102, 0, 0.3);
		position: relative;
		overflow: hidden;
	}

	.reg-submit::before {
		content: '';
		position: absolute;
		top: 0;
		left: -60%;
		width: 40%;
		height: 100%;
		background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.35), transparent);
		transform: skewX(-20deg);
	}

	.reg-submit:active::before {
		left: 120%;
		transition: left 0.5s ease;
	}

	.reg-submit__text {
		font-size: 32rpx;
		font-weight: 700;
		letter-spacing: 2rpx;
		color: #FFFFFF;
	}

	.reg-submit__arrow {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.2);
	}

	.reg-switch {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		margin-top: 26rpx;
	}

	.reg-switch__text {
		font-size: 24rpx;
		color: #8C8C9C;
	}

	.reg-switch__link {
		font-size: 24rpx;
		color: #FF6600;
		font-weight: 600;
	}

	.reg-foot {
		margin-top: 28rpx;
		text-align: center;
	}

	.reg-foot__tip {
		font-size: 22rpx;
		color: #B7B7C4;
	}
</style>

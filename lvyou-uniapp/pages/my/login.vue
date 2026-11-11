<template>
	<view class="login-page">
		<view class="login-orb login-orb--one"></view>
		<view class="login-orb login-orb--two"></view>
		<view class="login-orb login-orb--three"></view>
		<view class="login-hero">
			<view class="login-tag">
				<text class="login-tag__dot"></text>
				<text class="login-tag__text">{{ ui.tagText }}</text>
			</view>
			<view class="login-logo">
				<image class="login-logo__img ry-placeholder-img" :src="brandLogo" mode="aspectFit" />
			</view>
			<view class="login-brandline">
				<view class="login-brandline__line"></view>
				<text class="login-brandline__title">{{ brandTitle }}</text>
				<view class="login-brandline__line"></view>
			</view>
			<text class="login-hero__subtitle">{{ ui.subtitle }}</text>
		</view>
		<view class="login-panel">
			<view class="login-methods">
				<view class="login-methods__item" :class="{ 'login-methods__item--active': loginMode === 'sms' }" @click="switchMode('sms')">{{ ui.tabSms }}</view>
				<view class="login-methods__item" :class="{ 'login-methods__item--active': loginMode === 'password' }" @click="switchMode('password')">{{ ui.tabPassword }}</view>
			</view>
			<template v-if="loginMode === 'sms'">
				<view class="login-field">
					<view class="login-field__icon"><u-icon name="phone-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input
							v-model="smsForm.phone"
							type="number"
							maxlength="11"
							:placeholder="ui.placeholderPhone"
							border="none"
							:clearable="false"
							:color="inputColor"
							:placeholder-style="placeholderStyle"
							:custom-style="inputStyle"
						/>
					</view>
				</view>
				<view class="login-field login-field--code">
					<view class="login-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input
							v-model="smsForm.code"
							type="number"
							maxlength="6"
							:placeholder="ui.placeholderCode"
							border="none"
							:clearable="false"
							:color="inputColor"
							:placeholder-style="placeholderStyle"
							:custom-style="inputStyle"
						/>
					</view>
					<view class="login-code" :class="{ 'login-code--disabled': countDown > 0 }" @click="sendCode">
						<text class="login-code__text">{{ codeText }}</text>
					</view>
				</view>
				<view class="login-field">
					<view class="login-field__icon"><u-icon name="gift-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input
							v-model="smsForm.inviteCode"
							maxlength="20"
							placeholder="邀请码（选填）"
							border="none"
							:clearable="false"
							:color="inputColor"
							:placeholder-style="placeholderStyle"
							:custom-style="inputStyle"
						/>
					</view>
				</view>
			</template>
			<template v-else>
				<view class="login-field">
					<view class="login-field__icon"><u-icon name="phone-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input
							v-model="pwdForm.phone"
							type="number"
							maxlength="11"
							:placeholder="ui.placeholderPhone"
							border="none"
							:clearable="false"
							:color="inputColor"
							:placeholder-style="placeholderStyle"
							:custom-style="inputStyle"
						/>
					</view>
				</view>
				<view class="login-field">
					<view class="login-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input
							v-model="pwdForm.password"
							:password="true"
							maxlength="20"
							:placeholder="ui.placeholderPassword"
							border="none"
							:clearable="false"
							:color="inputColor"
							:placeholder-style="placeholderStyle"
							:custom-style="inputStyle"
						/>
					</view>
				</view>
				<view class="login-forget" @click="onForget"><text class="login-forget__text">{{ ui.forgetPassword }}</text></view>
			</template>
			<view class="login-agree" @click="agree = !agree">
				<view class="login-agree__box" :class="{ 'login-agree__box--checked': agree }">
					<u-icon v-if="agree" name="checkmark" color="#FFFFFF" :size="14" />
				</view>
				<text class="login-agree__text">
					{{ ui.agreePrefix }}<text class="login-agree__link" @click.stop="onUserAgreement">{{ ui.userAgreement }}</text>{{ ui.and }}<text class="login-agree__link" @click.stop="onPrivacyPolicy">{{ ui.privacyPolicy }}</text>
				</text>
			</view>
			<view class="login-submit" @click="onLogin">
				<text class="login-submit__text">{{ submitText }}</text>
				<view class="login-submit__arrow"><u-icon name="arrow-right" color="#FFFFFF" :size="18" /></view>
			</view>
			<view class="login-register" @click="onRegister">
				<text class="login-register__text">{{ ui.noAccount }}</text>
				<text class="login-register__link">{{ ui.goRegister }}</text>
			</view>
			<!-- #ifndef H5 -->
			<view class="login-divider">
				<view class="login-divider__line"></view>
				<text class="login-divider__text">{{ ui.otherLogin }}</text>
				<view class="login-divider__line"></view>
			</view>
			<view class="login-wx" @click="onWxLogin">
				<view class="login-wx__icon"><u-icon name="weixin-fill" color="#2EAF6A" :size="26" /></view>
				<text class="login-wx__text">{{ ui.wxLogin }}</text>
			</view>
			<!-- #endif -->
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { LOGIN_UI } from '@/common/login-ui.js';
import { getProductName, loadProductName } from '@/common/brand.js';
import { AGREEMENT_KEYS, openAgreement } from '@/common/agreement.js';
import {
	hasToken,
	sendPhoneCode,
	phonePasswordLogin,
	phoneCodeLogin,
	saveLoginSession,
	VERIFY_BIZ
} from '@/common/api/auth.js';
import { afterLogin, openPage, buildUrl, PAGES } from '@/common/navigation.js';
import { resolveInviteCodeFromOptions } from '@/common/invite-ui.js';

const ui = LOGIN_UI;
const brandLogo = PLACEHOLDER_LOGO;
const brandTitle = ref(getProductName());
const loginMode = ref('sms');
const agree = ref(false);
const countDown = ref(0);
const submitting = ref(false);
const smsForm = ref({ phone: '', code: '', inviteCode: '' });
const pwdForm = ref({ phone: '', password: '' });
const inputColor = '#1a1a1a';
const placeholderStyle = 'color:#999999';
const inputStyle = {
	padding: '0',
	height: '100rpx',
	fontSize: '30rpx',
	color: inputColor,
	backgroundColor: 'transparent'
};

const codeText = computed(() => (countDown.value > 0 ? `${countDown.value}${ui.resendSuffix}` : ui.getCode));
const submitText = computed(() => (loginMode.value === 'password' ? ui.submitLogin : ui.submitLoginRegister));

let enteringMyPage = false;

/** 登录成功后统一进入「我的」Tab（H5 下 navigateBack 易触发页面刷新循环） */
function enterMyPage() {
	if (enteringMyPage) return;
	enteringMyPage = true;
	afterLogin();
}

onLoad(async (options) => {
	brandTitle.value = await loadProductName();
	const inviteCode = resolveInviteCodeFromOptions(options);
	if (inviteCode) smsForm.value.inviteCode = inviteCode;
	if (hasToken()) enterMyPage();
});

function onUserAgreement() { openAgreement(AGREEMENT_KEYS.USER); }
function onPrivacyPolicy() { openAgreement(AGREEMENT_KEYS.PRIVACY); }
function switchMode(mode) { loginMode.value = mode; }
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
	if (!/^1\d{10}$/.test(smsForm.value.phone)) { uni.showToast({ title: ui.toastWrongPhone, icon: 'none' }); return; }
	if (countDown.value > 0) return;
	try {
		const res = await sendPhoneCode(smsForm.value.phone, VERIFY_BIZ.LOGIN);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '验证码发送失败', icon: 'none' });
			return;
		}
		startCountDown();
		uni.showToast({ title: ui.toastCodeSent.replace(/：$/, ''), icon: 'none' });
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	}
}
function loginSuccess(title) {
	uni.showToast({ title, icon: 'success' });
	setTimeout(() => enterMyPage(), 400);
}
function onWxLogin() {
	if (!agree.value) { uni.showToast({ title: ui.toastAgree, icon: 'none' }); return; }
	uni.showToast({ title: '请使用手机号登录', icon: 'none' });
}
function onRegister() {
	if (!agree.value) { uni.showToast({ title: ui.toastAgree, icon: 'none' }); return; }
	const phone = loginMode.value === 'password' ? pwdForm.value.phone : smsForm.value.phone;
	const inviteCode = String(smsForm.value.inviteCode || '').trim();
	openPage(buildUrl(PAGES.REGISTER, { phone: phone || '', code: inviteCode || undefined }));
}
function onForget() {
	openPage(buildUrl(PAGES.FORGET, { phone: pwdForm.value.phone }));
}
async function onLogin() {
	if (submitting.value) return;
	if (!agree.value) { uni.showToast({ title: ui.toastAgree, icon: 'none' }); return; }
	if (loginMode.value === 'sms') {
		if (!/^1\d{10}$/.test(smsForm.value.phone)) { uni.showToast({ title: ui.toastWrongPhone, icon: 'none' }); return; }
		if (!/^\d{6}$/.test(smsForm.value.code)) { uni.showToast({ title: ui.toastWrongCode, icon: 'none' }); return; }
		submitting.value = true;
		try {
			const res = await phoneCodeLogin(smsForm.value.phone, smsForm.value.code, smsForm.value.inviteCode);
			if (res.code !== 0) {
				uni.showToast({ title: res.msg || '登录失败', icon: 'none' });
				return;
			}
			saveLoginSession(res.data);
			loginSuccess(ui.toastLoginSuccess);
		} catch (e) {
			uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
		} finally {
			submitting.value = false;
		}
		return;
	}
	if (!/^1\d{10}$/.test(pwdForm.value.phone)) { uni.showToast({ title: ui.toastWrongPhone, icon: 'none' }); return; }
	if (!pwdForm.value.password) { uni.showToast({ title: ui.toastNeedPassword, icon: 'none' }); return; }
	submitting.value = true;
	try {
		const res = await phonePasswordLogin(pwdForm.value.phone, pwdForm.value.password);
		if (res.code !== 0) {
			const msg = res.msg || '登录失败';
			if (msg.includes('未注册') || msg.includes('不存在')) {
				uni.showModal({
					title: ui.modalNoAccountTitle,
					content: ui.modalNoAccountContent,
					confirmText: ui.modalGoRegister,
					confirmColor: '#FF6600',
					success: (r) => { if (r.confirm) openPage(buildUrl(PAGES.REGISTER, { phone: pwdForm.value.phone })); }
				});
			} else {
				uni.showToast({ title: msg, icon: 'none' });
			}
			return;
		}
		saveLoginSession(res.data);
		loginSuccess(ui.toastLoginSuccess);
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		submitting.value = false;
	}
}
</script>

<style lang="scss" scoped>
	.login-page {
		position: relative;
		min-height: 100vh;
		padding: 0 48rpx;
		background: linear-gradient(180deg, #FFF6EE 0%, #FFF3E8 32%, #F6F8FF 32%, #F6F8FF 100%);
		overflow: hidden;
	}

	.login-orb {
		position: absolute;
		border-radius: 50%;
		pointer-events: none;
	}

	.login-orb--one {
		top: -140rpx;
		right: -120rpx;
		width: 420rpx;
		height: 420rpx;
		background: radial-gradient(circle at 30% 30%, rgba(255, 122, 42, 0.16), rgba(255, 122, 42, 0));
	}

	.login-orb--two {
		top: 260rpx;
		left: -160rpx;
		width: 360rpx;
		height: 360rpx;
		background: radial-gradient(circle at 60% 60%, rgba(255, 166, 99, 0.12), rgba(255, 166, 99, 0));
	}

	.login-orb--three {
		top: 60rpx;
		left: 18%;
		width: 8rpx;
		height: 8rpx;
		background: #FFB27D;
		opacity: 0.7;
		box-shadow: 60rpx 90rpx 0 #FFC7A3, 320rpx 40rpx 0 #FFB27D, 380rpx 180rpx 0 #FFD3B5;
	}

	.login-hero {
		position: relative;
		z-index: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 40rpx 0 56rpx;
	}

	.login-tag {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 10rpx;
		padding: 10rpx 26rpx;
		border-radius: 30rpx;
		background: rgba(255, 255, 255, 0.85);
		border: 1rpx solid rgba(255, 102, 0, 0.18);
		box-shadow: 0 8rpx 20rpx rgba(255, 122, 42, 0.08);
	}

	.login-tag__dot {
		width: 12rpx;
		height: 12rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #FFB066, #FF6600);
	}

	.login-tag__text {
		font-size: 22rpx;
		letter-spacing: 2rpx;
		color: #A85A26;
		font-weight: 500;
	}

	.login-logo {
		margin-top: 36rpx;
		position: relative;
		width: 128rpx;
		height: 128rpx;
		border-radius: 36rpx;
		overflow: hidden;
		background: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 18rpx 46rpx rgba(255, 102, 0, 0.18), inset 0 0 0 2rpx rgba(255, 255, 255, 0.9);
	}

	.login-logo::after {
		content: '';
		position: absolute;
		inset: -18rpx;
		border-radius: 52rpx;
		background: linear-gradient(135deg, rgba(255, 138, 61, 0.25), rgba(255, 102, 0, 0));
		z-index: -1;
	}

	.login-logo__img {
		width: 100%;
		height: 100%;
		border-radius: 36rpx;
	}

	.login-brandline {
		margin-top: 32rpx;
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 24rpx;
	}

	.login-brandline__line {
		width: 56rpx;
		height: 2rpx;
		background: linear-gradient(90deg, transparent, rgba(255, 102, 0, 0.45));
	}

	.login-brandline__line:last-child {
		background: linear-gradient(90deg, rgba(255, 102, 0, 0.45), transparent);
	}

	.login-brandline__title {
		font-size: 52rpx;
		font-weight: 800;
		letter-spacing: 6rpx;
		color: #2B2B33;
	}

	.login-hero__subtitle {
		margin-top: 14rpx;
		font-size: 26rpx;
		letter-spacing: 1rpx;
		color: #9A8B7C;
	}

	.login-panel {
		position: relative;
		z-index: 2;
		padding: 36rpx 36rpx 40rpx;
		border-radius: 40rpx;
		background: rgba(255, 255, 255, 0.98);
		box-shadow: 0 24rpx 80rpx rgba(40, 46, 92, 0.08);
	}

	.login-methods {
		display: flex;
		flex-direction: row;
		padding: 6rpx;
		border-radius: 24rpx;
		background: #F3F4F9;
	}

	.login-methods__item {
		flex: 1;
		text-align: center;
		padding: 18rpx 0;
		font-size: 27rpx;
		color: #8A8A99;
		border-radius: 20rpx;
		transition: all 0.2s ease;
	}

	.login-methods__item--active {
		background: #FFFFFF;
		color: #FF6600;
		font-weight: 600;
		box-shadow: 0 6rpx 18rpx rgba(40, 46, 92, 0.08);
	}

	.login-field {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 22rpx;
		height: 100rpx;
		margin-top: 28rpx;
		padding: 0 30rpx;
		border-radius: 26rpx;
		background: #F6F7FB;
		border: 2rpx solid transparent;
	}

	.login-field__icon {
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: rgba(255, 102, 0, 0.08);
	}

	.login-field__body {
		flex: 1;
		min-width: 0;
		height: 100rpx;

		:deep(.u-input) {
			background-color: transparent !important;
		}

		:deep(.u-input__content) {
			background-color: transparent !important;
		}

		:deep(.u-input__content__field-wrapper__field) {
			color: #1a1a1a !important;
		}
	}

	.login-field--code .login-field__icon {
		background: rgba(41, 121, 255, 0.08);
	}

	.login-code {
		flex-shrink: 0;
		padding: 14rpx 22rpx;
		border-radius: 30rpx;
		background: rgba(255, 102, 0, 0.1);
	}

	.login-code--disabled {
		background: rgba(150, 150, 160, 0.12);
	}

	.login-code__text {
		font-size: 24rpx;
		font-weight: 500;
		color: #FF6600;
	}

	.login-code--disabled .login-code__text {
		color: #9B9BA9;
	}

	.login-forget {
		display: flex;
		justify-content: flex-end;
		margin-top: 18rpx;
		padding-right: 8rpx;
	}

	.login-forget__text {
		font-size: 24rpx;
		color: #8C8C9C;
	}

	.login-agree {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		gap: 14rpx;
		margin-top: 26rpx;
	}

	.login-agree__box {
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

	.login-agree__box--checked {
		border-color: #FF6600;
		background: linear-gradient(135deg, #FF8A3D, #FF6600);
	}

	.login-agree__text {
		font-size: 23rpx;
		color: #8C8C9C;
	}

	.login-agree__link {
		color: #FF6600;
	}

	.login-submit {
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

	.login-submit::before {
		content: '';
		position: absolute;
		top: 0;
		left: -60%;
		width: 40%;
		height: 100%;
		background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.35), transparent);
		transform: skewX(-20deg);
		transition: left 0.5s ease;
	}

	.login-submit:active::before {
		left: 120%;
	}

	.login-submit__text {
		font-size: 32rpx;
		font-weight: 700;
		letter-spacing: 2rpx;
		color: #FFFFFF;
	}

	.login-submit__arrow {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.2);
	}

	.login-register {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		margin-top: 26rpx;
	}

	.login-register__text {
		font-size: 24rpx;
		color: #8C8C9C;
	}

	.login-register__link {
		font-size: 24rpx;
		color: #FF6600;
		font-weight: 600;
	}

	.login-divider {
		display: flex;
		flex-direction: row;
		align-items: center;
		gap: 24rpx;
		margin-top: 32rpx;
	}

	.login-divider__line {
		flex: 1;
		height: 1rpx;
		background: #ECECF2;
	}

	.login-divider__text {
		font-size: 22rpx;
		color: #B7B7C4;
	}

	.login-wx {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		gap: 14rpx;
		height: 88rpx;
		margin-top: 24rpx;
		border-radius: 44rpx;
		border: 2rpx solid #E2F2E8;
		background: #FAFEFB;
	}

	.login-wx__icon {
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.login-wx__text {
		font-size: 28rpx;
		font-weight: 600;
		color: #2EAF6A;
	}

	.login-foot {
		display: flex;
		flex-direction: column;
		gap: 6rpx;
		margin-top: 28rpx;
		padding-bottom: 32rpx;
		text-align: center;
	}

	.login-foot__tip {
		display: block;
		font-size: 22rpx;
		color: #B7B7C4;
		line-height: 1.5;
	}
</style>

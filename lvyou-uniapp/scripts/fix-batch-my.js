const { patchPage } = require('./fix-batch-shared');

const inputStyle = `{
		padding: '0',
		height: '100rpx',
		fontSize: '30rpx',
		color: '#2B2B33'
	}`;

// login
patchPage('pages/my/login.vue', `<template>
	<view class="login-page">
		<view class="login-back" @click="onBack">
			<u-icon name="arrow-left" color="#5A4A3A" :size="22" />
		</view>
		<view class="login-orb login-orb--one"></view>
		<view class="login-orb login-orb--two"></view>
		<view class="login-orb login-orb--three"></view>
		<view class="login-hero">
			<view class="login-tag">
				<text class="login-tag__dot"></text>
				<text class="login-tag__text">游 · 荣 · 昌</text>
			</view>
			<view class="login-logo">
				<image class="login-logo__img" :src="PLACEHOLDER_LOGO" mode="aspectFit" />
			</view>
			<view class="login-brandline">
				<view class="login-brandline__line"></view>
				<text class="login-brandline__title">荣e游</text>
				<view class="login-brandline__line"></view>
			</view>
			<text class="login-hero__subtitle">一部手机游荣昌，吃得地道玩得明白</text>
		</view>
		<view class="login-panel">
			<view class="login-methods">
				<view class="login-methods__item" :class="{ 'login-methods__item--active': loginMode === 'sms' }" @click="switchMode('sms')">验证码登录</view>
				<view class="login-methods__item" :class="{ 'login-methods__item--active': loginMode === 'password' }" @click="switchMode('password')">密码登录</view>
			</view>
			<template v-if="loginMode === 'sms'">
				<view class="login-field">
					<view class="login-field__icon"><u-icon name="phone-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input v-model="smsForm.phone" type="number" maxlength="11" placeholder="请输入手机号" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" />
					</view>
				</view>
				<view class="login-field login-field--code">
					<view class="login-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input v-model="smsForm.code" type="number" maxlength="6" placeholder="请输入验证码" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" />
					</view>
					<view class="login-code" :class="{ 'login-code--disabled': countDown > 0 }" @click="sendCode">
						<text class="login-code__text">{{ codeText }}</text>
					</view>
				</view>
			</template>
			<template v-else>
				<view class="login-field">
					<view class="login-field__icon"><u-icon name="phone-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input v-model="pwdForm.phone" type="number" maxlength="11" placeholder="请输入手机号" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" />
					</view>
				</view>
				<view class="login-field">
					<view class="login-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
					<view class="login-field__body">
						<u-input v-model="pwdForm.password" :password="true" maxlength="20" placeholder="请输入登录密码" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" />
					</view>
				</view>
				<view class="login-forget" @click="onForget"><text class="login-forget__text">忘记密码？</text></view>
			</template>
			<view class="login-agree" @click="agree = !agree">
				<view class="login-agree__box" :class="{ 'login-agree__box--checked': agree }">
					<u-icon v-if="agree" name="checkmark" color="#FFFFFF" :size="14" />
				</view>
				<text class="login-agree__text">我已阅读并同意<text class="login-agree__link">《用户协议》</text>和<text class="login-agree__link">《隐私政策》</text></text>
			</view>
			<view class="login-submit" @click="onLogin">
				<text class="login-submit__text">{{ submitText }}</text>
				<view class="login-submit__arrow"><u-icon name="arrow-right" color="#FFFFFF" :size="18" /></view>
			</view>
			<view class="login-register" @click="onRegister">
				<text class="login-register__text">还没有账号？</text>
				<text class="login-register__link">去注册</text>
			</view>
			<view class="login-divider">
				<view class="login-divider__line"></view>
				<text class="login-divider__text">其他登录方式</text>
				<view class="login-divider__line"></view>
			</view>
			<view class="login-wx" @click="onWxLogin">
				<view class="login-wx__icon"><u-icon name="weixin-fill" color="#2EAF6A" :size="26" /></view>
				<text class="login-wx__text">微信登录</text>
			</view>
		</view>
		<view class="login-foot"><text class="login-foot__tip">演示 Mock 登录，验证码固定 123456</text></view>
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onShow } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { setLoggedIn, isLoggedIn, findAccount } from '@/common/destination-mock.js';

const loginMode = ref('sms');
const agree = ref(false);
const countDown = ref(0);
const smsForm = ref({ phone: '', code: '' });
const pwdForm = ref({ phone: '', password: '' });
const inputStyle = ${inputStyle};

const codeText = computed(() => countDown.value > 0 ? \`\${countDown.value}s 后重发\` : '获取验证码');
const submitText = computed(() => loginMode.value === 'password' ? '登录' : '登录 / 注册');

onShow(() => { if (isLoggedIn()) uni.navigateBack(); });
function switchMode(mode) { loginMode.value = mode; }
function onBack() { uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/my/my' }) }); }
function sendCode() {
	if (!/^1\\d{10}$/.test(smsForm.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	if (countDown.value > 0) return;
	smsForm.value.code = '123456';
	countDown.value = 60;
	const timer = setInterval(() => { countDown.value -= 1; if (countDown.value <= 0) { clearInterval(timer); countDown.value = 0; } }, 1000);
	uni.showToast({ title: '验证码已发送：123456', icon: 'none' });
}
function loginSuccess(title) { setLoggedIn(true); uni.showToast({ title, icon: 'success' }); setTimeout(() => uni.navigateBack(), 400); }
function onWxLogin() {
	if (!agree.value) { uni.showToast({ title: '请先同意协议', icon: 'none' }); return; }
	loginSuccess('微信登录成功');
}
function onRegister() {
	if (!agree.value) { uni.showToast({ title: '请先同意协议', icon: 'none' }); return; }
	uni.navigateTo({ url: '/pages/my/register?phone=' + (loginMode.value === 'password' ? pwdForm.value.phone : smsForm.value.phone) });
}
function onForget() { uni.navigateTo({ url: '/pages/my/forget?phone=' + pwdForm.value.phone }); }
function onLogin() {
	if (!agree.value) { uni.showToast({ title: '请先同意协议', icon: 'none' }); return; }
	if (loginMode.value === 'sms') {
		if (!/^1\\d{10}$/.test(smsForm.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
		if (!/^\\d{6}$/.test(smsForm.value.code)) { uni.showToast({ title: '请输入6位验证码', icon: 'none' }); return; }
		loginSuccess('登录成功');
		return;
	}
	if (!/^1\\d{10}$/.test(pwdForm.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	if (!pwdForm.value.password) { uni.showToast({ title: '请输入登录密码', icon: 'none' }); return; }
	const account = findAccount(pwdForm.value.phone);
	if (!account) {
		uni.showModal({ title: '账号不存在', content: '该手机号尚未注册，是否前往注册？', confirmText: '去注册', confirmColor: '#FF6600', success: (res) => { if (res.confirm) uni.navigateTo({ url: \`/pages/my/register?phone=\${pwdForm.value.phone}\` }); } });
		return;
	}
	if (String(account.password) !== String(pwdForm.value.password)) { uni.showToast({ title: '密码错误，请重试', icon: 'none' }); return; }
	loginSuccess('登录成功');
}
</script>`);

// register - similar structure, read styles preserved
patchPage('pages/my/register.vue', `<template>
	<view class="reg-page">
		<view class="reg-back" @click="onBack"><u-icon name="arrow-left" color="#5A4A3A" :size="22" /></view>
		<view class="reg-orb reg-orb--one"></view>
		<view class="reg-orb reg-orb--two"></view>
		<view class="reg-head">
			<view class="reg-head__logo"><image class="reg-head__img" :src="PLACEHOLDER_LOGO" mode="aspectFit" /></view>
			<text class="reg-head__title">注册账号</text>
			<text class="reg-head__sub">加入荣e游，开启荣昌之旅</text>
		</view>
		<view class="reg-panel">
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="phone-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.phone" type="number" maxlength="11" placeholder="请输入手机号" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.code" type="number" maxlength="6" placeholder="请输入验证码" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
				<view class="reg-code" :class="{ 'reg-code--disabled': countDown > 0 }" @click="sendCode"><text class="reg-code__text">{{ codeText }}</text></view>
			</view>
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.password" :password="true" maxlength="20" placeholder="设置密码（6-20位）" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="reg-field">
				<view class="reg-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="reg-field__body"><u-input v-model="form.confirm" :password="true" maxlength="20" placeholder="请再次输入密码" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="reg-agree" @click="agree = !agree">
				<view class="reg-agree__box" :class="{ 'reg-agree__box--checked': agree }"><u-icon v-if="agree" name="checkmark" color="#FFFFFF" :size="14" /></view>
				<text class="reg-agree__text">我已阅读并同意<text class="reg-agree__link">《用户协议》</text>和<text class="reg-agree__link">《隐私政策》</text></text>
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
		<view class="reg-foot"><text class="reg-foot__tip">演示 Mock 注册，验证码固定 123456</text></view>
	</view>
</template>`, `<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { PLACEHOLDER_LOGO } from '@/common/theme.js';
import { findAccount, saveAccount, setLoggedIn } from '@/common/destination-mock.js';

const form = ref({ phone: '', code: '', password: '', confirm: '' });
const agree = ref(false);
const countDown = ref(0);
const inputStyle = ${inputStyle};
const codeText = computed(() => countDown.value > 0 ? \`\${countDown.value}s 后重发\` : '获取验证码');

onLoad((options) => { if (options?.phone) form.value.phone = String(options.phone); });
function onBack() { uni.navigateBack({ fail: () => uni.switchTab({ url: '/pages/my/my' }) }); }
function onGoLogin() { uni.navigateBack(); }
function sendCode() {
	if (!/^1\\d{10}$/.test(form.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	if (countDown.value > 0) return;
	form.value.code = '123456';
	countDown.value = 60;
	const timer = setInterval(() => { countDown.value -= 1; if (countDown.value <= 0) { clearInterval(timer); countDown.value = 0; } }, 1000);
	uni.showToast({ title: '验证码已发送：123456', icon: 'none' });
}
function onRegister() {
	if (!agree.value) { uni.showToast({ title: '请先同意协议', icon: 'none' }); return; }
	if (!/^1\\d{10}$/.test(form.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	if (!/^\\d{6}$/.test(form.value.code)) { uni.showToast({ title: '请输入6位验证码', icon: 'none' }); return; }
	if (String(form.value.password).length < 6) { uni.showToast({ title: '密码至少6位', icon: 'none' }); return; }
	if (String(form.value.password) !== String(form.value.confirm)) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return; }
	if (findAccount(form.value.phone)) {
		uni.showModal({ title: '手机号已注册', content: '该手机号已注册，是否前往登录？', confirmText: '去登录', confirmColor: '#FF6600', success: (res) => { if (res.confirm) uni.navigateBack(); } });
		return;
	}
	saveAccount({ phone: form.value.phone, password: form.value.password });
	setLoggedIn(true);
	uni.showToast({ title: '注册成功', icon: 'success' });
	setTimeout(() => uni.navigateBack(), 400);
}
</script>`);

console.log('fix-batch-my: login, register done - run part2 for forget/profile/asset/settings/service');

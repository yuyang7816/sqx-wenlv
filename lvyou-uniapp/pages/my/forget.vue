<template>
	<view class="forget-page">
		<view class="forget-orb forget-orb--one"></view>
		<view class="forget-orb forget-orb--two"></view>
		<view class="forget-head">
			<view class="forget-head__icon"><u-icon name="lock-open" color="#FF6600" :size="40" /></view>
			<text class="forget-head__title">找回密码</text>
			<text class="forget-head__sub">验证手机号后设置新密码</text>
		</view>
		<view class="forget-panel">
			<view class="forget-step">
				<view class="forget-step__item forget-step__item--active"><text class="forget-step__num">1</text><text class="forget-step__label">验证手机</text></view>
				<view class="forget-step__line"></view>
				<view class="forget-step__item forget-step__item--active"><text class="forget-step__num">2</text><text class="forget-step__label">重置密码</text></view>
			</view>
			<view class="forget-field">
				<view class="forget-field__icon"><u-icon name="phone-fill" color="#FF6600" :size="22" /></view>
				<view class="forget-field__body"><u-input v-model="form.phone" type="number" maxlength="11" placeholder="请输入注册手机号" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="forget-field">
				<view class="forget-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="forget-field__body"><u-input v-model="form.code" type="number" maxlength="6" placeholder="请输入验证码" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
				<view class="forget-code" :class="{ 'forget-code--disabled': countDown > 0 }" @click="sendCode"><text class="forget-code__text">{{ codeText }}</text></view>
			</view>
			<view class="forget-field">
				<view class="forget-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="forget-field__body"><u-input v-model="form.password" :password="true" maxlength="20" placeholder="新密码（6-20位）" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="forget-field">
				<view class="forget-field__icon"><u-icon name="lock-fill" color="#FF6600" :size="22" /></view>
				<view class="forget-field__body"><u-input v-model="form.confirm" :password="true" maxlength="20" placeholder="请再次输入密码" border="none" :clearable="false" placeholder-style="color:#B7B7C7;" :custom-style="inputStyle" /></view>
			</view>
			<view class="forget-submit" @click="onSubmit">
				<text class="forget-submit__text">确认重置</text>
				<view class="forget-submit__arrow"><u-icon name="arrow-right" color="#FFFFFF" :size="18" /></view>
			</view>
			<view class="forget-back2login" @click="onBack">
				<text class="forget-back2login__text">想起密码了？</text>
				<text class="forget-back2login__link">返回登录</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed } from 'vue';
import { onLoad } from '@dcloudio/uni-app';
import { sendPhoneCode, resetPassword, VERIFY_BIZ } from '@/common/api/auth.js';
import { goBack, goBackDelayed } from '@/common/navigation.js';

const form = ref({ phone: '', code: '', password: '', confirm: '' });
const countDown = ref(0);
const submitting = ref(false);
const inputStyle = { padding: '0', height: '100rpx', fontSize: '30rpx', color: '#2B2B33' };
const codeText = computed(() => countDown.value > 0 ? `${countDown.value}s 后重发` : '获取验证码');

onLoad((options) => { if (options?.phone) form.value.phone = String(options.phone); });
function onBack() { goBack(); }
function startCountDown() {
	countDown.value = 60;
	const timer = setInterval(() => { countDown.value -= 1; if (countDown.value <= 0) { clearInterval(timer); countDown.value = 0; } }, 1000);
}
async function sendCode() {
	if (!/^1\d{10}$/.test(form.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	if (countDown.value > 0) return;
	try {
		const res = await sendPhoneCode(form.value.phone, VERIFY_BIZ.FORGET);
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '验证码发送失败', icon: 'none' });
			return;
		}
		startCountDown();
		uni.showToast({ title: '验证码已发送', icon: 'none' });
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	}
}
async function onSubmit() {
	if (submitting.value) return;
	if (!/^1\d{10}$/.test(form.value.phone)) { uni.showToast({ title: '请输入正确手机号', icon: 'none' }); return; }
	if (!/^\d{6}$/.test(form.value.code)) { uni.showToast({ title: '请输入6位验证码', icon: 'none' }); return; }
	if (String(form.value.password).length < 6) { uni.showToast({ title: '密码至少6位', icon: 'none' }); return; }
	if (String(form.value.password) !== String(form.value.confirm)) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return; }
	submitting.value = true;
	try {
		const res = await resetPassword({
			phone: form.value.phone,
			code: form.value.code,
			password: form.value.password
		});
		if (res.code !== 0) {
			uni.showToast({ title: res.msg || '重置失败', icon: 'none' });
			return;
		}
		uni.showToast({ title: '密码重置成功', icon: 'success' });
		goBackDelayed(400);
	} catch (e) {
		uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' });
	} finally {
		submitting.value = false;
	}
}
</script>

<style lang="scss" scoped>
	.forget-page {
		position: relative;
		min-height: 100vh;
		padding: 0 48rpx;
		background: linear-gradient(180deg, #FFF6EE 0%, #FFF3E8 26%, #F6F8FF 26%, #F6F8FF 100%);
		overflow: hidden;
	}

	.forget-orb {
		position: absolute;
		border-radius: 50%;
		pointer-events: none;
	}

	.forget-orb--one {
		top: -140rpx;
		right: -120rpx;
		width: 400rpx;
		height: 400rpx;
		background: radial-gradient(circle at 30% 30%, rgba(255, 122, 42, 0.16), rgba(255, 122, 42, 0));
	}

	.forget-orb--two {
		top: 240rpx;
		left: -160rpx;
		width: 340rpx;
		height: 340rpx;
		background: radial-gradient(circle at 60% 60%, rgba(255, 166, 99, 0.12), rgba(255, 166, 99, 0));
	}

	.forget-head {
		position: relative;
		z-index: 1;
		display: flex;
		flex-direction: column;
		align-items: center;
		padding: 96rpx 0 44rpx;
	}

	.forget-head__icon {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		background: #FFFFFF;
		display: flex;
		align-items: center;
		justify-content: center;
		box-shadow: 0 16rpx 40rpx rgba(255, 102, 0, 0.16);
	}

	.forget-head__title {
		margin-top: 30rpx;
		font-size: 44rpx;
		font-weight: 800;
		color: #2B2B33;
		letter-spacing: 2rpx;
	}

	.forget-head__sub {
		margin-top: 12rpx;
		font-size: 24rpx;
		color: #9A8B7C;
	}

	.forget-panel {
		position: relative;
		z-index: 2;
		padding: 32rpx 36rpx 36rpx;
		border-radius: 40rpx;
		background: rgba(255, 255, 255, 0.98);
		box-shadow: 0 24rpx 80rpx rgba(40, 46, 92, 0.08);
	}

	.forget-step {
		display: flex;
		flex-direction: row;
		align-items: center;
		margin-bottom: 8rpx;
	}

	.forget-step__item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 10rpx;
	}

	.forget-step__num {
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		background: rgba(255, 102, 0, 0.12);
		color: #FF6600;
		font-size: 24rpx;
		font-weight: 700;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.forget-step__label {
		font-size: 20rpx;
		color: #FF6600;
	}

	.forget-step__line {
		flex: 1;
		height: 2rpx;
		margin: 0 20rpx 30rpx;
		background: linear-gradient(90deg, rgba(255, 102, 0, 0.4), rgba(255, 102, 0, 0.1));
	}

	.forget-field {
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

	.forget-field__icon {
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
		width: 56rpx;
		height: 56rpx;
		border-radius: 18rpx;
		background: rgba(255, 102, 0, 0.1);
	}

	.forget-field__body {
		flex: 1;
		min-width: 0;
		height: 100rpx;
	}

	.forget-code {
		flex-shrink: 0;
		padding: 14rpx 22rpx;
		border-radius: 30rpx;
		background: rgba(255, 102, 0, 0.1);
	}

	.forget-code--disabled {
		background: rgba(150, 150, 160, 0.12);
	}

	.forget-code__text {
		font-size: 24rpx;
		font-weight: 500;
		color: #FF6600;
	}

	.forget-code--disabled .forget-code__text {
		color: #9B9BA9;
	}

	.forget-submit {
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

	.forget-submit::before {
		content: '';
		position: absolute;
		top: 0;
		left: -60%;
		width: 40%;
		height: 100%;
		background: linear-gradient(100deg, transparent, rgba(255, 255, 255, 0.3), transparent);
		transform: skewX(-20deg);
	}

	.forget-submit:active::before {
		left: 120%;
		transition: left 0.5s ease;
	}

	.forget-submit__text {
		font-size: 32rpx;
		font-weight: 700;
		letter-spacing: 2rpx;
		color: #FFFFFF;
	}

	.forget-submit__arrow {
		display: flex;
		align-items: center;
		justify-content: center;
		width: 44rpx;
		height: 44rpx;
		border-radius: 50%;
		background: rgba(255, 255, 255, 0.2);
	}

	.forget-back2login {
		display: flex;
		flex-direction: row;
		align-items: center;
		justify-content: center;
		gap: 6rpx;
		margin-top: 26rpx;
	}

	.forget-back2login__text {
		font-size: 24rpx;
		color: #8C8C9C;
	}

	.forget-back2login__link {
		font-size: 24rpx;
		color: #FF6600;
		font-weight: 600;
	}

	.forget-foot {
		margin-top: 28rpx;
		text-align: center;
	}

	.forget-foot__tip {
		font-size: 22rpx;
		color: #B7B7C4;
	}
</style>

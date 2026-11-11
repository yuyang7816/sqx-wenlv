/**
 * 用户端鉴权 API（路径对齐 AppAuthController / VerifyCodeController）
 */
import { get, postJson } from './request.js';

const AUTH_PREFIX = '/app/base/auth';
const VERIFY_PREFIX = '/app/base/verifyCode';

/** 短信模板业务码（对齐管理端短信模板 bizCode） */
export const VERIFY_BIZ = {
	LOGIN: 'login',
	REGISTER: 'register',
	FORGET: 'forget'
};

/** 发送手机验证码 */
export function sendPhoneCode(phone, bizCode) {
	return postJson(`${VERIFY_PREFIX}/sendPhoneCode`, { phone, bizCode });
}

/** 手机号 + 密码登录 */
export function phonePasswordLogin(phoneNo, password) {
	return postJson(`${AUTH_PREFIX}/phonePasswordLogin`, { phoneNo, password });
}

/** 手机号 + 验证码登录（未注册会自动注册） */
export function phoneCodeLogin(phoneNo, verifyCode, superiorInvitationCode) {
	const payload = { phoneNo, verifyCode };
	const code = String(superiorInvitationCode || '').trim();
	if (code) payload.superiorInvitationCode = code;
	return postJson(`${AUTH_PREFIX}/phoneCodeLogin`, payload);
}

/** 手机号注册 */
export function phoneRegister(payload) {
	return postJson(`${AUTH_PREFIX}/phoneRegister`, payload);
}

/** 忘记密码 / 重置密码 */
export function resetPassword(payload) {
	return postJson(`${AUTH_PREFIX}/resetPassword`, payload);
}

/** 当前登录用户信息（需 App JWT） */
export function getUserInfo() {
	return get('/app/base/user/info');
}

/** 登录成功后持久化 token 与用户摘要 */
export function saveLoginSession(loginData) {
	if (!loginData || !loginData.token) return;
	uni.setStorageSync('token', loginData.token);
	const user = loginData.user || {};
	const userId = user.userId ?? user.id;
	if (userId != null) uni.setStorageSync('userId', userId);
	if (user.phone) uni.setStorageSync('phone', user.phone);
	if (user.maOpenId || user.openId) uni.setStorageSync('openId', user.maOpenId || user.openId);
	try {
		uni.setStorageSync('ry_login_state', true);
	} catch (e) { /* ignore */ }
}

/** 是否已有 App token */
export function hasToken() {
	try {
		return Boolean(uni.getStorageSync('token'));
	} catch (e) {
		return false;
	}
}

/** 清除本地登录态 */
export function clearLoginSession() {
	uni.removeStorageSync('token');
	uni.removeStorageSync('userId');
	uni.removeStorageSync('phone');
	uni.removeStorageSync('openId');
	try {
		uni.removeStorageSync('ry_login_state');
	} catch (e) { /* ignore */ }
}

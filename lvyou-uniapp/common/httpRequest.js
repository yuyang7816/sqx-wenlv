import config from './config';
import { goLogin, PAGES } from './navigation.js';

// 提取重复的401处理逻辑
const handle401 = () => {
	uni.removeStorageSync("token");
	uni.removeStorageSync("userId");
	uni.removeStorageSync("phone");
	uni.removeStorageSync("openId");
	try { uni.removeStorageSync("ry_login_state"); } catch (e) { /* ignore */ }
	uni.showToast({
		title: '用户信息失效，请重新登录！',
		icon: 'none'
	});
	const pages = typeof getCurrentPages === 'function' ? getCurrentPages() : [];
	const cur = pages.length ? pages[pages.length - 1] : null;
	const route = cur && (cur.route || cur.$page && cur.$page.fullPath) || '';
	if (route && !String(route).includes(PAGES.LOGIN.replace(/^\//, ''))) {
		goLogin({ delay: 300 });
	}
};

// 提取通用的请求逻辑
const sendRequest = (url, data, method, header, hasToken = true) => {
	return new Promise((succ, error) => {
		const token = hasToken ? uni.getStorageSync("token") : null;
		uni.request({
			url,
			data,
			method,
			header: {
				"content-type": header,
				clientType: '1',
				...(hasToken && {
					token
				})
			},
			success: (result) => {
				if (result.data.code === 401 && token) {
					handle401();
				}
				succ(result.data);
			},
			fail: (e) => {
				error(e);
			}
		});
	});
};
// POST请求
export const post = (url, data, header = "application/x-www-form-urlencoded") => {
	const apiHost = config.APIHOST;
	const fullUrl = apiHost + url;
	return sendRequest(fullUrl, data, "POST", header);
};

// POSTT请求
export const postT = (url, data, header = "application/x-www-form-urlencoded") => {
	const apiHost1 = config.APIHOST;
	const fullUrl = apiHost1 + url;
	return sendRequest(fullUrl, data, "POST", header);
};

// POSTJson请求
export const postJson = (url, data, header = "application/json") => {
	const apiHost1 = config.APIHOST;
	const fullUrl = apiHost1 + url;
	return sendRequest(fullUrl, data, "POST", header);
};

// GETT请求
export const getT = (url, data, header = "application/x-www-form-urlencoded") => {
	const apiHost1 = config.APIHOST;
	const fullUrl = apiHost1 + url;
	return sendRequest(fullUrl, data, "GET", header);
};

// GET请求
export const get = (url, data, header = "application/x-www-form-urlencoded") => {
	const apiHost = config.APIHOST;
	const fullUrl = apiHost + url;
	return sendRequest(fullUrl, data, "GET", header);
};
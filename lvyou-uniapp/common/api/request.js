/**
 * 统一 HTTP 请求封装
 * 复用 httpRequest.js 的 token 注入与 401 登录态清理逻辑
 */
import { get as httpGet, post as httpPost, postJson as httpPostJson } from '../httpRequest.js';

/** GET 请求，url 为相对 APIHOST 的路径，如 /app/base/banner/page */
export function get(url, data, header) {
	return httpGet(url, data, header);
}

/** POST 表单请求（application/x-www-form-urlencoded） */
export function post(url, data, header) {
	return httpPost(url, data, header);
}

/** POST JSON 请求（application/json） */
export function postJson(url, data, header) {
	return httpPostJson(url, data, header);
}

export default {
	get,
	post,
	postJson
};

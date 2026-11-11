/**
 * 在线客服 IM API（路径对齐 AppKefuImController）
 */
import { postJson } from './request.js';

const PREFIX = '/app/kefu-im';

/** 打开或获取当前会话 */
export function openKefuImSession() {
	return postJson(`${PREFIX}/session/open`, {});
}

/** 发送消息 */
export function sendKefuImMessage(content) {
	return postJson(`${PREFIX}/message/send`, { content });
}

/** 拉取消息列表（sinceId 用于轮询新消息） */
export function fetchKefuImMessages(params = {}) {
	return postJson(`${PREFIX}/message/list`, params);
}

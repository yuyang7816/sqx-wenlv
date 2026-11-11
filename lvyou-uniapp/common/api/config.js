/**
 * 系统配置 API（路径对齐 AppConfigController / AppAgreementController）
 */
import { get } from './request.js';

/** 聚合配置（客服/邀请，公开接口） */
export function fetchConfigInfo() {
	return get('/app/config/info');
}

/** 协议正文 HTML（公开接口，key: user / privacy / about） */
export function fetchAgreement(key) {
	return get(`/app/agreement/${encodeURIComponent(key)}`);
}

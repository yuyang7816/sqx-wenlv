/**
 * 服务预约 API（路径对齐 AppServiceRecordController）
 */
import { postJson } from './request.js';

const PREFIX = '/app/service-record';

/** 提交服务预约（需 App JWT） */
export function submitServiceRecord(payload) {
	return postJson(`${PREFIX}/submit`, payload);
}

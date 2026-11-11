/**
 * 帮助中心 API（路径对齐 AppHelpController）
 */
import { get } from './request.js';

/** FAQ 分类问答列表（公开接口，对齐 GET /app/help/faq） */
export function fetchHelpFaq() {
	return get('/app/help/faq');
}

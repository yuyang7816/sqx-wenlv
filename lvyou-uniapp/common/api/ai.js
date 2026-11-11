/**
 * AI 问答 API（路径对齐 AppAiController POST /app/ai/chat）
 */
import { postJson } from './request.js';

/** AI 问答（公开接口，后端豆包大模型 + 规则跳转卡片） */
export function postAiChat(message) {
	return postJson('/app/ai/chat', { message });
}

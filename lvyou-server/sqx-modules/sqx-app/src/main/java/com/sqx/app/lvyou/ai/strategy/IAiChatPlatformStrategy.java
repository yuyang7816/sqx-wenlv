package com.sqx.app.lvyou.ai.strategy;

/**
 * AI 问答供应商策略
 */
public interface IAiChatPlatformStrategy {

    /**
     * 根据用户消息生成 AI 回复正文
     */
    String chat(String message);
}

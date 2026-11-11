package com.sqx.app.lvyou.ai.strategy;

import com.sqx.app.lvyou.ai.support.AiChatRuleSupport;

/**
 * 本地规则 FAQ（开发/豆包不可用时的兜底）
 */
public class LocalRuleAiChatStrategy implements IAiChatPlatformStrategy {

    @Override
    public String chat(String message) {
        return AiChatRuleSupport.match(message).getContent();
    }
}

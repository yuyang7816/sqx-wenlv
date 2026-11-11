package com.sqx.app.lvyou.ai.factory;

import com.sqx.app.lvyou.ai.enums.AiPlatformCodeEnums;
import com.sqx.app.lvyou.ai.strategy.DoubaoAiChatStrategy;
import com.sqx.app.lvyou.ai.strategy.IAiChatPlatformStrategy;
import com.sqx.app.lvyou.ai.strategy.LocalRuleAiChatStrategy;
import com.sqx.app.lvyou.ai.support.AiConfigReader;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI 问答供应商工厂
 */
@Component
public class AiChatFactory {

    private static final Map<String, IAiChatPlatformStrategy> STATIC_PLATFORM_MAP = new ConcurrentHashMap<>();

    static {
        STATIC_PLATFORM_MAP.put(AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode(), new LocalRuleAiChatStrategy());
    }

    @Resource
    private AiConfigReader aiConfigReader;

    public IAiChatPlatformStrategy getPlatform() {
        try {
            String code = aiConfigReader.getPlatform();
            if (StringUtils.isBlank(code)) {
                code = AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode();
            }
            if (AiPlatformCodeEnums.DOUBAO.getCode().equalsIgnoreCase(code)) {
                return new DoubaoAiChatStrategy(aiConfigReader);
            }
            IAiChatPlatformStrategy strategy = STATIC_PLATFORM_MAP.get(code);
            if (strategy != null) {
                return strategy;
            }
            return STATIC_PLATFORM_MAP.get(AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode());
        } catch (Exception ex) {
            return STATIC_PLATFORM_MAP.get(AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode());
        }
    }
}

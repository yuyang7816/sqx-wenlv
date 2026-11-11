package com.sqx.app.lvyou.ai.support;

import com.sqx.app.lvyou.ai.enums.AiPlatformCodeEnums;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * AI 问答配置（sys_config）
 */
@Component
public class AiConfigReader {

    public String getPlatform() {
        String platform = ConfigUtil.getConfigValueOrDefault("ai_platform");
        if (StringUtils.isBlank(platform)) {
            return AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode();
        }
        String normalized = platform.trim().toLowerCase();
        if (AiPlatformCodeEnums.DOUBAO.getCode().equals(normalized)) {
            return AiPlatformCodeEnums.DOUBAO.getCode();
        }
        if (AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode().equals(normalized)) {
            return AiPlatformCodeEnums.LOCAL_TEMPLATE.getCode();
        }
        return normalized;
    }

    public String getDoubaoApiKey() {
        return StringUtils.trimToEmpty(ConfigUtil.getConfigValueOrDefault("ai_doubao_api_key"));
    }

    public String getDoubaoModel() {
        String model = ConfigUtil.getConfigValueOrDefault("ai_doubao_model");
        return model == null || model.trim().isEmpty() ? "doubao-pro-32k" : model.trim();
    }

    public String getDoubaoEndpoint() {
        String endpoint = ConfigUtil.getConfigValueOrDefault("ai_doubao_endpoint");
        return endpoint == null || endpoint.trim().isEmpty()
                ? "https://ark.cn-beijing.volces.com/api/v3/chat/completions"
                : endpoint.trim();
    }
}

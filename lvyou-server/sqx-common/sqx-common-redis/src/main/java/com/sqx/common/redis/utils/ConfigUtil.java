package com.sqx.common.redis.utils;

import com.alibaba.fastjson2.JSONObject;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;

/**
 * @author chengming
 * @since 2025/5/14
 */
@Slf4j
public class ConfigUtil {
    public static String getConfigValue(String paramKey) {
        String key = RedisKeys.SYS_CONFIG_KEY + paramKey;
        if (!RedisUtil.hasKey(key)) {
            log.error("配置不存在: {}", paramKey);
            throw new BizException("配置不存在");
        }

        String config = RedisUtil.getCacheObject(key);
        JSONObject configJson = JSONObject.parseObject(config);
        return configJson.getString("paramValue");
    }

    public static String getConfigValueOrDefault(String paramKey) {
        String key = RedisKeys.SYS_CONFIG_KEY + paramKey;
        try {
            if (!RedisUtil.hasKey(key)) {
                return null;
            }
            String config = RedisUtil.getCacheObject(key);
            if (config == null || config.trim().isEmpty()) {
                return null;
            }
            JSONObject configJson = JSONObject.parseObject(config);
            if (configJson == null) {
                return null;
            }
            return configJson.getString("paramValue");
        } catch (Exception ex) {
            log.warn("读取配置失败，使用默认值: paramKey={}", paramKey, ex);
            return null;
        }
    }
}

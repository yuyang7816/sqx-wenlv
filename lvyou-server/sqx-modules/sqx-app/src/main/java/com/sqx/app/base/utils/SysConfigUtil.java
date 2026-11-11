package com.sqx.app.base.utils;

import com.sqx.common.core.utils.SpringContextHolder;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;

/**
 * @author chengming
 * @since 2025/5/14
 * 系统配置工具类
 */
public class SysConfigUtil {

    /**
     * 获取系统配置
     * @param paramKey key
     * @return
     */
    public static SysConfigEntity getConfig(String paramKey) {
        ISysConfigService sysConfigService = SpringContextHolder.getBean(ISysConfigService.class);
        String paramValue = sysConfigService.getConfigValue(paramKey);

        SysConfigEntity sysConfig = new SysConfigEntity();
        sysConfig.setParamKey(paramKey);
        sysConfig.setParamValue(paramValue);
        return sysConfig;
    }

    /**
     * 获取系统配置的值（Redis 未命中时回退数据库）
     * @param paramKey key
     * @return
     */
    public static String getConfigValue(String paramKey) {
        ISysConfigService sysConfigService = SpringContextHolder.getBean(ISysConfigService.class);
        return sysConfigService.getConfigValue(paramKey);
    }

    /**
     * 读取配置值；不存在时返回 null
     */
    public static String getConfigValueOrNull(String paramKey) {
        ISysConfigService sysConfigService = SpringContextHolder.getBean(ISysConfigService.class);
        return sysConfigService.getConfigValueOrNull(paramKey);
    }

    public static String getConfigValue(String paramKey, String defaultValue) {
        String value = getConfigValueOrNull(paramKey);
        return org.apache.commons.lang3.StringUtils.isNotBlank(value) ? value.trim() : defaultValue;
    }

    public static boolean getBooleanConfigValue(String paramKey, boolean defaultValue) {
        String value = getConfigValue(paramKey, defaultValue ? "1" : "0");
        return "1".equals(org.apache.commons.lang3.StringUtils.trimToEmpty(value))
                || "true".equalsIgnoreCase(org.apache.commons.lang3.StringUtils.trimToEmpty(value))
                || "yes".equalsIgnoreCase(org.apache.commons.lang3.StringUtils.trimToEmpty(value));
    }
}

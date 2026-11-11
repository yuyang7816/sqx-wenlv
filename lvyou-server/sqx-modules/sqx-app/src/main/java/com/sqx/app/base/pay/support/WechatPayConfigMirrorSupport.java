package com.sqx.app.base.pay.support;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sqx.common.pay.constants.PayConstant;
import com.sqx.common.pay.enums.PayConfigCache;
import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.redis.utils.RedisKeys;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 微信支付运行时键（PayConstant.Wx）与管理端兼容键（PayConfigCache）双向同步。
 */
public final class WechatPayConfigMirrorSupport {

    private static final Map<String, String> RUNTIME_TO_COMPAT;

    static {
        Map<String, String> runtimeToCompat = new LinkedHashMap<>();
        runtimeToCompat.put(PayConstant.Wx.MCH_ID, PayConfigCache.WX_MCH_ID.getCode());
        runtimeToCompat.put(PayConstant.Wx.MCH_KEY, PayConfigCache.WX_MCH_KEY.getCode());
        runtimeToCompat.put(PayConstant.Wx.KEY_PATH, PayConfigCache.WX_KEY_PATH.getCode());
        runtimeToCompat.put(PayConstant.Wx.User.MA_APP_ID, PayConfigCache.WX_JSAPI_APP_ID.getCode());
        runtimeToCompat.put(PayConstant.Wx.User.MP_APP_ID, PayConfigCache.WX_H5_APP_ID.getCode());
        runtimeToCompat.put(PayConstant.Wx.User.APP_ID, PayConfigCache.WX_APP_ID.getCode());
        RUNTIME_TO_COMPAT = Collections.unmodifiableMap(runtimeToCompat);
    }

    private WechatPayConfigMirrorSupport() {
    }

    public static String resolveJsapiAppId() {
        return firstNonBlank(
                safeConfigValue(PayConstant.Wx.User.MA_APP_ID),
                safeConfigValue(PayConfigCache.WX_JSAPI_APP_ID.getCode())
        );
    }

    public static String resolveH5AppId() {
        return firstNonBlank(
                safeConfigValue(PayConstant.Wx.User.MP_APP_ID),
                safeConfigValue(PayConfigCache.WX_H5_APP_ID.getCode())
        );
    }

    public static String resolveAppAppId() {
        return firstNonBlank(
                safeConfigValue(PayConstant.Wx.User.APP_ID),
                safeConfigValue(PayConfigCache.WX_APP_ID.getCode())
        );
    }

    public static boolean isConfigured(String paramKey, String paramValue) {
        if (StringUtils.isNotBlank(paramValue)) {
            return true;
        }
        String mirrorKey = mirrorKeyOf(paramKey);
        if (StringUtils.isBlank(mirrorKey)) {
            return false;
        }
        return StringUtils.isNotBlank(safeConfigValue(mirrorKey));
    }

    public static void syncMirrorValue(ISysConfigService sysConfigService, String paramKey, String paramValue) {
        String mirrorKey = mirrorKeyOf(paramKey);
        if (StringUtils.isBlank(mirrorKey)) {
            return;
        }
        SysConfigEntity mirror = sysConfigService.getOne(
                Wrappers.<SysConfigEntity>lambdaQuery()
                        .eq(SysConfigEntity::getParamKey, mirrorKey)
                        .last("LIMIT 1"),
                false
        );
        if (mirror == null) {
            return;
        }
        String normalized = StringUtils.defaultString(paramValue);
        if (StringUtils.equals(StringUtils.trimToEmpty(mirror.getParamValue()), normalized)) {
            return;
        }
        mirror.setParamValue(normalized);
        sysConfigService.updateById(mirror);
        cacheConfig(mirror);
    }

    public static void syncAllMirrorPairs(ISysConfigService sysConfigService) {
        for (Map.Entry<String, String> entry : RUNTIME_TO_COMPAT.entrySet()) {
            syncPair(sysConfigService, entry.getKey(), entry.getValue());
        }
    }

    private static void syncPair(ISysConfigService sysConfigService, String runtimeKey, String compatKey) {
        String runtimeValue = readConfigValue(sysConfigService, runtimeKey);
        String compatValue = readConfigValue(sysConfigService, compatKey);
        if (StringUtils.isNotBlank(runtimeValue) && !StringUtils.equals(runtimeValue, compatValue)) {
            upsertValue(sysConfigService, compatKey, runtimeValue);
        } else if (StringUtils.isNotBlank(compatValue) && !StringUtils.equals(compatValue, runtimeValue)) {
            upsertValue(sysConfigService, runtimeKey, compatValue);
        }
    }

    private static void upsertValue(ISysConfigService sysConfigService, String paramKey, String value) {
        SysConfigEntity entity = sysConfigService.getOne(
                Wrappers.<SysConfigEntity>lambdaQuery()
                        .eq(SysConfigEntity::getParamKey, paramKey)
                        .last("LIMIT 1"),
                false
        );
        if (entity == null || StringUtils.equals(StringUtils.trimToEmpty(entity.getParamValue()), value)) {
            return;
        }
        entity.setParamValue(value);
        sysConfigService.updateById(entity);
        cacheConfig(entity);
    }

    private static String readConfigValue(ISysConfigService sysConfigService, String paramKey) {
        SysConfigEntity entity = sysConfigService.getOne(
                Wrappers.<SysConfigEntity>lambdaQuery()
                        .eq(SysConfigEntity::getParamKey, paramKey)
                        .last("LIMIT 1"),
                false
        );
        return entity == null ? "" : StringUtils.trimToEmpty(entity.getParamValue());
    }

    private static String mirrorKeyOf(String paramKey) {
        if (RUNTIME_TO_COMPAT.containsKey(paramKey)) {
            return RUNTIME_TO_COMPAT.get(paramKey);
        }
        for (Map.Entry<String, String> entry : RUNTIME_TO_COMPAT.entrySet()) {
            if (StringUtils.equals(entry.getValue(), paramKey)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private static String safeConfigValue(String key) {
        try {
            return StringUtils.trimToNull(ConfigUtil.getConfigValueOrDefault(key));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            throw new IllegalArgumentException("微信支付 AppId 未配置");
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return StringUtils.trim(value);
            }
        }
        throw new IllegalArgumentException("微信支付 AppId 未配置");
    }

    private static void cacheConfig(SysConfigEntity config) {
        RedisUtil.setCacheObject(RedisKeys.SYS_CONFIG_KEY + config.getParamKey(), JSONObject.toJSONString(config));
    }

    public static Map<String, String> runtimeToCompatMap() {
        return RUNTIME_TO_COMPAT;
    }
}

package com.sqx.app.base.utils;

import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 微信小程序 AppId / Secret 读取（兼容旧配置键）
 */
public final class WeChatMaConfigSupport {

    private WeChatMaConfigSupport() {
    }

    public static String resolveMaAppId() {
        return firstNonBlank(
                SysConfigUtil.getConfigValueOrNull(SysConfigConstant.Sys.SYS_WECHAT_JSAPI_APPID),
                SysConfigUtil.getConfigValueOrNull("sys_user_ma_appid"),
                safeConfigValue("pay_wx_jsapi_app_id")
        );
    }

    public static String resolveMaSecret() {
        return firstNonBlank(
                SysConfigUtil.getConfigValueOrNull(SysConfigConstant.Sys.SYS_WECHAT_JSAPI_SECRET),
                SysConfigUtil.getConfigValueOrNull("sys_user_ma_secret")
        );
    }

    private static String safeConfigValue(String key) {
        try {
            return ConfigUtil.getConfigValueOrDefault(key);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return "";
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return StringUtils.trim(value);
            }
        }
        return "";
    }
}

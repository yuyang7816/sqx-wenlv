package com.sqx.app.base.utils;

import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;

/**
 * 用户头像默认值（与 sys_config.sys_default_avatar 一致）
 */
public final class AvatarSupport {

    public static final String FALLBACK_URL = "";

    private AvatarSupport() {
    }

    public static String getDefaultAvatarUrl() {
        String value = ConfigUtil.getConfigValueOrDefault(SysConfigConstant.Sys.SYS_DEFAULT_AVATAR);
        if (StringUtils.isBlank(value)) {
            return FALLBACK_URL;
        }
        return value.trim();
    }

    public static String resolve(String avatar) {
        if (StringUtils.isNotBlank(avatar)) {
            return avatar.trim();
        }
        return getDefaultAvatarUrl();
    }
}

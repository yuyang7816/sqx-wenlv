package com.sqx.app.lvyou.transport.support;

import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.utils.SysConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * 实时公交平台配置（sys_config，管理端「平台配置」维护）
 */
@Component
public class BusRealtimeConfigSupport {

    public boolean isEnabled() {
        String value = SysConfigUtil.getConfigValue(SysConfigConstant.Transport.BUS_REALTIME_ENABLED);
        if (StringUtils.isBlank(value)) {
            return false;
        }
        return !"0".equals(value.trim());
    }

    public String getApiKey() {
        String value = SysConfigUtil.getConfigValue(SysConfigConstant.Transport.BUS_REALTIME_API_KEY);
        return StringUtils.trimToEmpty(value);
    }

    /** 开关开启且已在平台配置中填写 API Key */
    public boolean isAvailable() {
        return isEnabled() && StringUtils.isNotBlank(getApiKey());
    }
}

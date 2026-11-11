package com.sqx.app.lvyou.config;

import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.utils.SysConfigUtil;
import org.springframework.stereotype.Component;

/**
 * 荣e游业务开关配置
 */
@Component
public class LvyouAppProperties {

    public boolean isMpLoginEnabled() {
        return SysConfigUtil.getBooleanConfigValue(SysConfigConstant.Sys.SYS_MP_LOGIN_ENABLED, true);
    }
}

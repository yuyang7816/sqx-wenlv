package com.sqx.system.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.system.db.entity.SysConfigEntity;

/**
 * 系统配置信息
 *
 */
public interface ISysConfigService extends IService<SysConfigEntity> {


    String getConfigValue(String configKey);

    /**
     * 读取配置值；不存在或未启用时返回 null，不抛异常
     */
    String getConfigValueOrNull(String configKey);

    void loadSysConfig();
}

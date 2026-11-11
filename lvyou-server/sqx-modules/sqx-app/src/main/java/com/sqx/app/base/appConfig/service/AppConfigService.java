package com.sqx.app.base.appConfig.service;

import com.sqx.app.base.appConfig.domain.bo.SysConfigInfoBO;
import com.sqx.app.base.appConfig.domain.vo.AgreementVO;
import com.sqx.app.base.appConfig.domain.vo.AppConfigInfoVO;
import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;

/**
 * @author chengming
 * @since 2025/6/9
 */
public interface AppConfigService {

    /**
     * 按 key 查询单条配置（兼容旧 POST 接口）
     */
    SysConfigVO info(SysConfigInfoBO sysConfigInfoBO);

    /**
     * 聚合客服与邀请配置
     */
    AppConfigInfoVO getAppConfigInfo();

    /**
     * 按协议路径 key 读取 HTML 正文
     *
     * @param key user / privacy / about
     */
    AgreementVO getAgreement(String key);
}

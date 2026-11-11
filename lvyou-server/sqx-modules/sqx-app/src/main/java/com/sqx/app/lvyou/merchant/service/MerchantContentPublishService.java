package com.sqx.app.lvyou.merchant.service;

import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;

import java.util.Map;

/**
 * 商户发布内容同步至用户端展示表
 */
public interface MerchantContentPublishService {

    /**
     * 同步门店装修至用户端详情数据
     */
    void syncStoreDecor(MerchantEntity merchant, Map<String, Object> decor);

    /**
     * 同步单个商户服务至用户端（房型/线路/商品等）
     */
    void syncService(MerchantEntity merchant, MerchantServiceEntity service);

    /**
     * 删除商户服务在用户端的展示数据（房型/线路/商品等）
     */
    void removeService(MerchantEntity merchant, Long serviceId);

    /**
     * 同步商户全部已上架服务
     */
    void syncAllServices(MerchantEntity merchant);

    /**
     * 解析用户端预览目标 ID
     */
    Long resolveUserPreviewId(MerchantEntity merchant);
}

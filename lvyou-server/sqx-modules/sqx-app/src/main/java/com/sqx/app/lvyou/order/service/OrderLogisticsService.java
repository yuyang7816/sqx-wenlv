package com.sqx.app.lvyou.order.service;

import com.sqx.app.lvyou.order.domain.vo.OrderLogisticsVo;

/**
 * 物流查询服务
 */
public interface OrderLogisticsService {

    /**
     * @param userId  当前用户 ID，管理端传 null
     * @param orderId 订单 ID
     */
    OrderLogisticsVo queryLogistics(Long userId, Long orderId);
}

package com.sqx.app.lvyou.order.service;

import com.sqx.app.lvyou.order.domain.entity.OrderEntity;

/**
 * 订单生命周期定时处理
 */
public interface OrderLifecycleService {

    /** 关闭超时未支付订单 */
    int cancelExpiredUnpaidOrders();

    /** 酒店 No-show 处理 */
    int processHotelNoShowOrders();

    /** 酒店离店自动完成 */
    int autoCompleteHotelOrders();

    /** 单笔酒店订单：符合 No-show 则关闭 */
    boolean applyHotelNoShowIfNeeded(OrderEntity order);

    /** 单笔酒店订单：符合离店自动完成则完成 */
    boolean applyHotelAutoCompleteIfNeeded(OrderEntity order);
}

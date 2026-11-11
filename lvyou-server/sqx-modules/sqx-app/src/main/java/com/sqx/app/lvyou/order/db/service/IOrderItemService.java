package com.sqx.app.lvyou.order.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;

import java.util.List;
import java.util.Map;

/**
 * 订单明细 DB 服务
 */
public interface IOrderItemService extends IService<OrderItemEntity> {

    List<OrderItemEntity> listByOrderIds(List<Long> orderIds);

    Map<Long, List<OrderItemEntity>> mapByOrderIds(List<Long> orderIds);
}

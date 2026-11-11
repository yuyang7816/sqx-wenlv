package com.sqx.app.lvyou.order.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.order.db.mapper.OrderMapper;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import org.springframework.stereotype.Service;

/**
 * 订单 DB 服务实现
 */
@Service
public class IOrderServiceImpl extends ServiceImpl<OrderMapper, OrderEntity> implements IOrderService {
}

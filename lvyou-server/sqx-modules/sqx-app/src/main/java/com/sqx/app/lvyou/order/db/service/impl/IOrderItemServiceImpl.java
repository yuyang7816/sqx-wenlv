package com.sqx.app.lvyou.order.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.order.db.mapper.OrderItemMapper;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 订单明细 DB 服务实现
 */
@Service
public class IOrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItemEntity> implements IOrderItemService {

    @Override
    public List<OrderItemEntity> listByOrderIds(List<Long> orderIds) {
        if (CollectionUtils.isEmpty(orderIds)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<OrderItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderItemEntity::getOrderId, orderIds)
                .orderByAsc(OrderItemEntity::getId);
        return list(queryWrapper);
    }

    @Override
    public Map<Long, List<OrderItemEntity>> mapByOrderIds(List<Long> orderIds) {
        List<OrderItemEntity> items = listByOrderIds(orderIds);
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyMap();
        }
        return items.stream().collect(Collectors.groupingBy(OrderItemEntity::getOrderId, HashMap::new, Collectors.toList()));
    }
}

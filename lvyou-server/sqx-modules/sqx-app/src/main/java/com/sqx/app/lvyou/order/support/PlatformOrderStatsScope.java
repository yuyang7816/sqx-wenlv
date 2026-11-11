package com.sqx.app.lvyou.order.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;

/**
 * 平台订单统计口径（概览、趋势、管理端订单监管列表默认范围须一致）。
 * <p>
 * 排除餐饮预约同步产生的影子单（order_category=restaurant），其余含空分类订单均计入。
 * </p>
 */
public final class PlatformOrderStatsScope {

    /** 订单分类字段名（QueryWrapper 原生 SQL 使用） */
    public static final String ORDER_CATEGORY_COLUMN = "order_category";

    private PlatformOrderStatsScope() {
    }

    /**
     * 应用平台订单统计范围（Lambda 条件）
     */
    public static void apply(LambdaQueryWrapper<OrderEntity> queryWrapper) {
        queryWrapper.and(wrapper -> wrapper.isNull(OrderEntity::getOrderCategory)
                .or(inner -> inner.ne(OrderEntity::getOrderCategory,
                        AppOrderStatusSupport.RESTAURANT_SHADOW_CATEGORY)));
    }

    /**
     * 应用平台订单统计范围（原生字段名，供聚合 SQL 使用）
     */
    public static void apply(QueryWrapper<OrderEntity> queryWrapper) {
        queryWrapper.and(wrapper -> wrapper.isNull(ORDER_CATEGORY_COLUMN)
                .or(inner -> inner.ne(ORDER_CATEGORY_COLUMN,
                        AppOrderStatusSupport.RESTAURANT_SHADOW_CATEGORY)));
    }
}

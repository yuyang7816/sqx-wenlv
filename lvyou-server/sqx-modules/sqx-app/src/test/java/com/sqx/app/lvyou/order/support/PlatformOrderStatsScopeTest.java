package com.sqx.app.lvyou.order.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * 平台订单统计口径单元测试
 */
public class PlatformOrderStatsScopeTest {

    @Test
    public void excludedCategory_matchesAppOrderStatusSupport() {
        Assert.assertTrue(AppOrderStatusSupport.excludedFromPlatformOrderStats("restaurant"));
        Assert.assertFalse(AppOrderStatusSupport.excludedFromPlatformOrderStats(null));
        Assert.assertFalse(AppOrderStatusSupport.excludedFromPlatformOrderStats("hotel"));
    }

    @Test
    public void apply_lambdaWrapper_doesNotThrow() {
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<com.sqx.app.lvyou.order.domain.entity.OrderEntity> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
        PlatformOrderStatsScope.apply(queryWrapper);
        Assert.assertNotNull(queryWrapper);
    }

    @Test
    public void apply_queryWrapper_doesNotThrow() {
        com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<com.sqx.app.lvyou.order.domain.entity.OrderEntity> queryWrapper =
                new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<>();
        PlatformOrderStatsScope.apply(queryWrapper);
        Assert.assertNotNull(queryWrapper);
    }
}

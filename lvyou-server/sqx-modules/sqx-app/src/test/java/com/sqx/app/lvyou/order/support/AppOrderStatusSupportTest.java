package com.sqx.app.lvyou.order.support;

import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * 用户端订单分类与平台统计口径单元测试
 */
public class AppOrderStatusSupportTest {

    @Test
    public void excludedFromPlatformOrderStats_restaurantShadowOrder() {
        Assert.assertTrue(AppOrderStatusSupport.excludedFromPlatformOrderStats("restaurant"));
        Assert.assertTrue(AppOrderStatusSupport.excludedFromPlatformOrderStats("RESTAURANT"));
    }

    @Test
    public void excludedFromPlatformOrderStats_userOrderTabCategories() {
        Assert.assertFalse(AppOrderStatusSupport.excludedFromPlatformOrderStats("hotel"));
        Assert.assertFalse(AppOrderStatusSupport.excludedFromPlatformOrderStats("route"));
        Assert.assertFalse(AppOrderStatusSupport.excludedFromPlatformOrderStats("goods"));
        Assert.assertFalse(AppOrderStatusSupport.excludedFromPlatformOrderStats("culture"));
    }

    @Test
    public void hotelCheckedIn_afterVerify() {
        OrderEntity order = new OrderEntity();
        order.setOrderCategory("hotel");
        order.setStatus(MerchantOrderStatusGroup.COMPLETED);
        order.setVerifyTime(LocalDateTime.now());

        Assert.assertTrue(AppOrderStatusSupport.isHotelCheckedIn(order));
        Assert.assertEquals(AppOrderStatusSupport.COMPLETED, AppOrderStatusSupport.toApiStatus(order));
        Assert.assertEquals("已入住", AppOrderStatusSupport.toDisplayStatus(order));
    }

    @Test
    public void hotelCheckedIn_legacyPendingUseWithVerifyTime() {
        OrderEntity order = new OrderEntity();
        order.setOrderCategory("hotel");
        order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        order.setVerifyTime(LocalDateTime.now());

        Assert.assertTrue(AppOrderStatusSupport.isHotelCheckedIn(order));
        Assert.assertEquals("已入住", AppOrderStatusSupport.toDisplayStatus(order));
    }

    @Test
    public void refundedOrder_displayAndFilter() {
        OrderEntity order = new OrderEntity();
        order.setStatus(MerchantOrderStatusGroup.CANCELLED);
        order.setRemark("退款已完成");

        Assert.assertTrue(AppOrderStatusSupport.isRefundedOrder(order));
        Assert.assertEquals("已退款", AppOrderStatusSupport.toDisplayStatus(order));
        Assert.assertTrue(AppOrderStatusSupport.isRefundedStatusFilter("refunded"));
    }
}

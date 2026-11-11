package com.sqx.app.lvyou.order.support;

import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import org.junit.Assert;
import org.junit.Test;

/**
 * 商户订单状态映射单元测试
 */
public class MerchantOrderStatusSupportTest {

    @Test
    public void toApiStatus_hotelPendingUse_returnsPendingCheckin() {
        OrderEntity order = new OrderEntity();
        order.setOrderCategory("hotel");
        order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        Assert.assertEquals(MerchantOrderStatusSupport.PENDING_CHECKIN, MerchantOrderStatusSupport.toApiStatus(order));
    }

    @Test
    public void toApiStatus_restaurantPendingUse_returnsPendingArrival() {
        OrderEntity order = new OrderEntity();
        order.setOrderCategory("restaurant");
        order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        Assert.assertEquals(MerchantOrderStatusSupport.PENDING_ARRIVAL, MerchantOrderStatusSupport.toApiStatus(order));
        Assert.assertEquals("待到店", MerchantOrderStatusSupport.restaurantLabelOfApiStatus(
                MerchantOrderStatusSupport.PENDING_ARRIVAL));
        Assert.assertEquals("已到店", MerchantOrderStatusSupport.restaurantLabelOfApiStatus(
                MerchantOrderStatusSupport.COMPLETED));
    }

    @Test
    public void toApiStatus_goodsPendingUse_returnsPendingShip() {
        OrderEntity order = new OrderEntity();
        order.setOrderCategory("goods");
        order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        Assert.assertEquals(MerchantOrderStatusSupport.PENDING_SHIP, MerchantOrderStatusSupport.toApiStatus(order));
    }

    @Test
    public void toApiStatus_goodsPendingReceive_returnsShipped() {
        OrderEntity order = new OrderEntity();
        order.setOrderCategory("goods");
        order.setStatus(MerchantOrderStatusGroup.PENDING_RECEIVE);
        Assert.assertEquals(MerchantOrderStatusSupport.SHIPPED, MerchantOrderStatusSupport.toApiStatus(order));
    }

    @Test
    public void categoryFromStoreType_travel_returnsRoute() {
        Assert.assertEquals("route", MerchantOrderStatusSupport.categoryFromStoreType("travel"));
    }

    @Test
    public void toApiStatus_hotelPendingConfirm_returnsPendingCheckin() {
        OrderEntity order = new OrderEntity();
        order.setOrderCategory("hotel");
        order.setStatus(MerchantOrderStatusGroup.PENDING_CONFIRM);
        Assert.assertEquals(MerchantOrderStatusSupport.PENDING_CHECKIN, MerchantOrderStatusSupport.toApiStatus(order));
    }

    @Test
    public void matchesTab_pending_includesPendingConfirmAndPendingShip() {
        OrderEntity confirm = new OrderEntity();
        confirm.setOrderCategory("route");
        confirm.setStatus(MerchantOrderStatusGroup.PENDING_CONFIRM);
        OrderEntity ship = new OrderEntity();
        ship.setOrderCategory("goods");
        ship.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        OrderEntity hotelLegacy = new OrderEntity();
        hotelLegacy.setOrderCategory("hotel");
        hotelLegacy.setStatus(MerchantOrderStatusGroup.PENDING_CONFIRM);
        Assert.assertTrue(MerchantOrderStatusSupport.matchesTab("pending", confirm));
        Assert.assertTrue(MerchantOrderStatusSupport.matchesTab("pending", ship));
        Assert.assertFalse(MerchantOrderStatusSupport.matchesTab("pending", hotelLegacy));
        Assert.assertTrue(MerchantOrderStatusSupport.matchesTab("processing", hotelLegacy));
    }
}

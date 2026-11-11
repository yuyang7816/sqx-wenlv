package com.sqx.app.lvyou.order.support;

import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import org.junit.Assert;
import org.junit.Test;

/**
 * 订单状态机单元测试
 */
public class OrderStatusMachineTest {

    @Test
    public void initialStatus_returnsUnpaid() {
        Assert.assertEquals(MerchantOrderStatusGroup.UNPAID, OrderStatusMachine.initialStatus());
    }

    @Test
    public void assertUserCancelAllowed_unpaid_ok() {
        OrderStatusMachine.assertUserCancelAllowed(MerchantOrderStatusGroup.UNPAID);
    }

    @Test(expected = com.sqx.common.core.exception.BizException.class)
    public void assertUserCancelAllowed_paid_rejected() {
        OrderStatusMachine.assertUserCancelAllowed(MerchantOrderStatusGroup.PENDING_CONFIRM);
    }

    @Test
    public void assertAdminCancelAllowed_pendingUse_ok() {
        OrderStatusMachine.assertAdminCancelAllowed(MerchantOrderStatusGroup.PENDING_USE);
    }

    @Test(expected = com.sqx.common.core.exception.BizException.class)
    public void assertAdminCancelAllowed_completed_rejected() {
        OrderStatusMachine.assertAdminCancelAllowed(MerchantOrderStatusGroup.COMPLETED);
    }

    @Test
    public void assertPayAllowed_unpaid_ok() {
        OrderStatusMachine.assertPayAllowed(MerchantOrderStatusGroup.UNPAID);
    }

    @Test(expected = com.sqx.common.core.exception.BizException.class)
    public void assertPayAllowed_paid_rejected() {
        OrderStatusMachine.assertPayAllowed(MerchantOrderStatusGroup.PENDING_CONFIRM);
    }

    @Test
    public void toPaidStatus_hotel_returnsPendingUse() {
        Assert.assertEquals(MerchantOrderStatusGroup.PENDING_USE,
                OrderStatusMachine.toPaidStatus("hotel"));
    }

    @Test
    public void toPaidStatus_goods_returnsPendingUse() {
        Assert.assertEquals(MerchantOrderStatusGroup.PENDING_USE,
                OrderStatusMachine.toPaidStatus("goods"));
    }

    @Test
    public void toPaidStatus_culture_returnsPendingUse() {
        Assert.assertEquals(MerchantOrderStatusGroup.PENDING_USE,
                OrderStatusMachine.toPaidStatus("culture"));
    }

    @Test
    public void appStatus_goodsPendingConfirm_displayPendingShip() {
        com.sqx.app.lvyou.order.domain.entity.OrderEntity order = new com.sqx.app.lvyou.order.domain.entity.OrderEntity();
        order.setOrderCategory("culture");
        order.setStatus(MerchantOrderStatusGroup.PENDING_CONFIRM);
        Assert.assertEquals("pending_use", AppOrderStatusSupport.toApiStatus(order));
        Assert.assertEquals("待发货", AppOrderStatusSupport.toDisplayStatus(order));
    }

    @Test
    public void toPaidStatus_returnsPendingConfirm() {
        Assert.assertEquals(MerchantOrderStatusGroup.PENDING_CONFIRM, OrderStatusMachine.toPaidStatus());
    }

    @Test
    public void appStatus_hotelPendingConfirm_displayPendingStay() {
        com.sqx.app.lvyou.order.domain.entity.OrderEntity order = new com.sqx.app.lvyou.order.domain.entity.OrderEntity();
        order.setOrderCategory("hotel");
        order.setStatus(MerchantOrderStatusGroup.PENDING_CONFIRM);
        Assert.assertEquals("pending_use", AppOrderStatusSupport.toApiStatus(order));
        Assert.assertEquals("待入住", AppOrderStatusSupport.toDisplayStatus(order));
    }

    @Test
    public void appStatus_hotelPendingUse_displayPendingStay() {
        com.sqx.app.lvyou.order.domain.entity.OrderEntity order = new com.sqx.app.lvyou.order.domain.entity.OrderEntity();
        order.setOrderCategory("hotel");
        order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        Assert.assertEquals("pending_use", AppOrderStatusSupport.toApiStatus(order));
        Assert.assertEquals("待入住", AppOrderStatusSupport.toDisplayStatus(order));
    }

    @Test
    public void appStatus_pendingPay_display() {
        com.sqx.app.lvyou.order.domain.entity.OrderEntity order = new com.sqx.app.lvyou.order.domain.entity.OrderEntity();
        order.setOrderCategory("hotel");
        order.setStatus(MerchantOrderStatusGroup.UNPAID);
        Assert.assertEquals("pending_pay", AppOrderStatusSupport.toApiStatus(order));
        Assert.assertEquals("待支付", AppOrderStatusSupport.toDisplayStatus(order));
    }

    @Test
    public void appStatus_goodsPendingUse_displayPendingShip() {
        com.sqx.app.lvyou.order.domain.entity.OrderEntity order = new com.sqx.app.lvyou.order.domain.entity.OrderEntity();
        order.setOrderCategory("goods");
        order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        Assert.assertEquals("待发货", AppOrderStatusSupport.toDisplayStatus(order));
    }

    @Test
    public void assertUserCompleteAllowed_goodsPendingReceive_ok() {
        OrderStatusMachine.assertUserCompleteAllowed(MerchantOrderStatusGroup.PENDING_RECEIVE, "goods");
    }

    @Test(expected = com.sqx.common.core.exception.BizException.class)
    public void assertUserCompleteAllowed_hotelPendingReceive_rejected() {
        OrderStatusMachine.assertUserCompleteAllowed(MerchantOrderStatusGroup.PENDING_RECEIVE, "hotel");
    }

    @Test
    public void assertUserRefundAllowed_pendingUse_ok() {
        OrderStatusMachine.assertUserRefundAllowed(MerchantOrderStatusGroup.PENDING_USE);
    }

    @Test(expected = com.sqx.common.core.exception.BizException.class)
    public void assertUserRefundAllowed_completed_rejected() {
        OrderStatusMachine.assertUserRefundAllowed(MerchantOrderStatusGroup.COMPLETED);
    }
}

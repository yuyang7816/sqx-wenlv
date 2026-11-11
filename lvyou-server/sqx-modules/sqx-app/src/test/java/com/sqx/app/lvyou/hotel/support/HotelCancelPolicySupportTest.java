package com.sqx.app.lvyou.hotel.support;

import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class HotelCancelPolicySupportTest {

    @Test
    public void isFreeCancelAllowed_beforeDeadline_ok() {
        OrderEntity order = hotelOrder(LocalDate.now().plusDays(3));
        Assert.assertTrue(HotelCancelPolicySupport.isFreeCancelAllowed(order, LocalDateTime.now()));
    }

    @Test
    public void isFreeCancelAllowed_afterDeadline_false() {
        OrderEntity order = hotelOrder(LocalDate.now());
        LocalDateTime now = LocalDate.now().atTime(19, 0);
        Assert.assertFalse(HotelCancelPolicySupport.isFreeCancelAllowed(order, now));
    }

    @Test
    public void isNoShowCandidate_afterHoldUntil_true() {
        OrderEntity order = hotelOrder(LocalDate.now().minusDays(1));
        order.setPayTime(LocalDate.now().minusDays(1).atTime(10, 0));
        Assert.assertTrue(HotelCancelPolicySupport.isNoShowCandidate(order, LocalDateTime.now()));
    }

    @Test
    public void isNoShowCandidate_paidAfterHoldUntil_false() {
        OrderEntity order = hotelOrder(LocalDate.now());
        LocalDateTime now = LocalDate.now().atTime(19, 30);
        order.setPayTime(now);
        Assert.assertFalse(HotelCancelPolicySupport.isNoShowCandidate(order, now));
    }

    @Test
    public void shouldAutoComplete_afterCheckout_true() {
        OrderEntity order = hotelOrder(LocalDate.now().minusDays(2));
        order.setCheckOutDate(LocalDate.now().minusDays(1));
        order.setVerifyTime(LocalDateTime.now().minusDays(2));
        LocalDateTime now = LocalDate.now().atTime(15, 0);
        Assert.assertTrue(HotelCancelPolicySupport.shouldAutoComplete(order, now));
    }

    @Test
    public void isUserRefundAllowed_noShow_false() {
        OrderEntity order = hotelOrder(LocalDate.now().minusDays(1));
        Assert.assertFalse(HotelCancelPolicySupport.isUserRefundAllowed(order, LocalDateTime.now()));
    }

    @Test
    public void isUserRefundAllowed_afterCheckoutDeadline_false() {
        OrderEntity order = hotelOrder(LocalDate.now().minusDays(2));
        order.setCheckOutDate(LocalDate.now().minusDays(1));
        LocalDateTime now = LocalDate.now().atTime(15, 0);
        Assert.assertFalse(HotelCancelPolicySupport.isUserRefundAllowed(order, now));
    }

    @Test
    public void isUserRefundAllowed_beforeDeadline_true() {
        OrderEntity order = hotelOrder(LocalDate.now().plusDays(3));
        Assert.assertTrue(HotelCancelPolicySupport.isUserRefundAllowed(order, LocalDateTime.now()));
    }

    private OrderEntity hotelOrder(LocalDate checkIn) {
        OrderEntity order = new OrderEntity();
        order.setOrderCategory("hotel");
        order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        order.setCheckInDate(checkIn);
        order.setCheckOutDate(checkIn.plusDays(1));
        order.setCancelPolicyJson(HotelCancelPolicySupport.toJson(HotelCancelPolicySupport.defaultPolicyMap()));
        return order;
    }
}

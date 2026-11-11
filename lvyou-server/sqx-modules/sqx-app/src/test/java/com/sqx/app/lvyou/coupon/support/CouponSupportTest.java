package com.sqx.app.lvyou.coupon.support;

import com.sqx.app.lvyou.coupon.domain.entity.UserCouponEntity;
import com.sqx.app.lvyou.coupon.enums.UserCouponStatus;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 优惠券辅助逻辑测试
 */
public class CouponSupportTest {

    @Test
    public void scopeMatchAllAppliesEverywhere() {
        Assert.assertTrue(CouponSupport.scopeMatch("all", "hotel"));
        Assert.assertTrue(CouponSupport.scopeMatch("hotel", "hotel"));
        Assert.assertFalse(CouponSupport.scopeMatch("hotel", "route"));
    }

    @Test
    public void isApplicableChecksThresholdAndValidity() {
        UserCouponEntity coupon = buildCoupon("hotel", "10", "100");
        coupon.setValidStart(LocalDateTime.now().minusDays(1));
        coupon.setValidEnd(LocalDateTime.now().plusDays(1));

        Assert.assertTrue(CouponSupport.isApplicable(coupon, "hotel", new BigDecimal("100"), LocalDateTime.now()));
        Assert.assertFalse(CouponSupport.isApplicable(coupon, "hotel", new BigDecimal("99"), LocalDateTime.now()));
        Assert.assertFalse(CouponSupport.isApplicable(coupon, "route", new BigDecimal("200"), LocalDateTime.now()));
    }

    @Test
    public void calcDiscountCapsAtOrderAmount() {
        UserCouponEntity coupon = buildCoupon("all", "50", "0");
        coupon.setValidStart(LocalDateTime.now().minusDays(1));
        coupon.setValidEnd(LocalDateTime.now().plusDays(1));

        BigDecimal discount = CouponSupport.calcDiscount(coupon, "hotel", new BigDecimal("30"), LocalDateTime.now());
        Assert.assertEquals(new BigDecimal("30.00"), discount);
    }

    @Test
    public void resolveDisplayStatusMarksExpired() {
        UserCouponEntity coupon = buildCoupon("all", "10", "0");
        coupon.setStatus(UserCouponStatus.UNUSED.getCode());
        coupon.setValidEnd(LocalDateTime.now().minusDays(1));

        Assert.assertEquals(UserCouponStatus.EXPIRED, CouponSupport.resolveDisplayStatus(coupon, LocalDateTime.now()));
    }

    private UserCouponEntity buildCoupon(String scope, String amount, String minAmount) {
        UserCouponEntity coupon = new UserCouponEntity();
        coupon.setScope(scope);
        coupon.setAmount(new BigDecimal(amount));
        coupon.setMinAmount(new BigDecimal(minAmount));
        coupon.setStatus(UserCouponStatus.UNUSED.getCode());
        return coupon;
    }
}

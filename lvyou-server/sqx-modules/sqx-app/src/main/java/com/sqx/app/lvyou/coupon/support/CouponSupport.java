package com.sqx.app.lvyou.coupon.support;

import com.sqx.app.lvyou.coupon.domain.entity.UserCouponEntity;
import com.sqx.app.lvyou.coupon.enums.UserCouponStatus;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 优惠券业务辅助
 */
public final class CouponSupport {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private CouponSupport() {
    }

    public static boolean scopeMatch(String couponScope, String orderScope) {
        if (StringUtils.isBlank(couponScope) || "all".equalsIgnoreCase(couponScope)) {
            return true;
        }
        return couponScope.equalsIgnoreCase(orderScope);
    }

    public static boolean isApplicable(UserCouponEntity coupon, String orderScope, BigDecimal orderAmount, LocalDateTime now) {
        if (coupon == null || coupon.getStatus() == null || coupon.getStatus() != UserCouponStatus.UNUSED.getCode()) {
            return false;
        }
        LocalDateTime checkTime = now == null ? LocalDateTime.now() : now;
        if (coupon.getValidStart() != null && checkTime.isBefore(coupon.getValidStart())) {
            return false;
        }
        if (coupon.getValidEnd() != null && checkTime.isAfter(coupon.getValidEnd())) {
            return false;
        }
        if (!scopeMatch(coupon.getScope(), orderScope)) {
            return false;
        }
        BigDecimal amount = orderAmount == null ? BigDecimal.ZERO : orderAmount;
        BigDecimal minAmount = coupon.getMinAmount() == null ? BigDecimal.ZERO : coupon.getMinAmount();
        return amount.compareTo(minAmount) >= 0;
    }

    public static BigDecimal calcDiscount(UserCouponEntity coupon, String orderScope, BigDecimal orderAmount, LocalDateTime now) {
        if (!isApplicable(coupon, orderScope, orderAmount, now)) {
            return BigDecimal.ZERO;
        }
        BigDecimal amount = orderAmount == null ? BigDecimal.ZERO : orderAmount;
        BigDecimal discount = coupon.getAmount() == null ? BigDecimal.ZERO : coupon.getAmount();
        if (discount.compareTo(amount) > 0) {
            discount = amount;
        }
        return discount.setScale(2, RoundingMode.HALF_UP);
    }

    public static UserCouponStatus resolveDisplayStatus(UserCouponEntity coupon, LocalDateTime now) {
        if (coupon == null) {
            return UserCouponStatus.EXPIRED;
        }
        if (coupon.getStatus() != null && coupon.getStatus() == UserCouponStatus.USED.getCode()) {
            return UserCouponStatus.USED;
        }
        if (coupon.getStatus() != null && coupon.getStatus() == UserCouponStatus.EXPIRED.getCode()) {
            return UserCouponStatus.EXPIRED;
        }
        LocalDateTime checkTime = now == null ? LocalDateTime.now() : now;
        if (coupon.getValidEnd() != null && checkTime.isAfter(coupon.getValidEnd())) {
            return UserCouponStatus.EXPIRED;
        }
        return UserCouponStatus.UNUSED;
    }

    public static String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.toLocalDate().format(DATE_FORMAT);
    }

    public static String formatDate(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(DATE_FORMAT);
    }

    public static String generateCouponCode(Long userId) {
        return "CPN-" + userId + "-" + System.currentTimeMillis();
    }
}

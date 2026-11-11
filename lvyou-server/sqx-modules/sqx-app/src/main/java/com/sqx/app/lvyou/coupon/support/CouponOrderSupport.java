package com.sqx.app.lvyou.coupon.support;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.coupon.db.service.IUserCouponService;
import com.sqx.app.lvyou.coupon.domain.entity.UserCouponEntity;
import com.sqx.app.lvyou.coupon.enums.UserCouponStatus;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * 订单与优惠券联动：下单占用、取消退还
 */
@Component
public class CouponOrderSupport {

    @Resource
    private IUserCouponService iUserCouponService;

    /**
     * 下单成功后占用优惠券（需已写入订单号）
     */
    public void consumeCoupon(OrderEntity order, Long userId) {
        if (order == null || order.getCouponId() == null) {
            return;
        }
        if (StringUtils.isBlank(order.getOrderNo())) {
            throw new BizException(AppError.ORDER_COUPON_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        LambdaUpdateWrapper<UserCouponEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserCouponEntity::getId, order.getCouponId())
                .eq(UserCouponEntity::getUserId, userId)
                .eq(UserCouponEntity::getStatus, UserCouponStatus.UNUSED.getCode())
                .set(UserCouponEntity::getStatus, UserCouponStatus.USED.getCode())
                .set(UserCouponEntity::getUsedOrderNo, order.getOrderNo())
                .set(UserCouponEntity::getUsedTime, now);
        if (!iUserCouponService.update(updateWrapper)) {
            throw new BizException(AppError.ORDER_COUPON_INVALID);
        }
    }

    /**
     * 订单取消/关闭时退还优惠券
     */
    public void restoreCouponIfNeeded(OrderEntity order) {
        if (order == null || order.getCouponId() == null) {
            return;
        }
        UserCouponEntity coupon = iUserCouponService.getById(order.getCouponId());
        if (coupon == null || !Objects.equals(coupon.getStatus(), UserCouponStatus.USED.getCode())) {
            return;
        }
        if (StringUtils.isNotBlank(coupon.getUsedOrderNo())
                && !coupon.getUsedOrderNo().equals(order.getOrderNo())) {
            return;
        }
        LambdaUpdateWrapper<UserCouponEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserCouponEntity::getId, order.getCouponId())
                .eq(UserCouponEntity::getStatus, UserCouponStatus.USED.getCode())
                .set(UserCouponEntity::getStatus, UserCouponStatus.UNUSED.getCode())
                .set(UserCouponEntity::getUsedOrderNo, null)
                .set(UserCouponEntity::getUsedTime, null);
        iUserCouponService.update(updateWrapper);
    }
}

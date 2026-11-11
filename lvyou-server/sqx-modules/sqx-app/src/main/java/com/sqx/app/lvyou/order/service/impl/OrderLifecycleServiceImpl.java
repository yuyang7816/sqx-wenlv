package com.sqx.app.lvyou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.messages.support.InstationMessageSupport;
import com.sqx.app.lvyou.hotel.support.HotelCancelPolicySupport;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.service.OrderLifecycleService;
import com.sqx.app.lvyou.order.support.AppOrderStatusSupport;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.app.lvyou.coupon.support.CouponOrderSupport;
import com.sqx.app.lvyou.order.support.OrderStatusMachine;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class OrderLifecycleServiceImpl implements OrderLifecycleService {

    @Resource
    private IOrderService iOrderService;

    @Resource
    private InstationMessageSupport instationMessageSupport;

    @Resource
    private CouponOrderSupport couponOrderSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int cancelExpiredUnpaidOrders() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getStatus, MerchantOrderStatusGroup.UNPAID)
                .isNotNull(OrderEntity::getPayExpireTime)
                .lt(OrderEntity::getPayExpireTime, now);
        List<OrderEntity> orders = iOrderService.list(queryWrapper);
        if (CollectionUtils.isEmpty(orders)) {
            return 0;
        }
        int count = 0;
        for (OrderEntity order : orders) {
            order.setStatus(OrderStatusMachine.toCancelledStatus());
            order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
            order.setRemark(appendRemark(order.getRemark(), "系统自动取消：支付超时"));
            iOrderService.updateById(order);
            couponOrderSupport.restoreCouponIfNeeded(order);
            if (order.getUserId() != null) {
                instationMessageSupport.sendUserMessage(order.getUserId(), "订单已取消",
                        "订单 " + order.getOrderNo() + " 因支付超时已自动取消", order.getId());
            }
            count++;
        }
        if (count > 0) {
            log.info("cancelExpiredUnpaidOrders count={}", count);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int processHotelNoShowOrders() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getOrderCategory, "hotel")
                .eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_USE)
                .isNull(OrderEntity::getVerifyTime);
        List<OrderEntity> orders = iOrderService.list(queryWrapper);
        if (CollectionUtils.isEmpty(orders)) {
            return 0;
        }
        int count = 0;
        for (OrderEntity order : orders) {
            if (applyHotelNoShowIfNeeded(order, now)) {
                count++;
            }
        }
        if (count > 0) {
            log.info("processHotelNoShowOrders count={}", count);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int autoCompleteHotelOrders() {
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getOrderCategory, "hotel")
                .eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_USE)
                .isNotNull(OrderEntity::getVerifyTime);
        List<OrderEntity> orders = iOrderService.list(queryWrapper);
        if (CollectionUtils.isEmpty(orders)) {
            return 0;
        }
        int count = 0;
        for (OrderEntity order : orders) {
            if (applyHotelAutoCompleteIfNeeded(order, now)) {
                count++;
            }
        }
        if (count > 0) {
            log.info("autoCompleteHotelOrders count={}", count);
        }
        return count;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyHotelNoShowIfNeeded(OrderEntity order) {
        return applyHotelNoShowIfNeeded(order, LocalDateTime.now());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean applyHotelAutoCompleteIfNeeded(OrderEntity order) {
        return applyHotelAutoCompleteIfNeeded(order, LocalDateTime.now());
    }

    private boolean applyHotelNoShowIfNeeded(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getId() == null) {
            return false;
        }
        if (!"hotel".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            return false;
        }
        if (!Objects.equals(order.getStatus(), MerchantOrderStatusGroup.PENDING_USE)) {
            return false;
        }
        if (!HotelCancelPolicySupport.isNoShowCandidate(order, now)) {
            return false;
        }
        order.setStatus(OrderStatusMachine.toCancelledStatus());
        order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
        order.setRemark(appendRemark(order.getRemark(), "No-show：逾期未到店"));
        iOrderService.updateById(order);
        couponOrderSupport.restoreCouponIfNeeded(order);
        if (order.getUserId() != null) {
            instationMessageSupport.sendUserMessage(order.getUserId(), "订单已关闭",
                    "订单 " + order.getOrderNo() + " 已过保留时间未到店，订单已关闭", order.getId());
        }
        return true;
    }

    private boolean applyHotelAutoCompleteIfNeeded(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getId() == null) {
            return false;
        }
        if (!"hotel".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            return false;
        }
        if (!Objects.equals(order.getStatus(), MerchantOrderStatusGroup.PENDING_USE)) {
            return false;
        }
        if (!HotelCancelPolicySupport.shouldAutoComplete(order, now)) {
            return false;
        }
        order.setStatus(OrderStatusMachine.toCompletedStatus());
        order.setStatusText(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.COMPLETED));
        iOrderService.updateById(order);
        if (order.getUserId() != null) {
            instationMessageSupport.sendUserMessage(order.getUserId(), "订单已完成",
                    "您的酒店订单 " + order.getOrderNo() + " 已离店完成，欢迎评价", order.getId());
        }
        return true;
    }

    private String appendRemark(String original, String extra) {
        if (StringUtils.isBlank(original)) {
            return extra;
        }
        return original + "；" + extra;
    }
}

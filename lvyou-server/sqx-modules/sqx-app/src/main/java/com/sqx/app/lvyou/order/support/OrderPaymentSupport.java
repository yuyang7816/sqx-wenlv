package com.sqx.app.lvyou.order.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.pay.enums.PayChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 订单支付落库与消息通知
 */
@Slf4j
@Component
public class OrderPaymentSupport {

    private static final String PAY_METHOD_WALLET = "wallet";

    @Resource
    private IOrderService iOrderService;

    @Resource
    private INotificationService notificationService;

    public OrderEntity requireUnpaidOrder(Long userId, String orderNo) {
        OrderEntity order = findOrderByNo(orderNo);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(AppError.ORDER_NOT_FOUND);
        }
        OrderStatusMachine.assertPayAllowed(order.getStatus());
        return order;
    }

    public void markOrderPaid(OrderEntity order, String payMethod, String tradeNo) {
        if (order.getStatus() != null && order.getStatus() != MerchantOrderStatusGroup.UNPAID) {
            log.info("订单已支付，跳过重复回调 orderNo={}", order.getOrderNo());
            return;
        }
        OrderStatusMachine.assertPayAllowed(order.getStatus());
        LocalDateTime payTime = LocalDateTime.now();
        LambdaUpdateWrapper<OrderEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(OrderEntity::getId, order.getId())
                .eq(OrderEntity::getStatus, MerchantOrderStatusGroup.UNPAID)
                .set(OrderEntity::getStatus, OrderStatusMachine.toPaidStatus(order.getOrderCategory()))
                .set(OrderEntity::getPayMethod, payMethod)
                .set(OrderEntity::getPayTime, payTime);
        OrderEntity updated = new OrderEntity();
        updated.setOrderCategory(order.getOrderCategory());
        updated.setStatus(OrderStatusMachine.toPaidStatus(order.getOrderCategory()));
        updateWrapper.set(OrderEntity::getStatusText, AppOrderStatusSupport.toDisplayStatus(updated));
        String category = MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory());
        if ("hotel".equals(category) || "route".equals(category)) {
            updateWrapper.set(OrderEntity::getVerifyCode, allocateVerifyCode());
        }
        if (!iOrderService.update(updateWrapper)) {
            log.info("订单支付状态更新冲突，可能已处理 orderNo={}", order.getOrderNo());
            return;
        }
        sendOrderPaidMessage(order.getUserId(), order.getId(), order.getOrderNo(), payMethod);
    }

    public OrderEntity findOrderByNo(String orderNo) {
        if (StringUtils.isBlank(orderNo)) {
            return null;
        }
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getOrderNo, orderNo).last("LIMIT 1");
        return iOrderService.getOne(queryWrapper, false);
    }

    private String allocateVerifyCode() {
        for (int attempt = 0; attempt < 8; attempt++) {
            String code = OrderVerifyCodeSupport.generateVerifyCode();
            LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(OrderEntity::getVerifyCode, code).last("LIMIT 1");
            if (iOrderService.getOne(queryWrapper, false) == null) {
                return code;
            }
        }
        return OrderVerifyCodeSupport.generateVerifyCode()
                + ThreadLocalRandom.current().nextInt(10, 99);
    }

    private void sendOrderPaidMessage(Long userId, Long orderId, String orderNo, String payMethod) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle("支付成功");
        request.setContent("订单 " + orderNo + " 已支付成功（" + payMethodLabel(payMethod) + "）");
        request.setType(2);
        request.setDataId(orderId);
        notificationService.send(request, NotificationChannel.INSTATION.code());
    }

    private String payMethodLabel(String payMethod) {
        if (Objects.equals(payMethod, PayChannel.WX.getValue())) {
            return "微信支付";
        }
        if (Objects.equals(payMethod, PayChannel.ALI.getValue())) {
            return "支付宝";
        }
        if (Objects.equals(payMethod, PAY_METHOD_WALLET)) {
            return "余额支付";
        }
        return payMethod;
    }
}

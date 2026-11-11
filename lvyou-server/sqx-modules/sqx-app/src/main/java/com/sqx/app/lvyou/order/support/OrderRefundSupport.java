package com.sqx.app.lvyou.order.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.wallet.db.service.IUserWalletTransactionService;
import com.sqx.app.lvyou.wallet.domain.entity.UserWalletTransactionEntity;
import com.sqx.app.lvyou.wallet.enums.WalletTxType;
import com.sqx.app.lvyou.wallet.service.WalletService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.pay.domain.RefundRequest;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.service.IPayService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 订单原路退款（线路等在线支付单）
 */
@Slf4j
@Component
public class OrderRefundSupport {

    private static final String PAY_METHOD_WALLET = "wallet";
    private static final int TX_SUCCESS = 1;

    @Resource
    private WalletService walletService;

    @Resource
    private IPayService iPayService;

    @Resource
    private IUserWalletTransactionService iUserWalletTransactionService;

    /** 订单是否已完成对用户退款（含余额流水幂等判断） */
    public boolean hasCompletedRefund(OrderEntity order) {
        if (order == null) {
            return false;
        }
        if (defaultDecimal(order.getRefundAmount()).compareTo(BigDecimal.ZERO) > 0) {
            return true;
        }
        return shouldRefundToWallet(order) && hasWalletRefund(order.getUserId(), order.getOrderNo());
    }

    /** 解析应退给用户的金额（优先订单实付，余额支付时回查消费流水） */
    public BigDecimal resolveUserRefundAmount(OrderEntity order, BigDecimal amount) {
        return resolveRefundAmount(order, amount);
    }

    public void refundToUser(OrderEntity order, BigDecimal amount, String title) {
        if (order == null) {
            return;
        }
        BigDecimal refundAmount = resolveRefundAmount(order, amount);
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        if (shouldRefundToWallet(order)) {
            if (hasWalletRefund(order.getUserId(), order.getOrderNo())) {
                log.info("钱包退款已处理，跳过 orderNo={}", order.getOrderNo());
                return;
            }
            walletService.refundBalance(order.getUserId(), refundAmount, order.getOrderNo(), title);
            return;
        }
        if (StringUtils.isBlank(order.getOrderNo())) {
            return;
        }
        String payMethod = StringUtils.defaultString(order.getPayMethod());
        try {
            RefundRequest request = new RefundRequest();
            request.setChannel(resolveChannel(payMethod));
            request.setOrderNo(order.getOrderNo());
            request.setAmount(defaultDecimal(order.getPayAmount()));
            request.setRefundAmount(refundAmount);
            request.setReason(title);
            iPayService.refund(request);
        } catch (Exception ex) {
            log.error("订单原路退款失败 orderNo={}", order.getOrderNo(), ex);
            throw new BizException(AppError.ORDER_REFUND_FAILED);
        }
    }

    private BigDecimal resolveRefundAmount(OrderEntity order, BigDecimal amount) {
        BigDecimal requested = defaultDecimal(amount);
        if (requested.compareTo(BigDecimal.ZERO) > 0) {
            return requested;
        }
        return resolveWalletConsumeAmount(order);
    }

    private boolean shouldRefundToWallet(OrderEntity order) {
        if (PAY_METHOD_WALLET.equalsIgnoreCase(StringUtils.defaultString(order.getPayMethod()))) {
            return true;
        }
        return resolveWalletConsumeAmount(order).compareTo(BigDecimal.ZERO) > 0;
    }

    private BigDecimal resolveWalletConsumeAmount(OrderEntity order) {
        if (order == null || order.getUserId() == null || StringUtils.isBlank(order.getOrderNo())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        LambdaQueryWrapper<UserWalletTransactionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserWalletTransactionEntity::getUserId, order.getUserId())
                .eq(UserWalletTransactionEntity::getRefNo, order.getOrderNo())
                .eq(UserWalletTransactionEntity::getTxType, WalletTxType.CONSUME)
                .eq(UserWalletTransactionEntity::getStatus, TX_SUCCESS)
                .orderByDesc(UserWalletTransactionEntity::getId)
                .last("LIMIT 1");
        UserWalletTransactionEntity consumeTx = iUserWalletTransactionService.getOne(queryWrapper, false);
        if (consumeTx == null || consumeTx.getAmount() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return consumeTx.getAmount().abs().setScale(2, RoundingMode.HALF_UP);
    }

    private boolean hasWalletRefund(Long userId, String orderNo) {
        if (userId == null || StringUtils.isBlank(orderNo)) {
            return false;
        }
        LambdaQueryWrapper<UserWalletTransactionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserWalletTransactionEntity::getUserId, userId)
                .eq(UserWalletTransactionEntity::getRefNo, orderNo)
                .eq(UserWalletTransactionEntity::getTxType, WalletTxType.INCOME)
                .gt(UserWalletTransactionEntity::getAmount, BigDecimal.ZERO)
                .eq(UserWalletTransactionEntity::getStatus, TX_SUCCESS);
        return iUserWalletTransactionService.count(queryWrapper) > 0;
    }

    private String resolveChannel(String payMethod) {
        if (PayChannel.WX.getValue().equalsIgnoreCase(payMethod)) {
            return PayChannel.WX.getValue();
        }
        if (PayChannel.ALI.getValue().equalsIgnoreCase(payMethod)) {
            return PayChannel.ALI.getValue();
        }
        return payMethod;
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value.setScale(2, RoundingMode.HALF_UP);
    }
}

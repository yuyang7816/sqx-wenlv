package com.sqx.app.base.pay.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.notification.core.enums.NotificationChannel;
import com.sqx.app.base.notification.core.model.NotificationRequest;
import com.sqx.app.base.notification.core.service.INotificationService;
import com.sqx.app.base.pay.business.service.PaymentBizService;
import com.sqx.app.base.pay.domain.BusinessParam;
import com.sqx.app.base.pay.enums.BusinessType;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.support.OrderPaymentSupport;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantDepositPayEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantDepositPayStatus;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantDepositPayService;
import com.sqx.app.lvyou.wallet.db.service.IWalletRechargeService;
import com.sqx.app.lvyou.wallet.domain.entity.WalletRechargeEntity;
import com.sqx.app.lvyou.wallet.enums.WalletRechargeStatus;
import com.sqx.app.lvyou.wallet.service.WalletService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.pay.enums.PayChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 支付业务编排实现
 */
@Slf4j
@Service
public class PaymentBizServiceImpl implements PaymentBizService {

    @Resource
    private OrderPaymentSupport orderPaymentSupport;

    @Resource
    private IWalletRechargeService iWalletRechargeService;

    @Resource
    private WalletService walletService;

    @Resource
    private INotificationService notificationService;

    @Resource
    private IMerchantDepositPayService iMerchantDepositPayService;

    @Resource
    private MerchantWalletService merchantWalletService;

    @Override
    public void validateBeforePay(BusinessParam param) {
        BusinessType businessType = BusinessType.fromCode(param.getBusinessType());
        if (businessType == BusinessType.TOURISM) {
            validateTourismPay(param);
            return;
        }
        if (businessType == BusinessType.WALLET_RECHARGE) {
            validateWalletRechargePay(param);
            return;
        }
        if (businessType == BusinessType.MERCHANT_DEPOSIT) {
            validateMerchantDepositPay(param);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void onChannelPaymentSuccess(String channel, String outTradeNo, String tradeNo) {
        if (StringUtils.isBlank(outTradeNo)) {
            log.warn("支付回调缺少 outTradeNo, channel={}", channel);
            return;
        }
        OrderEntity order = orderPaymentSupport.findOrderByNo(outTradeNo);
        if (order != null) {
            orderPaymentSupport.markOrderPaid(order, normalizePayMethod(channel), tradeNo);
            return;
        }
        WalletRechargeEntity recharge = findRechargeByNo(outTradeNo);
        if (recharge != null) {
            walletService.creditRecharge(recharge.getUserId(), recharge.getRechargeNo(),
                    normalizePayMethod(channel), tradeNo);
            sendRechargeSuccessMessage(recharge.getUserId(), recharge.getRechargeNo(), recharge.getAmount());
            return;
        }
        MerchantDepositPayEntity depositPay = findDepositPayByNo(outTradeNo);
        if (depositPay != null) {
            merchantWalletService.creditDepositFromChannel(depositPay.getUserId(), depositPay.getPayNo(),
                    normalizePayMethod(channel), tradeNo);
            sendDepositSuccessMessage(depositPay.getUserId(), depositPay.getPayNo(), depositPay.getAmount());
        }
    }

    @Override
    public void onChannelPaymentFailed(String channel, String outTradeNo, String tradeNo) {
        log.info("支付失败回调 channel={}, outTradeNo={}, tradeNo={}", channel, outTradeNo, tradeNo);
    }

    private void validateTourismPay(BusinessParam param) {
        if (param.getUserId() == null) {
            throw new BizException(AppError.PAY_USER_REQUIRED);
        }
        if (StringUtils.isBlank(param.getOrderNo())) {
            throw new BizException(AppError.PAY_ORDER_NO_REQUIRED);
        }
        OrderEntity order = orderPaymentSupport.requireUnpaidOrder(param.getUserId(), param.getOrderNo());
        assertAmountMatch(param.getAmount(), order.getPayAmount());
        param.setOrderNo(order.getOrderNo());
        param.setAmount(scaleAmount(order.getPayAmount()));
    }

    private void validateWalletRechargePay(BusinessParam param) {
        if (param.getUserId() == null) {
            throw new BizException(AppError.PAY_USER_REQUIRED);
        }
        if (StringUtils.isBlank(param.getOrderNo())) {
            throw new BizException(AppError.WALLET_RECHARGE_NO_REQUIRED);
        }
        WalletRechargeEntity recharge = requirePendingRecharge(param.getUserId(), param.getOrderNo());
        assertAmountMatch(param.getAmount(), recharge.getAmount());
        param.setOrderNo(recharge.getRechargeNo());
        param.setAmount(scaleAmount(recharge.getAmount()));
    }

    private void validateMerchantDepositPay(BusinessParam param) {
        if (param.getUserId() == null) {
            throw new BizException(AppError.PAY_USER_REQUIRED);
        }
        if (StringUtils.isBlank(param.getOrderNo())) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_NO_REQUIRED);
        }
        MerchantDepositPayEntity depositPay = requirePendingDepositPay(param.getUserId(), param.getOrderNo());
        assertAmountMatch(param.getAmount(), depositPay.getAmount());
        param.setOrderNo(depositPay.getPayNo());
        param.setAmount(scaleAmount(depositPay.getAmount()));
    }

    private WalletRechargeEntity requirePendingRecharge(Long userId, String rechargeNo) {
        WalletRechargeEntity recharge = findRechargeByNo(rechargeNo);
        if (recharge == null || !Objects.equals(recharge.getUserId(), userId)) {
            throw new BizException(AppError.WALLET_RECHARGE_NOT_FOUND);
        }
        if (!Objects.equals(recharge.getStatus(), WalletRechargeStatus.PENDING_PAY)) {
            throw new BizException(AppError.WALLET_RECHARGE_STATUS_INVALID);
        }
        return recharge;
    }

    private WalletRechargeEntity findRechargeByNo(String rechargeNo) {
        if (StringUtils.isBlank(rechargeNo)) {
            return null;
        }
        LambdaQueryWrapper<WalletRechargeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WalletRechargeEntity::getRechargeNo, rechargeNo).last("LIMIT 1");
        return iWalletRechargeService.getOne(queryWrapper, false);
    }

    private MerchantDepositPayEntity requirePendingDepositPay(Long userId, String payNo) {
        MerchantDepositPayEntity depositPay = findDepositPayByNo(payNo);
        if (depositPay == null || !Objects.equals(depositPay.getUserId(), userId)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_NOT_FOUND);
        }
        if (!Objects.equals(depositPay.getStatus(), MerchantDepositPayStatus.PENDING_PAY)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_STATUS_INVALID);
        }
        return depositPay;
    }

    private MerchantDepositPayEntity findDepositPayByNo(String payNo) {
        if (StringUtils.isBlank(payNo)) {
            return null;
        }
        LambdaQueryWrapper<MerchantDepositPayEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantDepositPayEntity::getPayNo, payNo.trim()).last("LIMIT 1");
        return iMerchantDepositPayService.getOne(queryWrapper, false);
    }

    private void assertAmountMatch(BigDecimal requestAmount, BigDecimal expectedAmount) {
        BigDecimal expected = scaleAmount(expectedAmount);
        BigDecimal request = scaleAmount(requestAmount);
        if (expected.compareTo(request) != 0) {
            throw new BizException(AppError.ORDER_AMOUNT_MISMATCH);
        }
    }

    private BigDecimal scaleAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizePayMethod(String channel) {
        if (Objects.equals(channel, PayChannel.WX.getValue())) {
            return PayChannel.WX.getValue();
        }
        if (Objects.equals(channel, PayChannel.ALI.getValue())) {
            return PayChannel.ALI.getValue();
        }
        return channel;
    }

    private void sendRechargeSuccessMessage(Long userId, String rechargeNo, BigDecimal amount) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle("充值成功");
        request.setContent("充值单 " + rechargeNo + " 已到账，金额 ¥" + scaleAmount(amount));
        request.setType(2);
        notificationService.send(request, NotificationChannel.INSTATION.code());
    }

    private void sendDepositSuccessMessage(Long userId, String payNo, BigDecimal amount) {
        NotificationRequest request = new NotificationRequest();
        request.setUserId(userId);
        request.setTitle("保证金缴纳成功");
        request.setContent("支付单 " + payNo + " 已完成，金额 ¥" + scaleAmount(amount));
        request.setType(2);
        notificationService.send(request, NotificationChannel.INSTATION.code());
    }
}

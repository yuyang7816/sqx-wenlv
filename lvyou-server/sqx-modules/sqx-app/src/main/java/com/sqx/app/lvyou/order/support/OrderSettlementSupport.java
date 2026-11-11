package com.sqx.app.lvyou.order.support;

import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.enums.OrderType;
import com.sqx.app.lvyou.route.support.RouteCancelPolicySupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

/**
 * 订单结算：抽成计算 + 商户钱包入账
 */
@Component
public class OrderSettlementSupport {

    public static class SettlementQuote {
        private BigDecimal grossKeep;
        private BigDecimal platformFee;
        private BigDecimal merchantIncome;
        private int commissionRate;

        public BigDecimal getGrossKeep() {
            return grossKeep;
        }

        public BigDecimal getPlatformFee() {
            return platformFee;
        }

        public BigDecimal getMerchantIncome() {
            return merchantIncome;
        }

        public int getCommissionRate() {
            return commissionRate;
        }
    }

    @Resource
    private MerchantWalletService merchantWalletService;

    @Resource
    private IOrderService iOrderService;

    @Resource
    private OrderRefundSupport orderRefundSupport;

    /** 核销完成结算（酒店 + 线路） */
    public void settleOnVerifyComplete(OrderEntity order) {
        if (order == null || order.getMerchantId() == null || isSettled(order)) {
            return;
        }
        BigDecimal grossKeep = scale(order.getPayAmount());
        if (grossKeep.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        SettlementQuote quote = calcSettlement(grossKeep, resolveCommissionRate(order));
        applySettlement(order, quote, null, "订单核销结算");
    }

    /** 好物/文创订单完成结算（确认收货或商户完成） */
    public void settleOnGoodsComplete(OrderEntity order) {
        if (order == null || order.getMerchantId() == null || isSettled(order)) {
            return;
        }
        if (!MerchantOrderStatusSupport.isGoodsCategory(order.getOrderCategory())) {
            return;
        }
        if (order.getStatus() == null || order.getStatus() != MerchantOrderStatusGroup.COMPLETED) {
            return;
        }
        BigDecimal grossKeep = scale(order.getPayAmount());
        if (grossKeep.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        SettlementQuote quote = calcSettlement(grossKeep, resolveCommissionRate(order));
        applySettlement(order, quote, null, "好物订单完成结算");
    }

    /** 线路取消/退款完成：原路退用户 + 扣留部分结算给商户 */
    public void settleOnRouteRefundComplete(OrderEntity order, RouteCancelPolicySupport.RefundQuote refundQuote) {
        if (order == null || refundQuote == null) {
            return;
        }
        reverseSettleIfNeeded(order);
        BigDecimal userRefund = scale(refundQuote.getUserRefund());
        if (userRefund.compareTo(BigDecimal.ZERO) > 0) {
            orderRefundSupport.refundToUser(order, userRefund, "线路订单退款");
        }
        order.setRefundAmount(userRefund);
        order.setPlatformFeeAmount(scale(refundQuote.getPlatformFee()));
        order.setMerchantSettleAmount(scale(refundQuote.getMerchantIncome()));
        if (refundQuote.getMerchantIncome() != null
                && refundQuote.getMerchantIncome().compareTo(BigDecimal.ZERO) > 0
                && order.getMerchantId() != null) {
            merchantWalletService.creditSettle(order.getMerchantId(), refundQuote.getMerchantIncome(),
                    order.getOrderNo(), "线路退款扣留结算");
            order.setSettleTime(LocalDateTime.now());
        }
        iOrderService.updateById(order);
    }

    /** 酒店免费取消：全额退实付（余额/微信/支付宝原路退回） */
    public void settleOnHotelCancelRefund(OrderEntity order) {
        refundPaidOrderToUser(order, "酒店订单取消退款");
    }

    /**
     * 已支付订单全额退还给用户（优惠券由 CouponOrderSupport 单独退还）
     */
    public void refundPaidOrderToUser(OrderEntity order, String title) {
        if (order == null || order.getPayTime() == null) {
            return;
        }
        if (orderRefundSupport.hasCompletedRefund(order)) {
            return;
        }
        BigDecimal payAmount = scale(order.getPayAmount());
        BigDecimal refundAmount = orderRefundSupport.resolveUserRefundAmount(order, payAmount);
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        reverseSettleIfNeeded(order);
        orderRefundSupport.refundToUser(order, refundAmount, title);
        order.setRefundAmount(refundAmount);
        iOrderService.updateById(order);
    }

    public SettlementQuote calcSettlement(BigDecimal grossKeep, int commissionRate) {
        SettlementQuote quote = new SettlementQuote();
        BigDecimal base = scale(grossKeep);
        int rate = clampRate(commissionRate);
        BigDecimal platformFee = base.multiply(BigDecimal.valueOf(rate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal merchantIncome = base.subtract(platformFee).max(BigDecimal.ZERO);
        quote.grossKeep = base;
        quote.platformFee = platformFee;
        quote.merchantIncome = merchantIncome;
        quote.commissionRate = rate;
        return quote;
    }

    /** 平台强制退款：refundRatio 表示退用户比例（0-100） */
    public RouteCancelPolicySupport.RefundQuote calcRefundSettlement(BigDecimal payAmount,
                                                                     int refundRatio,
                                                                     int commissionRate) {
        RouteCancelPolicySupport.RefundQuote quote = new RouteCancelPolicySupport.RefundQuote();
        BigDecimal pay = scale(payAmount);
        int userRefundRatio = clampRate(refundRatio);
        int deductRatio = 100 - userRefundRatio;
        RouteCancelPolicySupport.applyRefundAmounts(quote, pay, deductRatio, clampRate(commissionRate));
        return quote;
    }

    public int resolveCommissionRate(OrderEntity order) {
        OrderType type = OrderTypeSupport.resolve(order);
        if (type == OrderType.HOTEL) {
            return readRate(SysConfigConstant.Order.PLATFORM_COMMISSION_RATE_HOTEL);
        }
        if (type == OrderType.ROUTE) {
            return readRate(SysConfigConstant.Order.PLATFORM_COMMISSION_RATE_ROUTE);
        }
        return 0;
    }

    private void applySettlement(OrderEntity order, SettlementQuote quote, BigDecimal refundAmount, String title) {
        order.setRefundAmount(refundAmount);
        order.setPlatformFeeAmount(quote.getPlatformFee());
        order.setMerchantSettleAmount(quote.getMerchantIncome());
        order.setSettleTime(LocalDateTime.now());
        iOrderService.updateById(order);
        if (quote.getMerchantIncome().compareTo(BigDecimal.ZERO) > 0) {
            merchantWalletService.creditSettle(order.getMerchantId(), quote.getMerchantIncome(),
                    order.getOrderNo(), title);
        }
    }

    private void reverseSettleIfNeeded(OrderEntity order) {
        if (!isSettled(order) || order.getMerchantId() == null) {
            return;
        }
        BigDecimal settled = scale(order.getMerchantSettleAmount());
        if (settled.compareTo(BigDecimal.ZERO) > 0) {
            merchantWalletService.debitSettle(order.getMerchantId(), settled, order.getOrderNo(), "订单退款冲正");
        }
        order.setSettleTime(null);
        order.setMerchantSettleAmount(BigDecimal.ZERO);
        order.setPlatformFeeAmount(BigDecimal.ZERO);
    }

    private boolean isSettled(OrderEntity order) {
        return order.getSettleTime() != null
                && order.getMerchantSettleAmount() != null
                && order.getMerchantSettleAmount().compareTo(BigDecimal.ZERO) > 0;
    }

    private int readRate(String key) {
        return clampRate(parseInt(SysConfigUtil.getConfigValueOrNull(key), 0));
    }

    private int parseInt(String value, int fallback) {
        if (StringUtils.isBlank(value)) {
            return fallback;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }

    private int clampRate(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 100);
    }

    private BigDecimal scale(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}

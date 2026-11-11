package com.sqx.app.lvyou.route.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 线路退改规则引擎（仅 route，不影响酒店）
 */
@Component
public class RouteCancelPolicySupport {

    public enum RefundScene {
        BEFORE_TRIP,
        ON_TRIP_DAY,
        AFTER_TRIP,
        NOSHOW,
        WEATHER_FULL
    }

    public static class RefundQuote {
        /** 扣款比例 0-100（从实付中扣留，余款退用户） */
        private int refundRatio;
        /** 退还用户比例 0-100 */
        private int userRefundRatio;
        private int commissionRate;
        private BigDecimal payAmount;
        private BigDecimal userRefund;
        private BigDecimal grossKeep;
        private BigDecimal platformFee;
        private BigDecimal merchantIncome;
        private String ruleLabel;

        public int getRefundRatio() { return refundRatio; }
        public void setRefundRatio(int refundRatio) { this.refundRatio = refundRatio; }
        public int getUserRefundRatio() { return userRefundRatio; }
        public void setUserRefundRatio(int userRefundRatio) { this.userRefundRatio = userRefundRatio; }
        public int getCommissionRate() { return commissionRate; }
        public void setCommissionRate(int commissionRate) { this.commissionRate = commissionRate; }
        public BigDecimal getPayAmount() { return payAmount; }
        public void setPayAmount(BigDecimal payAmount) { this.payAmount = payAmount; }
        public BigDecimal getUserRefund() { return userRefund; }
        public void setUserRefund(BigDecimal userRefund) { this.userRefund = userRefund; }
        public BigDecimal getGrossKeep() { return grossKeep; }
        public void setGrossKeep(BigDecimal grossKeep) { this.grossKeep = grossKeep; }
        public BigDecimal getPlatformFee() { return platformFee; }
        public void setPlatformFee(BigDecimal platformFee) { this.platformFee = platformFee; }
        public BigDecimal getMerchantIncome() { return merchantIncome; }
        public void setMerchantIncome(BigDecimal merchantIncome) { this.merchantIncome = merchantIncome; }
        public String getRuleLabel() { return ruleLabel; }
        public void setRuleLabel(String ruleLabel) { this.ruleLabel = ruleLabel; }
    }

    /** 历史仅预约单：无支付时间 */
    public boolean isLegacyRouteBooking(OrderEntity order) {
        if (order == null || !"route".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            return false;
        }
        return order.getPayTime() == null;
    }

    /** 在线支付线路单 */
    public boolean isPaidRouteOrder(OrderEntity order) {
        if (order == null || !"route".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            return false;
        }
        return order.getPayTime() != null;
    }

    public RefundScene resolveScene(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getTravelDate() == null) {
            return RefundScene.BEFORE_TRIP;
        }
        LocalDate today = (now == null ? LocalDate.now() : now.toLocalDate());
        LocalDate travelDate = order.getTravelDate();
        if (today.isBefore(travelDate)) {
            return RefundScene.BEFORE_TRIP;
        }
        if (today.isAfter(travelDate)) {
            return RefundScene.AFTER_TRIP;
        }
        if (isNoShowCandidate(order, now)) {
            return RefundScene.NOSHOW;
        }
        return RefundScene.ON_TRIP_DAY;
    }

    public int resolveRefundRatio(OrderEntity order, LocalDateTime now) {
        return resolveRefundRatio(order, resolveScene(order, now), now);
    }

    public int resolveRefundRatio(OrderEntity order, RefundScene scene, LocalDateTime now) {
        if (scene == RefundScene.WEATHER_FULL) {
            return 0;
        }
        if (scene == RefundScene.BEFORE_TRIP) {
            return readRatio(SysConfigConstant.Order.ROUTE_REFUND_RATIO_BEFORE_TRIP, 0);
        }
        if (scene == RefundScene.ON_TRIP_DAY) {
            return readRatio(SysConfigConstant.Order.ROUTE_REFUND_RATIO_ON_TRIP_DAY, 0);
        }
        if (scene == RefundScene.NOSHOW) {
            String noshow = StringUtils.trimToEmpty(SysConfigUtil.getConfigValueOrNull(
                    SysConfigConstant.Order.ROUTE_REFUND_RATIO_NOSHOW));
            if (StringUtils.isNotBlank(noshow)) {
                return clampRatio(parseInt(noshow, 0));
            }
            return readRatio(SysConfigConstant.Order.ROUTE_REFUND_RATIO_AFTER_TRIP, 0);
        }
        return readRatio(SysConfigConstant.Order.ROUTE_REFUND_RATIO_AFTER_TRIP, 0);
    }

    /** ratio=0 时可走「取消」；ratio>0 须走「申请退款」 */
    public boolean canDirectCancel(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getVerifyTime() != null) {
            return false;
        }
        RefundScene scene = resolveScene(order, now);
        if (scene == RefundScene.AFTER_TRIP || scene == RefundScene.NOSHOW) {
            return false;
        }
        return resolveRefundRatio(order, scene, now) == 0;
    }

    public boolean canApplyRefund(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getVerifyTime() != null || !isPaidRouteOrder(order)) {
            return false;
        }
        RefundScene scene = resolveScene(order, now);
        if (scene != RefundScene.AFTER_TRIP && scene != RefundScene.NOSHOW) {
            return resolveRefundRatio(order, scene, now) > 0;
        }
        return resolveRefundRatio(order, scene, now) >= 0;
    }

    public RefundQuote quoteRefund(OrderEntity order, LocalDateTime now) {
        return quoteRefund(order, resolveScene(order, now), now);
    }

    public RefundQuote quoteRefund(OrderEntity order, RefundScene scene, LocalDateTime now) {
        RefundQuote quote = new RefundQuote();
        BigDecimal payAmount = scaleAmount(order == null ? BigDecimal.ZERO : order.getPayAmount());
        int deductRatio = resolveRefundRatio(order, scene, now);
        int commissionRate = readRatio(SysConfigConstant.Order.PLATFORM_COMMISSION_RATE_ROUTE, 0);
        applyRefundAmounts(quote, payAmount, deductRatio, commissionRate);
        quote.ruleLabel = ruleLabelOf(scene);
        return quote;
    }

    /**
     * 按扣款比例计算退款分账。
     *
     * @param deductRatio 扣款比例 0-100（从实付中扣留，余款退用户；0 表示全额退用户）
     */
    public static void applyRefundAmounts(RefundQuote quote, BigDecimal payAmount,
                                          int deductRatio, int commissionRate) {
        if (quote == null) {
            return;
        }
        BigDecimal pay = scaleAmount(payAmount);
        int deduct = clampStaticRatio(deductRatio);
        int userRatio = 100 - deduct;
        BigDecimal grossKeep = pay.multiply(BigDecimal.valueOf(deduct))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal userRefund = pay.subtract(grossKeep).max(BigDecimal.ZERO);
        int rate = clampStaticRatio(commissionRate);
        BigDecimal platformFee = grossKeep.multiply(BigDecimal.valueOf(rate))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal merchantIncome = grossKeep.subtract(platformFee).max(BigDecimal.ZERO);

        quote.refundRatio = deduct;
        quote.userRefundRatio = userRatio;
        quote.commissionRate = rate;
        quote.payAmount = pay;
        quote.userRefund = userRefund;
        quote.grossKeep = grossKeep;
        quote.platformFee = platformFee;
        quote.merchantIncome = merchantIncome;
    }

    public String policySummary(OrderEntity order) {
        String template = SysConfigUtil.getConfigValueOrNull(SysConfigConstant.Order.ROUTE_REFUND_RULE_SUMMARY);
        if (StringUtils.isNotBlank(template)) {
            return template;
        }
        if (order != null && StringUtils.isNotBlank(order.getCancelPolicyJson())) {
            Map<String, Object> map = parsePolicy(order.getCancelPolicyJson());
            Object summary = map.get("summary");
            if (summary != null && StringUtils.isNotBlank(String.valueOf(summary))) {
                return String.valueOf(summary);
            }
        }
        return "出行前可免费取消；已核销订单不支持在线退款";
    }

    public String buildSnapshotJson() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("summary", policySummary(null));
        map.put("beforeTripRatio", readRatio(SysConfigConstant.Order.ROUTE_REFUND_RATIO_BEFORE_TRIP, 0));
        map.put("onTripDayRatio", readRatio(SysConfigConstant.Order.ROUTE_REFUND_RATIO_ON_TRIP_DAY, 0));
        map.put("afterTripRatio", readRatio(SysConfigConstant.Order.ROUTE_REFUND_RATIO_AFTER_TRIP, 0));
        map.put("meetHoldHour", readMeetHoldHour());
        return JSON.toJSONString(map);
    }

    public boolean isNoShowCandidate(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getVerifyTime() != null || order.getTravelDate() == null) {
            return false;
        }
        LocalDateTime nowTime = now == null ? LocalDateTime.now() : now;
        if (!nowTime.toLocalDate().equals(order.getTravelDate())) {
            return false;
        }
        int holdHour = readMeetHoldHour();
        LocalDateTime holdUntil = order.getTravelDate().atTime(holdHour, 0);
        return !nowTime.isBefore(holdUntil);
    }

    public int readMeetHoldHour() {
        return clampHour(parseInt(SysConfigUtil.getConfigValueOrNull(SysConfigConstant.Order.ROUTE_MEET_HOLD_HOUR), 10));
    }

    private Map<String, Object> parsePolicy(String json) {
        try {
            Map<String, Object> map = JSON.parseObject(json, new TypeReference<Map<String, Object>>() {
            });
            return map == null ? new LinkedHashMap<>() : map;
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    private String ruleLabelOf(RefundScene scene) {
        switch (scene) {
            case BEFORE_TRIP:
                return "出行日前未核销";
            case ON_TRIP_DAY:
                return "出行日当天未核销";
            case AFTER_TRIP:
                return "出行日已过未核销";
            case NOSHOW:
                return "超过集合保留时间未核销";
            case WEATHER_FULL:
                return "商户天气/不可抗力全额退";
            default:
                return "线路退改规则";
        }
    }

    private int readRatio(String key, int fallback) {
        return clampRatio(parseInt(SysConfigUtil.getConfigValueOrNull(key), fallback));
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

    private int clampRatio(int value) {
        return clampStaticRatio(value);
    }

    private static int clampStaticRatio(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 100);
    }

    private int clampHour(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 23);
    }

    private BigDecimal scale(BigDecimal amount) {
        return scaleAmount(amount);
    }

    private static BigDecimal scaleAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}

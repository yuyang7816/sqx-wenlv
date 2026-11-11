package com.sqx.app.lvyou.invite.support;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 邀请分销工具
 */
public final class InviteSupport {

    public static final String COMMISSION_STATUS_SETTLED = "settled";
    public static final int LEVEL1 = 1;
    public static final int LEVEL2 = 2;

    private static final BigDecimal DEFAULT_RATE_LEVEL1 = new BigDecimal("0.05");
    private static final BigDecimal DEFAULT_RATE_LEVEL2 = new BigDecimal("0.02");

    private InviteSupport() {
    }

    /**
     * 解析并存储用户自定义比例；blank 表示清除自定义（使用系统默认）
     */
    public static BigDecimal parseStoredRate(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        try {
            BigDecimal rate = new BigDecimal(raw.trim());
            if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                return null;
            }
            if (rate.compareTo(BigDecimal.ONE) > 0) {
                return rate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            }
            return rate.setScale(4, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return null;
        }
    }

    public static BigDecimal resolveCommissionRate(BigDecimal userRate, String defaultConfigValue, BigDecimal fallbackDefault) {
        if (userRate != null && userRate.compareTo(BigDecimal.ZERO) > 0) {
            return userRate.setScale(4, RoundingMode.HALF_UP);
        }
        return parseRate(defaultConfigValue, fallbackDefault);
    }

    public static String formatRatePlain(BigDecimal rate) {
        if (rate == null) {
            return "";
        }
        return rate.stripTrailingZeros().toPlainString();
    }

    public static String formatRatePercent(BigDecimal rate) {
        if (rate == null) {
            return "-";
        }
        return rate.multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString() + "%";
    }

    public static BigDecimal parseRate(String raw, BigDecimal defaultRate) {
        if (StringUtils.isBlank(raw)) {
            return defaultRate;
        }
        try {
            BigDecimal rate = new BigDecimal(raw.trim());
            if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                return defaultRate;
            }
            if (rate.compareTo(BigDecimal.ONE) > 0) {
                return rate.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
            }
            return rate.setScale(4, RoundingMode.HALF_UP);
        } catch (Exception ex) {
            return defaultRate;
        }
    }

    public static BigDecimal defaultRateLevel1() {
        return DEFAULT_RATE_LEVEL1;
    }

    public static BigDecimal defaultRateLevel2() {
        return DEFAULT_RATE_LEVEL2;
    }

    public static BigDecimal calcCommission(BigDecimal orderAmount, BigDecimal rate) {
        if (orderAmount == null || rate == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        if (orderAmount.compareTo(BigDecimal.ZERO) <= 0 || rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return orderAmount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatMoney(BigDecimal amount) {
        if (amount == null) {
            return "0.00";
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static String maskPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return "";
        }
        String digits = phone.trim();
        if (digits.length() >= 7) {
            return digits.substring(0, 3) + "****" + digits.substring(digits.length() - 4);
        }
        return digits;
    }

    public static String buildQrContent(String shareLink, String inviteCode) {
        if (StringUtils.isBlank(inviteCode)) {
            return StringUtils.defaultString(shareLink);
        }
        String base = StringUtils.defaultIfBlank(shareLink, "");
        if (StringUtils.isBlank(base)) {
            return inviteCode;
        }
        if (base.contains("code=")) {
            return base;
        }
        final String joiner = base.contains("?") ? "&" : "?";
        return base + joiner + "code=" + inviteCode;
    }
}

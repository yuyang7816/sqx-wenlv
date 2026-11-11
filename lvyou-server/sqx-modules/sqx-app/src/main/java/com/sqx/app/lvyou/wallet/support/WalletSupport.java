package com.sqx.app.lvyou.wallet.support;

import com.sqx.app.lvyou.wallet.domain.entity.UserWalletTransactionEntity;
import com.sqx.app.lvyou.wallet.enums.WithdrawMethod;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;

/**
 * 钱包金额与展示辅助
 */
public final class WalletSupport {

    public static final BigDecimal MIN_WITHDRAW_AMOUNT = new BigDecimal("10.00");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int TX_SUCCESS = 1;
    private static final int TX_PENDING = 0;
    private static final int TX_FAILED = 2;

    private WalletSupport() {
    }

    public static BigDecimal scaleAmount(BigDecimal amount) {
        if (amount == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }

    public static String formatMoney(BigDecimal amount) {
        return scaleAmount(amount).toPlainString();
    }

    public static String maskAccount(String account) {
        String text = StringUtils.trimToEmpty(account);
        if (text.contains("@")) {
            int atIndex = text.indexOf('@');
            String name = text.substring(0, atIndex);
            String domain = text.substring(atIndex + 1);
            if (name.length() <= 2) {
                return name.charAt(0) + "***@" + domain;
            }
            return name.substring(0, 2) + "***@" + domain;
        }
        if (text.length() >= 7) {
            return text.substring(0, 3) + "****" + text.substring(text.length() - 4);
        }
        return text;
    }

    public static String resolveWithdrawAccountDisplay(String method, String accountRaw, String qrCodeUrl) {
        if (WithdrawMethod.WECHAT.equals(method)) {
            return StringUtils.isNotBlank(qrCodeUrl) ? "微信收款码" : "微信";
        }
        return maskAccount(accountRaw);
    }

    public static String txStatusToCode(Integer status) {
        if (status == null || status == TX_SUCCESS) {
            return "success";
        }
        if (status == TX_PENDING) {
            return "pending";
        }
        if (status == TX_FAILED) {
            return "failed";
        }
        return "success";
    }

    public static int txStatusToInt(String status) {
        if ("pending".equals(status)) {
            return TX_PENDING;
        }
        if ("failed".equals(status)) {
            return TX_FAILED;
        }
        return TX_SUCCESS;
    }

    public static String formatTime(UserWalletTransactionEntity entity) {
        if (entity == null || entity.getCreateTime() == null) {
            return "";
        }
        return entity.getCreateTime().format(TIME_FORMATTER);
    }
}

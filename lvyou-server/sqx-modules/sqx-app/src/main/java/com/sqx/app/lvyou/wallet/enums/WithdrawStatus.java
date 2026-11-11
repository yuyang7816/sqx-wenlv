package com.sqx.app.lvyou.wallet.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 提现状态
 */
public final class WithdrawStatus {

    public static final String PENDING = "pending";
    public static final String SUCCESS = "success";
    public static final String FAILED = "failed";

    private WithdrawStatus() {
    }

    public static String normalize(String status) {
        String value = StringUtils.lowerCase(StringUtils.trimToEmpty(status));
        if (PENDING.equals(value) || SUCCESS.equals(value) || FAILED.equals(value)) {
            return value;
        }
        return null;
    }
}

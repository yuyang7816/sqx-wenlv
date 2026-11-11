package com.sqx.app.lvyou.wallet.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 提现方式
 */
public final class WithdrawMethod {

    public static final String ALIPAY = "alipay";
    public static final String WECHAT = "wechat";

    private WithdrawMethod() {
    }

    public static String normalize(String method) {
        String value = StringUtils.lowerCase(StringUtils.trimToEmpty(method));
        if (ALIPAY.equals(value) || WECHAT.equals(value)) {
            return value;
        }
        return null;
    }
}

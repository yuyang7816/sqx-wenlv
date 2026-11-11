package com.sqx.app.lvyou.wallet.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 提现主体类型
 */
public final class WithdrawOwnerType {

    public static final String USER = "user";
    public static final String MERCHANT = "merchant";

    private WithdrawOwnerType() {
    }

    public static String normalize(String ownerType) {
        String value = StringUtils.lowerCase(StringUtils.trimToEmpty(ownerType));
        if (USER.equals(value) || MERCHANT.equals(value)) {
            return value;
        }
        return null;
    }
}

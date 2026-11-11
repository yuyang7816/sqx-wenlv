package com.sqx.app.lvyou.order.support;

import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.ThreadLocalRandom;

/**
 * 酒店订单核销码
 */
public final class OrderVerifyCodeSupport {

    private OrderVerifyCodeSupport() {
    }

    public static String generateVerifyCode() {
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1_000_000));
    }

    public static String resolveVerifyCode(String orderNo, String storedCode) {
        if (StringUtils.isNotBlank(storedCode)) {
            return storedCode.trim();
        }
        if (StringUtils.isBlank(orderNo)) {
            return "";
        }
        String digits = orderNo.replaceAll("\\D", "");
        if (digits.length() >= 6) {
            return digits.substring(digits.length() - 6);
        }
        return String.format("%06d", Integer.parseInt(StringUtils.defaultIfBlank(digits, "0")));
    }
}

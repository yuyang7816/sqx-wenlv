package com.sqx.app.lvyou.coupon.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 优惠券适用范围
 */
public enum CouponScope {

    ALL("all", "全场通用"),
    HOTEL("hotel", "酒店预订"),
    ROUTE("route", "旅游线路"),
    GOODS("goods", "非遗好物");

    private final String code;
    private final String label;

    CouponScope(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        for (CouponScope scope : values()) {
            if (scope.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public static String labelOf(String code) {
        if (StringUtils.isBlank(code)) {
            return ALL.label;
        }
        for (CouponScope scope : values()) {
            if (scope.code.equalsIgnoreCase(code)) {
                return scope.label;
            }
        }
        return code;
    }
}

package com.sqx.app.lvyou.coupon.enums;

/**
 * 优惠券模板状态
 */
public enum CouponTemplateStatus {

    OFFLINE(0, "下架"),
    ONLINE(1, "可领取");

    private final int code;
    private final String label;

    CouponTemplateStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(Integer code) {
        if (code == null) {
            return "";
        }
        for (CouponTemplateStatus status : values()) {
            if (status.code == code) {
                return status.label;
            }
        }
        return String.valueOf(code);
    }
}

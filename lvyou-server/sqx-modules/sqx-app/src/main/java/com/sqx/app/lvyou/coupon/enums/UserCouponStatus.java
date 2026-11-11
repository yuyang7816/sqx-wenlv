package com.sqx.app.lvyou.coupon.enums;

/**
 * 用户优惠券状态（数据库存储值）
 */
public enum UserCouponStatus {

    UNUSED(1, "available", "未使用"),
    USED(2, "used", "已使用"),
    EXPIRED(3, "expired", "已过期");

    private final int code;
    private final String appCode;
    private final String label;

    UserCouponStatus(int code, String appCode, String label) {
        this.code = code;
        this.appCode = appCode;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getAppCode() {
        return appCode;
    }

    public String getLabel() {
        return label;
    }

    public static UserCouponStatus fromCode(Integer code) {
        if (code == null) {
            return UNUSED;
        }
        for (UserCouponStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return UNUSED;
    }

    public static UserCouponStatus fromAppCode(String appCode) {
        if (appCode == null) {
            return null;
        }
        for (UserCouponStatus status : values()) {
            if (status.appCode.equalsIgnoreCase(appCode)) {
                return status;
            }
        }
        return null;
    }
}

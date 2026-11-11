package com.sqx.app.base.user.cancel.enums;

/**
 * 用户注销申请状态
 */
public enum CancelAccountStatus {

    PENDING("pending", "待审核"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回");

    private final String code;
    private final String label;

    CancelAccountStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static String labelOf(String code) {
        if (code == null) {
            return "";
        }
        for (CancelAccountStatus status : values()) {
            if (status.code.equals(code)) {
                return status.label;
            }
        }
        return code;
    }

    public static String normalize(String code) {
        if (code == null) {
            return null;
        }
        String normalized = code.trim().toLowerCase();
        for (CancelAccountStatus status : values()) {
            if (status.code.equals(normalized)) {
                return status.code;
            }
        }
        return null;
    }
}

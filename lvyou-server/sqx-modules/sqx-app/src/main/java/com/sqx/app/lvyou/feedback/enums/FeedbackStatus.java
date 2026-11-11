package com.sqx.app.lvyou.feedback.enums;

/**
 * 反馈处理状态
 */
public enum FeedbackStatus {

    PENDING("pending", "待处理"),
    PROCESSED("processed", "已处理");

    private final String code;
    private final String label;

    FeedbackStatus(String code, String label) {
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
        for (FeedbackStatus status : values()) {
            if (status.code.equals(code)) {
                return status.label;
            }
        }
        return code;
    }
}

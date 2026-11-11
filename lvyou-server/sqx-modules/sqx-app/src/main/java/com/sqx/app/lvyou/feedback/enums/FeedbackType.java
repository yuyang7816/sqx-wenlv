package com.sqx.app.lvyou.feedback.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 反馈类型（与用户端选项一致）
 */
public enum FeedbackType {

    BUG("功能异常"),
    UI("界面体验"),
    ORDER("订单问题"),
    SUGGEST("建议反馈"),
    OTHER("其他");

    private final String label;

    FeedbackType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(String label) {
        if (StringUtils.isBlank(label)) {
            return false;
        }
        for (FeedbackType type : values()) {
            if (type.label.equals(label.trim())) {
                return true;
            }
        }
        return false;
    }
}

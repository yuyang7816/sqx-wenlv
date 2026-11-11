package com.sqx.app.lvyou.notice.enums;

import lombok.Getter;

/**
 * 公告分类
 */
@Getter
public enum NoticeCategory {

    ACTIVITY("activity", "活动"),
    SCENIC("scenic", "景区"),
    TRAFFIC("traffic", "交通"),
    POLICY("policy", "政策");

    private final String code;
    private final String label;

    NoticeCategory(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(String category) {
        if (category == null) {
            return "";
        }
        for (NoticeCategory item : values()) {
            if (item.code.equals(category)) {
                return item.label;
            }
        }
        return category;
    }

    public static boolean isValid(String category) {
        if (category == null) {
            return false;
        }
        for (NoticeCategory item : values()) {
            if (item.code.equals(category)) {
                return true;
            }
        }
        return false;
    }
}

package com.sqx.app.lvyou.activity.enums;

import lombok.Getter;

/**
 * 活动进行阶段（用户端 Tab 筛选）
 */
@Getter
public enum ActivityPhase {

    ONGOING("ongoing", "进行中"),
    UPCOMING("upcoming", "即将开始"),
    ENDED("ended", "已结束");

    private final String code;
    private final String label;

    ActivityPhase(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(String code) {
        if (code == null) {
            return "";
        }
        for (ActivityPhase item : values()) {
            if (item.code.equals(code)) {
                return item.label;
            }
        }
        return "";
    }

    public static boolean isValidFilter(String code) {
        if (code == null || "all".equalsIgnoreCase(code)) {
            return true;
        }
        for (ActivityPhase item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }
}

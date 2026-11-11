package com.sqx.app.lvyou.activity.enums;

import lombok.Getter;

/**
 * 活动上下架状态
 */
@Getter
public enum ActivityStatus {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final int code;
    private final String label;

    ActivityStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(Integer status) {
        if (status == null) {
            return "";
        }
        for (ActivityStatus item : values()) {
            if (item.code == status) {
                return item.label;
            }
        }
        return "";
    }
}

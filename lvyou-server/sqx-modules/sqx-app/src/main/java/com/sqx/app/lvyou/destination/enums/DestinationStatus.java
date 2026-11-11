package com.sqx.app.lvyou.destination.enums;

import lombok.Getter;

/**
 * 目的地状态
 */
@Getter
public enum DestinationStatus {

    DISABLED(0, "禁用"),
    ONLINE(1, "已上线"),
    COMING(2, "即将上线");

    private final int code;
    private final String label;

    DestinationStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(Integer status) {
        if (status == null) {
            return "";
        }
        for (DestinationStatus item : values()) {
            if (item.code == status) {
                return item.label;
            }
        }
        return "";
    }
}

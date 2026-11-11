package com.sqx.app.lvyou.route.enums;

import lombok.Getter;

/**
 * 线路上下架状态
 */
@Getter
public enum RouteStatus {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final int code;
    private final String label;

    RouteStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(Integer status) {
        if (status == null) {
            return "";
        }
        for (RouteStatus item : values()) {
            if (item.code == status) {
                return item.label;
            }
        }
        return "";
    }
}

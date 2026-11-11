package com.sqx.app.lvyou.hotel.enums;

import lombok.Getter;

/**
 * 酒店上下架状态
 */
@Getter
public enum HotelStatus {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final int code;
    private final String label;

    HotelStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(Integer status) {
        if (status == null) {
            return "";
        }
        for (HotelStatus item : values()) {
            if (item.code == status) {
                return item.label;
            }
        }
        return "";
    }
}

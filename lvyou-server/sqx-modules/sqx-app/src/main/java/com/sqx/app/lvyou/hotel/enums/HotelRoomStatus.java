package com.sqx.app.lvyou.hotel.enums;

import lombok.Getter;

/**
 * 房型在售状态
 */
@Getter
public enum HotelRoomStatus {

    OFFLINE(0, "下架"),
    ON_SALE(1, "在售");

    private final int code;
    private final String label;

    HotelRoomStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(Integer status) {
        if (status == null) {
            return "";
        }
        for (HotelRoomStatus item : values()) {
            if (item.code == status) {
                return item.label;
            }
        }
        return "";
    }
}

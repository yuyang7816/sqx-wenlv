package com.sqx.app.lvyou.heritage.enums;

import lombok.Getter;

/**
 * 非遗工艺/店铺上下架状态
 */
@Getter
public enum HeritageStatus {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final int code;
    private final String label;

    HeritageStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(Integer status) {
        if (status == null) {
            return "";
        }
        for (HeritageStatus item : values()) {
            if (item.code == status) {
                return item.label;
            }
        }
        return "";
    }
}

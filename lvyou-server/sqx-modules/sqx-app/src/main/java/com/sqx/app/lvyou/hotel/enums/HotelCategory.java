package com.sqx.app.lvyou.hotel.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 酒店分类
 */
@Getter
public enum HotelCategory {

    LUXURY("luxury", "豪华酒店"),
    BOUTIQUE("boutique", "精品客栈"),
    BUSINESS("business", "商务酒店"),
    RESORT("resort", "度假酒店");

    private final String code;
    private final String label;

    HotelCategory(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        for (HotelCategory item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public static String labelOf(String code) {
        if (StringUtils.isBlank(code) || "all".equalsIgnoreCase(code.trim())) {
            return "";
        }
        for (HotelCategory item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return item.label;
            }
        }
        return "";
    }

    public static String normalize(String code) {
        if (StringUtils.isBlank(code) || "all".equalsIgnoreCase(code.trim())) {
            return "";
        }
        for (HotelCategory item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return item.code;
            }
        }
        return "";
    }
}

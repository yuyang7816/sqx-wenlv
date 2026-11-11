package com.sqx.app.base.map.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 地图 POI 类型
 */
public enum MapPoiType {

    /** 景区 */
    SCENIC("scenic"),
    /** 美食 */
    FOOD("food"),
    /** 停车场 */
    PARKING("parking"),
    /** 交通枢纽 */
    TRANSPORT("transport");

    private final String code;

    MapPoiType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    /**
     * 解析前端 type 参数；空值表示不过滤类型
     */
    public static MapPoiType resolve(String rawType) {
        if (StringUtils.isBlank(rawType)) {
            return null;
        }
        String normalized = rawType.trim().toLowerCase();
        switch (normalized) {
            case "scenic":
            case "navigation":
            case "map":
            case "hotel":
                return SCENIC;
            case "food":
            case "food-map":
            case "goose":
            case "restaurant":
                return FOOD;
            case "parking":
                return PARKING;
            case "transport":
                return TRANSPORT;
            default:
                return null;
        }
    }

    public static boolean isValidCode(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        for (MapPoiType value : values()) {
            if (value.code.equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }
}

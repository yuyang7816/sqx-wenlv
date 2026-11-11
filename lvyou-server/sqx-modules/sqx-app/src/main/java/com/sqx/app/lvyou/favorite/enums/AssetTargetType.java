package com.sqx.app.lvyou.favorite.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 收藏/足迹目标类型
 */
public enum AssetTargetType {

    SCENIC("scenic", "景区"),
    HOTEL("hotel", "酒店"),
    ROUTE("route", "线路"),
    GOODS("goods", "非遗商品"),
    GOOSE("goose", "灰鹅美食"),
    ACTIVITY("activity", "精彩活动");

    private final String code;
    private final String label;

    AssetTargetType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static AssetTargetType fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (AssetTargetType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
}

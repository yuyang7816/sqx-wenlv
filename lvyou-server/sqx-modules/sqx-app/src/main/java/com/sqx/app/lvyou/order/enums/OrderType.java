package com.sqx.app.lvyou.order.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 平台订单类型（与 tb_order.order_type 一致）
 */
public enum OrderType {

    HOTEL("hotel", "酒店订单"),
    ROUTE("route", "线路预约"),
    GOODS("goods", "商品订单"),
    CULTURE("culture", "非遗好物"),
    SERVICE("service", "服务订单"),
    RESTAURANT("restaurant", "餐饮预约");

    private final String code;
    private final String label;

    OrderType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static OrderType fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        String normalized = code.trim().toLowerCase();
        for (OrderType type : values()) {
            if (type.code.equals(normalized)) {
                return type;
            }
        }
        if ("travel".equals(normalized)) {
            return ROUTE;
        }
        return null;
    }
}

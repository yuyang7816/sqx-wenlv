package com.sqx.app.lvyou.search.enums;

import lombok.Getter;

/**
 * 综合搜索内容类型
 */
@Getter
public enum SearchContentType {

    HOTEL("hotel", "酒店住宿"),
    ROUTE("route", "旅游线路"),
    SCENIC("scenic", "旅游景区"),
    ACTIVITY("activity", "精彩活动"),
    GOODS("goods", "非遗商品"),
    SHOP("shop", "非遗店铺");

    private final String type;
    private final String label;

    SearchContentType(String type, String label) {
        this.type = type;
        this.label = label;
    }
}

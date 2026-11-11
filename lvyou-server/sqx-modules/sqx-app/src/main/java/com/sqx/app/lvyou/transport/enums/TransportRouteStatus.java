package com.sqx.app.lvyou.transport.enums;

import lombok.Getter;

/**
 * 公交线路上下架状态
 */
@Getter
public enum TransportRouteStatus {

    OFFLINE(0, "下架"),
    ONLINE(1, "上架");

    private final int code;
    private final String label;

    TransportRouteStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }
}

package com.sqx.app.lvyou.kefu.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 在线客服消息发送方
 */
public enum KefuImSenderType {

    USER("user", "用户"),
    STAFF("staff", "客服");

    private final String code;
    private final String label;

    KefuImSenderType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        for (KefuImSenderType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String labelOf(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (KefuImSenderType type : values()) {
            if (type.code.equalsIgnoreCase(code.trim())) {
                return type.label;
            }
        }
        return code;
    }
}

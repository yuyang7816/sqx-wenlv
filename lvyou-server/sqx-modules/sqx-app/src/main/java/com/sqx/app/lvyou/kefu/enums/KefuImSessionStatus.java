package com.sqx.app.lvyou.kefu.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 在线客服会话状态
 */
public enum KefuImSessionStatus {

    OPEN("open", "进行中"),
    CLOSED("closed", "已关闭");

    private final String code;
    private final String label;

    KefuImSessionStatus(String code, String label) {
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
        for (KefuImSessionStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    public static String labelOf(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (KefuImSessionStatus status : values()) {
            if (status.code.equalsIgnoreCase(code.trim())) {
                return status.label;
            }
        }
        return code;
    }
}

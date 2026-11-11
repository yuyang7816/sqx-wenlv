package com.sqx.app.lvyou.help.enums;

/**
 * FAQ 状态
 */
public enum HelpFaqState {

    DISABLED(0, "禁用"),
    ENABLED(1, "启用");

    private final int code;
    private final String label;

    HelpFaqState(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public int getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }

    public static boolean isValid(Integer state) {
        if (state == null) {
            return false;
        }
        return state == DISABLED.code || state == ENABLED.code;
    }

    public static String resolveLabel(Integer state) {
        if (state == null) {
            return "";
        }
        for (HelpFaqState item : values()) {
            if (item.code == state) {
                return item.label;
            }
        }
        return String.valueOf(state);
    }
}

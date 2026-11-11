package com.sqx.app.lvyou.help.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * FAQ 分类枚举
 */
public enum HelpFaqCategory {

    BOOKING("booking", "预约与订单"),
    ACCOUNT("account", "账户与设置"),
    PAYMENT("payment", "支付与优惠"),
    SERVICE("service", "客服帮助");

    private final String code;
    private final String label;

    HelpFaqCategory(String code, String label) {
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
        for (HelpFaqCategory category : values()) {
            if (category.code.equalsIgnoreCase(code.trim())) {
                return true;
            }
        }
        return false;
    }

    public static HelpFaqCategory fromCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (HelpFaqCategory category : values()) {
            if (category.code.equalsIgnoreCase(code.trim())) {
                return category;
            }
        }
        return null;
    }

    public static String resolveLabel(String code) {
        HelpFaqCategory category = fromCode(code);
        return category == null ? code : category.getLabel();
    }
}

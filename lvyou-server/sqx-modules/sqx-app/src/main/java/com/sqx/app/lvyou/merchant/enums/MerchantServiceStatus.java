package com.sqx.app.lvyou.merchant.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 商户服务状态
 */
@Getter
public enum MerchantServiceStatus {

    ON_SALE("on_sale", "上架中"),
    OFF_SALE("off_sale", "已下架"),
    DRAFT("draft", "草稿");

    private final String code;
    private final String label;

    MerchantServiceStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        for (MerchantServiceStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public static String normalize(String code) {
        if (StringUtils.isBlank(code)) {
            return DRAFT.code;
        }
        for (MerchantServiceStatus status : values()) {
            if (status.code.equalsIgnoreCase(code)) {
                return status.code;
            }
        }
        return DRAFT.code;
    }

    public static String labelOf(String code) {
        for (MerchantServiceStatus status : values()) {
            if (status.code.equals(normalize(code))) {
                return status.label;
            }
        }
        return normalize(code);
    }
}

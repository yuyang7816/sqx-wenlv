package com.sqx.app.lvyou.merchant.enums;

import lombok.Getter;

/**
 * 用户端商户入驻状态
 */
@Getter
public enum MerchantAppStatus {

    NONE("none", "未申请"),
    PENDING("pending", "审核中"),
    APPROVED("approved", "已通过"),
    REJECTED("rejected", "已驳回");

    private final String code;
    private final String label;

    MerchantAppStatus(String code, String label) {
        this.code = code;
        this.label = label;
    }
}

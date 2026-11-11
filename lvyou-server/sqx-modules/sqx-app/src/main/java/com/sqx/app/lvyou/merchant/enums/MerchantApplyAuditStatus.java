package com.sqx.app.lvyou.merchant.enums;

import lombok.Getter;

/**
 * 商户入驻申请审核状态
 */
@Getter
public enum MerchantApplyAuditStatus {

    PENDING(0, "待审核"),
    APPROVED(1, "通过"),
    REJECTED(2, "驳回");

    private final int code;
    private final String label;

    MerchantApplyAuditStatus(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static MerchantApplyAuditStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        for (MerchantApplyAuditStatus status : values()) {
            if (status.code == code) {
                return status;
            }
        }
        return null;
    }
}

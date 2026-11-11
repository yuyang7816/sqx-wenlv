package com.sqx.app.lvyou.merchant.enums;

/**
 * 商户保证金退还申请状态
 */
public final class MerchantDepositRefundStatus {

    /** 待审核 */
    public static final int PENDING = 0;
    /** 已通过 */
    public static final int APPROVED = 1;
    /** 已驳回 */
    public static final int REJECTED = 2;

    private MerchantDepositRefundStatus() {
    }
}

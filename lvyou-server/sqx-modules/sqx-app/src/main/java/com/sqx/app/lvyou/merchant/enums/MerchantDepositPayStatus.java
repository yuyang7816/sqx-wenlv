package com.sqx.app.lvyou.merchant.enums;

/**
 * 商户保证金支付单状态
 */
public final class MerchantDepositPayStatus {

    /** 待支付 */
    public static final int PENDING_PAY = 0;
    /** 已支付 */
    public static final int PAID = 1;
    /** 已取消 */
    public static final int CANCELLED = 2;

    private MerchantDepositPayStatus() {
    }
}

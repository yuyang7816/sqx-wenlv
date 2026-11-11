package com.sqx.app.lvyou.wallet.enums;

/**
 * 钱包充值单状态
 */
public final class WalletRechargeStatus {

    /** 待支付 */
    public static final int PENDING_PAY = 0;
    /** 已支付 */
    public static final int PAID = 1;
    /** 已取消 */
    public static final int CANCELLED = 2;

    private WalletRechargeStatus() {
    }
}

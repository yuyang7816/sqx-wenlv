package com.sqx.app.lvyou.wallet.enums;

/**
 * 钱包流水类型
 */
public final class WalletTxType {

    public static final String RECHARGE = "recharge";
    public static final String CONSUME = "consume";
    public static final String INCOME = "income";
    public static final String WITHDRAW = "withdraw";
    /** 商户保证金缴纳 */
    public static final String DEPOSIT = "deposit";
    /** 商户保证金退还 */
    public static final String DEPOSIT_REFUND = "deposit_refund";
    /** 商户保证金调整 */
    public static final String DEPOSIT_ADJUST = "deposit_adjust";
    /** 商户订单结算收入 */
    public static final String SETTLE = "settle";
    /** 管理员调整余额 */
    public static final String ADMIN_ADJUST = "admin_adjust";

    private WalletTxType() {
    }
}

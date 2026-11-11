package com.sqx.app.lvyou.merchant.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 订单状态分组（对应 tb_order.status）
 */
public final class MerchantOrderStatusGroup {

    /** 待支付 */
    public static final int UNPAID = 0;
    /** 待确认 */
    public static final int PENDING_CONFIRM = 10;
    /** 待使用/待入住 */
    public static final int PENDING_USE = 20;
    /** 待收货 */
    public static final int PENDING_RECEIVE = 30;
    /** 已完成 */
    public static final int COMPLETED = 40;
    /** 已取消 */
    public static final int CANCELLED = 50;
    /** 退款中 */
    public static final int REFUNDING = 60;

    public static final List<Integer> PENDING = Collections.singletonList(PENDING_CONFIRM);

    public static final List<Integer> PROCESSING = Arrays.asList(PENDING_USE, PENDING_RECEIVE, REFUNDING);

    public static final List<Integer> CLOSED = Arrays.asList(COMPLETED, CANCELLED);

    public static final List<Integer> PAID = Arrays.asList(PENDING_CONFIRM, PENDING_USE, PENDING_RECEIVE, COMPLETED, REFUNDING);

    private MerchantOrderStatusGroup() {
    }
}

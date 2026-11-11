package com.sqx.app.lvyou.canteen.support;

import org.apache.commons.lang3.StringUtils;

/**
 * 餐饮预约状态文案
 */
public final class CanteenReservationStatusSupport {

    public static final String PENDING = "pending";
    public static final String CONFIRMED = "confirmed";
    public static final String COMPLETED = "completed";
    public static final String CANCELLED = "cancelled";

    private CanteenReservationStatusSupport() {
    }

    public static String normalize(String status) {
        if (StringUtils.isBlank(status)) {
            return PENDING;
        }
        return status.trim().toLowerCase();
    }

    public static String labelOf(String status) {
        switch (normalize(status)) {
            case PENDING:
                return "待确认";
            case CONFIRMED:
                return "待到店";
            case COMPLETED:
                return "已完成";
            case CANCELLED:
                return "已取消";
            default:
                return status;
        }
    }

    public static boolean isPending(String status) {
        return PENDING.equals(normalize(status));
    }
}

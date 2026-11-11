package com.sqx.app.lvyou.hotel.support;

/**
 * 酒店预定业务常量
 */
public final class HotelBookingConstants {

    /** 待支付关单分钟数 */
    public static final int PAY_TIMEOUT_MINUTES = 30;

    /** 免费取消截止：入住日 18:00 */
    public static final int FREE_CANCEL_HOUR = 18;

    /** No-show 保留截止：入住日 18:00 */
    public static final int HOLD_UNTIL_HOUR = 18;

    /** 离店日自动完成时刻：14:00 */
    public static final int AUTO_COMPLETE_HOUR = 14;

    private HotelBookingConstants() {
    }
}

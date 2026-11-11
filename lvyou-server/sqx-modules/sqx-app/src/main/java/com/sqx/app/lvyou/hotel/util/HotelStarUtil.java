package com.sqx.app.lvyou.hotel.util;

/**
 * 酒店星级文案
 */
public final class HotelStarUtil {

    private static final String[] STAR_LABELS = {"", "一星级", "二星级", "三星级", "四星级", "五星级"};

    private HotelStarUtil() {
    }

    public static String labelOf(Integer starLevel) {
        if (starLevel == null || starLevel <= 0) {
            return "";
        }
        if (starLevel >= 1 && starLevel <= 5) {
            return STAR_LABELS[starLevel];
        }
        return starLevel + "星级";
    }
}

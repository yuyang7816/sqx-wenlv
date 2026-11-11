package com.sqx.app.lvyou.activity.util;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 活动时间展示工具
 */
public final class ActivityTimeUtil {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");

    private ActivityTimeUtil() {
    }

    /**
     * 格式化活动时间区间
     */
    public static String formatTimeRange(LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime == null && endTime == null) {
            return "";
        }
        if (startTime != null && endTime != null) {
            if (startTime.toLocalDate().equals(endTime.toLocalDate())) {
                return startTime.format(DATETIME_FMT) + " - " + endTime.format(TIME_FMT);
            }
            return startTime.format(DATETIME_FMT) + " - " + endTime.format(DATETIME_FMT);
        }
        if (startTime != null) {
            return startTime.format(DATETIME_FMT) + " 起";
        }
        return "截至 " + endTime.format(DATETIME_FMT);
    }

    public static String trimToNull(String value) {
        return StringUtils.trimToNull(value);
    }
}

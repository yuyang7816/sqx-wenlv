package com.sqx.app.lvyou.stats.support;

import com.sqx.app.lvyou.stats.domain.vo.StatsTrendPointVo;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 趋势折线日期序列补全
 */
public final class StatsTrendSupport {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private StatsTrendSupport() {
    }

    public static List<StatsTrendPointVo> buildOrderTrend(LocalDate endDate, int days, Map<String, Long> dailyCounts) {
        List<StatsTrendPointVo> points = new ArrayList<>(days);
        LocalDate startDate = endDate.minusDays(days - 1L);
        for (LocalDate cursor = startDate; !cursor.isAfter(endDate); cursor = cursor.plusDays(1)) {
            String dateKey = cursor.format(DATE_FORMAT);
            StatsTrendPointVo point = new StatsTrendPointVo();
            point.setDate(dateKey);
            point.setCount(dailyCounts.getOrDefault(dateKey, 0L));
            points.add(point);
        }
        return points;
    }

    public static List<StatsTrendPointVo> buildGmvTrend(LocalDate endDate, int days, Map<String, BigDecimal> dailyAmounts) {
        List<StatsTrendPointVo> points = new ArrayList<>(days);
        LocalDate startDate = endDate.minusDays(days - 1L);
        for (LocalDate cursor = startDate; !cursor.isAfter(endDate); cursor = cursor.plusDays(1)) {
            String dateKey = cursor.format(DATE_FORMAT);
            StatsTrendPointVo point = new StatsTrendPointVo();
            point.setDate(dateKey);
            point.setAmount(dailyAmounts.getOrDefault(dateKey, BigDecimal.ZERO));
            points.add(point);
        }
        return points;
    }

    public static List<StatsTrendPointVo> buildUserTrend(LocalDate endDate, int days, Map<String, Long> dailyCounts) {
        return buildOrderTrend(endDate, days, dailyCounts);
    }

    public static String normalizeDateKey(Object rawDate) {
        if (rawDate == null) {
            return null;
        }
        if (rawDate instanceof java.sql.Date) {
            return ((java.sql.Date) rawDate).toLocalDate().format(DATE_FORMAT);
        }
        if (rawDate instanceof java.util.Date) {
            return new java.sql.Date(((java.util.Date) rawDate).getTime()).toLocalDate().format(DATE_FORMAT);
        }
        if (rawDate instanceof LocalDate) {
            return ((LocalDate) rawDate).format(DATE_FORMAT);
        }
        String text = String.valueOf(rawDate);
        if (text.length() >= 10) {
            return text.substring(0, 10);
        }
        return text;
    }
}

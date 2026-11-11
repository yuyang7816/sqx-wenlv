package com.sqx.app.base.map.support;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 地图距离计算工具
 */
public final class MapDistanceUtil {

    private static final double EARTH_RADIUS_KM = 6371.0D;

    private MapDistanceUtil() {
    }

    /**
     * 计算两点球面距离（公里），保留 1 位小数
     */
    public static BigDecimal distanceKm(BigDecimal lat1, BigDecimal lng1, BigDecimal lat2, BigDecimal lng2) {
        if (lat1 == null || lng1 == null || lat2 == null || lng2 == null) {
            return null;
        }
        double radLat1 = Math.toRadians(lat1.doubleValue());
        double radLat2 = Math.toRadians(lat2.doubleValue());
        double deltaLat = radLat1 - radLat2;
        double deltaLng = Math.toRadians(lng1.doubleValue()) - Math.toRadians(lng2.doubleValue());
        double distance = 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(
                Math.pow(Math.sin(deltaLat / 2), 2)
                        + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(deltaLng / 2), 2)
        ));
        return BigDecimal.valueOf(distance).setScale(1, RoundingMode.HALF_UP);
    }

    public static BigDecimal parseCoordinate(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}

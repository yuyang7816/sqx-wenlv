package com.sqx.app.base.map.support;

import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 坐标系转换（GCJ-02 / WGS84）
 */
public final class MapCoordinateUtil {

    private static final double PI = Math.PI;
    private static final double AXIS = 6378245.0D;
    private static final double OFFSET = 0.006693421622965943D;

    private MapCoordinateUtil() {
    }

    /**
     * GCJ-02 转 WGS84（BigDataCloud 等海外平台使用 WGS84）
     */
    public static BigDecimal[] gcj02ToWgs84(String lat, String lng) {
        BigDecimal gcjLat = MapDistanceUtil.parseCoordinate(lat);
        BigDecimal gcjLng = MapDistanceUtil.parseCoordinate(lng);
        if (gcjLat == null || gcjLng == null) {
            return null;
        }
        double[] wgs = gcj02ToWgs84(gcjLat.doubleValue(), gcjLng.doubleValue());
        return new BigDecimal[]{
                BigDecimal.valueOf(wgs[0]).setScale(6, RoundingMode.HALF_UP),
                BigDecimal.valueOf(wgs[1]).setScale(6, RoundingMode.HALF_UP)
        };
    }

    public static String[] gcj02ToWgs84String(String lat, String lng) {
        BigDecimal[] converted = gcj02ToWgs84(lat, lng);
        if (converted == null) {
            return null;
        }
        return new String[]{converted[0].toPlainString(), converted[1].toPlainString()};
    }

    private static double[] gcj02ToWgs84(double lat, double lng) {
        if (outOfChina(lat, lng)) {
            return new double[]{lat, lng};
        }
        double[] delta = delta(lat, lng);
        return new double[]{lat - delta[0], lng - delta[1]};
    }

    private static boolean outOfChina(double lat, double lng) {
        return lng < 72.004D || lng > 137.8347D || lat < 0.8293D || lat > 55.8271D;
    }

    private static double[] delta(double lat, double lng) {
        double dLat = transformLat(lng - 105.0D, lat - 35.0D);
        double dLng = transformLng(lng - 105.0D, lat - 35.0D);
        double radLat = lat / 180.0D * PI;
        double magic = Math.sin(radLat);
        magic = 1 - OFFSET * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLat = (dLat * 180.0D) / ((AXIS * (1 - OFFSET)) / (magic * sqrtMagic) * PI);
        dLng = (dLng * 180.0D) / (AXIS / sqrtMagic * Math.cos(radLat) * PI);
        return new double[]{dLat, dLng};
    }

    private static double transformLat(double x, double y) {
        double ret = -100.0D + 2.0D * x + 3.0D * y + 0.2D * y * y + 0.1D * x * y + 0.2D * Math.sqrt(Math.abs(x));
        ret += (20.0D * Math.sin(6.0D * x * PI) + 20.0D * Math.sin(2.0D * x * PI)) * 2.0D / 3.0D;
        ret += (20.0D * Math.sin(y * PI) + 40.0D * Math.sin(y / 3.0D * PI)) * 2.0D / 3.0D;
        ret += (160.0D * Math.sin(y / 12.0D * PI) + 320.0D * Math.sin(y * PI / 30.0D)) * 2.0D / 3.0D;
        return ret;
    }

    private static double transformLng(double x, double y) {
        double ret = 300.0D + x + 2.0D * y + 0.1D * x * x + 0.1D * x * y + 0.1D * Math.sqrt(Math.abs(x));
        ret += (20.0D * Math.sin(6.0D * x * PI) + 20.0D * Math.sin(2.0D * x * PI)) * 2.0D / 3.0D;
        ret += (20.0D * Math.sin(x * PI) + 40.0D * Math.sin(x / 3.0D * PI)) * 2.0D / 3.0D;
        ret += (150.0D * Math.sin(x / 12.0D * PI) + 300.0D * Math.sin(x / 30.0D * PI)) * 2.0D / 3.0D;
        return ret;
    }

    public static boolean isValidCoordinate(String lat, String lng) {
        return MapDistanceUtil.parseCoordinate(lat) != null && MapDistanceUtil.parseCoordinate(lng) != null;
    }

    public static String formatCoordinate(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.stripTrailingZeros().toPlainString();
    }

    public static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (StringUtils.isNotBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }
}

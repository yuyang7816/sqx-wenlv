package com.sqx.app.lvyou.order.util;

import org.apache.commons.lang3.StringUtils;

/**
 * 收货地址省市区解析与格式化
 */
public final class AddressRegionUtil {

    private AddressRegionUtil() {
    }

    /**
     * 将 "省 市 区" 或 "省 区" 格式解析为三段
     */
    public static RegionParts parseRegion(String region) {
        RegionParts parts = new RegionParts();
        if (StringUtils.isBlank(region)) {
            return parts;
        }
        String[] segments = region.trim().split("\\s+");
        if (segments.length >= 3) {
            parts.setProvince(segments[0]);
            parts.setCity(segments[1]);
            parts.setDistrict(segments[2]);
        } else if (segments.length == 2) {
            parts.setProvince(segments[0]);
            parts.setDistrict(segments[1]);
        } else if (segments.length == 1) {
            parts.setProvince(segments[0]);
        }
        return parts;
    }

    /**
     * 将省市区格式化为前端 region 字段
     */
    public static String formatRegion(String province, String city, String district) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, province);
        appendPart(builder, city);
        appendPart(builder, district);
        return builder.toString().trim();
    }

    private static void appendPart(StringBuilder builder, String part) {
        if (StringUtils.isBlank(part)) {
            return;
        }
        if (builder.length() > 0) {
            builder.append(' ');
        }
        builder.append(part.trim());
    }

    public static class RegionParts {
        private String province;
        private String city;
        private String district;

        public String getProvince() {
            return province;
        }

        public void setProvince(String province) {
            this.province = province;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getDistrict() {
            return district;
        }

        public void setDistrict(String district) {
            this.district = district;
        }
    }
}

package com.sqx.app.lvyou.goose.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 灰鹅美食门店装修 JSON 解析
 */
public final class GooseDecorUtil {

    private GooseDecorUtil() {
    }

    public static Map<String, Object> parseDecor(String decorJson) {
        if (StringUtils.isBlank(decorJson)) {
            return new LinkedHashMap<>();
        }
        try {
            JSONObject object = JSON.parseObject(decorJson);
            if (object == null) {
                return new LinkedHashMap<>();
            }
            return new LinkedHashMap<>(object);
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    public static String resolveBadgeText(Map<String, Object> decor, String defaultText) {
        Object value = decor.get("badgeText");
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return defaultText;
        }
        return String.valueOf(value).trim();
    }

    public static String resolveRating(Map<String, Object> decor, String defaultRating) {
        Object value = decor.get("rating");
        if (value == null) {
            return defaultRating;
        }
        return String.valueOf(value);
    }

    public static Integer resolveMonthlySales(Map<String, Object> decor, int defaultSales) {
        Object value = decor.get("monthlySales");
        if (value == null) {
            return defaultSales;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultSales;
        }
    }

    public static String resolveIntro(Map<String, Object> decor, String storeName) {
        Object value = decor.get("intro");
        if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
            return String.valueOf(value);
        }
        return storeName + "欢迎您的光临，传承地道卤鹅风味，支持在线预约到店用餐。";
    }

    public static List<String> parseTags(Map<String, Object> decor) {
        Object value = decor.get("tags");
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List<String> tags = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                String tag = array.getString(i);
                if (StringUtils.isNotBlank(tag)) {
                    tags.add(tag.trim());
                }
            }
            return tags;
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<String> tags = new ArrayList<>();
            for (Object item : list) {
                if (item != null && StringUtils.isNotBlank(String.valueOf(item))) {
                    tags.add(String.valueOf(item).trim());
                }
            }
            return tags;
        }
        return Collections.emptyList();
    }

    public static String resolveCover(Map<String, Object> decor) {
        Object value = decor.get("cover");
        if (value == null) {
            value = decor.get("coverImage");
        }
        if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
            return String.valueOf(value).trim();
        }
        List<String> banners = parseBannerList(decor);
        if (CollectionUtils.isNotEmpty(banners)) {
            return banners.get(0);
        }
        return "";
    }

    public static List<String> parseBannerList(Map<String, Object> decor) {
        Object value = decor.get("bannerList");
        if (value == null) {
            value = decor.get("bannerImages");
        }
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List<String> banners = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                String banner = array.getString(i);
                if (StringUtils.isNotBlank(banner)) {
                    banners.add(banner.trim());
                }
            }
            return banners;
        }
        if (value instanceof List) {
            List<?> list = (List<?>) value;
            List<String> banners = new ArrayList<>();
            for (Object item : list) {
                if (item != null && StringUtils.isNotBlank(String.valueOf(item))) {
                    banners.add(String.valueOf(item).trim());
                }
            }
            return banners;
        }
        return Collections.emptyList();
    }

    public static List<Map<String, Object>> parseBusinessHours(Map<String, Object> decor, String storeHours) {
        Object value = decor.get("businessHours");
        if (value instanceof JSONArray) {
            JSONArray array = (JSONArray) value;
            List<Map<String, Object>> hours = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject item = array.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                Map<String, Object> hour = new LinkedHashMap<>();
                hour.put("id", item.get("id"));
                hour.put("label", item.getString("label"));
                hour.put("time", item.getString("time"));
                hours.add(hour);
            }
            if (CollectionUtils.isNotEmpty(hours)) {
                return hours;
            }
        }
        return defaultBusinessHours(storeHours);
    }

    private static List<Map<String, Object>> defaultBusinessHours(String storeHours) {
        String time = StringUtils.defaultIfBlank(storeHours, "09:00-21:00").replace("-", " - ");
        List<Map<String, Object>> hours = new ArrayList<>();
        Map<String, Object> weekday = new LinkedHashMap<>();
        weekday.put("id", 1);
        weekday.put("label", "周一至周日");
        weekday.put("time", time);
        hours.add(weekday);
        return hours;
    }
}

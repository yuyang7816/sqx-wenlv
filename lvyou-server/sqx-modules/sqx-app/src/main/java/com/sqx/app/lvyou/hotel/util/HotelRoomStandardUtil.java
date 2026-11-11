package com.sqx.app.lvyou.hotel.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomStandardEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 客房标准模板 JSON 解析
 */
public final class HotelRoomStandardUtil {

    private HotelRoomStandardUtil() {
    }

    public static Map<String, Object> emptyDefaults() {
        Map<String, Object> defaults = new LinkedHashMap<>();
        defaults.put("windowType", "");
        defaults.put("smokingPolicy", "");
        defaults.put("petPolicy", "");
        return defaults;
    }

    public static Map<String, Object> parseDefaults(String defaultsJson) {
        Map<String, Object> defaults = emptyDefaults();
        if (StringUtils.isBlank(defaultsJson)) {
            return defaults;
        }
        try {
            Map<String, Object> parsed = JSON.parseObject(defaultsJson, new TypeReference<Map<String, Object>>() {
            });
            if (parsed != null) {
                defaults.putAll(parsed);
            }
        } catch (Exception ignored) {
            // ignore invalid json
        }
        return defaults;
    }

    public static List<Map<String, Object>> parseFacilitiesJson(String facilitiesJson) {
        if (StringUtils.isBlank(facilitiesJson)) {
            return new ArrayList<>();
        }
        try {
            List<Map<String, Object>> list = JSON.parseObject(facilitiesJson, new TypeReference<List<Map<String, Object>>>() {
            });
            return list == null ? new ArrayList<>() : list;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static List<String> parseStringListJson(String json) {
        if (StringUtils.isBlank(json)) {
            return new ArrayList<>();
        }
        try {
            List<String> list = JSON.parseArray(json, String.class);
            if (CollectionUtils.isEmpty(list)) {
                return new ArrayList<>();
            }
            List<String> cleaned = new ArrayList<>();
            for (String item : list) {
                if (StringUtils.isNotBlank(item)) {
                    cleaned.add(item.trim());
                }
            }
            return cleaned;
        } catch (Exception ignored) {
            return new ArrayList<>();
        }
    }

    public static String facilitiesToJson(List<Map<String, Object>> facilities) {
        return JSON.toJSONString(facilities == null ? new ArrayList<>() : facilities);
    }

    public static String defaultsToJson(Map<String, Object> defaults) {
        Map<String, Object> merged = emptyDefaults();
        if (defaults != null) {
            merged.putAll(defaults);
        }
        return JSON.toJSONString(merged);
    }

    public static String stringListToJson(List<String> list) {
        return JSON.toJSONString(list == null ? new ArrayList<>() : list);
    }

    public static Map<String, Object> toDetailMap(HotelRoomStandardEntity standard) {
        Map<String, Object> detail = HotelRoomDetailUtil.emptyDetailMap();
        if (standard == null) {
            return detail;
        }
        Map<String, Object> defaults = parseDefaults(standard.getDefaultsJson());
        detail.put("windowType", String.valueOf(defaults.getOrDefault("windowType", "")));
        detail.put("smokingPolicy", String.valueOf(defaults.getOrDefault("smokingPolicy", "")));
        detail.put("petPolicy", String.valueOf(defaults.getOrDefault("petPolicy", "")));
        detail.put("facilities", parseFacilitiesJson(standard.getFacilitiesJson()));
        detail.put("gallery", parseStringListJson(standard.getGalleryJson()));
        detail.put("tags", parseStringListJson(standard.getTagsJson()));
        if (StringUtils.isNotBlank(standard.getFloorHint())) {
            detail.put("floor", standard.getFloorHint());
        }
        return detail;
    }

    public static List<String> mergeStringLists(List<String> base, List<String> extra) {
        Set<String> merged = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(base)) {
            merged.addAll(base);
        }
        if (CollectionUtils.isNotEmpty(extra)) {
            merged.addAll(extra);
        }
        return new ArrayList<>(merged);
    }
}

package com.sqx.app.lvyou.hotel.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomStandardEntity;
import com.sqx.app.lvyou.hotel.domain.vo.HotelFacilityVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 酒店房型扩展 JSON 解析与规格摘要生成
 */
public final class HotelRoomDetailUtil {

    private HotelRoomDetailUtil() {
    }

    private static final String[] INHERITABLE_SCALAR_KEYS = {
            "windowType", "smokingPolicy", "petPolicy"
    };

    public static Map<String, Object> emptyDetailMap() {
        Map<String, Object> detail = new LinkedHashMap<>();
        detail.put("inheritStandard", false);
        detail.put("gallery", new ArrayList<String>());
        detail.put("area", "");
        detail.put("floor", "");
        detail.put("occupancy", null);
        detail.put("bedType", "");
        detail.put("bedCount", null);
        detail.put("bedSize", "");
        detail.put("extraBedPolicy", "");
        detail.put("petPolicy", "");
        detail.put("windowType", "");
        detail.put("smokingPolicy", "");
        detail.put("facilities", new ArrayList<Map<String, Object>>());
        detail.put("tags", new ArrayList<String>());
        detail.put("excludedTags", new ArrayList<String>());
        detail.put("excludedFacilities", new ArrayList<String>());
        detail.put("desc", "");
        return detail;
    }

    public static Map<String, Object> parseMap(String roomDetailJson) {
        if (StringUtils.isBlank(roomDetailJson)) {
            return emptyDetailMap();
        }
        try {
            Map<String, Object> parsed = JSON.parseObject(roomDetailJson, new TypeReference<Map<String, Object>>() {
            });
            return mergeDetailMaps(emptyDetailMap(), parsed);
        } catch (Exception ex) {
            return emptyDetailMap();
        }
    }

    public static Map<String, Object> parseRawMap(String roomDetailJson) {
        if (StringUtils.isBlank(roomDetailJson)) {
            return new LinkedHashMap<>();
        }
        try {
            Map<String, Object> parsed = JSON.parseObject(roomDetailJson, new TypeReference<Map<String, Object>>() {
            });
            return parsed == null ? new LinkedHashMap<>() : parsed;
        } catch (Exception ex) {
            return new LinkedHashMap<>();
        }
    }

    public static Map<String, Object> mergeDetailMaps(Map<String, Object> base, Map<String, Object> incoming) {
        Map<String, Object> merged = new LinkedHashMap<>(base == null ? emptyDetailMap() : base);
        if (incoming == null || incoming.isEmpty()) {
            return merged;
        }
        for (Map.Entry<String, Object> entry : incoming.entrySet()) {
            if (entry.getValue() != null) {
                merged.put(entry.getKey(), entry.getValue());
            }
        }
        return merged;
    }

    public static String toJson(Map<String, Object> detailMap) {
        Map<String, Object> merged = mergeDetailMaps(emptyDetailMap(), detailMap);
        return JSON.toJSONString(merged);
    }

    /** 继承模式下仅序列化差异项，避免写入默认值导致覆盖标准 */
    public static String toOverrideJson(Map<String, Object> overrideMap) {
        return JSON.toJSONString(overrideMap == null ? new LinkedHashMap<>() : overrideMap);
    }

    public static String buildSpecSummary(Map<String, Object> detailMap) {
        if (detailMap == null || detailMap.isEmpty()) {
            return "";
        }
        List<String> parts = new ArrayList<>();
        String area = readString(detailMap, "area");
        if (StringUtils.isNotBlank(area)) {
            parts.add(area.contains("㎡") ? area : area + "㎡");
        }
        Integer occupancy = readInteger(detailMap, "occupancy");
        if (occupancy != null && occupancy > 0) {
            parts.add(occupancy + "人");
        }
        String windowType = readString(detailMap, "windowType");
        if (StringUtils.isNotBlank(windowType)) {
            parts.add(windowType);
        }
        String smokingPolicy = readString(detailMap, "smokingPolicy");
        if (StringUtils.isNotBlank(smokingPolicy)) {
            parts.add(smokingPolicy);
        }
        String bedSummary = buildBedSummary(detailMap);
        if (StringUtils.isNotBlank(bedSummary)) {
            parts.add(bedSummary);
        }
        String petPolicy = readString(detailMap, "petPolicy");
        if (StringUtils.isNotBlank(petPolicy)) {
            parts.add(petPolicy);
        }
        return String.join(" · ", parts);
    }

    public static List<String> parseGallery(Map<String, Object> detailMap, String coverImage) {
        List<String> gallery = readStringList(detailMap == null ? null : detailMap.get("gallery"));
        if (CollectionUtils.isNotEmpty(gallery)) {
            return gallery.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList());
        }
        if (StringUtils.isNotBlank(coverImage)) {
            List<String> fallback = new ArrayList<>();
            fallback.add(coverImage);
            return fallback;
        }
        return new ArrayList<>();
    }

    public static List<HotelFacilityVo> parseFacilities(Map<String, Object> detailMap) {
        List<HotelFacilityVo> facilities = new ArrayList<>();
        if (detailMap == null) {
            return facilities;
        }
        Object raw = detailMap.get("facilities");
        if (!(raw instanceof List)) {
            return facilities;
        }
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) raw;
        int index = 1;
        for (Object item : list) {
            if (item instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> map = (Map<String, Object>) item;
                String label = readString(map, "label");
                if (StringUtils.isBlank(label)) {
                    continue;
                }
                HotelFacilityVo vo = new HotelFacilityVo();
                vo.setId(map.get("id") == null ? (long) index : Long.valueOf(String.valueOf(map.get("id"))));
                vo.setLabel(label);
                vo.setIcon(StringUtils.defaultIfBlank(readString(map, "icon"), "grid-fill"));
                facilities.add(vo);
                index++;
            }
        }
        return facilities;
    }

    public static List<String> parseTags(Map<String, Object> detailMap) {
        return readStringList(detailMap == null ? null : detailMap.get("tags"));
    }

    public static boolean isInheritStandard(Map<String, Object> roomDetail) {
        if (roomDetail == null || roomDetail.isEmpty()) {
            return false;
        }
        Object value = roomDetail.get("inheritStandard");
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value == null) {
            return false;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    public static Map<String, Object> resolveEffectiveDetail(HotelRoomStandardEntity standard,
                                                             String roomDetailJson,
                                                             String description) {
        Map<String, Object> rawParsed = parseRawMap(roomDetailJson);
        Map<String, Object> roomDetail = mergeDetailMaps(emptyDetailMap(), rawParsed);
        if (standard == null || !isInheritStandard(roomDetail)) {
            if (StringUtils.isNotBlank(description)) {
                roomDetail.put("desc", description);
            }
            return roomDetail;
        }
        Map<String, Object> merged = HotelRoomStandardUtil.toDetailMap(standard);
        applyRoomScalars(merged, roomDetail);
        for (String key : INHERITABLE_SCALAR_KEYS) {
            String override = readString(roomDetail, key);
            if (StringUtils.isNotBlank(override)) {
                merged.put(key, override);
            }
        }
        merged.put("gallery", HotelRoomStandardUtil.mergeStringLists(
                readStringList(merged.get("gallery")),
                readStringList(roomDetail.get("gallery"))
        ));
        merged.put("tags", filterExcludedStringList(
                HotelRoomStandardUtil.mergeStringLists(
                        readStringList(merged.get("tags")),
                        readStringList(roomDetail.get("tags"))
                ),
                readStringList(roomDetail.get("excludedTags"))
        ));
        merged.put("facilities", filterExcludedFacilityMaps(
                mergeFacilityMaps(
                        readFacilityMapList(merged.get("facilities")),
                        readFacilityMapList(roomDetail.get("facilities"))
                ),
                readStringList(roomDetail.get("excludedFacilities"))
        ));
        if (StringUtils.isNotBlank(description)) {
            merged.put("desc", description);
        }
        return merged;
    }

    public static Map<String, Object> normalizeRoomOverrideDetail(Map<String, Object> roomDetail) {
        Map<String, Object> source = roomDetail == null ? new LinkedHashMap<>() : roomDetail;
        Map<String, Object> normalized = new LinkedHashMap<>();
        normalized.put("inheritStandard", true);
        applyRoomScalars(normalized, source);
        for (String key : INHERITABLE_SCALAR_KEYS) {
            if (source.containsKey(key)) {
                normalized.put(key, readString(source, key));
            }
        }
        normalized.put("gallery", readStringList(source.get("gallery")));
        normalized.put("tags", readStringList(source.get("tags")));
        normalized.put("facilities", readFacilityMapList(source.get("facilities")));
        if (source.containsKey("excludedTags")) {
            normalized.put("excludedTags", readStringList(source.get("excludedTags")));
        }
        if (source.containsKey("excludedFacilities")) {
            normalized.put("excludedFacilities", readStringList(source.get("excludedFacilities")));
        }
        return normalized;
    }

    private static List<String> filterExcludedStringList(List<String> items, List<String> excluded) {
        if (CollectionUtils.isEmpty(items) || CollectionUtils.isEmpty(excluded)) {
            return items == null ? new ArrayList<>() : items;
        }
        java.util.Set<String> excludedSet = excluded.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());
        if (excludedSet.isEmpty()) {
            return items;
        }
        return items.stream()
                .filter(item -> StringUtils.isNotBlank(item) && !excludedSet.contains(item.trim()))
                .collect(Collectors.toList());
    }

    private static List<Map<String, Object>> filterExcludedFacilityMaps(List<Map<String, Object>> items,
                                                                        List<String> excluded) {
        if (CollectionUtils.isEmpty(items) || CollectionUtils.isEmpty(excluded)) {
            return items == null ? new ArrayList<>() : items;
        }
        java.util.Set<String> excludedSet = excluded.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toSet());
        if (excludedSet.isEmpty()) {
            return items;
        }
        return items.stream()
                .filter(item -> {
                    String label = readString(item, "label");
                    return StringUtils.isNotBlank(label) && !excludedSet.contains(label.trim());
                })
                .collect(Collectors.toList());
    }

    private static List<Map<String, Object>> readFacilityMapList(Object raw) {
        List<Map<String, Object>> facilities = new ArrayList<>();
        if (!(raw instanceof List)) {
            return facilities;
        }
        @SuppressWarnings("unchecked")
        List<Object> list = (List<Object>) raw;
        int index = 1;
        for (Object item : list) {
            if (!(item instanceof Map)) {
                continue;
            }
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) item;
            String label = readString(map, "label");
            if (StringUtils.isBlank(label)) {
                continue;
            }
            Map<String, Object> facility = new LinkedHashMap<>();
            facility.put("id", map.get("id") == null ? index : map.get("id"));
            facility.put("label", label);
            facility.put("icon", StringUtils.defaultIfBlank(readString(map, "icon"), "grid-fill"));
            facilities.add(facility);
            index++;
        }
        return facilities;
    }

    private static List<Map<String, Object>> mergeFacilityMaps(List<Map<String, Object>> base,
                                                               List<Map<String, Object>> extra) {
        LinkedHashMap<String, Map<String, Object>> merged = new LinkedHashMap<>();
        appendFacilityMaps(merged, base);
        appendFacilityMaps(merged, extra);
        return new ArrayList<>(merged.values());
    }

    private static void appendFacilityMaps(LinkedHashMap<String, Map<String, Object>> target,
                                           List<Map<String, Object>> source) {
        if (CollectionUtils.isEmpty(source)) {
            return;
        }
        int index = target.size();
        for (Map<String, Object> item : source) {
            String label = readString(item, "label");
            if (StringUtils.isBlank(label) || target.containsKey(label)) {
                continue;
            }
            Map<String, Object> facility = new LinkedHashMap<>();
            facility.put("id", item.get("id") == null ? ++index : item.get("id"));
            facility.put("label", label);
            facility.put("icon", StringUtils.defaultIfBlank(readString(item, "icon"), "grid-fill"));
            target.put(label, facility);
        }
    }

    private static void applyRoomScalars(Map<String, Object> target, Map<String, Object> source) {
        if (target == null || source == null) {
            return;
        }
        putIfNotBlank(target, "area", readString(source, "area"));
        putIfNotBlank(target, "floor", readString(source, "floor"));
        putIfNotBlank(target, "bedType", readString(source, "bedType"));
        putIfNotBlank(target, "bedSize", readString(source, "bedSize"));
        putIfNotBlank(target, "extraBedPolicy", readString(source, "extraBedPolicy"));
        Integer occupancy = readInteger(source, "occupancy");
        if (occupancy != null) {
            target.put("occupancy", occupancy);
        }
        Integer bedCount = readInteger(source, "bedCount");
        if (bedCount != null) {
            target.put("bedCount", bedCount);
        }
    }

    private static void putIfNotBlank(Map<String, Object> target, String key, String value) {
        if (StringUtils.isNotBlank(value)) {
            target.put(key, value);
        }
    }

    private static String buildBedSummary(Map<String, Object> detailMap) {
        Integer bedCount = readInteger(detailMap, "bedCount");
        String bedType = readString(detailMap, "bedType");
        String bedSize = readString(detailMap, "bedSize");
        if (bedCount == null && StringUtils.isBlank(bedType) && StringUtils.isBlank(bedSize)) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        if (bedCount != null && bedCount > 0) {
            builder.append(bedCount).append("张");
        }
        if (StringUtils.isNotBlank(bedType)) {
            builder.append(bedType);
        }
        if (StringUtils.isNotBlank(bedSize)) {
            builder.append(bedSize);
            if (!bedSize.contains("米")) {
                builder.append("米");
            }
        }
        return builder.toString();
    }

    private static String readString(Map<String, Object> map, String key) {
        if (map == null || !map.containsKey(key) || map.get(key) == null) {
            return "";
        }
        return StringUtils.trimToEmpty(String.valueOf(map.get(key)));
    }

    private static Integer readInteger(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static Boolean readBoolean(Map<String, Object> map, String key) {
        if (map == null || map.get(key) == null) {
            return null;
        }
        Object value = map.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private static List<String> readStringList(Object raw) {
        List<String> list = new ArrayList<>();
        if (raw == null) {
            return list;
        }
        if (raw instanceof List) {
            @SuppressWarnings("unchecked")
            List<Object> values = (List<Object>) raw;
            for (Object value : values) {
                if (value != null && StringUtils.isNotBlank(String.valueOf(value))) {
                    list.add(String.valueOf(value).trim());
                }
            }
            return list;
        }
        if (raw instanceof String && StringUtils.isNotBlank((String) raw)) {
            for (String part : ((String) raw).split("[,，、]")) {
                if (StringUtils.isNotBlank(part)) {
                    list.add(part.trim());
                }
            }
        }
        return list;
    }
}

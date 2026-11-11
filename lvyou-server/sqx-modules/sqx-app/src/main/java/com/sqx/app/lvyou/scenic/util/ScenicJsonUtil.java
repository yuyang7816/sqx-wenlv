package com.sqx.app.lvyou.scenic.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 景区 JSON 字段解析工具
 */
public final class ScenicJsonUtil {

    private ScenicJsonUtil() {
    }

    /**
     * 解析标签 JSON 数组或逗号分隔字符串
     */
    public static List<String> parseTags(String raw) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                List<String> items = JSON.parseArray(trimmed, String.class);
                if (items != null) {
                    for (String item : items) {
                        if (StringUtils.isNotBlank(item)) {
                            result.add(item.trim());
                        }
                    }
                }
                return result;
            } catch (Exception ignored) {
                // fall through
            }
        }
        if (trimmed.contains(",")) {
            for (String part : trimmed.split(",")) {
                if (StringUtils.isNotBlank(part)) {
                    result.add(part.trim());
                }
            }
            return result;
        }
        result.add(trimmed);
        return result;
    }

    /**
     * 解析游玩提示 JSON 数组
     */
    public static List<String> parseTips(String raw) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                List<String> items = JSON.parseArray(trimmed, String.class);
                if (items != null) {
                    for (String item : items) {
                        if (StringUtils.isNotBlank(item)) {
                            result.add(item.trim());
                        }
                    }
                }
                return result;
            } catch (Exception ignored) {
                // fall through
            }
        }
        for (String part : trimmed.split("[\\n;；]")) {
            if (StringUtils.isNotBlank(part)) {
                result.add(part.trim());
            }
        }
        return result;
    }

    /**
     * 解析轮播图 JSON 数组
     */
    public static List<String> parseBannerImages(String raw) {
        return parseImageList(raw);
    }

    /**
     * 解析特色配图：支持 JSON 数组，或历史单图 URL
     */
    public static List<String> parseFeatureImages(String raw) {
        return parseImageList(raw);
    }

    private static List<String> parseImageList(String raw) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                List<String> items = JSON.parseArray(trimmed, String.class);
                if (items != null) {
                    for (String item : items) {
                        if (StringUtils.isNotBlank(item)) {
                            result.add(item.trim());
                        }
                    }
                }
                return result;
            } catch (Exception ignored) {
                // fall through as single url
            }
        }
        result.add(trimmed);
        return result;
    }

    public static String toJsonArray(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        return JSON.toJSONString(items);
    }
}

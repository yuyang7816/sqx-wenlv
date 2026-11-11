package com.sqx.app.lvyou.heritage.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 非遗 JSON 字段解析工具
 */
public final class HeritageJsonUtil {

    private HeritageJsonUtil() {
    }

    /**
     * 将 JSON 数组或逗号分隔字符串转为字符串列表
     */
    public static List<String> parseStringList(String raw) {
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
        for (String part : trimmed.split("[,，]")) {
            if (StringUtils.isNotBlank(part)) {
                result.add(part.trim());
            }
        }
        return result;
    }

    /**
     * 将字符串列表序列化为 JSON 数组
     */
    public static String toJsonArray(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        return JSON.toJSONString(items);
    }
}

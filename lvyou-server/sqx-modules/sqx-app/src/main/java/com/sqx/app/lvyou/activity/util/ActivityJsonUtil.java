package com.sqx.app.lvyou.activity.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.lvyou.activity.domain.vo.ActivityHighlightVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 活动 JSON 字段解析工具
 */
public final class ActivityJsonUtil {

    private ActivityJsonUtil() {
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
        result.add(trimmed);
        return result;
    }

    /**
     * 解析活动亮点 JSON 数组
     */
    public static List<ActivityHighlightVo> parseHighlights(String raw) {
        List<ActivityHighlightVo> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        try {
            List<JSONObject> items = JSON.parseArray(raw.trim(), JSONObject.class);
            if (items == null) {
                return result;
            }
            for (JSONObject item : items) {
                if (item == null) {
                    continue;
                }
                ActivityHighlightVo vo = new ActivityHighlightVo();
                vo.setLabel(item.getString("label"));
                vo.setDesc(item.getString("desc"));
                if (StringUtils.isNotBlank(vo.getLabel()) || StringUtils.isNotBlank(vo.getDesc())) {
                    result.add(vo);
                }
            }
        } catch (Exception ignored) {
            // ignore invalid json
        }
        return result;
    }

    /**
     * 解析参与须知 JSON 数组
     */
    public static List<String> parseNotices(String raw) {
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

    public static String toJsonArray(List<String> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        return JSON.toJSONString(items);
    }

    public static String toHighlightsJson(List<ActivityHighlightVo> items) {
        if (items == null || items.isEmpty()) {
            return "[]";
        }
        return JSON.toJSONString(items);
    }
}

package com.sqx.app.lvyou.route.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.lvyou.route.domain.vo.RouteTagVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 线路标签解析工具
 */
public final class RouteTagUtil {

    private RouteTagUtil() {
    }

    /**
     * 将 JSON 数组或逗号分隔标签转为前端展示结构
     */
    public static List<RouteTagVo> parseTags(String rawTags) {
        List<RouteTagVo> tags = new ArrayList<>();
        if (StringUtils.isBlank(rawTags)) {
            return tags;
        }
        String trimmed = rawTags.trim();
        if (!trimmed.startsWith("[")) {
            for (String part : trimmed.split("[,，]")) {
                if (StringUtils.isNotBlank(part)) {
                    tags.add(buildTag(part.trim(), "default"));
                }
            }
            return tags;
        }
        try {
            List<Object> items = JSON.parseArray(trimmed, Object.class);
            if (items == null || items.isEmpty()) {
                return tags;
            }
            for (Object item : items) {
                if (item instanceof JSONObject) {
                    JSONObject json = (JSONObject) item;
                    String text = StringUtils.defaultIfBlank(json.getString("text"), json.getString("label"));
                    String type = StringUtils.defaultIfBlank(json.getString("type"), "default");
                    if (StringUtils.isNotBlank(text)) {
                        tags.add(buildTag(text, type));
                    }
                } else if (item != null && StringUtils.isNotBlank(String.valueOf(item))) {
                    tags.add(buildTag(String.valueOf(item), "default"));
                }
            }
        } catch (Exception ignored) {
            tags.add(buildTag(trimmed, "default"));
        }
        return tags;
    }

    private static RouteTagVo buildTag(String text, String type) {
        RouteTagVo vo = new RouteTagVo();
        vo.setText(text);
        vo.setType(type);
        return vo;
    }
}

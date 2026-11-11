package com.sqx.app.lvyou.home.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 推荐标签解析工具
 */
public final class RecommendTagUtil {

    private RecommendTagUtil() {
    }

    /**
     * 将单标签或 JSON 数组标签转为展示用字符串
     */
    public static String resolveTag(String rawTag) {
        if (StringUtils.isBlank(rawTag)) {
            return "";
        }
        String trimmed = rawTag.trim();
        if (trimmed.startsWith("[")) {
            try {
                List<String> tags = JSON.parseArray(trimmed, String.class);
                if (tags == null || tags.isEmpty()) {
                    return "";
                }
                return String.join("·", tags);
            } catch (Exception ignored) {
                return rawTag;
            }
        }
        return rawTag;
    }
}

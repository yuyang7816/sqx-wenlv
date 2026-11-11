package com.sqx.app.lvyou.feedback.util;

import com.alibaba.fastjson.JSON;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 反馈截图 JSON 序列化工具
 */
public final class FeedbackJsonUtil {

    private FeedbackJsonUtil() {
    }

    public static String toJson(List<String> images) {
        if (CollectionUtils.isEmpty(images)) {
            return null;
        }
        List<String> cleaned = images.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.toList());
        if (cleaned.isEmpty()) {
            return null;
        }
        return JSON.toJSONString(cleaned);
    }

    public static List<String> parseList(String imagesJson) {
        if (StringUtils.isBlank(imagesJson)) {
            return Collections.emptyList();
        }
        try {
            List<String> parsed = JSON.parseArray(imagesJson, String.class);
            if (CollectionUtils.isEmpty(parsed)) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            for (String item : parsed) {
                if (StringUtils.isNotBlank(item)) {
                    result.add(item.trim());
                }
            }
            return result;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }
}

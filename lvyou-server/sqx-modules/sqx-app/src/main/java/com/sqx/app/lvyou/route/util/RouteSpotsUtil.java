package com.sqx.app.lvyou.route.util;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 线路途经景点解析工具
 */
public final class RouteSpotsUtil {

    private RouteSpotsUtil() {
    }

    /**
     * 逗号分隔景点转为列表
     */
    public static List<String> parseSpots(String rawSpots) {
        if (StringUtils.isBlank(rawSpots)) {
            return new ArrayList<>();
        }
        return Arrays.stream(rawSpots.split("[,，·]"))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .collect(Collectors.toList());
    }

    /**
     * 列表转为逗号分隔存储
     */
    public static String joinSpots(List<String> spots) {
        if (spots == null || spots.isEmpty()) {
            return "";
        }
        return spots.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .collect(Collectors.joining(","));
    }
}

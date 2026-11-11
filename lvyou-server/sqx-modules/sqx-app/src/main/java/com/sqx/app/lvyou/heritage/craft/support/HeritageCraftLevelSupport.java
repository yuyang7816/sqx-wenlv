package com.sqx.app.lvyou.heritage.craft.support;

import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 非遗工艺级别文案（世界级 → 区县级）
 */
public final class HeritageCraftLevelSupport {

    private static final Map<String, String> CODE_LABEL = new LinkedHashMap<>();
    private static final Map<String, String> CODE_SHORT = new LinkedHashMap<>();

    static {
        CODE_LABEL.put("world", "世界级（联合国教科文组织人类非物质文化遗产）");
        CODE_LABEL.put("national", "国家级");
        CODE_LABEL.put("provincial", "省级");
        CODE_LABEL.put("municipal", "市级");
        CODE_LABEL.put("county", "区县级");

        CODE_SHORT.put("world", "世界级");
        CODE_SHORT.put("national", "国家级");
        CODE_SHORT.put("provincial", "省级");
        CODE_SHORT.put("municipal", "市级");
        CODE_SHORT.put("county", "区县级");
    }

    private HeritageCraftLevelSupport() {
    }

    public static String resolveLabel(String levelCode, String levelLabel) {
        if (StringUtils.isNotBlank(levelLabel)) {
            return levelLabel.trim();
        }
        if (StringUtils.isBlank(levelCode)) {
            return "";
        }
        return CODE_LABEL.getOrDefault(levelCode.trim().toLowerCase(), levelCode);
    }

    public static String resolveShort(String levelCode, String levelLabel) {
        if (StringUtils.isNotBlank(levelLabel)) {
            String label = levelLabel.trim();
            if (label.contains("世界级") || label.contains("联合国")) {
                return "世界级";
            }
            if (label.contains("国家级")) {
                return "国家级";
            }
            if (label.contains("省级")) {
                return "省级";
            }
            if (label.contains("市级")) {
                return "市级";
            }
            if (label.contains("区县") || label.contains("县级")) {
                return "区县级";
            }
            return label;
        }
        if (StringUtils.isBlank(levelCode)) {
            return "";
        }
        return CODE_SHORT.getOrDefault(levelCode.trim().toLowerCase(), levelCode);
    }
}

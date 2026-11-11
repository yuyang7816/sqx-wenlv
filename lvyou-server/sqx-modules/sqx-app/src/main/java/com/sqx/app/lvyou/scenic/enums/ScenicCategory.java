package com.sqx.app.lvyou.scenic.enums;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 景区分类
 */
@Getter
public enum ScenicCategory {

    CULTURE("culture", "人文古迹"),
    NATURE("nature", "自然风光"),
    RED("red", "红色旅游"),
    FOLK("folk", "民俗风情");

    private final String code;
    private final String label;

    ScenicCategory(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValidFilter(String code) {
        if (StringUtils.isBlank(code) || "all".equalsIgnoreCase(code)) {
            return true;
        }
        for (ScenicCategory item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return false;
    }

    public static String labelOf(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ScenicCategory item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return item.label;
            }
        }
        return code;
    }

    public static String normalize(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        for (ScenicCategory item : values()) {
            if (item.code.equalsIgnoreCase(code)) {
                return item.code;
            }
        }
        return code;
    }
}

package com.sqx.app.lvyou.notice.enums;

import lombok.Getter;

/**
 * 公告发布状态
 */
@Getter
public enum NoticeState {

    DRAFT(0, "草稿"),
    PUBLISHED(1, "已发布");

    private final int code;
    private final String label;

    NoticeState(int code, String label) {
        this.code = code;
        this.label = label;
    }

    public static String labelOf(Integer state) {
        if (state == null) {
            return "";
        }
        for (NoticeState item : values()) {
            if (item.code == state) {
                return item.label;
            }
        }
        return "";
    }
}

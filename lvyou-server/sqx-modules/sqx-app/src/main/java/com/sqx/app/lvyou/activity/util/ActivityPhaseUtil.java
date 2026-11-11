package com.sqx.app.lvyou.activity.util;

import com.sqx.app.lvyou.activity.enums.ActivityPhase;

import java.time.LocalDateTime;

/**
 * 活动阶段计算工具
 */
public final class ActivityPhaseUtil {

    private ActivityPhaseUtil() {
    }

    /**
     * 根据开始/结束时间计算活动阶段
     */
    public static String resolvePhase(LocalDateTime startTime, LocalDateTime endTime, LocalDateTime now) {
        if (now == null) {
            now = LocalDateTime.now();
        }
        if (startTime != null && now.isBefore(startTime)) {
            return ActivityPhase.UPCOMING.getCode();
        }
        if (endTime != null && now.isAfter(endTime)) {
            return ActivityPhase.ENDED.getCode();
        }
        return ActivityPhase.ONGOING.getCode();
    }

    public static String resolvePhaseLabel(String phaseCode) {
        return ActivityPhase.labelOf(phaseCode);
    }
}

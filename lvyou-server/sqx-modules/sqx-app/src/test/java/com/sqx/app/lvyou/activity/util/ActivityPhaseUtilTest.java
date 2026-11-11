package com.sqx.app.lvyou.activity.util;

import com.sqx.app.lvyou.activity.enums.ActivityPhase;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDateTime;

/**
 * 活动阶段计算测试
 */
public class ActivityPhaseUtilTest {

    @Test
    public void resolveUpcoming() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 9, 12, 0);
        LocalDateTime start = LocalDateTime.of(2026, 9, 10, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 20, 18, 0);
        Assert.assertEquals(ActivityPhase.UPCOMING.getCode(),
                ActivityPhaseUtil.resolvePhase(start, end, now));
    }

    @Test
    public void resolveOngoing() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 9, 12, 0);
        LocalDateTime start = LocalDateTime.of(2026, 9, 1, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 9, 30, 18, 0);
        Assert.assertEquals(ActivityPhase.ONGOING.getCode(),
                ActivityPhaseUtil.resolvePhase(start, end, now));
    }

    @Test
    public void resolveEnded() {
        LocalDateTime now = LocalDateTime.of(2026, 9, 9, 12, 0);
        LocalDateTime start = LocalDateTime.of(2026, 7, 1, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 7, 31, 18, 0);
        Assert.assertEquals(ActivityPhase.ENDED.getCode(),
                ActivityPhaseUtil.resolvePhase(start, end, now));
    }

    @Test
    public void parseTagsAndHighlights() {
        Assert.assertEquals(2, ActivityJsonUtil.parseTags("[\"非遗体验\",\"亲子\"]").size());
        Assert.assertEquals(1, ActivityJsonUtil.parseHighlights(
                "[{\"label\":\"陶艺体验\",\"desc\":\"现场指导拉坯制陶\"}]").size());
        Assert.assertEquals(2, ActivityJsonUtil.parseNotices("[\"请文明参与\",\"建议错峰前往\"]").size());
    }
}

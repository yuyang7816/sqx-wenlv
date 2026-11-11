package com.sqx.app.lvyou.stats.support;

import com.sqx.app.lvyou.stats.domain.vo.StatsTrendPointVo;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 趋势折线日期补全单元测试
 */
public class StatsTrendSupportTest {

    @Test
    public void buildOrderTrend_fillsMissingDaysWithZero() {
        LocalDate endDate = LocalDate.of(2026, 9, 9);
        Map<String, Long> dailyCounts = new HashMap<String, Long>();
        dailyCounts.put("2026-09-08", 3L);
        dailyCounts.put("2026-09-09", 5L);

        List<StatsTrendPointVo> points = StatsTrendSupport.buildOrderTrend(endDate, 3, dailyCounts);

        Assert.assertEquals(3, points.size());
        Assert.assertEquals("2026-09-07", points.get(0).getDate());
        Assert.assertEquals(Long.valueOf(0L), points.get(0).getCount());
        Assert.assertEquals(Long.valueOf(3L), points.get(1).getCount());
        Assert.assertEquals(Long.valueOf(5L), points.get(2).getCount());
    }

    @Test
    public void buildGmvTrend_fillsMissingDaysWithZero() {
        LocalDate endDate = LocalDate.of(2026, 9, 9);
        Map<String, BigDecimal> dailyAmounts = new HashMap<String, BigDecimal>();
        dailyAmounts.put("2026-09-09", new BigDecimal("128.50"));

        List<StatsTrendPointVo> points = StatsTrendSupport.buildGmvTrend(endDate, 2, dailyAmounts);

        Assert.assertEquals(2, points.size());
        Assert.assertEquals(0, points.get(0).getAmount().compareTo(BigDecimal.ZERO));
        Assert.assertEquals(0, points.get(1).getAmount().compareTo(new BigDecimal("128.50")));
    }

    @Test
    public void normalizeDateKey_supportsSqlDate() {
        java.sql.Date sqlDate = java.sql.Date.valueOf("2026-09-01");
        Assert.assertEquals("2026-09-01", StatsTrendSupport.normalizeDateKey(sqlDate));
    }
}

package com.sqx.app.base.map.support;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * 地图距离计算单元测试
 */
public class MapDistanceUtilTest {

    @Test
    public void distanceKm_shouldReturnPositiveValue() {
        BigDecimal distance = MapDistanceUtil.distanceKm(
                new BigDecimal("29.4050"),
                new BigDecimal("105.5980"),
                new BigDecimal("29.4182"),
                new BigDecimal("105.6128")
        );
        Assert.assertNotNull(distance);
        Assert.assertTrue(distance.compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    public void parseCoordinate_shouldReturnNullForBlank() {
        Assert.assertNull(MapDistanceUtil.parseCoordinate(" "));
        Assert.assertNull(MapDistanceUtil.parseCoordinate(null));
    }

    @Test
    public void parseCoordinate_shouldParseValidNumber() {
        Assert.assertEquals(new BigDecimal("29.405"), MapDistanceUtil.parseCoordinate("29.405"));
    }
}

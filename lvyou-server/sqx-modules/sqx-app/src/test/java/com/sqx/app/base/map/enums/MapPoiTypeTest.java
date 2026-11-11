package com.sqx.app.base.map.enums;

import org.junit.Assert;
import org.junit.Test;

/**
 * 地图 POI 类型解析单元测试
 */
public class MapPoiTypeTest {

    @Test
    public void resolve_shouldMapAliases() {
        Assert.assertEquals(MapPoiType.SCENIC, MapPoiType.resolve("navigation"));
        Assert.assertEquals(MapPoiType.FOOD, MapPoiType.resolve("goose"));
        Assert.assertEquals(MapPoiType.PARKING, MapPoiType.resolve("parking"));
        Assert.assertEquals(MapPoiType.TRANSPORT, MapPoiType.resolve("transport"));
    }

    @Test
    public void resolve_blankShouldReturnNull() {
        Assert.assertNull(MapPoiType.resolve(""));
        Assert.assertNull(MapPoiType.resolve(null));
    }
}

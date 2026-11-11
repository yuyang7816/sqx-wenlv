package com.sqx.app.lvyou.favorite.support;

import com.sqx.app.lvyou.favorite.enums.AssetTargetType;
import org.junit.Assert;
import org.junit.Test;

/**
 * 收藏/足迹目标类型与路径辅助测试
 */
public class AssetTargetSupportTest {

    @Test
    public void fromCodeAcceptsKnownTypes() {
        Assert.assertEquals(AssetTargetType.SCENIC, AssetTargetType.fromCode("scenic"));
        Assert.assertEquals(AssetTargetType.HOTEL, AssetTargetType.fromCode("hotel"));
        Assert.assertEquals(AssetTargetType.GOODS, AssetTargetType.fromCode("goods"));
    }

    @Test
    public void buildKeyCombinesTypeAndId() {
        Assert.assertEquals("route-9", AssetTargetSupport.buildKey("route", 9L));
    }

    @Test
    public void buildPathMatchesUniappRoutes() {
        AssetTargetSupport support = new AssetTargetSupport();
        Assert.assertEquals("/pages/scenic/detail?id=1", support.buildPath("scenic", 1L));
        Assert.assertEquals("/pages/hotel/detail?id=2", support.buildPath("hotel", 2L));
        Assert.assertEquals("/pages/route/detail?id=3", support.buildPath("route", 3L));
        Assert.assertEquals("/pages/heritage/goods-detail?id=4", support.buildPath("goods", 4L));
    }
}

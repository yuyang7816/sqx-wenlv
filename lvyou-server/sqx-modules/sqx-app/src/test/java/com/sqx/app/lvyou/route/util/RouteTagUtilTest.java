package com.sqx.app.lvyou.route.util;

import com.sqx.app.lvyou.route.domain.vo.RouteTagVo;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

/**
 * 线路标签解析测试
 */
public class RouteTagUtilTest {

    @Test
    public void parseStringArrayTags() {
        List<RouteTagVo> tags = RouteTagUtil.parseTags("[\"古镇\",\"一日游\"]");
        Assert.assertEquals(2, tags.size());
        Assert.assertEquals("古镇", tags.get(0).getText());
        Assert.assertEquals("一日游", tags.get(1).getText());
    }

    @Test
    public void parseObjectArrayTags() {
        List<RouteTagVo> tags = RouteTagUtil.parseTags("[{\"text\":\"美食\",\"type\":\"food\"}]");
        Assert.assertEquals(1, tags.size());
        Assert.assertEquals("美食", tags.get(0).getText());
        Assert.assertEquals("food", tags.get(0).getType());
    }

    @Test
    public void parseSpots() {
        List<String> spots = RouteSpotsUtil.parseSpots("万灵古镇,安陶小镇");
        Assert.assertEquals(2, spots.size());
        Assert.assertEquals("万灵古镇", spots.get(0));
    }
}

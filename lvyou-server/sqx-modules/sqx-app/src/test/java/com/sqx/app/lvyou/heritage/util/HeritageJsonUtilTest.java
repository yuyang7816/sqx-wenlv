package com.sqx.app.lvyou.heritage.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * 非遗 JSON 字段解析测试
 */
public class HeritageJsonUtilTest {

    @Test
    public void parseJsonArray() {
        List<String> items = HeritageJsonUtil.parseStringList("[\"陶艺体验\",\"手工制陶\"]");
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("陶艺体验", items.get(0));
        Assert.assertEquals("手工制陶", items.get(1));
    }

    @Test
    public void parseCommaSeparated() {
        List<String> items = HeritageJsonUtil.parseStringList("夏布展示,文创销售");
        Assert.assertEquals(2, items.size());
        Assert.assertEquals("夏布展示", items.get(0));
    }

    @Test
    public void toJsonArray() {
        String json = HeritageJsonUtil.toJsonArray(Arrays.asList("非遗展示", "古镇导览"));
        Assert.assertTrue(json.contains("非遗展示"));
        Assert.assertTrue(json.contains("古镇导览"));
    }
}

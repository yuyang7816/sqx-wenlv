package com.sqx.app.lvyou.scenic.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 景区 JSON 工具测试
 */
public class ScenicJsonUtilTest {

    @Test
    public void parseTags_supportJsonAndPlainText() {
        List<String> jsonTags = ScenicJsonUtil.parseTags("[\"千年古邑\",\"水乡古镇\"]");
        Assert.assertEquals(Arrays.asList("千年古邑", "水乡古镇"), jsonTags);

        List<String> plainTag = ScenicJsonUtil.parseTags("非遗体验");
        Assert.assertEquals(Collections.singletonList("非遗体验"), plainTag);
    }

    @Test
    public void parseTips_supportJsonArray() {
        List<String> tips = ScenicJsonUtil.parseTips("[\"建议穿舒适平底鞋\",\"节假日人流较大\"]");
        Assert.assertEquals(2, tips.size());
        Assert.assertEquals("建议穿舒适平底鞋", tips.get(0));
    }

    @Test
    public void parseFeatureImages_supportJsonAndSingleUrl() {
        List<String> jsonImages = ScenicJsonUtil.parseFeatureImages("[\"https://a.png\",\"https://b.png\"]");
        Assert.assertEquals(Arrays.asList("https://a.png", "https://b.png"), jsonImages);

        List<String> single = ScenicJsonUtil.parseFeatureImages("https://legacy.png");
        Assert.assertEquals(Collections.singletonList("https://legacy.png"), single);
    }

    @Test
    public void toJsonArray_roundTrip() {
        String json = ScenicJsonUtil.toJsonArray(Arrays.asList("标签A", "标签B"));
        List<String> parsed = ScenicJsonUtil.parseTags(json);
        Assert.assertEquals(Arrays.asList("标签A", "标签B"), parsed);
    }
}

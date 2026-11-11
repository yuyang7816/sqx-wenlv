package com.sqx.app.lvyou.feedback.util;

import com.sqx.app.lvyou.feedback.enums.FeedbackType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class FeedbackJsonUtilTest {

    @Test
    public void shouldSerializeAndParseImages() {
        List<String> images = Arrays.asList("https://cdn/a.jpg", "https://cdn/b.jpg");
        String json = FeedbackJsonUtil.toJson(images);
        List<String> parsed = FeedbackJsonUtil.parseList(json);
        Assert.assertEquals(2, parsed.size());
        Assert.assertEquals("https://cdn/a.jpg", parsed.get(0));
    }

    @Test
    public void shouldReturnEmptyForBlankJson() {
        Assert.assertTrue(FeedbackJsonUtil.parseList(null).isEmpty());
        Assert.assertTrue(FeedbackJsonUtil.parseList("").isEmpty());
    }

    @Test
    public void shouldValidateFeedbackType() {
        Assert.assertTrue(FeedbackType.isValid("功能异常"));
        Assert.assertTrue(FeedbackType.isValid("其他"));
        Assert.assertFalse(FeedbackType.isValid("未知类型"));
        Assert.assertFalse(FeedbackType.isValid(null));
    }

    @Test
    public void shouldReturnNullForEmptyImages() {
        Assert.assertNull(FeedbackJsonUtil.toJson(Collections.emptyList()));
        Assert.assertNull(FeedbackJsonUtil.toJson(null));
    }
}

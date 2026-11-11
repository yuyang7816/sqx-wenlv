package com.sqx.app.lvyou.goose.util;

import com.sqx.app.lvyou.goose.support.GooseStoreTypeSupport;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * 灰鹅美食工具类单元测试
 */
public class GooseDecorUtilTest {

    @Test
    public void parseDecor_shouldReadRatingAndTags() {
        String json = "{\"rating\":\"4.9\",\"monthlySales\":1286,\"tags\":[\"百年传承\",\"可堂食\"],\"intro\":\"测试介绍\"}";
        Map<String, Object> decor = GooseDecorUtil.parseDecor(json);
        Assert.assertEquals("4.9", GooseDecorUtil.resolveRating(decor, "4.8"));
        Assert.assertEquals(Integer.valueOf(1286), GooseDecorUtil.resolveMonthlySales(decor, 0));
        List<String> tags = GooseDecorUtil.parseTags(decor);
        Assert.assertEquals(Arrays.asList("百年传承", "可堂食"), tags);
        Assert.assertEquals("测试介绍", GooseDecorUtil.resolveIntro(decor, "默认店名"));
    }

    @Test
    public void foodStoreType_shouldAcceptGooseAndRestaurant() {
        Assert.assertTrue(GooseStoreTypeSupport.isFoodStore(MerchantStoreType.LEGACY_GOOSE));
        Assert.assertTrue(GooseStoreTypeSupport.isFoodStore(MerchantStoreType.RESTAURANT.getCode()));
        Assert.assertFalse(GooseStoreTypeSupport.isFoodStore(MerchantStoreType.HOTEL.getCode()));
        Assert.assertEquals(2, GooseStoreTypeSupport.foodStoreTypes().size());
    }
}

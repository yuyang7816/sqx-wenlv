package com.sqx.app.lvyou.help.util;

import com.sqx.app.lvyou.help.domain.entity.HelpFaqEntity;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqCategoryVo;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqListVo;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * FAQ 分类分组工具测试
 */
public class HelpFaqGroupUtilTest {

    @Test
    public void groupByCategory_shouldGroupAndPreserveOrder() {
        HelpFaqEntity booking1 = entity(1L, "booking", "预约与订单", "如何预约？", "按提示预约");
        HelpFaqEntity account = entity(2L, "account", "账户与设置", "如何切换地区？", "点击顶部地区");
        HelpFaqEntity booking2 = entity(3L, "booking", "预约与订单", "如何退款？", "在订单页申请");

        HelpFaqListVo listVo = HelpFaqGroupUtil.groupByCategory(Arrays.asList(booking1, account, booking2));

        Assert.assertEquals(2, listVo.getCategories().size());

        HelpFaqCategoryVo bookingCategory = listVo.getCategories().get(0);
        Assert.assertEquals("booking", bookingCategory.getCategoryCode());
        Assert.assertEquals("预约与订单", bookingCategory.getCategoryName());
        Assert.assertEquals(2, bookingCategory.getItems().size());
        Assert.assertEquals("如何预约？", bookingCategory.getItems().get(0).getQuestion());

        HelpFaqCategoryVo accountCategory = listVo.getCategories().get(1);
        Assert.assertEquals("account", accountCategory.getCategoryCode());
        Assert.assertEquals(1, accountCategory.getItems().size());
    }

    @Test
    public void groupByCategory_emptyList() {
        HelpFaqListVo listVo = HelpFaqGroupUtil.groupByCategory(Collections.emptyList());
        Assert.assertNotNull(listVo.getCategories());
        Assert.assertTrue(listVo.getCategories().isEmpty());
    }

    private HelpFaqEntity entity(Long id, String code, String name, String question, String answer) {
        HelpFaqEntity entity = new HelpFaqEntity();
        entity.setId(id);
        entity.setCategoryCode(code);
        entity.setCategoryName(name);
        entity.setQuestion(question);
        entity.setAnswer(answer);
        return entity;
    }
}

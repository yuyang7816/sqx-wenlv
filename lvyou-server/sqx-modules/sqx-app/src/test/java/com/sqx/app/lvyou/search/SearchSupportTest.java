package com.sqx.app.lvyou.search;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.search.enums.SearchContentType;
import com.sqx.app.lvyou.search.support.SearchSupport;
import org.junit.Assert;
import org.junit.Test;

public class SearchSupportTest {

    private final SearchSupport searchSupport = new SearchSupport();

    @Test
    public void searchKeywordCodeRequired_shouldUseCode400() {
        Assert.assertEquals(Integer.valueOf(400), AppError.SEARCH_KEYWORD_CODE_REQUIRED.getCode());
        Assert.assertEquals("请提供搜索关键词编码", AppError.SEARCH_KEYWORD_CODE_REQUIRED.getMessage());
    }

    @Test
    public void resolveKeywordLabel_shouldMapCodeToChinese() {
        Assert.assertEquals("万灵古镇", searchSupport.resolveKeywordLabel("wanling_guzhen"));
        Assert.assertEquals("景区", searchSupport.resolveKeywordLabel("scenic"));
    }

    @Test
    public void resolveKeywordLabel_shouldRejectChineseInput() {
        try {
            searchSupport.resolveKeywordLabel("万灵古镇");
            Assert.fail("expected BizException");
        } catch (com.sqx.common.core.exception.BizException ex) {
            Assert.assertEquals(AppError.SEARCH_KEYWORD_CODE_INVALID, ex.getErrorCode());
        }
    }

    @Test
    public void searchContentType_shouldCoverSixModules() {
        Assert.assertEquals(6, SearchContentType.values().length);
        Assert.assertEquals("hotel", SearchContentType.HOTEL.getType());
        Assert.assertEquals("shop", SearchContentType.SHOP.getType());
        Assert.assertEquals("非遗商品", SearchContentType.GOODS.getLabel());
    }
}

package com.sqx.app.lvyou.ai.support;

import com.sqx.app.lvyou.ai.domain.vo.AiChatVo;
import org.junit.Assert;
import org.junit.Test;

/**
 * AI 问答规则 Mock 测试
 */
public class AiChatRuleSupportTest {

    @Test
    public void match_shouldHitFaqByKeyword() {
        AiChatVo vo = AiChatRuleSupport.match("推荐附近的特色美食");

        Assert.assertTrue(vo.getMatched());
        Assert.assertTrue(vo.getContent().contains("美食"));
        Assert.assertEquals(2, vo.getLinks().size());
        Assert.assertEquals("特色美食", vo.getLinks().get(0).getLabel());
    }

    @Test
    public void match_shouldReturnDefaultWhenNoKeyword() {
        AiChatVo vo = AiChatRuleSupport.match("你好");

        Assert.assertFalse(vo.getMatched());
        Assert.assertEquals(AiChatRuleSupport.DEFAULT_REPLY, vo.getContent());
        Assert.assertTrue(vo.getLinks().isEmpty());
    }

    @Test
    public void match_blankMessage_shouldReturnDefault() {
        AiChatVo vo = AiChatRuleSupport.match("   ");

        Assert.assertFalse(vo.getMatched());
        Assert.assertEquals(AiChatRuleSupport.DEFAULT_REPLY, vo.getContent());
    }
}

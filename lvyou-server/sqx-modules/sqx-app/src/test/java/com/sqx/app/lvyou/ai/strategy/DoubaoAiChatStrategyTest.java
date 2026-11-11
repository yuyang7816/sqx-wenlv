package com.sqx.app.lvyou.ai.strategy;

import com.alibaba.fastjson.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * 豆包请求体构建测试
 */
public class DoubaoAiChatStrategyTest {

    @Test
    public void buildRequestBody_shouldIncludeSystemAndUserMessage() {
        DoubaoAiChatStrategy strategy = new DoubaoAiChatStrategy(
                "test-key",
                "doubao-test-model",
                "https://example.com/chat/completions"
        );
        JSONObject body = strategy.buildRequestBody("推荐附近的特色美食");

        Assert.assertEquals("doubao-test-model", body.getString("model"));
        Assert.assertEquals(2, body.getJSONArray("messages").size());
        Assert.assertEquals("system", body.getJSONArray("messages").getJSONObject(0).getString("role"));
        Assert.assertTrue(body.getJSONArray("messages").getJSONObject(0).getString("content").contains("省钱兄文旅"));
        Assert.assertFalse(body.getJSONArray("messages").getJSONObject(0).getString("content").contains("荣昌"));
        Assert.assertEquals("user", body.getJSONArray("messages").getJSONObject(1).getString("role"));
        Assert.assertEquals("推荐附近的特色美食", body.getJSONArray("messages").getJSONObject(1).getString("content"));
    }

    @Test
    public void parseReply_shouldExtractAssistantContent() {
        DoubaoAiChatStrategy strategy = new DoubaoAiChatStrategy("test-key", "model", "https://example.com");
        String resp = "{\"choices\":[{\"message\":{\"content\":\"  欢迎体验文旅服务  \"}}]}";
        Assert.assertEquals("欢迎体验文旅服务", strategy.parseReply(resp));
    }
}

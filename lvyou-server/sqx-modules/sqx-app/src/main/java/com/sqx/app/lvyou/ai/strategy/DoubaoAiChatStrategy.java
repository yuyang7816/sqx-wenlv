package com.sqx.app.lvyou.ai.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.lvyou.ai.enums.AiError;
import com.sqx.app.lvyou.ai.support.AiConfigReader;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 豆包大模型（Volcengine Ark Chat Completions）
 */
@Slf4j
public class DoubaoAiChatStrategy implements IAiChatPlatformStrategy {

    static final String DEFAULT_SYSTEM_PROMPT =
            "你是「省钱兄文旅」平台的 AI 助手，面向多地区文旅咨询，包括景点、古镇、非遗、美食、酒店、线路、交通与活动等。"
                    + "请根据用户问题灵活回答，不要默认绑定某一固定城市或地区；涉及具体地点时以用户提到的位置为准，或引导其说明所在区域。"
                    + "请用简洁友好的中文回答；不确定的信息请建议用户在平台对应模块查看或联系人工客服，不要编造票价、营业时间等具体数字。";

    private final String apiKey;
    private final String model;
    private final String endpoint;

    public DoubaoAiChatStrategy(AiConfigReader aiConfigReader) {
        this(aiConfigReader.getDoubaoApiKey(), aiConfigReader.getDoubaoModel(), aiConfigReader.getDoubaoEndpoint());
    }

    DoubaoAiChatStrategy(String apiKey, String model, String endpoint) {
        this.apiKey = apiKey;
        this.model = model;
        this.endpoint = endpoint;
    }

    @Override
    public String chat(String message) {
        if (StringUtils.isBlank(apiKey)) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        try {
            JSONObject body = buildRequestBody(message);
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", "Bearer " + apiKey.trim());
            String resp = OkHttpUtil.doPost(endpoint, body.toJSONString(), "application/json", headers);
            return parseReply(resp);
        } catch (BizException ex) {
            log.warn("豆包 AI 调用失败: {}", ex.getMessage());
            throw new BizException(AiError.CHAT_FAILED);
        } catch (Exception ex) {
            log.error("豆包 AI 调用失败", ex);
            throw new BizException(AiError.CHAT_FAILED);
        }
    }

    JSONObject buildRequestBody(String message) {
        JSONObject body = new JSONObject();
        body.put("model", model);
        JSONArray messages = new JSONArray();

        JSONObject system = new JSONObject();
        system.put("role", "system");
        system.put("content", DEFAULT_SYSTEM_PROMPT);
        messages.add(system);

        JSONObject userMsg = new JSONObject();
        userMsg.put("role", "user");
        userMsg.put("content", StringUtils.trimToEmpty(message));
        messages.add(userMsg);

        body.put("messages", messages);
        return body;
    }

    String parseReply(String resp) {
        if (StringUtils.isBlank(resp)) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        JSONObject json = JSON.parseObject(resp);
        JSONArray choices = json.getJSONArray("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        JSONObject choiceMessage = choices.getJSONObject(0).getJSONObject("message");
        if (choiceMessage == null) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        String content = choiceMessage.getString("content");
        if (StringUtils.isBlank(content)) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        return content.trim();
    }
}

package com.sqx.app.lvyou.ai.service.impl;

import com.sqx.app.lvyou.ai.domain.bo.AiChatBo;
import com.sqx.app.lvyou.ai.domain.vo.AiChatLinkVo;
import com.sqx.app.lvyou.ai.domain.vo.AiChatVo;
import com.sqx.app.lvyou.ai.enums.AiError;
import com.sqx.app.lvyou.ai.enums.AiPlatformCodeEnums;
import com.sqx.app.lvyou.ai.factory.AiChatFactory;
import com.sqx.app.lvyou.ai.service.AiChatService;
import com.sqx.app.lvyou.ai.strategy.IAiChatPlatformStrategy;
import com.sqx.app.lvyou.ai.strategy.LocalRuleAiChatStrategy;
import com.sqx.app.lvyou.ai.support.AiChatRuleSupport;
import com.sqx.app.lvyou.ai.support.AiConfigReader;
import com.sqx.common.core.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * AI 问答：豆包大模型 + 规则 FAQ 兜底与跳转卡片
 */
@Slf4j
@Service
public class AiChatServiceImpl implements AiChatService {

    private static final LocalRuleAiChatStrategy LOCAL_RULE_STRATEGY = new LocalRuleAiChatStrategy();

    @Resource
    private AiChatFactory aiChatFactory;

    @Resource
    private AiConfigReader aiConfigReader;

    @Override
    public AiChatVo chat(AiChatBo chatBo) {
        String message = chatBo == null ? "" : StringUtils.trimToEmpty(chatBo.getMessage());
        AiChatVo ruleHints = AiChatRuleSupport.match(message);
        try {
            String content = resolveReply(message);
            return buildChatVo(content, ruleHints);
        } catch (Exception ex) {
            log.error("AI 问答异常，降级为本地规则回复", ex);
            return buildLocalFallbackVo(message, ruleHints);
        }
    }

    /** 调用当前 AI 供应商；豆包不可用时降级本地规则，避免用户直接看到失败 */
    String resolveReply(String message) {
        IAiChatPlatformStrategy platform = aiChatFactory.getPlatform();
        try {
            String reply = platform.chat(message);
            if (StringUtils.isBlank(reply)) {
                return fallbackReply(message, "empty reply");
            }
            return reply.trim();
        } catch (BizException ex) {
            if (shouldFallbackToLocalRule(ex)) {
                return fallbackReply(message, ex.getMessage());
            }
            throw ex;
        } catch (Exception ex) {
            if (isDoubaoPlatform()) {
                return fallbackReply(message, ex.getMessage());
            }
            log.error("AI 供应商调用异常", ex);
            throw new BizException(AiError.CHAT_FAILED);
        }
    }

    boolean shouldFallbackToLocalRule(BizException ex) {
        if (!isDoubaoPlatform()) {
            return false;
        }
        if (ex.getErrorCode() instanceof AiError && ex.getErrorCode() == AiError.CHAT_FAILED) {
            return true;
        }
        if (ex.getErrorCode() != null) {
            return AiError.CHAT_FAILED.getCode().equals(ex.getErrorCode().getCode());
        }
        String errorMessage = StringUtils.defaultString(ex.getMessage()).trim();
        return errorMessage.isEmpty()
                || "请求失败".equals(errorMessage)
                || errorMessage.startsWith("错误的code:");
    }

    boolean isDoubaoPlatform() {
        return AiPlatformCodeEnums.DOUBAO.getCode().equals(StringUtils.trimToEmpty(aiConfigReader.getPlatform()));
    }

    String fallbackReply(String message, String reason) {
        log.warn("豆包 AI 不可用（{}），降级为本地规则回复", reason);
        String reply = LOCAL_RULE_STRATEGY.chat(message);
        if (StringUtils.isBlank(reply)) {
            throw new BizException(AiError.CHAT_FAILED);
        }
        return reply.trim();
    }

    Boolean resolveMatched(String content, AiChatVo ruleHints) {
        if (isDoubaoPlatform() && !AiChatRuleSupport.DEFAULT_REPLY.equals(content)) {
            return Boolean.TRUE;
        }
        return ruleHints.getMatched();
    }

    private AiChatVo buildChatVo(String content, AiChatVo ruleHints) {
        AiChatVo vo = new AiChatVo();
        vo.setContent(content);
        vo.setLinks(copyLinks(ruleHints.getLinks()));
        vo.setMatched(resolveMatched(content, ruleHints));
        return vo;
    }

    private AiChatVo buildLocalFallbackVo(String message, AiChatVo ruleHints) {
        String content = LOCAL_RULE_STRATEGY.chat(message);
        if (StringUtils.isBlank(content)) {
            content = ruleHints.getContent();
        }
        return buildChatVo(content, ruleHints);
    }

    private List<AiChatLinkVo> copyLinks(List<AiChatLinkVo> links) {
        if (links == null || links.isEmpty()) {
            return new ArrayList<>();
        }
        List<AiChatLinkVo> copied = new ArrayList<>(links.size());
        for (AiChatLinkVo link : links) {
            AiChatLinkVo item = new AiChatLinkVo();
            item.setLabel(link.getLabel());
            item.setPath(link.getPath());
            copied.add(item);
        }
        return copied;
    }
}

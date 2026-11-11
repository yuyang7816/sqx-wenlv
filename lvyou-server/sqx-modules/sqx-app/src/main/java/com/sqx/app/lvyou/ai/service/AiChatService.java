package com.sqx.app.lvyou.ai.service;

import com.sqx.app.lvyou.ai.domain.bo.AiChatBo;
import com.sqx.app.lvyou.ai.domain.vo.AiChatVo;

/**
 * AI 问答（规则 Mock）
 */
public interface AiChatService {

    /**
     * 按关键词匹配 FAQ 并返回固定结构回复
     */
    AiChatVo chat(AiChatBo chatBo);
}

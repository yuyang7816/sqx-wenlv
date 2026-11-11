package com.sqx.app.lvyou.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * AI 问答回复
 */
@Data
@Schema(description = "AI 问答回复")
public class AiChatVo {

    @Schema(description = "回复正文")
    private String content;

    @Schema(description = "是否命中 FAQ 规则")
    private Boolean matched;

    @Schema(description = "推荐跳转卡片")
    private List<AiChatLinkVo> links = new ArrayList<>();
}

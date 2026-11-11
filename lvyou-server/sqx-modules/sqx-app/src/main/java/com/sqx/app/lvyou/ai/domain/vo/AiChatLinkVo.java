package com.sqx.app.lvyou.ai.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * AI 回复推荐跳转
 */
@Data
@Schema(description = "AI 回复推荐跳转")
public class AiChatLinkVo {

    @Schema(description = "跳转文案")
    private String label;

    @Schema(description = "用户端页面路径")
    private String path;
}

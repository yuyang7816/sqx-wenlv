package com.sqx.app.lvyou.ai.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * AI 问答请求
 */
@Data
@Schema(description = "AI 问答请求")
public class AiChatBo {

    @NotBlank(message = "提问内容不能为空")
    @Size(max = 500, message = "提问内容最多500字")
    @Schema(description = "用户提问内容（关键词匹配 FAQ）")
    private String message;
}

package com.sqx.app.lvyou.ai.controller;

import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.ai.domain.bo.AiChatBo;
import com.sqx.app.lvyou.ai.domain.vo.AiChatVo;
import com.sqx.app.lvyou.ai.service.AiChatService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端 AI 问答（豆包大模型，sys_config.ai_platform 可切换本地规则兜底）
 */
@Tag(name = "用户端-AI问答", description = "豆包大模型 + 规则 FAQ 跳转卡片")
@RestController
@RequestMapping("/app/ai")
public class AppAiController {

    @Resource
    private AiChatService aiChatService;

    @Operation(summary = "AI 问答（公开，豆包/规则可配置）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = AiChatVo.class)))
    @PostMapping("chat")
    public Result<AiChatVo> chat(@RequestBody @Validated AiChatBo chatBo) {
        return Result.success(aiChatService.chat(chatBo));
    }
}

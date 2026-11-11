package com.sqx.app.lvyou.help.controller;

import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqListVo;
import com.sqx.app.lvyou.help.service.HelpFaqService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端帮助中心
 */
@Tag(name = "用户端-帮助中心", description = "用户端-帮助中心")
@RestController
@RequestMapping("/app/help")
public class AppHelpController {

    @Resource
    private HelpFaqService helpFaqService;

    @Operation(summary = "FAQ 分类问答列表（公开）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HelpFaqListVo.class)))
    @GetMapping("faq")
    public Result<HelpFaqListVo> faq() {
        return Result.success(helpFaqService.appList());
    }
}

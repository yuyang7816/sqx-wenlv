package com.sqx.app.lvyou.feedback.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackSubmitBo;
import com.sqx.app.lvyou.feedback.domain.vo.FeedbackSubmitVo;
import com.sqx.app.lvyou.feedback.service.FeedbackService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端意见反馈
 */
@Tag(name = "用户端-意见反馈", description = "用户端-意见反馈")
@RestController
@RequestMapping("/app/feedback")
public class AppFeedbackController {

    @Resource
    private FeedbackService feedbackService;

    @Operation(summary = "提交意见反馈")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = FeedbackSubmitVo.class)))
    @PostMapping("submit")
    public Result<FeedbackSubmitVo> submit(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                           @RequestBody @Validated FeedbackSubmitBo submitBo) {
        return Result.success(feedbackService.submit(userId, submitBo));
    }
}

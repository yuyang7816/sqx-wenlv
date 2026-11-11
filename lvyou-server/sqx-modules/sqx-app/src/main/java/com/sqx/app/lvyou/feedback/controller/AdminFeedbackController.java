package com.sqx.app.lvyou.feedback.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackPageBo;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackProcessBo;
import com.sqx.app.lvyou.feedback.domain.vo.FeedbackVo;
import com.sqx.app.lvyou.feedback.service.FeedbackService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端意见反馈
 */
@Tag(name = "管理端-意见反馈", description = "管理端-意见反馈")
@RestController
@RequestMapping("/admin/feedback")
public class AdminFeedbackController {

    @Resource
    private FeedbackService feedbackService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:feedback:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = FeedbackVo.class)))
    @PostMapping("page")
    public Result<IPage<FeedbackVo>> page(@RequestBody @Validated FeedbackPageBo pageBo) {
        return Result.success(feedbackService.pageInfo(pageBo));
    }

    @Operation(summary = "标记已处理")
    @RequiresPermissions("lvyou:feedback:process")
    @PostMapping("process")
    public Result<Void> process(@RequestBody @Validated FeedbackProcessBo processBo) {
        feedbackService.process(processBo);
        return Result.success();
    }
}

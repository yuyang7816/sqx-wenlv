package com.sqx.app.lvyou.invite.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.appConfig.domain.vo.InviteConfigVO;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.invite.domain.vo.InviteCommissionDetailVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteCommissionSummaryVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteSummaryVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteTeamMemberVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteTeamSummaryVo;
import com.sqx.app.lvyou.invite.service.InviteService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端邀请分销
 */
@Tag(name = "用户端-邀请分销", description = "用户端-邀请分销")
@RestController
@RequestMapping("/app/invite")
public class AppInviteController {

    @Resource
    private InviteService inviteService;

    @Operation(summary = "邀请展示配置")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = InviteConfigVO.class)))
    @GetMapping("config")
    public Result<InviteConfigVO> config() {
        return Result.success(inviteService.getConfig());
    }

    @Operation(summary = "邀请统计与用户展示信息")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = InviteSummaryVo.class)))
    @GetMapping("summary")
    public Result<InviteSummaryVo> summary(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(inviteService.getSummary(userId));
    }

    @Operation(summary = "团队汇总")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = InviteTeamSummaryVo.class)))
    @GetMapping("team/summary")
    public Result<InviteTeamSummaryVo> teamSummary(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(inviteService.getTeamSummary(userId));
    }

    @Operation(summary = "团队成员（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = InviteTeamMemberVo.class)))
    @GetMapping("team/members")
    public Result<IPage<InviteTeamMemberVo>> teamMembers(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestParam(value = "level", required = false) Integer level,
            @RequestParam(value = "page", required = false) Long page,
            @RequestParam(value = "limit", required = false) Long limit) {
        return Result.success(inviteService.getTeamMembers(userId, level, page, limit));
    }

    @Operation(summary = "收益概览")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = InviteCommissionSummaryVo.class)))
    @GetMapping("commission/summary")
    public Result<InviteCommissionSummaryVo> commissionSummary(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(inviteService.getCommissionSummary(userId));
    }

    @Operation(summary = "收益明细（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = InviteCommissionDetailVo.class)))
    @GetMapping("commission/details")
    public Result<IPage<InviteCommissionDetailVo>> commissionDetails(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestParam(value = "level", required = false) Integer level,
            @RequestParam(value = "page", required = false) Long page,
            @RequestParam(value = "limit", required = false) Long limit) {
        return Result.success(inviteService.getCommissionDetails(userId, level, page, limit));
    }
}

package com.sqx.app.lvyou.invite.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.invite.domain.bo.CommissionRecordAdminPageBo;
import com.sqx.app.lvyou.invite.domain.bo.InviteRelationAdminPageBo;
import com.sqx.app.lvyou.invite.domain.vo.CommissionRecordAdminVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteRelationAdminVo;
import com.sqx.app.lvyou.invite.service.InviteAdminService;
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
 * 管理端邀请分销监管
 */
@Tag(name = "管理端-邀请分销", description = "管理端-邀请分销")
@RestController
@RequestMapping("/admin/invite")
public class AdminInviteController {

    @Resource
    private InviteAdminService inviteAdminService;

    @Operation(summary = "邀请关系分页列表")
    @RequiresPermissions("lvyou:invite:relation:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = InviteRelationAdminVo.class)))
    @PostMapping("relation/page")
    public Result<IPage<InviteRelationAdminVo>> relationPage(
            @RequestBody @Validated InviteRelationAdminPageBo pageBo) {
        return Result.success(inviteAdminService.relationPage(pageBo));
    }

    @Operation(summary = "佣金记录分页列表")
    @RequiresPermissions("lvyou:invite:commission:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CommissionRecordAdminVo.class)))
    @PostMapping("commission/page")
    public Result<IPage<CommissionRecordAdminVo>> commissionPage(
            @RequestBody @Validated CommissionRecordAdminPageBo pageBo) {
        return Result.success(inviteAdminService.commissionPage(pageBo));
    }
}

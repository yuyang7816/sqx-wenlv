package com.sqx.app.lvyou.wallet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.wallet.domain.bo.WithdrawAdminAuditBo;
import com.sqx.app.lvyou.wallet.domain.bo.WithdrawAdminPageBo;
import com.sqx.app.lvyou.wallet.domain.vo.WithdrawAdminVo;
import com.sqx.app.lvyou.wallet.service.WithdrawAdminService;
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
 * 管理端提现审核
 */
@Tag(name = "管理端-提现", description = "管理端-提现审核")
@RestController
@RequestMapping("/admin/withdraw")
public class AdminWithdrawController {

    @Resource
    private WithdrawAdminService withdrawAdminService;

    @Operation(summary = "提现分页列表（用户/商户）")
    @RequiresPermissions("lvyou:withdraw:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = WithdrawAdminVo.class)))
    @PostMapping("page")
    public Result<IPage<WithdrawAdminVo>> page(@RequestBody @Validated WithdrawAdminPageBo pageBo) {
        return Result.success(withdrawAdminService.page(pageBo));
    }

    @Operation(summary = "提现审核（通过/驳回）")
    @RequiresPermissions("lvyou:withdraw:audit")
    @PostMapping("audit")
    public Result<Void> audit(@RequestBody @Validated WithdrawAdminAuditBo auditBo) {
        withdrawAdminService.audit(auditBo);
        return Result.success();
    }
}

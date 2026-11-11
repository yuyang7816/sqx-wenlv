package com.sqx.app.base.user.cancel.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountAuditBo;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountPageBo;
import com.sqx.app.base.user.cancel.domain.vo.CancelAccountVo;
import com.sqx.app.base.user.cancel.service.CancelAccountService;
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
 * 管理端注销审核
 */
@Tag(name = "管理端-注销审核", description = "管理端-注销审核")
@RestController
@RequestMapping(BaseConstant.BASE_ADMIN_PREFIX + "/cancelAccount")
public class AdminCancelAccountController {

    @Resource
    private CancelAccountService cancelAccountService;

    @Operation(summary = "注销申请分页列表")
    @RequiresPermissions("base:cancelAccount:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = CancelAccountVo.class)))
    @PostMapping("page")
    public Result<IPage<CancelAccountVo>> page(@RequestBody @Validated CancelAccountPageBo pageBo) {
        return Result.success(cancelAccountService.page(pageBo));
    }

    @Operation(summary = "注销审核（通过/驳回）")
    @RequiresPermissions("base:cancelAccount:audit")
    @PostMapping("audit")
    public Result<Void> audit(@RequestBody @Validated CancelAccountAuditBo auditBo) {
        cancelAccountService.audit(auditBo);
        return Result.success();
    }
}

package com.sqx.app.lvyou.merchant.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantAdjustBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyAuditBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyPageBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantInfoBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantPageBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantSaveBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantUpdateBo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantAdminVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantApplyVo;
import com.sqx.app.lvyou.merchant.service.MerchantService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端商户入驻审核
 */
@Tag(name = "管理端-商户入驻", description = "管理端-商户入驻")
@RestController
@RequestMapping("/admin/merchant")
public class AdminMerchantController {

    @Resource
    private MerchantService merchantService;

    @Operation(summary = "入驻申请分页列表")
    @RequiresPermissions("lvyou:merchant:apply:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantApplyVo.class)))
    @PostMapping("apply/page")
    public Result<IPage<MerchantApplyVo>> page(@RequestBody @Validated MerchantApplyPageBo pageBo) {
        return Result.success(merchantService.pageApply(pageBo));
    }

    @Operation(summary = "入驻申请详情")
    @RequiresPermissions("lvyou:merchant:apply:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantApplyVo.class)))
    @GetMapping("apply/{id}")
    public Result<MerchantApplyVo> detail(@PathVariable("id") Long id) {
        return Result.success(merchantService.getApplyDetail(id));
    }

    @Operation(summary = "审核入驻申请")
    @RequiresPermissions("lvyou:merchant:apply:audit")
    @PostMapping("audit")
    public Result<Void> audit(@RequestBody @Validated MerchantApplyAuditBo auditBo) {
        merchantService.audit(auditBo);
        return Result.success();
    }

    @Operation(summary = "商户分页列表")
    @RequiresPermissions("lvyou:merchant:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantAdminVo.class)))
    @PostMapping("page")
    public Result<IPage<MerchantAdminVo>> merchantPage(@RequestBody @Validated MerchantPageBo pageBo) {
        return Result.success(merchantService.pageMerchant(pageBo));
    }

    @Operation(summary = "商户详情")
    @RequiresPermissions("lvyou:merchant:info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantAdminVo.class)))
    @GetMapping("{id}")
    public Result<MerchantAdminVo> merchantDetail(@PathVariable("id") Long id) {
        return Result.success(merchantService.getMerchantDetail(id));
    }

    @Operation(summary = "商户状态调整")
    @RequiresPermissions("lvyou:merchant:adjust")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantAdminVo.class)))
    @PostMapping("adjust")
    public Result<MerchantAdminVo> adjust(@RequestBody @Validated MerchantAdjustBo adjustBo) {
        return Result.success(merchantService.adjust(adjustBo));
    }

    @Operation(summary = "新增商户")
    @RequiresPermissions(value = {"lvyou:merchant:save", "lvyou:merchant:adjust"}, logical = Logical.OR)
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated MerchantSaveBo saveBo) {
        merchantService.saveMerchant(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新商户")
    @RequiresPermissions(value = {"lvyou:merchant:update", "lvyou:merchant:adjust"}, logical = Logical.OR)
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated MerchantUpdateBo updateBo) {
        merchantService.updateMerchant(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除商户")
    @RequiresPermissions(value = {"lvyou:merchant:delete", "lvyou:merchant:adjust"}, logical = Logical.OR)
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated MerchantInfoBo infoBo) {
        merchantService.deleteMerchant(infoBo);
        return Result.success();
    }
}

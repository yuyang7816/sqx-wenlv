package com.sqx.app.lvyou.merchant.controller;

import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositAdjustBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositRefundAuditBo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositRefundVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositVo;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@Tag(name = "管理端-商户保证金", description = "管理端-商户保证金调整与退保审核")
@RestController
@RequestMapping("/admin/merchant/deposit")
public class AdminMerchantDepositController {

    @Resource
    private MerchantWalletService merchantWalletService;

    @Operation(summary = "调整商户保证金")
    @RequiresPermissions("lvyou:merchant:adjust")
    @PostMapping("adjust")
    public Result<MerchantDepositVo> adjust(@RequestBody @Validated MerchantDepositAdjustBo adjustBo) {
        return Result.success(merchantWalletService.adminAdjustDeposit(adjustBo));
    }

    @Operation(summary = "审核商户退保申请")
    @RequiresPermissions("lvyou:merchant:adjust")
    @PostMapping("refund/audit")
    public Result<MerchantDepositRefundVo> auditRefund(@RequestBody @Validated MerchantDepositRefundAuditBo auditBo) {
        return Result.success(merchantWalletService.auditDepositRefund(auditBo));
    }
}

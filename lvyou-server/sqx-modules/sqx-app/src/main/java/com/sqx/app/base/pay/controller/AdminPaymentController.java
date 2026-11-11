package com.sqx.app.base.pay.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.pay.domain.bo.PaymentAdminPageBo;
import com.sqx.app.base.pay.domain.bo.PaymentConfigUpdateBo;
import com.sqx.app.base.pay.domain.vo.PaymentAdminVo;
import com.sqx.app.base.pay.domain.vo.PaymentConfigAdminVo;
import com.sqx.app.base.pay.service.PaymentAdminService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 管理端支付监管
 */
@Tag(name = "管理端-支付", description = "管理端-支付流水与渠道配置")
@RestController
@RequestMapping("/admin/payment")
public class AdminPaymentController {

    @Resource
    private PaymentAdminService paymentAdminService;

    @Operation(summary = "支付流水分页列表")
    @RequiresPermissions("lvyou:payment:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PaymentAdminVo.class)))
    @PostMapping("page")
    public Result<IPage<PaymentAdminVo>> page(@RequestBody PaymentAdminPageBo pageBo) {
        return Result.success(paymentAdminService.page(pageBo));
    }

    @Operation(summary = "支付流水详情")
    @RequiresPermissions("lvyou:payment:info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PaymentAdminVo.class)))
    @GetMapping("info")
    public Result<PaymentAdminVo> info(@RequestParam("payNo") String payNo,
                                       @RequestParam(value = "businessType", required = false) String businessType) {
        return Result.success(paymentAdminService.info(payNo, businessType));
    }

    @Operation(summary = "支付渠道配置列表")
    @RequiresPermissions("lvyou:payment:config:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = PaymentConfigAdminVo.class)))
    @PostMapping("config/list")
    public Result<List<PaymentConfigAdminVo>> configList() {
        return Result.success(paymentAdminService.listConfig());
    }

    @Operation(summary = "更新支付渠道配置")
    @RequiresPermissions("lvyou:payment:config:update")
    @PostMapping("config/update")
    public Result<Void> configUpdate(@RequestBody @Validated PaymentConfigUpdateBo updateBo) {
        paymentAdminService.updateConfig(updateBo);
        return Result.success();
    }
}

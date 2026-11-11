package com.sqx.app.lvyou.merchant.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositRefundApplyBo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositRefundVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositPayVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantFundRecordVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantWalletSummaryVo;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.wallet.domain.bo.WalletWithdrawBo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawRecordVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawVo;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端商户钱包
 */
@Tag(name = "用户端-商户钱包", description = "用户端-商户钱包")
@RestController
@RequestMapping("/app/merchant/wallet")
public class AppMerchantWalletController {

    @Resource
    private MerchantWalletService merchantWalletService;

    @Operation(summary = "商户钱包概览")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantWalletSummaryVo.class)))
    @GetMapping("summary")
    public Result<MerchantWalletSummaryVo> summary(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(merchantWalletService.getSummary(userId));
    }

    @Operation(summary = "创建保证金支付单")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantDepositPayVo.class)))
    @PostMapping("deposit/order")
    public Result<MerchantDepositPayVo> createDepositOrder(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestBody(required = false) MerchantDepositBo depositBo) {
        if (depositBo == null) {
            depositBo = new MerchantDepositBo();
        }
        return Result.success(merchantWalletService.createDepositPayOrder(userId, depositBo));
    }

    @Operation(summary = "余额缴纳保证金")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantDepositVo.class)))
    @PostMapping("deposit/pay-balance")
    public Result<MerchantDepositVo> payDepositByBalance(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestBody MerchantDepositBo depositBo) {
        return Result.success(merchantWalletService.payDepositByBalance(userId, depositBo.getPayNo()));
    }

    @Operation(summary = "申请退还保证金")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantDepositRefundVo.class)))
    @PostMapping("deposit/refund/apply")
    public Result<MerchantDepositRefundVo> applyDepositRefund(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestBody(required = false) MerchantDepositRefundApplyBo applyBo) {
        if (applyBo == null) {
            applyBo = new MerchantDepositRefundApplyBo();
        }
        return Result.success(merchantWalletService.applyDepositRefund(userId, applyBo));
    }

    @Operation(summary = "申请提现")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = WalletWithdrawVo.class)))
    @PostMapping("withdraw")
    public Result<WalletWithdrawVo> withdraw(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                             @RequestBody @Validated WalletWithdrawBo withdrawBo) {
        return Result.success(merchantWalletService.createWithdraw(userId, withdrawBo));
    }

    @Operation(summary = "资金明细（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantFundRecordVo.class)))
    @GetMapping("fund-records")
    public Result<IPage<MerchantFundRecordVo>> fundRecords(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestParam(value = "page", required = false) Long page,
            @RequestParam(value = "limit", required = false) Long limit,
            @RequestParam(value = "filter", required = false) String filter) {
        return Result.success(merchantWalletService.listFundRecords(userId, page, limit, filter));
    }

    @Operation(summary = "提现记录（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = WalletWithdrawRecordVo.class)))
    @GetMapping("withdraw-records")
    public Result<IPage<WalletWithdrawRecordVo>> withdrawRecords(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestParam(value = "page", required = false) Long page,
            @RequestParam(value = "limit", required = false) Long limit) {
        return Result.success(merchantWalletService.listWithdrawRecords(userId, page, limit));
    }
}

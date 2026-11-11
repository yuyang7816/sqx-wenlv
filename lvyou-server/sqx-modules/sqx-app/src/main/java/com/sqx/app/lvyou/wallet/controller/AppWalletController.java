package com.sqx.app.lvyou.wallet.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.wallet.domain.bo.WalletPayBo;
import com.sqx.app.lvyou.wallet.domain.bo.WalletRechargeBo;
import com.sqx.app.lvyou.wallet.domain.bo.WalletWithdrawBo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletPayVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletRechargeVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletSummaryVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletTransactionVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawRecordVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawVo;
import com.sqx.app.lvyou.wallet.service.WalletService;
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
 * 用户端钱包
 */
@Tag(name = "用户端-钱包", description = "用户端-钱包")
@RestController
@RequestMapping("/app/wallet")
public class AppWalletController {

    @Resource
    private WalletService walletService;

    @Operation(summary = "钱包概览")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = WalletSummaryVo.class)))
    @GetMapping("summary")
    public Result<WalletSummaryVo> summary(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(walletService.getSummary(userId));
    }

    @Operation(summary = "资金明细（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = WalletTransactionVo.class)))
    @GetMapping("transactions")
    public Result<IPage<WalletTransactionVo>> transactions(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestParam(value = "page", required = false) Long page,
            @RequestParam(value = "limit", required = false) Long limit,
            @RequestParam(value = "filter", required = false) String filter) {
        return Result.success(walletService.listTransactions(userId, page, limit, filter));
    }

    @Operation(summary = "提现记录（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = WalletWithdrawRecordVo.class)))
    @GetMapping("withdraw-records")
    public Result<IPage<WalletWithdrawRecordVo>> withdrawRecords(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
            @RequestParam(value = "page", required = false) Long page,
            @RequestParam(value = "limit", required = false) Long limit) {
        return Result.success(walletService.listWithdrawRecords(userId, page, limit));
    }

    @Operation(summary = "创建充值单（待第三方支付）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = WalletRechargeVo.class)))
    @PostMapping("recharge")
    public Result<WalletRechargeVo> recharge(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                             @RequestBody @Validated WalletRechargeBo rechargeBo) {
        return Result.success(walletService.createRecharge(userId, rechargeBo));
    }

    @Operation(summary = "申请提现")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = WalletWithdrawVo.class)))
    @PostMapping("withdraw")
    public Result<WalletWithdrawVo> withdraw(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                           @RequestBody @Validated WalletWithdrawBo withdrawBo) {
        return Result.success(walletService.createWithdraw(userId, withdrawBo));
    }

    @Operation(summary = "余额支付订单")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = WalletPayVo.class)))
    @PostMapping("pay")
    public Result<WalletPayVo> pay(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                   @RequestBody @Validated WalletPayBo payBo) {
        return Result.success(walletService.payByBalance(userId, payBo));
    }
}

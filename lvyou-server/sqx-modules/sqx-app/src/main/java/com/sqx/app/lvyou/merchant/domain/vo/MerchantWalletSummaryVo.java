package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户钱包概览
 */
@Data
public class MerchantWalletSummaryVo {

    @Schema(description = "可提现余额")
    private BigDecimal balance;

    @Schema(description = "待结算金额")
    private BigDecimal pendingSettle;

    @Schema(description = "累计收入")
    private BigDecimal totalIncome;

    @Schema(description = "应缴保证金")
    private BigDecimal depositRequired;

    @Schema(description = "已缴保证金")
    private BigDecimal depositPaid;

    @Schema(description = "保证金状态：paid/unpaid")
    private String depositStatus;

    @Schema(description = "保证金状态展示")
    private String depositStatusLabel;

    @Schema(description = "提现记录笔数")
    private Integer withdrawCount;

    @Schema(description = "提现规则")
    private com.sqx.app.lvyou.wallet.domain.vo.WithdrawRulesVo withdrawRules;

    @Schema(description = "是否可申请退保")
    private Boolean canApplyDepositRefund;

    @Schema(description = "待审核退保申请")
    private MerchantDepositRefundVo pendingDepositRefund;

    @Schema(description = "最近驳回的退保申请（含驳回原因）")
    private MerchantDepositRefundVo rejectedDepositRefund;
}

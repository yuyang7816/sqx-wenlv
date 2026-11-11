package com.sqx.app.lvyou.wallet.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包充值单结果
 */
@Data
public class WalletRechargeVo {

    @Schema(description = "充值单号（用于统一支付 outTradeNo）")
    private String rechargeNo;

    @Schema(description = "充值金额")
    private BigDecimal amount;

    @Schema(description = "状态：pending_pay/paid/cancelled")
    private String status;
}

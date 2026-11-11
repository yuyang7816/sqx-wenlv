package com.sqx.app.lvyou.wallet.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 提现申请结果
 */
@Data
public class WalletWithdrawVo {

    @Schema(description = "提现单号")
    private String withdrawNo;

    @Schema(description = "提现金额")
    private BigDecimal amount;

    @Schema(description = "手续费")
    private BigDecimal feeAmount;

    @Schema(description = "实际到账金额")
    private BigDecimal actualAmount;

    @Schema(description = "状态：pending")
    private String status;

    @Schema(description = "提示文案")
    private String remark;
}

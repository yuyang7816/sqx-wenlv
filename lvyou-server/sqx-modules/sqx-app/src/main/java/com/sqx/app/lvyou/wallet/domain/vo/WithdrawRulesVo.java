package com.sqx.app.lvyou.wallet.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 提现规则（最小/最大金额、手续费比例）
 */
@Data
public class WithdrawRulesVo {

    @Schema(description = "最低提现金额（元）")
    private String minAmount;

    @Schema(description = "最高提现金额（元）")
    private String maxAmount;

    @Schema(description = "手续费比例（%，如 1 表示 1%）")
    private String feeRate;
}

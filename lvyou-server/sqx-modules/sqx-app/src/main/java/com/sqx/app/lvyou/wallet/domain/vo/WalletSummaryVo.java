package com.sqx.app.lvyou.wallet.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 钱包概览
 */
@Data
public class WalletSummaryVo {

    @Schema(description = "账户余额（元，两位小数）")
    private String balance;

    @Schema(description = "可提现金额（元，两位小数）")
    private String available;

    @Schema(description = "累计充值（元，两位小数）")
    private String totalRecharge;

    @Schema(description = "累计消费（元，两位小数）")
    private String totalConsume;

    @Schema(description = "累计提现（元，两位小数）")
    private String totalWithdraw;

    @Schema(description = "提现记录笔数")
    private Integer withdrawCount;

    @Schema(description = "提现规则")
    private WithdrawRulesVo withdrawRules;
}

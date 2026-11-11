package com.sqx.app.lvyou.wallet.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 钱包流水明细
 */
@Data
public class WalletTransactionVo {

    @Schema(description = "流水号")
    private String id;

    @Schema(description = "类型：recharge/consume/income/withdraw")
    private String type;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "变动金额（正收入负支出）")
    private BigDecimal amount;

    @Schema(description = "时间 yyyy-MM-dd HH:mm:ss")
    private String time;

    @Schema(description = "状态：pending/success/failed")
    private String status;
}

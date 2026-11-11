package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户资金明细
 */
@Data
public class MerchantFundRecordVo {

    @Schema(description = "流水号")
    private String id;

    @Schema(description = "流水类型：deposit/withdraw/income/settle")
    private String type;

    @Schema(description = "流水标题")
    private String title;

    @Schema(description = "变动金额（正收入负支出）")
    private BigDecimal amount;

    @Schema(description = "发生时间")
    private String time;

    @Schema(description = "关联订单号/提现单号")
    private String orderNo;

    @Schema(description = "变动后可提现余额")
    private BigDecimal balance;

    @Schema(description = "状态：pending/success/failed")
    private String status;
}

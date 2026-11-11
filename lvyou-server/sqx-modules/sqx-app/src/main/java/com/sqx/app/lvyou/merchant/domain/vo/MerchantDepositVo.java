package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户保证金缴纳结果
 */
@Data
public class MerchantDepositVo {

    @Schema(description = "本次缴纳金额")
    private BigDecimal paid;

    @Schema(description = "缴纳后已缴保证金")
    private BigDecimal depositPaid;

    @Schema(description = "保证金状态：paid/unpaid")
    private String depositStatus;
}

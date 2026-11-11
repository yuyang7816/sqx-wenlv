package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户保证金支付单
 */
@Data
public class MerchantDepositPayVo {

    @Schema(description = "支付单号")
    private String payNo;

    @Schema(description = "支付金额")
    private BigDecimal amount;
}

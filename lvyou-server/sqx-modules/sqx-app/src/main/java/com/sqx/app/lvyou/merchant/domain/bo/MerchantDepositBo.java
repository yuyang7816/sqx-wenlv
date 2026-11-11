package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户保证金缴纳入参
 */
@Data
public class MerchantDepositBo {

    @Schema(description = "缴纳金额（不传则按应缴差额一次性缴足）")
    private BigDecimal amount;

    @Schema(description = "保证金支付单号（余额/第三方支付时使用）")
    private String payNo;
}

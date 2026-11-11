package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class MerchantDepositRefundApplyBo {

    @Schema(description = "申请原因")
    private String reason;
}

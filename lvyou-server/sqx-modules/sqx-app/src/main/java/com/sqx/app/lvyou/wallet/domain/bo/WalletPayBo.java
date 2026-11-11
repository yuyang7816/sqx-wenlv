package com.sqx.app.lvyou.wallet.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 余额支付入参
 */
@Data
public class WalletPayBo {

    @NotBlank(message = "订单号不能为空")
    @Schema(description = "待支付订单号")
    private String orderNo;
}

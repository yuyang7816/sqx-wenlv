package com.sqx.app.lvyou.wallet.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 用户提现入参
 */
@Data
public class WalletWithdrawBo {

    @NotNull(message = "提现金额不能为空")
    @DecimalMin(value = "0.01", message = "提现金额不正确")
    @Schema(description = "提现金额")
    private BigDecimal amount;

    @NotBlank(message = "提现方式不能为空")
    @Schema(description = "提现方式：alipay/wechat")
    private String method;

    @Schema(description = "支付宝账号（method=alipay 时必填）")
    private String alipayAccount;

    @Schema(description = "支付宝实名姓名（method=alipay 时必填）")
    private String alipayName;

    @Schema(description = "微信收款码URL（method=wechat 时必填）")
    private String wechatQrCode;
}

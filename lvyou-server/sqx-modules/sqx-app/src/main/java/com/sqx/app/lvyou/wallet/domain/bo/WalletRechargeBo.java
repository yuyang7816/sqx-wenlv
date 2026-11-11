package com.sqx.app.lvyou.wallet.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 钱包充值入参
 */
@Data
public class WalletRechargeBo {

    @NotNull(message = "充值金额不能为空")
    @DecimalMin(value = "0.01", message = "充值金额至少 0.01 元")
    @Schema(description = "充值金额")
    private BigDecimal amount;
}

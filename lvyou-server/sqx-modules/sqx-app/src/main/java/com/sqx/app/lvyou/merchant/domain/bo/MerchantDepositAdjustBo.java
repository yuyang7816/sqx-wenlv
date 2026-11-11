package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

@Data
public class MerchantDepositAdjustBo {

    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    /**
     * paid_increase / paid_decrease / required_increase / required_decrease
     */
    @NotBlank(message = "调整类型不能为空")
    private String adjustType;

    @NotNull(message = "调整金额不能为空")
    @DecimalMin(value = "0.01", message = "调整金额必须大于0")
    private BigDecimal amount;

    private String remark;
}

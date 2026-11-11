package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 保存/更新商户入驻类型
 */
@Data
@Schema(description = "商户入驻类型保存")
public class MerchantStoreTypeSaveBo {

    @Schema(description = "字典ID，更新时必填")
    private Long id;

    @NotBlank(message = "类型编码不能为空")
    @Schema(description = "类型编码，如 hotel")
    private String code;

    @NotBlank(message = "类型名称不能为空")
    @Schema(description = "展示名称")
    private String label;

    @NotNull(message = "保证金不能为空")
    @DecimalMin(value = "0", message = "保证金不能为负数")
    @Schema(description = "应缴保证金（元）")
    private BigDecimal depositAmount;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注")
    private String remark;
}

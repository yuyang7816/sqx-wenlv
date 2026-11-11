package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户入驻门店类型（管理端 / C 端）
 */
@Data
@Schema(description = "商户入驻门店类型")
public class MerchantStoreTypeVo {

    @Schema(description = "字典ID")
    private Long id;

    @Schema(description = "类型编码，如 hotel")
    private String code;

    @Schema(description = "展示名称")
    private String label;

    @Schema(description = "应缴保证金（元）")
    private BigDecimal depositAmount;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "备注说明")
    private String remark;
}

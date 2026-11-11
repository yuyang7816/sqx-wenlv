package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商户订单明细 VO
 */
@Data
@Schema(description = "商户订单明细")
public class MerchantOrderItemVo {

    @Schema(description = "明细名称")
    private String name;

    @Schema(description = "规格描述")
    private String spec;

    @Schema(description = "单价")
    private BigDecimal price;

    @Schema(description = "数量")
    private Integer qty;

    @Schema(description = "封面图")
    private String cover;
}

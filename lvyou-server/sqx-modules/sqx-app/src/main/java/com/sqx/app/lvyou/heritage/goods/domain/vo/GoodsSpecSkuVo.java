package com.sqx.app.lvyou.heritage.goods.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 商品规格 SKU
 */
@Data
@Schema(description = "商品规格SKU")
public class GoodsSpecSkuVo {

    @Schema(description = "规格名称")
    private String name;

    @Schema(description = "销售价（元）")
    private BigDecimal price;

    @Schema(description = "库存")
    private Integer stock;
}

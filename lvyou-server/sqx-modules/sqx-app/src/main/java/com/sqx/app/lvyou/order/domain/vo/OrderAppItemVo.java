package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 用户端订单明细 VO
 */
@Data
@Schema(description = "订单明细")
public class OrderAppItemVo {

    @Schema(description = "明细名称")
    private String name;

    @Schema(description = "规格描述")
    private String spec;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "数量")
    private Integer quantity;

    @Schema(description = "单价")
    private BigDecimal unitPrice;

    @Schema(description = "小计")
    private BigDecimal totalPrice;
}

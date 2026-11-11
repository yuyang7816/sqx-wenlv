package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建订单结果
 */
@Data
@Schema(description = "创建订单结果")
public class OrderCreateResultVo {

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "实付金额")
    private BigDecimal payAmount;

    @Schema(description = "订单状态码")
    private String statusCode;

    @Schema(description = "订单状态文案")
    private String status;

    @Schema(description = "订单类型（权威）")
    private String orderType;
}

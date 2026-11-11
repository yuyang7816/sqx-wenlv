package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 创建商品订单
 */
@Data
@Schema(description = "创建商品订单")
public class OrderGoodsCreateBo {

    @NotNull(message = "商品ID不能为空")
    @Schema(description = "商品ID（C 端 tb_heritage_goods.id 或 tb_merchant_service.id）")
    private Long goodsId;

    @NotNull(message = "收货地址ID不能为空")
    @Schema(description = "收货地址ID")
    private Long addressId;

    @Min(value = 1, message = "购买数量至少为1")
    @Schema(description = "购买数量")
    private Integer quantity;

    @Schema(description = "规格描述")
    private String itemSpec;

    @Schema(description = "用户优惠券ID")
    private Long couponId;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "客户端传入单价（校验用，可选）")
    private BigDecimal unitPrice;
}

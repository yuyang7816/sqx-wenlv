package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端订单操作
 */
@Data
@Schema(description = "管理端订单操作")
public class OrderAdminActionBo {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "订单ID")
    private Long orderId;

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作：cancel/complete/refund")
    private String action;

    @Schema(description = "操作原因/备注")
    private String reason;

    @Min(value = 0, message = "退款比例不能小于0")
    @Max(value = 100, message = "退款比例不能大于100")
    @Schema(description = "线路强制退款比例（0-100，仅 route 退款时必填）")
    private Integer refundRatio;
}

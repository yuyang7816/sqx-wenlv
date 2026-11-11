package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 用户端订单操作入参
 */
@Data
@Schema(description = "用户端订单操作")
public class OrderAppActionBo {

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作：complete（确认收货）/ refund（申请退款）")
    private String action;

    @Schema(description = "退款原因（申请退款时建议填写）")
    private String reason;
}

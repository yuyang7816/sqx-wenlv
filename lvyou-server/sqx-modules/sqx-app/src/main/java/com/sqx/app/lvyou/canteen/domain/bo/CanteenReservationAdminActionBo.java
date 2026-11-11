package com.sqx.app.lvyou.canteen.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端餐饮预约操作
 */
@Data
@Schema(description = "管理端餐饮预约操作")
public class CanteenReservationAdminActionBo {

    @NotNull(message = "预约ID不能为空")
    @Schema(description = "预约ID")
    private Long reservationId;

    @NotBlank(message = "操作类型不能为空")
    @Schema(description = "操作：confirm/cancel/complete")
    private String action;

    @Schema(description = "操作原因/备注")
    private String reason;
}

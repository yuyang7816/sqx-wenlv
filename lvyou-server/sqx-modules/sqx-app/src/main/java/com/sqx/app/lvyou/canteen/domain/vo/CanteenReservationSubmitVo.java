package com.sqx.app.lvyou.canteen.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 餐饮预约提交结果
 */
@Data
@Schema(description = "餐饮预约提交结果")
public class CanteenReservationSubmitVo {

    @Schema(description = "预约ID")
    private Long id;

    @Schema(description = "预约业务单号")
    private String reservationNo;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "门店名称")
    private String shopName;
}

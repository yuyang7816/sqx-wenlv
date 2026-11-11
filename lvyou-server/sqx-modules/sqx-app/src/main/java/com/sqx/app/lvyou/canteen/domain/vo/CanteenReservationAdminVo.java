package com.sqx.app.lvyou.canteen.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端餐饮预约 VO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理端餐饮预约")
public class CanteenReservationAdminVo extends CanteenReservationVo {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "关联订单ID")
    private Long orderId;

    @Schema(description = "关联订单号")
    private String orderNo;

    @Schema(description = "更新时间 yyyy-MM-dd HH:mm")
    private String updateTime;
}

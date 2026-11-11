package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端订单 VO
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理端订单")
public class OrderAdminVo extends OrderAppVo {

    @Schema(description = "下单用户ID")
    private Long userId;

    @Schema(description = "下单用户名")
    private String userName;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "支付时间")
    private String payTime;

    @Schema(description = "创建时间")
    private String createTime;

    @Schema(description = "更新时间")
    private String updateTime;
}

package com.sqx.app.lvyou.invite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端佣金记录 VO
 */
@Data
@Schema(description = "管理端佣金记录")
public class CommissionRecordAdminVo {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "受益人昵称")
    private String beneficiaryName;

    @Schema(description = "受益人手机号")
    private String beneficiaryPhone;

    @Schema(description = "下单用户昵称")
    private String sourceName;

    @Schema(description = "下单用户手机号")
    private String sourcePhone;

    @Schema(description = "佣金层级：1一级 2二级")
    private Integer commissionLevel;

    @Schema(description = "佣金层级文案")
    private String commissionLevelLabel;

    @Schema(description = "订单实付金额")
    private String orderAmount;

    @Schema(description = "佣金比例")
    private String commissionRate;

    @Schema(description = "佣金金额")
    private String commissionAmount;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "结算时间 yyyy-MM-dd HH:mm:ss")
    private String settleTime;
}

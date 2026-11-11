package com.sqx.app.lvyou.invite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收益明细
 */
@Data
public class InviteCommissionDetailVo {

    @Schema(description = "记录ID")
    private Long id;

    @Schema(description = "下单用户头像")
    private String avatar;

    @Schema(description = "下单用户昵称")
    private String nickname;

    @Schema(description = "佣金金额")
    private String reward;

    @Schema(description = "结算时间")
    private String time;
}

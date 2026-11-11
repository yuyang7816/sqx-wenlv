package com.sqx.app.lvyou.invite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邀请页统计与用户展示信息
 */
@Data
public class InviteSummaryVo {

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "脱敏手机号")
    private String phone;

    @Schema(description = "我的邀请码")
    private String inviteCode;

    @Schema(description = "分享二维码内容")
    private String qrContent;

    @Schema(description = "团队总人数（一级+二级）")
    private Integer inviteCount;

    @Schema(description = "累计收益")
    private String totalEarnings;
}

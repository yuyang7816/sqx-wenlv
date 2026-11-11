package com.sqx.app.lvyou.invite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 团队汇总
 */
@Data
public class InviteTeamSummaryVo {

    @Schema(description = "邀请好友总人数")
    private Integer inviteCount;

    @Schema(description = "累计收益")
    private String totalEarnings;

    @Schema(description = "一级成员数")
    private Integer level1Count;

    @Schema(description = "二级成员数")
    private Integer level2Count;
}

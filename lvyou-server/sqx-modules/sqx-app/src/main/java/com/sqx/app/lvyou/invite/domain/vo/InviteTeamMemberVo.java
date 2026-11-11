package com.sqx.app.lvyou.invite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 团队成员
 */
@Data
public class InviteTeamMemberVo {

    @Schema(description = "成员用户ID")
    private Long id;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "加入时间")
    private String joinTime;
}

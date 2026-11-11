package com.sqx.app.lvyou.invite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 管理端邀请关系 VO
 */
@Data
@Schema(description = "管理端邀请关系")
public class InviteRelationAdminVo {

    @Schema(description = "关系ID")
    private Long id;

    @Schema(description = "被邀请用户昵称")
    private String memberName;

    @Schema(description = "被邀请用户手机号")
    private String memberPhone;

    @Schema(description = "被邀请用户邀请码")
    private String memberInviteCode;

    @Schema(description = "注册时填写的邀请码")
    private String inviteCode;

    @Schema(description = "一级邀请人昵称")
    private String inviterName;

    @Schema(description = "一级邀请人手机号")
    private String inviterPhone;

    @Schema(description = "一级邀请人邀请码")
    private String inviterInviteCode;

    @Schema(description = "二级邀请人昵称")
    private String inviterLevel2Name;

    @Schema(description = "二级邀请人手机号")
    private String inviterLevel2Phone;

    @Schema(description = "二级邀请人邀请码")
    private String inviterLevel2InviteCode;

    @Schema(description = "绑定时间 yyyy-MM-dd HH:mm:ss")
    private String bindTime;
}

package com.sqx.app.base.auth.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 微信公众号注册/绑定
 */
@Data
public class WxMpRegisterBo {

    @Schema(description = "openid")
    private String openId;

    @Schema(description = "手机号")
    private String phone;

    @Schema(description = "邀请码")
    private String inviteCode;

    @Schema(description = "邀请码(兼容旧字段)")
    private String superiorInvitationCode;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "头像")
    private String avatar;
}

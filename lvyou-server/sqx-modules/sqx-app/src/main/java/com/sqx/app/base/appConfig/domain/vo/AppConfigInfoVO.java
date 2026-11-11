package com.sqx.app.base.appConfig.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * C 端聚合配置（产品名称 + 客服 + 邀请）
 */
@Data
public class AppConfigInfoVO {

    @Schema(description = "产品名称（用户端与管理端展示）")
    private String productName;

    @Schema(description = "默认头像 URL")
    private String defaultAvatar;

    @Schema(description = "客服配置")
    private KefuConfigVO kefu;

    @Schema(description = "邀请展示配置")
    private InviteConfigVO invite;

    @Schema(description = "是否展示 AI 助手入口（与客服形式独立）")
    private Boolean aiAssistantEnabled;

    @Schema(description = "是否启用 H5 微信公众号支付")
    private Boolean wechatMpEnabled;
}

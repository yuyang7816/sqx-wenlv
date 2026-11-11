package com.sqx.app.base.appConfig.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 客服配置（电话/微信/企业微信）
 */
@Data
public class KefuConfigVO {

    @Schema(description = "客服电话")
    private String phone;

    @Schema(description = "客服微信号")
    private String wechat;

    @Schema(description = "客服微信二维码图片 URL")
    private String wechatQrcode;

    @Schema(description = "企业微信客服链接")
    private String weworkUrl;

    @Schema(description = "企业微信 corpId")
    private String weworkCorpId;

    @Schema(description = "客服类型（可选）")
    private String type;
}

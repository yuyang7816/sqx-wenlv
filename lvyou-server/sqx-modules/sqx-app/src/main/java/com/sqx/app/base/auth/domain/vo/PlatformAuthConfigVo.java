package com.sqx.app.base.auth.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 多端登录配置
 */
@Data
@Schema(description = "多端登录配置")
public class PlatformAuthConfigVo {

    @Schema(description = "小程序AppId")
    private String maAppId;

    @Schema(description = "公众号AppId")
    private String mpAppId;

    @Schema(description = "App微信AppId")
    private String appWxAppId;

    @Schema(description = "是否开启公众号登录")
    private Boolean mpLoginEnabled;

    @Schema(description = "是否启用 H5 微信公众号支付")
    private Boolean wechatMpEnabled;
}

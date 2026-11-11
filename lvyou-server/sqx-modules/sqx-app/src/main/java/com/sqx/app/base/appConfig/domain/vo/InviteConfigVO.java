package com.sqx.app.base.appConfig.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 邀请展示配置（海报/文案/返佣说明）
 */
@Data
public class InviteConfigVO {

    @Schema(description = "邀请海报图片 URL")
    private String posterImage;

    @Schema(description = "邀请复制文案（支持 {code} 占位）")
    private String copyText;

    @Schema(description = "一级返佣说明")
    private String rebateLevel1;

    @Schema(description = "二级返佣说明")
    private String rebateLevel2;

    @Schema(description = "邀请分享链接前缀")
    private String shareLink;
}

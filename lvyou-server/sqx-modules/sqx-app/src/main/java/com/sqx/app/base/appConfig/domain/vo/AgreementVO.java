package com.sqx.app.base.appConfig.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 协议正文（HTML 富文本）
 */
@Data
public class AgreementVO {

    @Schema(description = "协议路径 key（user/privacy/about）")
    private String key;

    @Schema(description = "协议 HTML 正文")
    private String content;
}

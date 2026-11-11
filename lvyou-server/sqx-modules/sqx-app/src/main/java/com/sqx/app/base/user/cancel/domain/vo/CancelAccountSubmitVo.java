package com.sqx.app.base.user.cancel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户提交注销申请结果
 */
@Data
@Schema(description = "注销申请提交结果")
public class CancelAccountSubmitVo {

    @Schema(description = "申请ID")
    private Long id;

    @Schema(description = "状态")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;
}

package com.sqx.app.lvyou.feedback.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 提交意见反馈结果
 */
@Data
@Schema(description = "提交意见反馈结果")
public class FeedbackSubmitVo {

    @Schema(description = "反馈ID")
    private Long id;

    @Schema(description = "处理状态")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;
}

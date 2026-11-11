package com.sqx.app.lvyou.feedback.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 管理端标记意见反馈已处理
 */
@Data
@Schema(description = "标记意见反馈已处理")
public class FeedbackProcessBo {

    @Schema(description = "反馈ID")
    @NotNull(message = "id不能为空")
    private Long id;
}

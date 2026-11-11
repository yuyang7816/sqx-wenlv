package com.sqx.app.lvyou.feedback.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 用户端提交意见反馈
 */
@Data
@Schema(description = "提交意见反馈")
public class FeedbackSubmitBo {

    @NotBlank(message = "反馈类型不能为空")
    @Schema(description = "反馈类型：功能异常/界面体验/订单问题/建议反馈/其他")
    private String type;

    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 10, max = 500, message = "反馈内容长度为10-500字")
    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "截图URL列表（上传接口返回，最多3张）")
    private List<String> images;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "联系电话")
    private String phone;
}

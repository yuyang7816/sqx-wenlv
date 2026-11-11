package com.sqx.app.lvyou.feedback.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端意见反馈列表项
 */
@Data
@Schema(description = "意见反馈")
public class FeedbackVo {

    @Schema(description = "反馈ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户昵称")
    private String userName;

    @Schema(description = "用户手机号")
    private String userPhone;

    @Schema(description = "反馈类型")
    private String type;

    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "截图URL列表")
    private List<String> images;

    @Schema(description = "联系电话")
    private String contactPhone;

    @Schema(description = "处理状态")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

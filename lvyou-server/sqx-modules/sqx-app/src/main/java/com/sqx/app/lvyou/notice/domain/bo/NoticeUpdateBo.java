package com.sqx.app.lvyou.notice.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端更新公告
 */
@Data
@Schema(description = "更新公告")
public class NoticeUpdateBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;

    @Schema(description = "公告标题")
    @NotBlank(message = "公告标题不能为空")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "公告正文")
    @NotBlank(message = "公告正文不能为空")
    private String content;

    @Schema(description = "分类：activity/scenic/traffic/policy")
    @NotBlank(message = "公告分类不能为空")
    private String category;

    @Schema(description = "来源机构")
    private String source;

    @Schema(description = "关联目的地ID（服务端按经纬度自动解析，未匹配则为全平台）")
    private Long destinationId;

    @Schema(description = "发布位置纬度")
    private BigDecimal latitude;

    @Schema(description = "发布位置经度")
    private BigDecimal longitude;

    @Schema(description = "是否置顶：1是 0否")
    private Integer pinned;

    @Schema(description = "发布时间")
    @NotNull(message = "发布时间不能为空")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "状态：1已发布 0草稿")
    @NotNull(message = "状态不能为空")
    private Integer state;

    @Schema(description = "排序")
    private Integer sort;
}

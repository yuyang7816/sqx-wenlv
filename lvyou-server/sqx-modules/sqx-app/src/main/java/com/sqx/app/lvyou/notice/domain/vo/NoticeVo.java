package com.sqx.app.lvyou.notice.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端公告
 */
@Data
@Schema(description = "管理端公告")
public class NoticeVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "公告正文")
    private String content;

    @Schema(description = "分类：activity/scenic/traffic/policy")
    private String category;

    @Schema(description = "分类文案")
    private String categoryLabel;

    @Schema(description = "来源机构")
    private String source;

    @Schema(description = "关联目的地ID")
    private Long destinationId;

    @Schema(description = "关联目的地名称")
    private String destinationName;

    @Schema(description = "是否置顶：1是 0否")
    private Integer pinned;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime publishTime;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime expireTime;

    @Schema(description = "状态：1已发布 0草稿")
    private Integer state;

    @Schema(description = "状态文案")
    private String stateLabel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

package com.sqx.app.lvyou.notice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端公告
 */
@Data
@Schema(description = "用户端公告")
public class NoticeAppVo {

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "摘要")
    private String summary;

    @Schema(description = "公告正文（详情接口返回）")
    private String content;

    @Schema(description = "展示时间（yyyy-MM-dd）")
    private String time;

    @Schema(description = "分类：activity/scenic/traffic/policy")
    private String category;

    @Schema(description = "分类文案")
    private String categoryLabel;

    @Schema(description = "来源机构")
    private String source;

    @Schema(description = "是否置顶")
    private Boolean pinned;

    @Schema(description = "是否最新（近7日发布）")
    private Boolean isNew;
}

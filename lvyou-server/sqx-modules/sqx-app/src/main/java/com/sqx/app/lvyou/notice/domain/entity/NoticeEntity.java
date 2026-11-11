package com.sqx.app.lvyou.notice.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 平台公告
 */
@Getter
@Setter
@TableName("tb_notice")
@Schema(name = "NoticeEntity", description = "平台公告")
public class NoticeEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "公告标题")
    @TableField("title")
    private String title;

    @Schema(description = "摘要")
    @TableField("summary")
    private String summary;

    @Schema(description = "公告正文")
    @TableField("content")
    private String content;

    @Schema(description = "分类：activity/scenic/traffic/policy")
    @TableField("category")
    private String category;

    @Schema(description = "来源机构")
    @TableField("source")
    private String source;

    @Schema(description = "关联目的地ID，NULL表示全平台")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "是否置顶：1是 0否")
    @TableField("pinned")
    private Integer pinned;

    @Schema(description = "发布时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("publish_time")
    private LocalDateTime publishTime;

    @Schema(description = "过期时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("expire_time")
    private LocalDateTime expireTime;

    @Schema(description = "状态：1已发布 0草稿")
    @TableField("state")
    private Integer state;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

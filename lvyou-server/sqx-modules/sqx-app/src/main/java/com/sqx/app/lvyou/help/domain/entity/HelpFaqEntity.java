package com.sqx.app.lvyou.help.domain.entity;

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
 * 帮助中心 FAQ
 */
@Getter
@Setter
@TableName("tb_help_faq")
@Schema(name = "HelpFaqEntity", description = "帮助中心FAQ")
public class HelpFaqEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "分类编码")
    @TableField("category_code")
    private String categoryCode;

    @Schema(description = "分类名称")
    @TableField("category_name")
    private String categoryName;

    @Schema(description = "问题标题")
    @TableField("question")
    private String question;

    @Schema(description = "问题答案")
    @TableField("answer")
    private String answer;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "状态：1启用 0禁用")
    @TableField("state")
    private Integer state;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

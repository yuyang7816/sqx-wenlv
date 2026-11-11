package com.sqx.app.lvyou.home.domain.entity;

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
 * 首页宫格 / 推荐 Tab 配置
 */
@Getter
@Setter
@TableName("tb_home_module")
@Schema(name = "HomeModuleEntity", description = "首页模块配置")
public class HomeModuleEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "模块类型：grid=宫格 tab=推荐Tab")
    @TableField("module_type")
    private String moduleType;

    @Schema(description = "业务键")
    @TableField("module_key")
    private String moduleKey;

    @Schema(description = "展示名称")
    @TableField("label")
    private String label;

    @Schema(description = "图标名或图片URL")
    @TableField("icon")
    private String icon;

    @Schema(description = "图标颜色")
    @TableField("icon_color")
    private String iconColor;

    @Schema(description = "背景色")
    @TableField("bg_color")
    private String bgColor;

    @Schema(description = "跳转路径")
    @TableField("link_path")
    private String linkPath;

    @Schema(description = "排序")
    @TableField("sort")
    private Integer sort;

    @Schema(description = "状态：1启用 0禁用")
    @TableField("status")
    private Integer status;

    @Schema(description = "目的地ID，NULL=全平台")
    @TableField("destination_id")
    private Long destinationId;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}

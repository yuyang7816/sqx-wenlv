package com.sqx.app.lvyou.heritage.craft.domain.entity;

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
 * 非遗工艺
 */
@Getter
@Setter
@TableName("tb_heritage_craft")
@Schema(name = "HeritageCraftEntity", description = "非遗工艺")
public class HeritageCraftEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "纬度（地图选点）")
    @TableField("latitude")
    private java.math.BigDecimal latitude;

    @Schema(description = "经度（地图选点）")
    @TableField("longitude")
    private java.math.BigDecimal longitude;

    @Schema(description = "项目地址")
    @TableField("address")
    private String address;

    @Schema(description = "工艺名称")
    @TableField("name")
    private String name;

    @Schema(description = "短描述")
    @TableField("description")
    private String description;

    @Schema(description = "级别编码：national/provincial/municipal")
    @TableField("level_code")
    private String levelCode;

    @Schema(description = "级别展示文案")
    @TableField("level_label")
    private String levelLabel;

    @Schema(description = "工艺分类标签（首标签，兼容 C 端）")
    @TableField("tag")
    private String tag;

    @Schema(description = "分类标签 JSON 数组")
    @TableField("tags")
    private String tags;

    @Schema(description = "好店分类：pottery/textile/culture")
    @TableField("shop_category")
    private String shopCategory;

    @Schema(description = "图标标识")
    @TableField("icon")
    private String icon;

    @Schema(description = "封面渐变起始色")
    @TableField("cover_from")
    private String coverFrom;

    @Schema(description = "封面渐变结束色")
    @TableField("cover_to")
    private String coverTo;

    @Schema(description = "封面图URL")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "轮播图JSON数组")
    @TableField("banner_images")
    private String bannerImages;

    @Schema(description = "概述")
    @TableField("overview")
    private String overview;

    @Schema(description = "工艺特点")
    @TableField("feature")
    private String feature;

    @Schema(description = "文化价值")
    @TableField("culture")
    private String culture;

    @Schema(description = "状态：1上架 0下架")
    @TableField("status")
    private Integer status;

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

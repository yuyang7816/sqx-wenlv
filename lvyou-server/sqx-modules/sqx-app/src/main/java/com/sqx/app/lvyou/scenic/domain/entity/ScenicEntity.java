package com.sqx.app.lvyou.scenic.domain.entity;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 旅游景区
 */
@Getter
@Setter
@TableName("tb_scenic")
@Schema(name = "ScenicEntity", description = "旅游景区")
public class ScenicEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "景区名称")
    @TableField("name")
    private String name;

    @Schema(description = "景区地址")
    @TableField("address")
    private String address;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "开放时间")
    @TableField("open_time")
    private String openTime;

    @Schema(description = "咨询电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "景区等级")
    @TableField("level_label")
    private String levelLabel;

    @Schema(description = "分类编码")
    @TableField("category")
    private String category;

    @Schema(description = "景区特色介绍")
    @TableField("intro")
    private String intro;

    @Schema(description = "特色配图JSON数组（兼容历史单图URL）")
    @TableField("feature_image")
    private String featureImage;

    @Schema(description = "轮播图JSON")
    @TableField("banner_images")
    private String bannerImages;

    @Schema(description = "游玩提示JSON")
    @TableField("tips")
    private String tips;

    @Schema(description = "门票起步价")
    @TableField("price_from")
    private BigDecimal priceFrom;

    @Schema(description = "标签")
    @TableField("tag")
    private String tag;

    @Schema(description = "封面图")
    @TableField("cover_image")
    private String coverImage;

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

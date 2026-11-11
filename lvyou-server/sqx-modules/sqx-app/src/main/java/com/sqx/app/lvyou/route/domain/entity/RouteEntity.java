package com.sqx.app.lvyou.route.domain.entity;

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
 * 旅游线路
 */
@Getter
@Setter
@TableName("tb_route")
@Schema(name = "RouteEntity", description = "旅游线路")
public class RouteEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "关联商户服务ID")
    @TableField("merchant_service_id")
    private Long merchantServiceId;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "线路名称")
    @TableField("name")
    private String name;

    @Schema(description = "短描述")
    @TableField("description")
    private String description;

    @Schema(description = "详细介绍")
    @TableField("intro")
    private String intro;

    @Schema(description = "起步价/人")
    @TableField("price_from")
    private BigDecimal priceFrom;

    @Schema(description = "行程时长")
    @TableField("duration")
    private String duration;

    @Schema(description = "行程天数")
    @TableField("day_count")
    private Integer dayCount;

    @Schema(description = "集合地点")
    @TableField("meet_point")
    private String meetPoint;

    @Schema(description = "旅行城市")
    @TableField("travel_city")
    private String travelCity;

    @Schema(description = "途经景点")
    @TableField("spots")
    private String spots;

    @Schema(description = "标签JSON")
    @TableField("tags")
    private String tags;

    @Schema(description = "封面图")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "轮播图JSON数组")
    @TableField("banner_images")
    private String bannerImages;

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

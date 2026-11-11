package com.sqx.app.lvyou.heritage.shop.domain.entity;

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
 * 非遗店铺
 */
@Getter
@Setter
@TableName("tb_heritage_shop")
@Schema(name = "HeritageShopEntity", description = "非遗店铺")
public class HeritageShopEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "关联非遗工艺ID")
    @TableField("craft_id")
    private Long craftId;

    @Schema(description = "店铺名称")
    @TableField("name")
    private String name;

    @Schema(description = "地址")
    @TableField("address")
    private String address;

    @Schema(description = "纬度（地图选点）")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "经度（地图选点）")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "主标签")
    @TableField("tag")
    private String tag;

    @Schema(description = "标签JSON数组")
    @TableField("tags")
    private String tags;

    @Schema(description = "分类：pottery/textile/culture")
    @TableField("category")
    private String category;

    @Schema(description = "简介")
    @TableField("intro")
    private String intro;

    @Schema(description = "详细介绍")
    @TableField("intro_detail")
    private String introDetail;

    @Schema(description = "列表短描述")
    @TableField("description")
    private String description;

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

    @Schema(description = "运费配置JSON")
    @TableField("shipping_config")
    private String shippingConfig;

    @Schema(description = "营业时间")
    @TableField("business_hours")
    private String businessHours;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "评分")
    @TableField("rating")
    private BigDecimal rating;

    @Schema(description = "评价数")
    @TableField("review_count")
    private Integer reviewCount;

    @Schema(description = "服务项目JSON数组")
    @TableField("services")
    private String services;

    @Schema(description = "状态：1营业 0歇业")
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

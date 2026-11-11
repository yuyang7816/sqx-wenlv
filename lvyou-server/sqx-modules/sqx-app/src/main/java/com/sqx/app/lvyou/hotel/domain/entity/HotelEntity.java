package com.sqx.app.lvyou.hotel.domain.entity;

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
 * 酒店
 */
@Getter
@Setter
@TableName("tb_hotel")
@Schema(name = "HotelEntity", description = "酒店")
public class HotelEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "目的地ID")
    @TableField("destination_id")
    private Long destinationId;

    @Schema(description = "酒店名称")
    @TableField("name")
    private String name;

    @Schema(description = "分类")
    @TableField("category")
    private String category;

    @Schema(description = "星级")
    @TableField("star_level")
    private Integer starLevel;

    @Schema(description = "星级文案")
    @TableField("star_label")
    private String starLabel;

    @Schema(description = "地址")
    @TableField("address")
    private String address;

    @Schema(description = "纬度")
    @TableField("latitude")
    private BigDecimal latitude;

    @Schema(description = "经度")
    @TableField("longitude")
    private BigDecimal longitude;

    @Schema(description = "联系电话")
    @TableField("phone")
    private String phone;

    @Schema(description = "评分")
    @TableField("score")
    private BigDecimal score;

    @Schema(description = "标签")
    @TableField("tag")
    private String tag;

    @Schema(description = "起步价")
    @TableField("price_from")
    private BigDecimal priceFrom;

    @Schema(description = "封面图")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "轮播图JSON")
    @TableField("banner_images")
    private String bannerImages;

    @Schema(description = "入住时间说明")
    @TableField("check_in_time")
    private String checkInTime;

    @Schema(description = "退房时间说明")
    @TableField("check_out_time")
    private String checkOutTime;

    @Schema(description = "设施JSON")
    @TableField("facilities")
    private String facilities;

    @Schema(description = "简介")
    @TableField("intro")
    private String intro;

    @Schema(description = "酒店政策JSON")
    @TableField("policy_json")
    private String policyJson;

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

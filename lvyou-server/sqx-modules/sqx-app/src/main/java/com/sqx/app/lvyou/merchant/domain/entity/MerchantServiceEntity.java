package com.sqx.app.lvyou.merchant.domain.entity;

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
 * 商户服务
 */
@Getter
@Setter
@TableName("tb_merchant_service")
@Schema(name = "MerchantServiceEntity", description = "商户服务")
public class MerchantServiceEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "商户ID")
    @TableField("merchant_id")
    private Long merchantId;

    @Schema(description = "服务业务编码")
    @TableField("service_code")
    private String serviceCode;

    @Schema(description = "服务名称")
    @TableField("name")
    private String name;

    @Schema(description = "服务价格")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "库存/名额")
    @TableField("stock")
    private Integer stock;

    @Schema(description = "服务说明")
    @TableField("description")
    private String description;

    @Schema(description = "规格描述（酒店房型规格等）")
    @TableField("spec")
    private String spec;

    @Schema(description = "集合地点（旅游线路服务）")
    @TableField("meet_point")
    private String meetPoint;

    @Schema(description = "途经景点（逗号分隔，旅游线路服务）")
    @TableField("spots")
    private String spots;

    @Schema(description = "旅行城市")
    @TableField("travel_city")
    private String travelCity;

    @Schema(description = "行程天数")
    @TableField("day_count")
    private Integer dayCount;

    @Schema(description = "每日行程 JSON")
    @TableField("itinerary_json")
    private String itineraryJson;

    @Schema(description = "商品规格（逗号分隔，文创）")
    @TableField("goods_specs")
    private String goodsSpecs;

    @Schema(description = "详情介绍（文创商品等）")
    @TableField("detail")
    private String detail;

    @Schema(description = "酒店房型扩展 JSON")
    @TableField("room_detail_json")
    private String roomDetailJson;

    @Schema(description = "关联客房标准ID")
    @TableField("room_standard_id")
    private Long roomStandardId;

    @Schema(description = "封面图URL")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "轮播图JSON数组")
    @TableField("banner_images")
    private String bannerImages;

    @Schema(description = "商品详情图JSON数组（文创）")
    @TableField("detail_images")
    private String detailImages;

    @Schema(description = "状态：on_sale/off_sale/draft")
    @TableField("status")
    private String status;

    @Schema(description = "首页推荐Tab业务键")
    @TableField("home_tab_key")
    private String homeTabKey;

    @Schema(description = "月售数量（餐饮菜品）")
    @TableField("monthly_sales")
    private Integer monthlySales;

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

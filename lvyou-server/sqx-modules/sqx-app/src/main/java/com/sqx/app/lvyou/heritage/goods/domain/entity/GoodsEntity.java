package com.sqx.app.lvyou.heritage.goods.domain.entity;

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
 * 非遗商品
 */
@Getter
@Setter
@TableName("tb_heritage_goods")
@Schema(name = "GoodsEntity", description = "非遗商品")
public class GoodsEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "关联非遗店铺ID")
    @TableField("shop_id")
    private Long shopId;

    @Schema(description = "关联商户服务ID")
    @TableField("merchant_service_id")
    private Long merchantServiceId;

    @Schema(description = "商品名称")
    @TableField("name")
    private String name;

    @Schema(description = "列表短描述")
    @TableField("description")
    private String description;

    @Schema(description = "封面图URL")
    @TableField("cover_image")
    private String coverImage;

    @Schema(description = "售价（元）")
    @TableField("price")
    private BigDecimal price;

    @Schema(description = "销量")
    @TableField("sales")
    private Integer sales;

    @Schema(description = "库存")
    @TableField("stock")
    private Integer stock;

    @Schema(description = "评分")
    @TableField("rating")
    private BigDecimal rating;

    @Schema(description = "分类：featured/hot/new")
    @TableField("category")
    private String category;

    @Schema(description = "规格JSON数组")
    @TableField("specs")
    private String specs;

    @Schema(description = "详情介绍")
    @TableField("detail")
    private String detail;

    @Schema(description = "轮播图JSON数组")
    @TableField("banners")
    private String banners;

    @Schema(description = "商品详情图JSON数组")
    @TableField("detail_images")
    private String detailImages;

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

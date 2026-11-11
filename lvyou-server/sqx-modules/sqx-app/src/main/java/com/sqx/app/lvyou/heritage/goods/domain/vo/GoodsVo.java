package com.sqx.app.lvyou.heritage.goods.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端非遗商品
 */
@Data
@Schema(description = "管理端非遗商品")
public class GoodsVo {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "关联商户服务ID（有值表示商户发布）")
    private Long merchantServiceId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "列表短描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "售价（元）")
    private BigDecimal price;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "规格列表")
    private List<String> specs;

    @Schema(description = "详情介绍")
    private String detail;

    @Schema(description = "轮播图列表")
    private List<GoodsAppVo.BannerVo> banners;

    @Schema(description = "商品详情图")
    private List<String> detailImages;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
}

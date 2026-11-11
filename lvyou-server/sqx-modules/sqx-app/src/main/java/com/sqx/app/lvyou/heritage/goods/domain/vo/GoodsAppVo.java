package com.sqx.app.lvyou.heritage.goods.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopShippingVo;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端非遗商品
 */
@Data
@Schema(description = "用户端非遗商品")
public class GoodsAppVo {

    @Schema(description = "商品ID")
    private Long id;

    @Schema(description = "店铺ID")
    private Long shopId;

    @Schema(description = "店铺名称")
    private String shopName;

    @Schema(description = "店铺封面图")
    private String shopCover;

    @Schema(description = "店铺轮播图")
    private List<String> shopBannerImages;

    @Schema(description = "店铺地址")
    private String shopAddress;

    @Schema(description = "店铺联系电话")
    private String shopPhone;

    @Schema(description = "店铺运费配置")
    private HeritageShopShippingVo shopShipping;

    @Schema(description = "商品名称")
    private String name;

    @Schema(description = "短描述")
    private String desc;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "售价（元）")
    private BigDecimal price;

    @Schema(description = "销量")
    private Integer sales;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "评分")
    private String rating;

    @Schema(description = "分类")
    private String category;

    @Schema(description = "规格模式：single/multi")
    private String specMode;

    @Schema(description = "规格 SKU 列表")
    private List<GoodsSpecSkuVo> specs;

    @Schema(description = "是否展示「起」价")
    private Boolean priceFrom;

    @Schema(description = "详情介绍")
    private String detail;

    @Schema(description = "轮播图")
    private List<BannerVo> banners;

    @Schema(description = "商品详情图")
    private List<String> detailImages;

    @Data
    @Schema(description = "轮播图项")
    public static class BannerVo {
        @Schema(description = "图片URL")
        private String image;
    }
}

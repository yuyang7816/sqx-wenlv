package com.sqx.app.lvyou.heritage.goods.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.List;

/**
 * 管理端新增非遗商品
 */
@Data
@Schema(description = "新增非遗商品")
public class GoodsSaveBo {

    @Schema(description = "关联非遗店铺ID")
    @NotNull(message = "店铺不能为空")
    private Long shopId;

    @Schema(description = "商品名称")
    @NotBlank(message = "商品名称不能为空")
    private String name;

    @Schema(description = "列表短描述")
    private String description;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "售价（元）")
    @NotNull(message = "售价不能为空")
    private BigDecimal price;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "分类：featured/hot/new")
    private String category;

    @Schema(description = "规格列表")
    private List<String> specs;

    @Schema(description = "详情介绍")
    private String detail;

    @Schema(description = "轮播图列表")
    private List<BannerItem> banners;

    @Schema(description = "状态：1上架 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;

    @Data
    @Schema(description = "轮播图项")
    public static class BannerItem {
        @Schema(description = "图片URL")
        private String image;
    }
}

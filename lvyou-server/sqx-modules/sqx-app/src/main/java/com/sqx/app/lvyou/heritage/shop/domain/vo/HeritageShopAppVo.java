package com.sqx.app.lvyou.heritage.shop.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端非遗店铺
 */
@Data
@Schema(description = "用户端非遗店铺")
public class HeritageShopAppVo {

    @Schema(description = "店铺ID")
    private Long id;

    @Schema(description = "店铺名称")
    private String name;

    @Schema(description = "地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "主标签")
    private String tag;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "分类：pottery/textile/culture")
    private String category;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "详细介绍")
    private String introDetail;

    @Schema(description = "列表短描述")
    private String desc;

    @Schema(description = "封面渐变起始色")
    private String coverFrom;

    @Schema(description = "封面渐变结束色")
    private String coverTo;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "轮播图URL列表")
    private List<String> bannerImages;

    @Schema(description = "营业时间")
    private String hours;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "评分")
    private BigDecimal rating;

    @Schema(description = "评价数")
    private Integer reviewCount;

    @Schema(description = "服务项目列表")
    private List<String> services;

    @Schema(description = "关联工艺ID")
    private Long craftId;

    @Schema(description = "关联工艺名称")
    private String craftName;
}

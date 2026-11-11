package com.sqx.app.lvyou.heritage.craft.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 管理端新增非遗工艺
 */
@Data
@Schema(description = "新增非遗工艺")
public class HeritageCraftSaveBo {

    @Schema(description = "目的地ID（服务端按经纬度自动解析）")
    private Long destinationId;

    @Schema(description = "发布位置纬度")
    private java.math.BigDecimal latitude;

    @Schema(description = "发布位置经度")
    private java.math.BigDecimal longitude;

    @Schema(description = "项目地址")
    private String address;

    @Schema(description = "工艺名称")
    @NotBlank(message = "工艺名称不能为空")
    private String name;

    @Schema(description = "短描述")
    private String description;

    @Schema(description = "级别编码：world/national/provincial/municipal/county")
    private String levelCode;

    @Schema(description = "级别展示文案")
    private String levelLabel;

    @Schema(description = "工艺分类标签（首标签，服务端自动同步）")
    private String tag;

    @Schema(description = "分类标签列表")
    private List<String> tags;

    @Schema(description = "好店分类：pottery/textile/culture")
    private String shopCategory;

    @Schema(description = "图标标识")
    private String icon;

    @Schema(description = "封面渐变起始色")
    private String coverFrom;

    @Schema(description = "封面渐变结束色")
    private String coverTo;

    @Schema(description = "封面图URL")
    private String coverImage;

    @Schema(description = "轮播图URL列表")
    private List<String> bannerImages;

    @Schema(description = "概述")
    private String overview;

    @Schema(description = "工艺特点")
    private String feature;

    @Schema(description = "文化价值")
    private String culture;

    @Schema(description = "状态：1上架 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;
}

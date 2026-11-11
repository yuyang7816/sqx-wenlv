package com.sqx.app.lvyou.heritage.craft.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端非遗工艺
 */
@Data
@Schema(description = "管理端非遗工艺")
public class HeritageCraftVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "纬度（地图选点）")
    private java.math.BigDecimal latitude;

    @Schema(description = "经度（地图选点）")
    private java.math.BigDecimal longitude;

    @Schema(description = "项目地址")
    private String address;

    @Schema(description = "工艺名称")
    private String name;

    @Schema(description = "短描述")
    private String description;

    @Schema(description = "级别编码")
    private String levelCode;

    @Schema(description = "级别展示文案")
    private String levelLabel;

    @Schema(description = "工艺分类标签（首标签）")
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
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

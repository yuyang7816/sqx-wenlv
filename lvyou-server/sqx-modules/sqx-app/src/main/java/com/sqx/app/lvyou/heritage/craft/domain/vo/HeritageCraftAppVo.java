package com.sqx.app.lvyou.heritage.craft.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端非遗工艺
 */
@Data
@Schema(description = "用户端非遗工艺")
public class HeritageCraftAppVo {

    @Schema(description = "工艺ID")
    private Long id;

    @Schema(description = "工艺名称")
    private String name;

    @Schema(description = "短描述")
    private String desc;

    @Schema(description = "级别短文案（如国家级）")
    private String level;

    @Schema(description = "级别完整文案")
    private String levelLabel;

    @Schema(description = "工艺分类标签（首标签）")
    private String tag;

    @Schema(description = "分类标签列表")
    private List<String> tags;

    @Schema(description = "图标标识")
    private String icon;

    @Schema(description = "封面渐变起始色")
    private String coverFrom;

    @Schema(description = "封面渐变结束色")
    private String coverTo;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "轮播图URL列表")
    private List<String> bannerImages;

    @Schema(description = "概述")
    private String overview;

    @Schema(description = "工艺特点")
    private String feature;

    @Schema(description = "文化价值")
    private String culture;
}

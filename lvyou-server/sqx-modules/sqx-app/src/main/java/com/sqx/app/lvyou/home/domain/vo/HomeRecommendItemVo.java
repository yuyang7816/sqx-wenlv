package com.sqx.app.lvyou.home.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 首页推荐条目
 */
@Data
@Schema(name = "HomeRecommendItemVo", description = "首页推荐条目")
public class HomeRecommendItemVo {

    @Schema(description = "资源ID")
    private Long id;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "起步价")
    private BigDecimal price;

    @Schema(description = "标签")
    private String tag;

    @Schema(description = "地址/位置（酒店、景区等展示用）")
    private String address;

    @Schema(description = "条目类型：hotel/route/activity/scenic/merchant")
    private String itemType;

    @Schema(description = "跳转路径，优先于 itemType 默认详情页")
    private String linkPath;
}

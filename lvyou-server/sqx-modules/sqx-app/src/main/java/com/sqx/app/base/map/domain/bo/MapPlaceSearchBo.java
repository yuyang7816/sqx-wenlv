package com.sqx.app.base.map.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 腾讯地图地点搜索入参
 */
@Data
@Schema(description = "地图地点搜索入参")
public class MapPlaceSearchBo {

    @Schema(description = "搜索关键字")
    private String keyword;

    @Schema(description = "中心点纬度")
    private String lat;

    @Schema(description = "中心点经度")
    private String lng;

    @Schema(description = "搜索半径（米），默认 1000，最大 1000")
    private Integer radius;

    @Schema(description = "分类筛选，如 category=汽车:停车场")
    private String filter;

    @Schema(description = "无结果时是否自动扩大范围：0 不扩大，1 扩大；默认 0")
    private Integer autoExtend;

    @Schema(description = "检索模式：search 关键词搜索；explore 周边分类探索")
    private String mode;

    @Schema(description = "页码，从 1 开始")
    private Integer page;

    @Schema(description = "每页条数，最大 20")
    private Integer limit;
}

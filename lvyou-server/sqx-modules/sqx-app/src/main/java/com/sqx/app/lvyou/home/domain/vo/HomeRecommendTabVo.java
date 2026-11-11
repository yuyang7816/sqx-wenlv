package com.sqx.app.lvyou.home.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页推荐 Tab
 */
@Data
@Schema(name = "HomeRecommendTabVo", description = "首页推荐Tab")
public class HomeRecommendTabVo {

    @Schema(description = "Tab键：recommend/hotel/route/activity/scenic")
    private String key;

    @Schema(description = "Tab标题")
    private String title;

    @Schema(description = "Tab 列表页跳转路径")
    private String listPath;

    @Schema(description = "推荐条目列表")
    private List<HomeRecommendItemVo> items = new ArrayList<>();
}

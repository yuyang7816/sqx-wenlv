package com.sqx.app.lvyou.home.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 首页推荐聚合结果
 */
@Data
@Schema(name = "HomeRecommendVo", description = "首页推荐聚合结果")
public class HomeRecommendVo {

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "推荐Tab列表")
    private List<HomeRecommendTabVo> tabs = new ArrayList<>();
}

package com.sqx.app.lvyou.search.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 综合搜索响应
 */
@Data
@Schema(name = "SearchResultVo", description = "综合搜索响应")
public class SearchResultVo {

    @Schema(description = "搜索关键词编码")
    private String keywordCode;

    @Schema(description = "搜索关键词展示文案")
    private String keyword;

    @Schema(description = "目的地ID（可选筛选）")
    private Long destinationId;

    @Schema(description = "按类型分组的结果")
    private List<SearchGroupVo> groups = new ArrayList<>();
}

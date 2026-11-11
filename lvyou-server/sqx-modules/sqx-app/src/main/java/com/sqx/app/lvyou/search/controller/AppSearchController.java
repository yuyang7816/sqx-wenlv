package com.sqx.app.lvyou.search.controller;

import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.search.domain.vo.SearchResultVo;
import com.sqx.app.lvyou.search.service.SearchService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端综合搜索
 */
@Tag(name = "用户端-综合搜索", description = "跨模块关键词搜索")
@RestController
@RequestMapping("/app/search")
public class AppSearchController {

    @Resource
    private SearchService searchService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "综合搜索（酒店/线路/景区/活动/非遗商品/非遗店铺）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = SearchResultVo.class)))
    @GetMapping
    public Result<SearchResultVo> search(@RequestParam(value = "keywordCode", required = false) String keywordCode,
                                         @RequestParam(value = "keyword", required = false) String keyword,
                                         @RequestParam(value = "lat", required = false) String lat,
                                         @RequestParam(value = "lng", required = false) String lng) {
        String resolvedKeywordCode = StringUtils.isNotBlank(keywordCode) ? keywordCode : keyword;
        Long resolvedDestinationId = destinationSupport.resolveAppDestinationId(lat, lng);
        return Result.success(searchService.search(resolvedKeywordCode, resolvedDestinationId));
    }
}

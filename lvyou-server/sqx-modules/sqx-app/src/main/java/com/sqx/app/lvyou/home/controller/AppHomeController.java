package com.sqx.app.lvyou.home.controller;

import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleAppVo;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendVo;
import com.sqx.app.lvyou.home.service.HomeModuleService;
import com.sqx.app.lvyou.home.service.HomeRecommendService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端首页
 */
@Tag(name = "用户端-首页", description = "用户端-首页推荐")
@RestController
@RequestMapping("/app/home")
public class AppHomeController {

    @Resource
    private HomeRecommendService homeRecommendService;

    @Resource
    private HomeModuleService homeModuleService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "首页宫格/Tab 模块配置")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HomeModuleAppVo.class)))
    @GetMapping("modules")
    public Result<java.util.List<HomeModuleAppVo>> modules(
            @RequestParam("type") String type,
            @RequestParam(value = "lat", required = false) String lat,
            @RequestParam(value = "lng", required = false) String lng,
            @RequestParam(value = "destinationId", required = false) String destinationId) {
        Long resolvedDestinationId = destinationSupport.resolveBrowseDestinationId(destinationId, lat, lng);
        return Result.success(homeModuleService.listAppModules(type, resolvedDestinationId));
    }

    @Operation(summary = "首页推荐Tab（酒店/线路/活动/景区聚合）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = HomeRecommendVo.class)))
    @GetMapping("recommend")
    public Result<HomeRecommendVo> recommend(@RequestParam(value = "lat", required = false) String lat,
                                             @RequestParam(value = "lng", required = false) String lng,
                                             @RequestParam(value = "destinationId", required = false) String destinationId) {
        Long resolvedDestinationId = destinationSupport.resolveBrowseDestinationId(destinationId, lat, lng);
        return Result.success(homeRecommendService.recommend(resolvedDestinationId));
    }
}

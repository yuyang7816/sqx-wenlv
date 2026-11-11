package com.sqx.app.lvyou.stats.controller;

import com.sqx.app.lvyou.stats.domain.bo.StatsTrendBo;
import com.sqx.app.lvyou.stats.domain.vo.MenuBadgeVo;
import com.sqx.app.lvyou.stats.domain.vo.StatsOverviewVo;
import com.sqx.app.lvyou.stats.domain.vo.StatsTrendVo;
import com.sqx.app.lvyou.stats.service.StatsService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端业务统计
 */
@Tag(name = "管理端-业务统计", description = "管理端-业务统计")
@RestController
@RequestMapping("/admin/stats")
public class AdminStatsController {

    @Resource
    private StatsService statsService;

    @Operation(summary = "业务概览")
    @RequiresPermissions("admin:stats:view")
    @ApiResponse(content = @Content(schema = @Schema(implementation = StatsOverviewVo.class)))
    @PostMapping("overview")
    public Result<StatsOverviewVo> overview() {
        return Result.success(statsService.overview());
    }

    @Operation(summary = "业务趋势")
    @RequiresPermissions("admin:stats:view")
    @ApiResponse(content = @Content(schema = @Schema(implementation = StatsTrendVo.class)))
    @PostMapping("trend")
    public Result<StatsTrendVo> trend(@RequestBody(required = false) @Validated StatsTrendBo trendBo) {
        return Result.success(statsService.trend(trendBo));
    }

    @Operation(summary = "侧边栏菜单角标")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MenuBadgeVo.class)))
    @PostMapping("menu-badges")
    public Result<MenuBadgeVo> menuBadges() {
        return Result.success(statsService.menuBadges());
    }
}

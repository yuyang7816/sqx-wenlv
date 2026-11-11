package com.sqx.app.lvyou.route.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.route.domain.bo.RouteInfoBo;
import com.sqx.app.lvyou.route.domain.bo.RoutePageBo;
import com.sqx.app.lvyou.route.domain.bo.RouteSaveBo;
import com.sqx.app.lvyou.route.domain.bo.RouteUpdateBo;
import com.sqx.app.lvyou.route.domain.bo.RouteUpdateStateBo;
import com.sqx.app.lvyou.route.domain.vo.RouteVo;
import com.sqx.app.lvyou.route.service.RouteService;
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
 * 管理端旅游线路
 */
@Tag(name = "管理端-旅游线路", description = "管理端-旅游线路")
@RestController
@RequestMapping("/admin/route")
public class AdminRouteController {

    @Resource
    private RouteService routeService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:route:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = RouteVo.class)))
    @PostMapping("page")
    public Result<IPage<RouteVo>> page(@RequestBody @Validated RoutePageBo pageBo) {
        return Result.success(routeService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:route:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated RouteSaveBo saveBo) {
        routeService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:route:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated RouteUpdateBo updateBo) {
        routeService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:route:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated RouteInfoBo infoBo) {
        routeService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新状态")
    @RequiresPermissions("lvyou:route:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated RouteUpdateStateBo updateStateBo) {
        routeService.updateState(updateStateBo);
        return Result.success();
    }
}

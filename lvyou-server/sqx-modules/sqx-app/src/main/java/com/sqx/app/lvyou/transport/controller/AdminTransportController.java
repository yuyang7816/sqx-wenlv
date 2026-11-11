package com.sqx.app.lvyou.transport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.transport.domain.bo.TransportRouteInfoBo;
import com.sqx.app.lvyou.transport.domain.bo.TransportRoutePageBo;
import com.sqx.app.lvyou.transport.domain.bo.TransportRouteSaveBo;
import com.sqx.app.lvyou.transport.domain.bo.TransportRouteUpdateBo;
import com.sqx.app.lvyou.transport.domain.bo.TransportRouteUpdateStateBo;
import com.sqx.app.lvyou.transport.domain.vo.TransportRouteVo;
import com.sqx.app.lvyou.transport.service.TransportService;
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
 * 管理端公共交通
 */
@Tag(name = "管理端-公共交通", description = "管理端-公共交通")
@RestController
@RequestMapping("/admin/transport/route")
public class AdminTransportController {

    @Resource
    private TransportService transportService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:transport:route:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = TransportRouteVo.class)))
    @PostMapping("page")
    public Result<IPage<TransportRouteVo>> page(@RequestBody @Validated TransportRoutePageBo pageBo) {
        return Result.success(transportService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:transport:route:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated TransportRouteSaveBo saveBo) {
        transportService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:transport:route:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated TransportRouteUpdateBo updateBo) {
        transportService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:transport:route:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated TransportRouteInfoBo infoBo) {
        transportService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新状态")
    @RequiresPermissions("lvyou:transport:route:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated TransportRouteUpdateStateBo updateStateBo) {
        transportService.updateState(updateStateBo);
        return Result.success();
    }
}

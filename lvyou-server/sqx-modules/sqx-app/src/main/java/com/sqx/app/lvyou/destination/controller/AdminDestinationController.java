package com.sqx.app.lvyou.destination.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.destination.domain.bo.DestinationInfoBo;
import com.sqx.app.lvyou.destination.domain.bo.DestinationPageBo;
import com.sqx.app.lvyou.destination.domain.bo.DestinationSaveBo;
import com.sqx.app.lvyou.destination.domain.bo.DestinationUpdateBo;
import com.sqx.app.lvyou.destination.domain.bo.DestinationUpdateStateBo;
import com.sqx.app.lvyou.destination.domain.vo.DestinationVo;
import com.sqx.app.lvyou.destination.service.DestinationService;
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
 * 管理端目的地
 */
@Tag(name = "管理端-目的地", description = "管理端-目的地")
@RestController
@RequestMapping("/admin/destination")
public class AdminDestinationController {

    @Resource
    private DestinationService destinationService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:destination:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = DestinationVo.class)))
    @PostMapping("page")
    public Result<IPage<DestinationVo>> page(@RequestBody @Validated DestinationPageBo pageBo) {
        return Result.success(destinationService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:destination:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated DestinationSaveBo saveBo) {
        destinationService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:destination:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated DestinationUpdateBo updateBo) {
        destinationService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:destination:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated DestinationInfoBo infoBo) {
        destinationService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新状态")
    @RequiresPermissions("lvyou:destination:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated DestinationUpdateStateBo updateStateBo) {
        destinationService.updateState(updateStateBo);
        return Result.success();
    }
}

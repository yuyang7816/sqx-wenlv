package com.sqx.app.lvyou.home.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleInfoBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModulePageBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleSaveBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleUpdateBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleUpdateStateBo;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleVo;
import com.sqx.app.lvyou.home.service.HomeModuleService;
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
 * 管理端首页模块配置
 */
@Tag(name = "管理端-首页配置", description = "管理端-首页宫格与推荐Tab")
@RestController
@RequestMapping("/admin/homeModule")
public class AdminHomeModuleController {

    @Resource
    private HomeModuleService homeModuleService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:homeModule:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HomeModuleVo.class)))
    @PostMapping("page")
    public Result<IPage<HomeModuleVo>> page(@RequestBody @Validated HomeModulePageBo pageBo) {
        return Result.success(homeModuleService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:homeModule:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated HomeModuleSaveBo saveBo) {
        homeModuleService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:homeModule:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated HomeModuleUpdateBo updateBo) {
        homeModuleService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "启用/禁用")
    @RequiresPermissions("lvyou:homeModule:update")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated HomeModuleUpdateStateBo updateStateBo) {
        homeModuleService.updateState(updateStateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:homeModule:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated HomeModuleInfoBo infoBo) {
        homeModuleService.delete(infoBo);
        return Result.success();
    }
}

package com.sqx.app.lvyou.heritage.craft.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.heritage.craft.domain.bo.HeritageCraftInfoBo;
import com.sqx.app.lvyou.heritage.craft.domain.bo.HeritageCraftPageBo;
import com.sqx.app.lvyou.heritage.craft.domain.bo.HeritageCraftSaveBo;
import com.sqx.app.lvyou.heritage.craft.domain.bo.HeritageCraftUpdateBo;
import com.sqx.app.lvyou.heritage.craft.domain.bo.HeritageCraftUpdateStateBo;
import com.sqx.app.lvyou.heritage.craft.domain.vo.HeritageCraftVo;
import com.sqx.app.lvyou.heritage.craft.service.HeritageCraftService;
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
 * 管理端非遗工艺
 */
@Tag(name = "管理端-非遗工艺", description = "管理端-非遗工艺")
@RestController
@RequestMapping("/admin/heritage/craft")
public class AdminHeritageCraftController {

    @Resource
    private HeritageCraftService heritageCraftService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:heritage:craft:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageCraftVo.class)))
    @PostMapping("page")
    public Result<IPage<HeritageCraftVo>> page(@RequestBody @Validated HeritageCraftPageBo pageBo) {
        return Result.success(heritageCraftService.pageInfo(pageBo));
    }

    @Operation(summary = "详情")
    @RequiresPermissions("lvyou:heritage:craft:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageCraftVo.class)))
    @PostMapping("info")
    public Result<HeritageCraftVo> info(@RequestBody @Validated HeritageCraftInfoBo infoBo) {
        return Result.success(heritageCraftService.info(infoBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:heritage:craft:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated HeritageCraftSaveBo saveBo) {
        heritageCraftService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:heritage:craft:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated HeritageCraftUpdateBo updateBo) {
        heritageCraftService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:heritage:craft:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated HeritageCraftInfoBo infoBo) {
        heritageCraftService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新状态")
    @RequiresPermissions("lvyou:heritage:craft:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated HeritageCraftUpdateStateBo updateStateBo) {
        heritageCraftService.updateState(updateStateBo);
        return Result.success();
    }
}

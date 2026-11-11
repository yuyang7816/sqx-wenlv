package com.sqx.app.lvyou.heritage.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopInfoBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopPageBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopSaveBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopUpdateBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopUpdateStateBo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopVo;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopService;
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
 * 管理端非遗店铺
 */
@Tag(name = "管理端-非遗店铺", description = "管理端-非遗店铺")
@RestController
@RequestMapping("/admin/heritage/shop")
public class AdminHeritageShopController {

    @Resource
    private HeritageShopService heritageShopService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:heritage:shop:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageShopVo.class)))
    @PostMapping("page")
    public Result<IPage<HeritageShopVo>> page(@RequestBody @Validated HeritageShopPageBo pageBo) {
        return Result.success(heritageShopService.pageInfo(pageBo));
    }

    @Operation(summary = "详情")
    @RequiresPermissions("lvyou:heritage:shop:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageShopVo.class)))
    @PostMapping("info")
    public Result<HeritageShopVo> info(@RequestBody @Validated HeritageShopInfoBo infoBo) {
        return Result.success(heritageShopService.info(infoBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:heritage:shop:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated HeritageShopSaveBo saveBo) {
        heritageShopService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:heritage:shop:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated HeritageShopUpdateBo updateBo) {
        heritageShopService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:heritage:shop:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated HeritageShopInfoBo infoBo) {
        heritageShopService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新状态")
    @RequiresPermissions("lvyou:heritage:shop:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated HeritageShopUpdateStateBo updateStateBo) {
        heritageShopService.updateState(updateStateBo);
        return Result.success();
    }
}

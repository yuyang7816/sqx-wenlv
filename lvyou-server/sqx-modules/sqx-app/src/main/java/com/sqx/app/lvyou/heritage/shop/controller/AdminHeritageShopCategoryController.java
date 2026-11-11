package com.sqx.app.lvyou.heritage.shop.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryInfoBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryPageBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategorySaveBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryUpdateBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryUpdateStateBo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopCategoryOptionVo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopCategoryVo;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopCategoryService;
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
import java.util.List;

/**
 * 管理端非遗好店分类
 */
@Tag(name = "管理端-非遗好店分类", description = "管理端-非遗好店分类")
@RestController
@RequestMapping("/admin/heritage/shop/category")
public class AdminHeritageShopCategoryController {

    @Resource
    private HeritageShopCategoryService heritageShopCategoryService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:heritage:shop:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageShopCategoryVo.class)))
    @PostMapping("page")
    public Result<IPage<HeritageShopCategoryVo>> page(@RequestBody @Validated HeritageShopCategoryPageBo pageBo) {
        return Result.success(heritageShopCategoryService.pageInfo(pageBo));
    }

    @Operation(summary = "启用中的分类选项")
    @RequiresPermissions("lvyou:heritage:shop:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HeritageShopCategoryOptionVo.class)))
    @PostMapping("options")
    public Result<List<HeritageShopCategoryOptionVo>> options() {
        return Result.success(heritageShopCategoryService.listEnabledOptions());
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:heritage:shop:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated HeritageShopCategorySaveBo saveBo) {
        heritageShopCategoryService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:heritage:shop:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated HeritageShopCategoryUpdateBo updateBo) {
        heritageShopCategoryService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "启用/禁用")
    @RequiresPermissions("lvyou:heritage:shop:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated HeritageShopCategoryUpdateStateBo updateStateBo) {
        heritageShopCategoryService.updateState(updateStateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:heritage:shop:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated HeritageShopCategoryInfoBo infoBo) {
        heritageShopCategoryService.delete(infoBo);
        return Result.success();
    }
}

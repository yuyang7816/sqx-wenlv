package com.sqx.app.lvyou.scenic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryInfoBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryPageBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategorySaveBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryUpdateBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryUpdateStateBo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicCategoryOptionVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicCategoryVo;
import com.sqx.app.lvyou.scenic.service.ScenicCategoryService;
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
 * 管理端景区分类
 */
@Tag(name = "管理端-景区分类", description = "管理端-景区分类")
@RestController
@RequestMapping("/admin/scenic/category")
public class AdminScenicCategoryController {

    @Resource
    private ScenicCategoryService scenicCategoryService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:scenic:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ScenicCategoryVo.class)))
    @PostMapping("page")
    public Result<IPage<ScenicCategoryVo>> page(@RequestBody @Validated ScenicCategoryPageBo pageBo) {
        return Result.success(scenicCategoryService.pageInfo(pageBo));
    }

    @Operation(summary = "启用中的分类选项")
    @RequiresPermissions("lvyou:scenic:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ScenicCategoryOptionVo.class)))
    @PostMapping("options")
    public Result<List<ScenicCategoryOptionVo>> options() {
        return Result.success(scenicCategoryService.listEnabledOptions());
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:scenic:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated ScenicCategorySaveBo saveBo) {
        scenicCategoryService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:scenic:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated ScenicCategoryUpdateBo updateBo) {
        scenicCategoryService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "启用/禁用")
    @RequiresPermissions("lvyou:scenic:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated ScenicCategoryUpdateStateBo updateStateBo) {
        scenicCategoryService.updateState(updateStateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:scenic:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated ScenicCategoryInfoBo infoBo) {
        scenicCategoryService.delete(infoBo);
        return Result.success();
    }
}

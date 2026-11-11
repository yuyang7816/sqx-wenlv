package com.sqx.app.lvyou.scenic.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicInfoBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicPageBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicSaveBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicUpdateBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicUpdateStateBo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicVo;
import com.sqx.app.lvyou.scenic.service.ScenicService;
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
 * 管理端旅游景区
 */
@Tag(name = "管理端-旅游景区", description = "管理端-旅游景区")
@RestController
@RequestMapping("/admin/scenic")
public class AdminScenicController {

    @Resource
    private ScenicService scenicService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:scenic:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ScenicVo.class)))
    @PostMapping("page")
    public Result<IPage<ScenicVo>> page(@RequestBody @Validated ScenicPageBo pageBo) {
        return Result.success(scenicService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:scenic:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated ScenicSaveBo saveBo) {
        scenicService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:scenic:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated ScenicUpdateBo updateBo) {
        scenicService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:scenic:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated ScenicInfoBo infoBo) {
        scenicService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新状态")
    @RequiresPermissions("lvyou:scenic:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated ScenicUpdateStateBo updateStateBo) {
        scenicService.updateState(updateStateBo);
        return Result.success();
    }
}

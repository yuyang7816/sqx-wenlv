package com.sqx.app.lvyou.activity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.activity.domain.bo.ActivityInfoBo;
import com.sqx.app.lvyou.activity.domain.bo.ActivityPageBo;
import com.sqx.app.lvyou.activity.domain.bo.ActivitySaveBo;
import com.sqx.app.lvyou.activity.domain.bo.ActivityUpdateBo;
import com.sqx.app.lvyou.activity.domain.bo.ActivityUpdateStateBo;
import com.sqx.app.lvyou.activity.domain.vo.ActivityVo;
import com.sqx.app.lvyou.activity.service.ActivityService;
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
 * 管理端精彩活动
 */
@Tag(name = "管理端-精彩活动", description = "管理端-精彩活动")
@RestController
@RequestMapping("/admin/activity")
public class AdminActivityController {

    @Resource
    private ActivityService activityService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:activity:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = ActivityVo.class)))
    @PostMapping("page")
    public Result<IPage<ActivityVo>> page(@RequestBody @Validated ActivityPageBo pageBo) {
        return Result.success(activityService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:activity:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated ActivitySaveBo saveBo) {
        activityService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:activity:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated ActivityUpdateBo updateBo) {
        activityService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:activity:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated ActivityInfoBo infoBo) {
        activityService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新状态")
    @RequiresPermissions("lvyou:activity:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated ActivityUpdateStateBo updateStateBo) {
        activityService.updateState(updateStateBo);
        return Result.success();
    }
}

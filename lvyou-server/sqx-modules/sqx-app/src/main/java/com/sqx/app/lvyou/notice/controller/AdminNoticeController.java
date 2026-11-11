package com.sqx.app.lvyou.notice.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.notice.domain.bo.NoticeInfoBo;
import com.sqx.app.lvyou.notice.domain.bo.NoticePageBo;
import com.sqx.app.lvyou.notice.domain.bo.NoticeSaveBo;
import com.sqx.app.lvyou.notice.domain.bo.NoticeUpdateBo;
import com.sqx.app.lvyou.notice.domain.bo.NoticeUpdatePinnedBo;
import com.sqx.app.lvyou.notice.domain.bo.NoticeUpdateStateBo;
import com.sqx.app.lvyou.notice.domain.vo.NoticeVo;
import com.sqx.app.lvyou.notice.service.NoticeService;
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
 * 管理端公告
 */
@Tag(name = "管理端-公告", description = "管理端-公告")
@RestController
@RequestMapping("/admin/notice")
public class AdminNoticeController {

    @Resource
    private NoticeService noticeService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:notice:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = NoticeVo.class)))
    @PostMapping("page")
    public Result<IPage<NoticeVo>> page(@RequestBody @Validated NoticePageBo pageBo) {
        return Result.success(noticeService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:notice:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated NoticeSaveBo saveBo) {
        noticeService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:notice:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated NoticeUpdateBo updateBo) {
        noticeService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:notice:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated NoticeInfoBo infoBo) {
        noticeService.delete(infoBo);
        return Result.success();
    }

    @Operation(summary = "更新状态")
    @RequiresPermissions("lvyou:notice:updateState")
    @PostMapping("updateState")
    public Result<Void> updateState(@RequestBody @Validated NoticeUpdateStateBo updateStateBo) {
        noticeService.updateState(updateStateBo);
        return Result.success();
    }

    @Operation(summary = "更新置顶（全局仅一条）")
    @RequiresPermissions("lvyou:notice:update")
    @PostMapping("updatePinned")
    public Result<Void> updatePinned(@RequestBody @Validated NoticeUpdatePinnedBo updatePinnedBo) {
        noticeService.updatePinned(updatePinnedBo);
        return Result.success();
    }
}

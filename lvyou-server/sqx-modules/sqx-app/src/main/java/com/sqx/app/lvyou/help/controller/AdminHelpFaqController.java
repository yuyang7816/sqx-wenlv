package com.sqx.app.lvyou.help.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqInfoBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqPageBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqSaveBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqUpdateBo;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqVo;
import com.sqx.app.lvyou.help.service.HelpFaqService;
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
 * 管理端帮助中心 FAQ
 */
@Tag(name = "管理端-帮助中心FAQ", description = "管理端-帮助中心FAQ")
@RestController
@RequestMapping("/admin/help/faq")
public class AdminHelpFaqController {

    @Resource
    private HelpFaqService helpFaqService;

    @Operation(summary = "分页列表")
    @RequiresPermissions("lvyou:helpFaq:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = HelpFaqVo.class)))
    @PostMapping("page")
    public Result<IPage<HelpFaqVo>> page(@RequestBody @Validated HelpFaqPageBo pageBo) {
        return Result.success(helpFaqService.pageInfo(pageBo));
    }

    @Operation(summary = "新增")
    @RequiresPermissions("lvyou:helpFaq:save")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated HelpFaqSaveBo saveBo) {
        helpFaqService.saveInfo(saveBo);
        return Result.success();
    }

    @Operation(summary = "更新")
    @RequiresPermissions("lvyou:helpFaq:update")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated HelpFaqUpdateBo updateBo) {
        helpFaqService.updateInfo(updateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @RequiresPermissions("lvyou:helpFaq:delete")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated HelpFaqInfoBo infoBo) {
        helpFaqService.delete(infoBo);
        return Result.success();
    }
}

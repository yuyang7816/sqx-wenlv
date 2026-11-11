package com.sqx.app.base.appConfig.controller;

import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.appConfig.domain.bo.SysConfigInfoBO;
import com.sqx.app.base.appConfig.domain.vo.AppConfigInfoVO;
import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;
import com.sqx.app.base.appConfig.service.AppConfigService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author chengming
 * @since 2025/6/9
 */
@Tag(name = "用户端-系统配置")
@RestController
@RequestMapping("app/config")
public class AppConfigController {

    @Resource
    private AppConfigService appConfigService;

    @Operation(summary = "聚合配置（产品名称/客服/邀请）")
    @PublicAccess
    @GetMapping("info")
    public Result<AppConfigInfoVO> info() {
        return Result.success(appConfigService.getAppConfigInfo());
    }

    @Operation(summary = "按 key 查询单条配置")
    @PublicAccess
    @PostMapping("info")
    public Result<SysConfigVO> infoByKey(@RequestBody SysConfigInfoBO sysConfigInfoBO) {
        SysConfigVO vo = appConfigService.info(sysConfigInfoBO);
        return Result.success(vo);
    }
}

package com.sqx.app.base.appConfig.controller;

import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.appConfig.domain.vo.AgreementVO;
import com.sqx.app.base.appConfig.service.AppConfigService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端协议正文
 */
@Tag(name = "用户端-协议")
@RestController
@RequestMapping("app/agreement")
public class AppAgreementController {

    @Resource
    private AppConfigService appConfigService;

    @Operation(summary = "协议正文（HTML）", description = "key: user / privacy / about")
    @PublicAccess
    @GetMapping("{key}")
    public Result<AgreementVO> getAgreement(@PathVariable("key") String key) {
        return Result.success(appConfigService.getAgreement(key));
    }
}

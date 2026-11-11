package com.sqx.app.base.user.cancel.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.user.cancel.domain.bo.CancelAccountSubmitBo;
import com.sqx.app.base.user.cancel.domain.vo.CancelAccountSubmitVo;
import com.sqx.app.base.user.cancel.service.CancelAccountService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端注销申请
 */
@Tag(name = "用户端-注销账号", description = "用户端-注销账号")
@RestController
@RequestMapping(BaseConstant.BASE_APP_PREFIX + "/cancelAccount")
public class AppCancelAccountController {

    @Resource
    private CancelAccountService cancelAccountService;

    @Operation(summary = "提交注销申请")
    @Login
    @PostMapping("submit")
    public Result<CancelAccountSubmitVo> submit(@RequestAttribute("userId") Long userId,
                                                @RequestBody @Validated CancelAccountSubmitBo submitBo) {
        return Result.success(cancelAccountService.submit(userId, submitBo));
    }
}

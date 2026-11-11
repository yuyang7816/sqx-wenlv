package com.sqx.app.lvyou.kefu.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImMessagePageBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImSendBo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImMessageVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImSendVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImSessionVo;
import com.sqx.app.lvyou.kefu.service.KefuImService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "用户端-在线客服IM", description = "用户端-在线客服IM")
@RestController
@RequestMapping("/app/kefu-im")
public class AppKefuImController {

    @Resource
    private KefuImService kefuImService;

    @Operation(summary = "打开或获取当前会话")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = KefuImSessionVo.class)))
    @PostMapping("session/open")
    public Result<KefuImSessionVo> openSession(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(kefuImService.openSession(userId));
    }

    @Operation(summary = "发送消息")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = KefuImSendVo.class)))
    @PostMapping("message/send")
    public Result<KefuImSendVo> sendMessage(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                            @RequestBody @Validated KefuImSendBo sendBo) {
        return Result.success(kefuImService.sendUserMessage(userId, sendBo));
    }

    @Operation(summary = "消息列表")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = KefuImMessageVo.class)))
    @PostMapping("message/list")
    public Result<List<KefuImMessageVo>> listMessages(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                       @RequestBody(required = false) KefuImMessagePageBo pageBo) {
        if (pageBo == null) {
            pageBo = new KefuImMessagePageBo();
        }
        return Result.success(kefuImService.listUserMessages(userId, pageBo));
    }
}

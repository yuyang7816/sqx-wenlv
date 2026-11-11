package com.sqx.app.base.messages.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.base.messages.domain.bo.MessagesInfoBo;
import com.sqx.app.base.messages.domain.bo.MessagesPageBo;
import com.sqx.app.base.messages.domain.bo.MessagesReadAllBo;
import com.sqx.app.base.messages.domain.bo.MessagesSaveBo;
import com.sqx.app.base.messages.domain.bo.MessagesUpdateBo;
import com.sqx.app.base.messages.domain.vo.MessagesVo;
import com.sqx.app.base.messages.service.MessagesService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户端站内消息
 */
@Tag(name = "用户端-消息信息表", description = "用户端-消息信息表")
@RestController
@RequestMapping("/app/messages")
public class AppMessagesController {

    @Resource
    private MessagesService messagesService;

    @Operation(summary = "保存")
    @PostMapping("save")
    public Result<Void> save(@RequestBody @Validated MessagesSaveBo messagesSaveBo) {
        messagesService.saveInfo(messagesSaveBo);
        return Result.success();
    }

    @Operation(summary = "列表（当前登录用户）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MessagesVo.class)))
    @PostMapping("page")
    public Result<IPage<MessagesVo>> page(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                          @RequestBody @Validated MessagesPageBo messagesPageBo) {
        return Result.success(messagesService.pageInfo(userId, messagesPageBo));
    }

    @Operation(summary = "详情（当前登录用户）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MessagesVo.class)))
    @PostMapping("info")
    public Result<MessagesVo> info(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                   @RequestBody @Validated MessagesInfoBo messagesInfoBo) {
        return Result.success(messagesService.info(userId, messagesInfoBo));
    }

    @Operation(summary = "标记已读")
    @Login
    @PostMapping("read")
    public Result<Void> read(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                             @RequestBody @Validated MessagesInfoBo messagesInfoBo) {
        messagesService.markRead(userId, messagesInfoBo);
        return Result.success();
    }

    @Operation(summary = "全部标记已读")
    @Login
    @PostMapping("read-all")
    public Result<Map<String, Integer>> readAll(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                               @RequestBody(required = false) MessagesReadAllBo messagesReadAllBo) {
        Integer type = messagesReadAllBo != null ? messagesReadAllBo.getType() : null;
        int count = messagesService.markAllRead(userId, type);
        Map<String, Integer> data = new HashMap<>(1);
        data.put("count", count);
        return Result.success(data);
    }

    @Operation(summary = "未读数量")
    @Login
    @GetMapping("unread-count")
    public Result<Map<String, Long>> unreadCount(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        Map<String, Long> data = new HashMap<>(3);
        data.put("count", messagesService.unreadCount(userId));
        data.put("systemCount", messagesService.unreadCount(userId, 1));
        data.put("userCount", messagesService.unreadCount(userId, 2));
        return Result.success(data);
    }

    @Operation(summary = "更新")
    @PostMapping("update")
    public Result<Void> update(@RequestBody @Validated MessagesUpdateBo messagesUpdateBo) {
        messagesService.updateInfo(messagesUpdateBo);
        return Result.success();
    }

    @Operation(summary = "删除")
    @PostMapping("delete")
    public Result<Void> delete(@RequestBody @Validated MessagesInfoBo messagesInfoBo) {
        messagesService.delete(messagesInfoBo);
        return Result.success();
    }
}

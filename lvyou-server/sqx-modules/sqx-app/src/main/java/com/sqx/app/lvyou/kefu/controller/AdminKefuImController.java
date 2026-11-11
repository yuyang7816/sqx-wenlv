package com.sqx.app.lvyou.kefu.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImMessagePageBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImReplyBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImSessionInfoBo;
import com.sqx.app.lvyou.kefu.domain.bo.KefuImSessionPageBo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImMessageVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImSendVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImSessionVo;
import com.sqx.app.lvyou.kefu.domain.vo.KefuImUnreadVo;
import com.sqx.app.lvyou.kefu.service.KefuImService;
import com.sqx.common.core.domain.Result;
import com.sqx.system.controller.AbstractController;
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

@Tag(name = "管理端-在线客服IM", description = "管理端-在线客服IM")
@RestController
@RequestMapping("/admin/kefu-im")
public class AdminKefuImController extends AbstractController {

    @Resource
    private KefuImService kefuImService;

    @Operation(summary = "会话分页")
    @RequiresPermissions("lvyou:kefuIm:session:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = KefuImSessionVo.class)))
    @PostMapping("session/page")
    public Result<IPage<KefuImSessionVo>> sessionPage(@RequestBody @Validated KefuImSessionPageBo pageBo) {
        return Result.success(kefuImService.adminSessionPage(pageBo));
    }

    @Operation(summary = "会话消息列表")
    @RequiresPermissions("lvyou:kefuIm:message:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = KefuImMessageVo.class)))
    @PostMapping("message/list")
    public Result<List<KefuImMessageVo>> messageList(@RequestBody @Validated KefuImMessagePageBo pageBo) {
        return Result.success(kefuImService.adminListMessages(pageBo));
    }

    @Operation(summary = "客服回复")
    @RequiresPermissions("lvyou:kefuIm:message:reply")
    @ApiResponse(content = @Content(schema = @Schema(implementation = KefuImSendVo.class)))
    @PostMapping("message/reply")
    public Result<KefuImSendVo> reply(@RequestBody @Validated KefuImReplyBo replyBo) {
        return Result.success(kefuImService.adminReply(getUserId(), replyBo));
    }

    @Operation(summary = "关闭会话")
    @RequiresPermissions("lvyou:kefuIm:session:close")
    @PostMapping("session/close")
    public Result<Void> closeSession(@RequestBody @Validated KefuImSessionInfoBo infoBo) {
        kefuImService.adminCloseSession(infoBo.getSessionId());
        return Result.success();
    }

    @Operation(summary = "客服未读消息总数（菜单角标）")
    @RequiresPermissions("lvyou:kefuIm:session:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = KefuImUnreadVo.class)))
    @PostMapping("session/unread-count")
    public Result<KefuImUnreadVo> unreadCount() {
        return Result.success(kefuImService.adminUnreadTotal());
    }
}

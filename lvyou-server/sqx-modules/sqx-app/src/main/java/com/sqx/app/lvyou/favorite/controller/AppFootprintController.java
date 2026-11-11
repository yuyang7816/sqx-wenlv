package com.sqx.app.lvyou.favorite.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.favorite.domain.vo.AssetItemVo;
import com.sqx.app.lvyou.favorite.service.FootprintService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端浏览足迹
 */
@Tag(name = "用户端-浏览足迹", description = "用户端-浏览足迹")
@RestController
@RequestMapping("/app/footprint")
public class AppFootprintController {

    @Resource
    private FootprintService footprintService;

    @Operation(summary = "足迹列表（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = AssetItemVo.class)))
    @GetMapping("list")
    public Result<IPage<AssetItemVo>> list(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                           @RequestParam(value = "targetType", required = false) String targetType,
                                           @RequestParam(value = "page", required = false) Long page,
                                           @RequestParam(value = "limit", required = false) Long limit,
                                           @RequestParam(value = "size", required = false) Long size) {
        return Result.success(footprintService.list(userId, targetType, page, limit != null ? limit : size));
    }
}

package com.sqx.app.lvyou.favorite.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.favorite.domain.bo.FavoriteToggleBo;
import com.sqx.app.lvyou.favorite.domain.vo.AssetItemVo;
import com.sqx.app.lvyou.favorite.domain.vo.FavoriteToggleVo;
import com.sqx.app.lvyou.favorite.service.FavoriteService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端收藏
 */
@Tag(name = "用户端-收藏", description = "用户端-收藏")
@RestController
@RequestMapping("/app/favorite")
public class AppFavoriteController {

    @Resource
    private FavoriteService favoriteService;

    @Operation(summary = "收藏列表（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = AssetItemVo.class)))
    @GetMapping("list")
    public Result<IPage<AssetItemVo>> list(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                           @RequestParam(value = "targetType", required = false) String targetType,
                                           @RequestParam(value = "page", required = false) Long page,
                                           @RequestParam(value = "limit", required = false) Long limit,
                                           @RequestParam(value = "size", required = false) Long size) {
        return Result.success(favoriteService.list(userId, targetType, page, limit != null ? limit : size));
    }

    @Operation(summary = "收藏切换")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = FavoriteToggleVo.class)))
    @PostMapping("toggle")
    public Result<FavoriteToggleVo> toggle(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                           @RequestBody @Validated FavoriteToggleBo toggleBo) {
        return Result.success(favoriteService.toggle(userId, toggleBo));
    }
}

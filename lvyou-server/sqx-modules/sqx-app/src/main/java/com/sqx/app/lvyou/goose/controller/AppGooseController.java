package com.sqx.app.lvyou.goose.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.goose.domain.bo.GooseAppListBo;
import com.sqx.app.lvyou.goose.domain.vo.GooseAppDetailVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseAppVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseDishDetailVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseMenuItemVo;
import com.sqx.app.lvyou.goose.service.GooseService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户端灰鹅美食
 */
@Tag(name = "用户端-灰鹅美食", description = "用户端-灰鹅美食")
@RestController
@RequestMapping("/app/goose")
public class AppGooseController {

    @Resource
    private GooseService gooseService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "灰鹅美食商家列表（分页，仅正常营业）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = GooseAppVo.class)))
    @GetMapping("list")
    public Result<IPage<GooseAppVo>> list(@RequestParam(value = "lat", required = false) String lat,
                                          @RequestParam(value = "lng", required = false) String lng,
                                          @RequestParam(value = "keyword", required = false) String keyword,
                                          @RequestParam(value = "page", required = false) Long page,
                                          @RequestParam(value = "limit", required = false) Long limit,
                                          @RequestParam(value = "size", required = false) Long size) {
        GooseAppListBo listBo = new GooseAppListBo();
        listBo.setDestinationId(destinationSupport.resolveAppDestinationId(lat, lng));
        listBo.setKeyword(keyword);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(gooseService.appPage(listBo));
    }

    @Operation(summary = "灰鹅美食商家详情（含招牌菜预览、营业时间）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = GooseAppDetailVo.class)))
    @GetMapping("{id}")
    public Result<GooseAppDetailVo> detail(@PathVariable("id") Long id,
                                           @RequestAttribute(value = AuthorizationInterceptor.USER_KEY, required = false) Long userId) {
        return Result.success(gooseService.appDetail(id, userId));
    }

    @Operation(summary = "灰鹅美食全部在售菜单")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = GooseMenuItemVo.class)))
    @GetMapping("{id}/menu")
    public Result<List<GooseMenuItemVo>> menu(@PathVariable("id") Long id) {
        return Result.success(gooseService.appMenu(id));
    }

    @Operation(summary = "灰鹅美菜品详情（含轮播图）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = GooseDishDetailVo.class)))
    @GetMapping("dish/{serviceId}")
    public Result<GooseDishDetailVo> dishDetail(@PathVariable("serviceId") Long serviceId) {
        return Result.success(gooseService.appDishDetail(serviceId));
    }
}

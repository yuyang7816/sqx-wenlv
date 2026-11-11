package com.sqx.app.lvyou.activity.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.activity.domain.bo.ActivityAppListBo;
import com.sqx.app.lvyou.activity.domain.vo.ActivityAppDetailVo;
import com.sqx.app.lvyou.activity.domain.vo.ActivityAppVo;
import com.sqx.app.lvyou.activity.domain.vo.ActivityEnrollVo;
import com.sqx.app.lvyou.activity.service.ActivityService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端精彩活动
 */
@Tag(name = "用户端-精彩活动", description = "用户端-精彩活动")
@RestController
@RequestMapping("/app/activity")
public class AppActivityController {

    @Resource
    private ActivityService activityService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "活动列表（分页，仅上架）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = ActivityAppVo.class)))
    @GetMapping("list")
    public Result<IPage<ActivityAppVo>> list(@RequestParam(value = "lat", required = false) String lat,
                                             @RequestParam(value = "lng", required = false) String lng,
                                             @RequestParam(value = "name", required = false) String name,
                                             @RequestParam(value = "status", required = false) String status,
                                             @RequestParam(value = "page", required = false) Long page,
                                             @RequestParam(value = "limit", required = false) Long limit,
                                             @RequestParam(value = "size", required = false) Long size) {
        ActivityAppListBo listBo = new ActivityAppListBo();
        listBo.setDestinationId(destinationSupport.resolveAppDestinationId(lat, lng));
        listBo.setName(name);
        listBo.setStatus(status);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(activityService.appPage(listBo));
    }

    @Operation(summary = "活动详情")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = ActivityAppDetailVo.class)))
    @GetMapping("{id}")
    public Result<ActivityAppDetailVo> detail(@PathVariable("id") Long id,
                                              @RequestAttribute(value = AuthorizationInterceptor.USER_KEY, required = false) Long userId) {
        return Result.success(activityService.appDetail(id, userId));
    }

    @Operation(summary = "活动报名")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = ActivityEnrollVo.class)))
    @PostMapping("{id}/enroll")
    public Result<ActivityEnrollVo> enroll(@PathVariable("id") Long id,
                                           @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(activityService.enroll(id, userId));
    }
}

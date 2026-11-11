package com.sqx.app.lvyou.canteen.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationListBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationSubmitBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationUpdateBo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationPendingCountVo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationSubmitVo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationVo;
import com.sqx.app.lvyou.canteen.service.CanteenReservationService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端餐饮预约
 */
@Tag(name = "用户端-餐饮预约", description = "用户端-餐饮预约")
@RestController
@RequestMapping("/app/reservation/canteen")
public class AppCanteenReservationController {

    @Resource
    private CanteenReservationService canteenReservationService;

    @Operation(summary = "提交餐饮预约")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationSubmitVo.class)))
    @PostMapping("submit")
    public Result<CanteenReservationSubmitVo> submit(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                     @RequestBody @Validated CanteenReservationSubmitBo submitBo) {
        return Result.success(canteenReservationService.submit(userId, submitBo));
    }

    @Operation(summary = "餐饮预约列表（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationVo.class)))
    @GetMapping("list")
    public Result<IPage<CanteenReservationVo>> list(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                    @RequestParam(value = "page", required = false) Long page,
                                                    @RequestParam(value = "limit", required = false) Long limit,
                                                    @RequestParam(value = "status", required = false) String status) {
        CanteenReservationListBo listBo = new CanteenReservationListBo();
        listBo.setPage(page);
        listBo.setLimit(limit);
        listBo.setStatus(status);
        return Result.success(canteenReservationService.page(userId, listBo));
    }

    @Operation(summary = "餐饮预约详情")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationVo.class)))
    @GetMapping("{id}")
    public Result<CanteenReservationVo> detail(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                 @PathVariable("id") String id) {
        return Result.success(canteenReservationService.detail(userId, id));
    }

    @Operation(summary = "待确认预约数量（我的页角标）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationPendingCountVo.class)))
    @GetMapping("pending-count")
    public Result<CanteenReservationPendingCountVo> pendingCount(
            @RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(canteenReservationService.pendingCount(userId));
    }

    @Operation(summary = "取消餐饮预约")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationVo.class)))
    @PostMapping("{id}/cancel")
    public Result<CanteenReservationVo> cancel(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                               @PathVariable("id") String id) {
        return Result.success(canteenReservationService.cancel(userId, id));
    }

    @Operation(summary = "修改餐饮预约")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CanteenReservationVo.class)))
    @PostMapping("{id}/update")
    public Result<CanteenReservationVo> update(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                               @PathVariable("id") String id,
                                               @RequestBody @Validated CanteenReservationUpdateBo updateBo) {
        return Result.success(canteenReservationService.update(userId, id, updateBo));
    }
}

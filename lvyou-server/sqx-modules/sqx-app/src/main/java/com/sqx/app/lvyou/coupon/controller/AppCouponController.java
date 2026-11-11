package com.sqx.app.lvyou.coupon.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.coupon.domain.bo.CouponClaimBo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponAvailableVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponCenterVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponCountVo;
import com.sqx.app.lvyou.coupon.domain.vo.CouponMineVo;
import com.sqx.app.lvyou.coupon.service.CouponService;
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
import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端优惠券
 */
@Tag(name = "用户端-优惠券", description = "用户端-优惠券")
@RestController
@RequestMapping("/app/coupon")
public class AppCouponController {

    @Resource
    private CouponService couponService;

    @Operation(summary = "领券中心")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CouponCenterVo.class)))
    @GetMapping("center")
    public Result<List<CouponCenterVo>> center(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(couponService.center(userId));
    }

    @Operation(summary = "领取优惠券")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CouponMineVo.class)))
    @PostMapping("claim")
    public Result<CouponMineVo> claim(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                        @RequestBody @Validated CouponClaimBo claimBo) {
        return Result.success(couponService.claim(userId, claimBo));
    }

    @Operation(summary = "我的优惠券")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CouponMineVo.class)))
    @GetMapping("mine")
    public Result<List<CouponMineVo>> mine(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                            @RequestParam(value = "status", required = false) String status) {
        return Result.success(couponService.mine(userId, status));
    }

    @Operation(summary = "下单可用优惠券")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CouponAvailableVo.class)))
    @GetMapping("available")
    public Result<List<CouponAvailableVo>> available(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                       @RequestParam(value = "scope", required = false) String scope,
                                                       @RequestParam(value = "amount", required = false) BigDecimal amount) {
        return Result.success(couponService.available(userId, scope, amount));
    }

    @Operation(summary = "可用优惠券数量")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = CouponCountVo.class)))
    @GetMapping("count")
    public Result<CouponCountVo> count(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(couponService.countAvailable(userId));
    }
}

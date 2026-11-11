package com.sqx.app.lvyou.route.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;
import com.sqx.app.lvyou.route.domain.bo.RouteAppListBo;
import com.sqx.app.lvyou.route.domain.bo.RouteReviewAppListBo;
import com.sqx.app.lvyou.route.domain.bo.RouteReviewSubmitBo;
import com.sqx.app.lvyou.route.domain.vo.RouteAppDetailVo;
import com.sqx.app.lvyou.route.domain.vo.RouteAppVo;
import com.sqx.app.lvyou.route.domain.vo.RouteItineraryPageVo;
import com.sqx.app.lvyou.route.service.RouteService;
import com.sqx.common.core.domain.Result;
import org.springframework.validation.annotation.Validated;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
 * 用户端旅游线路
 */
@Tag(name = "用户端-旅游线路", description = "用户端-旅游线路")
@RestController
@RequestMapping("/app/route")
public class AppRouteController {

    @Resource
    private RouteService routeService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "线路列表（分页，仅上架）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = RouteAppVo.class)))
    @GetMapping("list")
    public Result<IPage<RouteAppVo>> list(@RequestParam(value = "lat", required = false) String lat,
                                          @RequestParam(value = "lng", required = false) String lng,
                                          @RequestParam(value = "name", required = false) String name,
                                          @RequestParam(value = "merchantId", required = false) Long merchantId,
                                          @RequestParam(value = "dayCount", required = false) Integer dayCount,
                                          @RequestParam(value = "minDayCount", required = false) Integer minDayCount,
                                          @RequestParam(value = "page", required = false) Long page,
                                          @RequestParam(value = "limit", required = false) Long limit,
                                          @RequestParam(value = "size", required = false) Long size) {
        RouteAppListBo listBo = new RouteAppListBo();
        // 按门店查线路时不再按城市目的地过滤，避免漏掉该商户全部线路
        if (merchantId == null) {
            listBo.setDestinationIds(destinationSupport.resolveAppCityDestinationIds(lat, lng));
        }
        listBo.setName(name);
        listBo.setMerchantId(merchantId);
        listBo.setDayCount(dayCount);
        listBo.setMinDayCount(minDayCount);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(routeService.appPage(listBo));
    }

    @Operation(summary = "线路详情（含 meetPoint/price/days/行程预览）")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = RouteAppDetailVo.class)))
    @GetMapping("{id}")
    public Result<RouteAppDetailVo> detail(@PathVariable("id") Long id,
                                           @RequestAttribute(value = AuthorizationInterceptor.USER_KEY, required = false) Long userId) {
        return Result.success(routeService.appDetail(id, userId));
    }

    @Operation(summary = "完整行程安排")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = RouteItineraryPageVo.class)))
    @GetMapping("{id}/itinerary")
    public Result<RouteItineraryPageVo> itinerary(@PathVariable("id") Long id) {
        return Result.success(routeService.appItinerary(id));
    }

    @Operation(summary = "旅行社门店评价列表")
    @PublicAccess
    @ApiResponse(content = @Content(schema = @Schema(implementation = ReviewAppVo.class)))
    @GetMapping("store/{merchantId}/reviews")
    public Result<IPage<ReviewAppVo>> storeReviews(@PathVariable("merchantId") Long merchantId,
                                                     @RequestParam(value = "page", required = false) Long page,
                                                     @RequestParam(value = "limit", required = false) Long limit,
                                                     @RequestParam(value = "size", required = false) Long size) {
        RouteReviewAppListBo listBo = new RouteReviewAppListBo();
        listBo.setMerchantId(merchantId);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(routeService.appStoreReviews(listBo));
    }

    @Operation(summary = "提交旅行社门店评价")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = ReviewAppVo.class)))
    @PostMapping("store/{merchantId}/reviews")
    public Result<ReviewAppVo> submitStoreReview(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                 @PathVariable("merchantId") Long merchantId,
                                                 @RequestBody @Validated RouteReviewSubmitBo submitBo) {
        return Result.success(routeService.submitStoreReview(userId, merchantId, submitBo));
    }
}

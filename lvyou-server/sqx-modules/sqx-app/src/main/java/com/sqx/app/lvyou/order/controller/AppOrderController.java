package com.sqx.app.lvyou.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.lvyou.order.domain.bo.OrderAppActionBo;
import com.sqx.app.lvyou.order.domain.bo.OrderCreateBo;
import com.sqx.app.lvyou.order.domain.bo.OrderGoodsCreateBo;
import com.sqx.app.lvyou.order.domain.bo.OrderListBo;
import com.sqx.app.lvyou.order.domain.bo.OrderLogisticsQueryBo;
import com.sqx.app.lvyou.order.domain.vo.OrderAppVo;
import com.sqx.app.lvyou.order.domain.vo.OrderCreateResultVo;
import com.sqx.app.lvyou.order.domain.vo.OrderLogisticsVo;
import com.sqx.app.lvyou.order.domain.vo.OrderRefundPreviewVo;
import com.sqx.app.lvyou.order.service.OrderLogisticsService;
import com.sqx.app.lvyou.order.service.OrderService;
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
 * 用户端统一订单
 */
@Tag(name = "用户端-订单", description = "用户端-订单")
@RestController
@RequestMapping("/app/order")
public class AppOrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderLogisticsService orderLogisticsService;

    @Operation(summary = "创建酒店/线路订单")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderCreateResultVo.class)))
    @PostMapping("create")
    public Result<OrderCreateResultVo> create(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                              @RequestBody @Validated OrderCreateBo createBo) {
        return Result.success(orderService.create(userId, createBo));
    }

    @Operation(summary = "创建商品订单")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderCreateResultVo.class)))
    @PostMapping("goods/create")
    public Result<OrderCreateResultVo> createGoods(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                   @RequestBody @Validated OrderGoodsCreateBo createBo) {
        return Result.success(orderService.createGoods(userId, createBo));
    }

    @Operation(summary = "订单列表（分页）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderAppVo.class)))
    @GetMapping("list")
    public Result<IPage<OrderAppVo>> list(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                          @RequestParam(value = "page", required = false) Long page,
                                          @RequestParam(value = "limit", required = false) Long limit,
                                          @RequestParam(value = "category", required = false) String category,
                                          @RequestParam(value = "status", required = false) String status,
                                          @RequestParam(value = "orderNo", required = false) String orderNo) {
        OrderListBo listBo = new OrderListBo();
        listBo.setPage(page);
        listBo.setLimit(limit);
        listBo.setCategory(category);
        listBo.setStatus(status);
        listBo.setOrderNo(orderNo);
        return Result.success(orderService.appPage(userId, listBo));
    }

    @Operation(summary = "订单详情")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderAppVo.class)))
    @GetMapping("{id}")
    public Result<OrderAppVo> detail(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                     @PathVariable("id") String id) {
        return Result.success(orderService.appDetail(userId, id));
    }

    @Operation(summary = "取消订单（仅待支付）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderAppVo.class)))
    @PostMapping("{id}/cancel")
    public Result<OrderAppVo> cancel(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                     @PathVariable("id") String id) {
        return Result.success(orderService.cancel(userId, id));
    }

    @Operation(summary = "订单操作（确认收货/申请退款）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderAppVo.class)))
    @PostMapping("{id}/action")
    public Result<OrderAppVo> action(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                       @PathVariable("id") String id,
                                       @RequestBody @Validated OrderAppActionBo actionBo) {
        return Result.success(orderService.appAction(userId, id, actionBo));
    }

    @Operation(summary = "线路订单退款预览")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderRefundPreviewVo.class)))
    @GetMapping("{id}/refund-preview")
    public Result<OrderRefundPreviewVo> refundPreview(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                      @PathVariable("id") String id) {
        return Result.success(orderService.refundPreview(userId, id));
    }

    @Operation(summary = "查询商品订单物流")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderLogisticsVo.class)))
    @PostMapping("logistics")
    public Result<OrderLogisticsVo> logistics(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                              @RequestBody @Validated OrderLogisticsQueryBo queryBo) {
        return Result.success(orderLogisticsService.queryLogistics(userId, queryBo.getId()));
    }
}

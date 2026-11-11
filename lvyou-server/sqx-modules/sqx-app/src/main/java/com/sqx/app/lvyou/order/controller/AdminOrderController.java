package com.sqx.app.lvyou.order.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.order.domain.bo.OrderAdminActionBo;
import com.sqx.app.lvyou.order.domain.bo.OrderAdminPageBo;
import com.sqx.app.lvyou.order.domain.bo.OrderLogisticsQueryBo;
import com.sqx.app.lvyou.order.domain.vo.OrderAdminVo;
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
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 管理端订单监管
 */
@Tag(name = "管理端-订单", description = "管理端-订单")
@RestController
@RequestMapping("/admin/order")
public class AdminOrderController {

    @Resource
    private OrderService orderService;
    @Resource
    private OrderLogisticsService orderLogisticsService;

    @Operation(summary = "订单分页列表")
    @RequiresPermissions("lvyou:order:page")
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderAdminVo.class)))
    @PostMapping("page")
    public Result<IPage<OrderAdminVo>> page(@RequestBody OrderAdminPageBo pageBo) {
        return Result.success(orderService.adminPage(pageBo));
    }

    @Operation(summary = "订单详情")
    @RequiresPermissions("lvyou:order:info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderAdminVo.class)))
    @GetMapping("{id}")
    public Result<OrderAdminVo> detail(@PathVariable("id") Long id) {
        return Result.success(orderService.adminDetail(id));
    }

    @Operation(summary = "订单操作（取消/完成/退款）")
    @RequiresPermissions("lvyou:order:action")
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderAdminVo.class)))
    @PostMapping("action")
    public Result<OrderAdminVo> action(@RequestBody @Validated OrderAdminActionBo actionBo) {
        return Result.success(orderService.adminAction(actionBo));
    }

    @Operation(summary = "线路订单强制退款预览（指定退款比例）")
    @RequiresPermissions("lvyou:order:action")
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderRefundPreviewVo.class)))
    @GetMapping("{id}/refund-preview")
    public Result<OrderRefundPreviewVo> routeRefundPreview(@PathVariable("id") Long id,
                                                           @RequestParam("refundRatio") Integer refundRatio) {
        return Result.success(orderService.adminRouteRefundPreview(id, refundRatio));
    }

    @Operation(summary = "查询商品订单物流")
    @RequiresPermissions("lvyou:order:info")
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderLogisticsVo.class)))
    @PostMapping("logistics")
    public Result<OrderLogisticsVo> logistics(@RequestBody @Validated OrderLogisticsQueryBo queryBo) {
        return Result.success(orderLogisticsService.queryLogistics(null, queryBo.getId()));
    }
}

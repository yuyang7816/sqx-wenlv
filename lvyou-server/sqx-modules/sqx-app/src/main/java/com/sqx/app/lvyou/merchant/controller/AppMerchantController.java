package com.sqx.app.lvyou.merchant.controller;

import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantApplyBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantOrderActionBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantOrderListBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelRoomStandardSaveBo;
import com.sqx.app.lvyou.hotel.domain.bo.HotelRoomCalendarSaveBo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelRoomStandardVo;
import com.sqx.app.lvyou.hotel.domain.vo.HotelRoomCalendarDayVo;
import com.sqx.app.lvyou.hotel.service.HotelService;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantProfileUpdateBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantQueueUpdateBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantServicePublishBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantServiceSaveBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantStoreDecorSaveBo;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleAppVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDashboardVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantOrderVo;
import com.sqx.app.lvyou.order.domain.vo.OrderLogisticsVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantProfileVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantServiceVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStatusVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStoreDecorVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStoreTypeVo;
import com.sqx.app.lvyou.merchant.service.MerchantCenterService;
import com.sqx.app.lvyou.merchant.service.MerchantOrderService;
import com.sqx.app.lvyou.merchant.service.MerchantService;
import com.sqx.app.lvyou.merchant.service.MerchantStoreTypeConfigService;
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
import java.util.List;

/**
 * 用户端商户入驻
 */
@Tag(name = "用户端-商户入驻", description = "用户端-商户入驻")
@RestController
@RequestMapping("/app/merchant")
public class AppMerchantController {

    @Resource
    private MerchantService merchantService;

    @Resource
    private MerchantCenterService merchantCenterService;

    @Resource
    private MerchantOrderService merchantOrderService;

    @Resource
    private MerchantStoreTypeConfigService merchantStoreTypeConfigService;

    @Resource
    private HotelService hotelService;

    @Operation(summary = "查询房型日历房态")
    @Login
    @GetMapping("hotel/room/{roomId}/calendar")
    public Result<List<HotelRoomCalendarDayVo>> roomCalendar(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                            @PathVariable("roomId") Long roomId,
                                                            @RequestParam("startDate") String startDate,
                                                            @RequestParam("endDate") String endDate) {
        return Result.success(hotelService.merchantRoomCalendar(userId, roomId, startDate, endDate));
    }

    @Operation(summary = "批量保存房型日历房态")
    @Login
    @PostMapping("hotel/room/{roomId}/calendar")
    public Result<Void> saveRoomCalendar(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                         @PathVariable("roomId") Long roomId,
                                         @RequestBody @Validated HotelRoomCalendarSaveBo saveBo) {
        hotelService.merchantSaveRoomCalendar(userId, roomId, saveBo);
        return Result.success();
    }

    @Operation(summary = "可选入驻类型列表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantStoreTypeVo.class)))
    @GetMapping("store-types")
    public Result<List<MerchantStoreTypeVo>> storeTypes() {
        return Result.success(merchantStoreTypeConfigService.listActive());
    }

    @Operation(summary = "提交入驻申请")
    @Login
    @PostMapping("apply")
    public Result<Void> apply(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                              @RequestBody @Validated MerchantApplyBo applyBo) {
        merchantService.apply(userId, applyBo);
        return Result.success();
    }

    @Operation(summary = "查询入驻审核状态")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantStatusVo.class)))
    @GetMapping("status")
    public Result<MerchantStatusVo> status(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(merchantService.getStatus(userId));
    }

    @Operation(summary = "商户中心概览")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantDashboardVo.class)))
    @GetMapping("dashboard")
    public Result<MerchantDashboardVo> dashboard(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(merchantCenterService.getDashboard(userId));
    }

    @Operation(summary = "待处理订单数")
    @Login
    @GetMapping("orders/pending-count")
    public Result<Long> pendingOrderCount(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(merchantCenterService.getPendingOrderCount(userId));
    }

    @Operation(summary = "订单列表（分页，Tab 筛选）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantOrderVo.class)))
    @GetMapping("orders")
    public Result<IPage<MerchantOrderVo>> orders(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                 @RequestParam(value = "tab", required = false) String tab,
                                                 @RequestParam(value = "page", required = false) Long page,
                                                 @RequestParam(value = "limit", required = false) Long limit,
                                                 @RequestParam(value = "orderNo", required = false) String orderNo,
                                                 @RequestParam(value = "status", required = false) String status) {
        MerchantOrderListBo listBo = new MerchantOrderListBo();
        listBo.setTab(tab);
        listBo.setPage(page);
        listBo.setLimit(limit);
        listBo.setOrderNo(orderNo);
        listBo.setStatus(status);
        return Result.success(merchantOrderService.pageOrders(userId, listBo));
    }

    @Operation(summary = "订单详情")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantOrderVo.class)))
    @GetMapping("orders/{id}")
    public Result<MerchantOrderVo> orderDetail(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                               @PathVariable("id") String id) {
        return Result.success(merchantOrderService.getOrderDetail(userId, id));
    }

    @Operation(summary = "订单操作（确认/发货/完成/取消/拒绝/退款）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantOrderVo.class)))
    @PostMapping("orders/{id}/action")
    public Result<MerchantOrderVo> orderAction(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                               @PathVariable("id") String id,
                                               @RequestBody @Validated MerchantOrderActionBo actionBo) {
        return Result.success(merchantOrderService.executeAction(userId, id, actionBo));
    }

    @Operation(summary = "查询商品订单物流")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = OrderLogisticsVo.class)))
    @PostMapping("orders/{id}/logistics")
    public Result<OrderLogisticsVo> orderLogistics(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                   @PathVariable("id") String id) {
        return Result.success(merchantOrderService.queryOrderLogistics(userId, id));
    }

    @Operation(summary = "商户资料")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantProfileVo.class)))
    @GetMapping("profile")
    public Result<MerchantProfileVo> profile(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(merchantCenterService.getProfile(userId));
    }

    @Operation(summary = "更新商户资料")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantProfileVo.class)))
    @PostMapping("profile")
    public Result<MerchantProfileVo> updateProfile(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                     @RequestBody MerchantProfileUpdateBo updateBo) {
        return Result.success(merchantCenterService.updateProfile(userId, updateBo));
    }

    @Operation(summary = "更新餐饮现场排队状态")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantProfileVo.class)))
    @PostMapping("queue-status")
    public Result<MerchantProfileVo> updateQueueStatus(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                         @RequestBody MerchantQueueUpdateBo updateBo) {
        return Result.success(merchantCenterService.updateQueueStatus(userId, updateBo));
    }

    @Operation(summary = "首页推荐Tab选项")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = HomeModuleAppVo.class)))
    @GetMapping("home-tabs")
    public Result<List<HomeModuleAppVo>> homeTabs(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(merchantCenterService.listHomeTabOptions(userId));
    }

    @Operation(summary = "服务列表")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantServiceVo.class)))
    @GetMapping("services")
    public Result<List<MerchantServiceVo>> services(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                    @RequestParam(value = "tab", required = false) String tab) {
        return Result.success(merchantCenterService.listServices(userId, tab));
    }

    @Operation(summary = "服务详情")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantServiceVo.class)))
    @GetMapping("services/{id}")
    public Result<MerchantServiceVo> serviceDetail(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                   @PathVariable("id") Long id) {
        return Result.success(merchantCenterService.getService(userId, id));
    }

    @Operation(summary = "保存服务")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantServiceVo.class)))
    @PostMapping("services/save")
    public Result<MerchantServiceVo> saveService(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                 @RequestBody @Validated MerchantServiceSaveBo saveBo) {
        return Result.success(merchantCenterService.saveService(userId, saveBo));
    }

    @Operation(summary = "服务上下架")
    @Login
    @PostMapping("services/{id}/publish")
    public Result<Void> publishService(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                       @PathVariable("id") Long id,
                                       @RequestBody @Validated MerchantServicePublishBo publishBo) {
        merchantCenterService.publishService(userId, id, publishBo);
        return Result.success();
    }

    @Operation(summary = "删除服务")
    @Login
    @PostMapping("services/{id}/delete")
    public Result<Void> deleteService(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                      @PathVariable("id") Long id) {
        merchantCenterService.deleteService(userId, id);
        return Result.success();
    }

    @Operation(summary = "门店装修")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantStoreDecorVo.class)))
    @GetMapping("store-decor")
    public Result<MerchantStoreDecorVo> storeDecor(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(merchantCenterService.getStoreDecor(userId));
    }

    @Operation(summary = "保存门店装修")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantStoreDecorVo.class)))
    @PostMapping("store-decor")
    public Result<MerchantStoreDecorVo> saveStoreDecor(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                       @RequestBody MerchantStoreDecorSaveBo saveBo) {
        return Result.success(merchantCenterService.saveStoreDecor(userId, saveBo));
    }

    @Operation(summary = "保存门店装修（别名路径）")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = MerchantStoreDecorVo.class)))
    @PostMapping("store-decor/save")
    public Result<MerchantStoreDecorVo> saveStoreDecorAlias(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                            @RequestBody MerchantStoreDecorSaveBo saveBo) {
        return Result.success(merchantCenterService.saveStoreDecor(userId, saveBo));
    }

    @Operation(summary = "客房标准列表")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelRoomStandardVo.class)))
    @GetMapping("room-standards")
    public Result<List<HotelRoomStandardVo>> roomStandards(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId) {
        return Result.success(merchantCenterService.listRoomStandards(userId));
    }

    @Operation(summary = "客房标准详情")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelRoomStandardVo.class)))
    @GetMapping("room-standards/{id}")
    public Result<HotelRoomStandardVo> roomStandardDetail(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                          @PathVariable("id") Long id) {
        return Result.success(merchantCenterService.getRoomStandard(userId, id));
    }

    @Operation(summary = "保存客房标准")
    @Login
    @ApiResponse(content = @Content(schema = @Schema(implementation = HotelRoomStandardVo.class)))
    @PostMapping("room-standards/save")
    public Result<HotelRoomStandardVo> saveRoomStandard(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                                        @RequestBody @Validated HotelRoomStandardSaveBo saveBo) {
        return Result.success(merchantCenterService.saveRoomStandard(userId, saveBo));
    }

    @Operation(summary = "删除客房标准")
    @Login
    @PostMapping("room-standards/{id}/delete")
    public Result<Void> deleteRoomStandard(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                                           @PathVariable("id") Long id) {
        merchantCenterService.deleteRoomStandard(userId, id);
        return Result.success();
    }
}

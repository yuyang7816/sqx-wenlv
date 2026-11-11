package com.sqx.app.lvyou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.messages.support.InstationMessageSupport;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.coupon.domain.entity.UserCouponEntity;
import com.sqx.app.lvyou.coupon.db.mapper.UserCouponMapper;
import com.sqx.app.lvyou.coupon.support.CouponOrderSupport;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.invite.service.InviteService;
import com.sqx.app.lvyou.hotel.db.service.IHotelReviewService;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomService;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelReviewEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomEntity;
import com.sqx.app.lvyou.hotel.support.HotelBookingConstants;
import com.sqx.app.lvyou.hotel.support.HotelCancelPolicySupport;
import com.sqx.app.lvyou.hotel.support.HotelInventorySupport;
import com.sqx.app.lvyou.hotel.support.HotelPriceSnapshotSupport;
import com.sqx.app.lvyou.goose.util.GooseDecorUtil;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantStoreDecorService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantStoreDecorEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.mapper.UserAddressMapper;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.heritage.goods.db.service.IGoodsService;
import com.sqx.app.lvyou.heritage.goods.db.service.IReviewService;
import com.sqx.app.lvyou.heritage.goods.domain.entity.GoodsEntity;
import com.sqx.app.lvyou.heritage.goods.domain.entity.ReviewEntity;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsSpecSkuVo;
import com.sqx.app.lvyou.heritage.enums.HeritageStatus;
import com.sqx.app.lvyou.heritage.goods.util.GoodsJsonUtil;
import com.sqx.app.lvyou.heritage.goods.util.GoodsSpecsUtil;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopShippingVo;
import com.sqx.app.lvyou.heritage.shop.support.HeritageShippingUtil;
import com.sqx.app.lvyou.order.domain.bo.OrderAdminActionBo;
import com.sqx.app.lvyou.order.domain.bo.OrderAdminPageBo;
import com.sqx.app.lvyou.order.domain.bo.OrderAppActionBo;
import com.sqx.app.lvyou.order.domain.bo.OrderCreateBo;
import com.sqx.app.lvyou.order.domain.bo.OrderGoodsCreateBo;
import com.sqx.app.lvyou.order.domain.bo.OrderListBo;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.domain.entity.UserAddressEntity;
import com.sqx.app.lvyou.order.domain.vo.OrderAdminVo;
import com.sqx.app.lvyou.order.domain.vo.OrderAppItemVo;
import com.sqx.app.lvyou.order.domain.vo.OrderAppVo;
import com.sqx.app.lvyou.order.domain.vo.OrderCreateResultVo;
import com.sqx.app.lvyou.order.enums.OrderType;
import com.sqx.app.lvyou.order.service.OrderLifecycleService;
import com.sqx.app.lvyou.order.service.OrderService;
import com.sqx.app.lvyou.order.support.AppOrderStatusSupport;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.app.lvyou.order.support.OrderTypeSupport;
import com.sqx.app.lvyou.order.support.PlatformOrderStatsScope;
import com.sqx.app.lvyou.order.support.OrderStatusMachine;
import com.sqx.app.lvyou.order.support.OrderVerifyCodeSupport;
import org.springframework.beans.BeanUtils;
import com.sqx.app.lvyou.order.domain.bo.OrderGuestBo;
import com.sqx.app.lvyou.order.domain.vo.OrderGuestVo;
import com.sqx.app.lvyou.order.domain.vo.OrderRefundPreviewVo;
import com.sqx.app.lvyou.order.support.OrderGuestInfoSupport;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.app.lvyou.order.support.OrderSettlementSupport;
import com.sqx.app.lvyou.route.db.service.IRouteReviewService;
import com.sqx.app.lvyou.route.db.service.IRouteService;
import com.sqx.app.lvyou.route.domain.entity.RouteEntity;
import com.sqx.app.lvyou.route.domain.entity.RouteReviewEntity;
import com.sqx.app.lvyou.route.support.RouteCancelPolicySupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 统一订单业务服务实现
 */
@Service
public class OrderServiceImpl implements OrderService {

    private static final int MAX_PAGE_LIMIT = 50;
    private static final int ROUTE_DAILY_CAPACITY = 200;
    private static final int ON_SALE = 1;
    private static final int COUPON_UNUSED = 1;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private IHotelService iHotelService;

    @Resource
    private IHotelRoomService iHotelRoomService;

    @Resource
    private HotelInventorySupport hotelInventorySupport;

    @Resource
    private IRouteService iRouteService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantStoreDecorService iMerchantStoreDecorService;

    @Resource
    private IMerchantServiceItemService iMerchantServiceItemService;

    @Resource
    private UserCouponMapper userCouponMapper;

    @Resource
    private CouponOrderSupport couponOrderSupport;

    @Resource
    private UserAddressMapper userAddressMapper;

    @Resource
    private InstationMessageSupport instationMessageSupport;

    @Resource
    private InviteService inviteService;

    @Resource
    private IReviewService iReviewService;

    @Resource
    private IGoodsService iHeritageGoodsService;

    @Resource
    private IHeritageShopService iHeritageShopService;

    @Resource
    private IHotelReviewService iHotelReviewService;

    @Resource
    private OrderLifecycleService orderLifecycleService;

    @Resource
    private RouteCancelPolicySupport routeCancelPolicySupport;

    @Resource
    private OrderSettlementSupport orderSettlementSupport;

    @Resource
    private IRouteReviewService iRouteReviewService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultVo create(Long userId, OrderCreateBo createBo) {
        String category = AppOrderStatusSupport.normalizeCategory(createBo.getCategory());
        if (!Arrays.asList("hotel", "route").contains(category)) {
            throw new BizException(AppError.ORDER_CATEGORY_INVALID);
        }
        UserEntity user = requireUser(userId);
        Long destinationId = resolveDestinationId(user);

        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setDestinationId(destinationId);
        OrderType orderType = OrderType.fromCode(category);
        OrderTypeSupport.apply(order, orderType != null ? orderType : OrderType.HOTEL);
        order.setStatus(OrderStatusMachine.initialStatus());
        order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
        order.setContactName(StringUtils.defaultIfBlank(createBo.getContactName(), user.getUserName()));
        order.setContactPhone(StringUtils.defaultIfBlank(createBo.getContactPhone(), user.getPhone()));
        order.setRemark(StringUtils.trimToNull(createBo.getRemark()));
        order.setGuestCount(createBo.getGuestCount() == null ? 1 : createBo.getGuestCount());
        if ("hotel".equals(category)) {
            order.setRoomCount(createBo.getRoomCount() == null ? 1 : createBo.getRoomCount());
            order.setExpectedArrivalTime(StringUtils.trimToNull(createBo.getExpectedArrivalTime()));
        }
        order.setPayExpireTime(LocalDateTime.now().plusMinutes(HotelBookingConstants.PAY_TIMEOUT_MINUTES));

        OrderItemEntity item;
        BigDecimal totalAmount;
        if ("hotel".equals(category)) {
            item = buildHotelItem(createBo, order);
            totalAmount = item.getTotalPrice();
        } else {
            item = buildRouteItem(createBo, order);
            totalAmount = item.getTotalPrice();
        }

        applyCoupon(order, userId, createBo.getCouponId(), category, totalAmount);
        order.setTotalAmount(totalAmount);
        if (order.getPayAmount() == null) {
            order.setPayAmount(calcPayAmount(totalAmount, order.getDiscountAmount()));
        }
        order.setOrderNo(generateOrderNo(category));

        iOrderService.save(order);
        item.setOrderId(order.getId());
        item.setOrderNo(order.getOrderNo());
        iOrderItemService.save(item);
        couponOrderSupport.consumeCoupon(order, userId);

        return toCreateResult(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderCreateResultVo createGoods(Long userId, OrderGoodsCreateBo createBo) {
        UserEntity user = requireUser(userId);
        Long destinationId = resolveDestinationId(user);
        int quantity = createBo.getQuantity() == null ? 1 : createBo.getQuantity();

        GoodsOrderContext orderContext = resolveGoodsOrderContext(createBo.getGoodsId());
        GoodsEntity heritageGoods = orderContext.getHeritageGoods();
        MerchantServiceEntity merchantService = orderContext.getMerchantService();
        assertGoodsOrderOnSale(orderContext);

        GoodsSpecsUtil.GoodsSpecsPayload specsPayload = resolveAppSpecsPayload(heritageGoods, merchantService);
        String itemSpec = StringUtils.trimToNull(createBo.getItemSpec());
        GoodsSpecSkuVo selectedSku = resolveGoodsSku(specsPayload, itemSpec);
        BigDecimal unitPrice = resolveGoodsUnitPrice(merchantService, specsPayload, selectedSku, heritageGoods);
        int availableStock = resolveGoodsAvailableStock(merchantService, specsPayload, selectedSku, heritageGoods);
        if (availableStock > 0 && availableStock < quantity) {
            throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
        }
        assertClientPrice(createBo.getUnitPrice(), unitPrice);

        MerchantEntity merchant = resolveGoodsOrderMerchant(heritageGoods, merchantService);

        UserAddressEntity address = requireAddress(userId, createBo.getAddressId());
        BigDecimal goodsAmount = unitPrice.multiply(BigDecimal.valueOf(quantity)).setScale(2, RoundingMode.HALF_UP);
        HeritageShopShippingVo shippingConfig = resolveShopShippingForOrder(heritageGoods, merchantService);
        BigDecimal shippingFee = HeritageShippingUtil.calculateFee(shippingConfig, goodsAmount);
        BigDecimal orderAmount = goodsAmount.add(shippingFee).setScale(2, RoundingMode.HALF_UP);

        OrderEntity order = new OrderEntity();
        order.setUserId(userId);
        order.setMerchantId(merchant.getId());
        order.setMerchantName(merchant.getStoreName());
        order.setDestinationId(destinationId);
        OrderTypeSupport.apply(order, OrderType.CULTURE);
        order.setStatus(OrderStatusMachine.initialStatus());
        order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverAddress(formatAddress(address));
        order.setContactName(address.getReceiverName());
        order.setContactPhone(address.getReceiverPhone());
        order.setRemark(StringUtils.trimToNull(createBo.getRemark()));
        order.setTotalAmount(orderAmount);
        order.setPayExpireTime(LocalDateTime.now().plusMinutes(
                com.sqx.app.lvyou.hotel.support.HotelBookingConstants.PAY_TIMEOUT_MINUTES));

        applyCoupon(order, userId, createBo.getCouponId(), "goods", goodsAmount);
        order.setPayAmount(calcPayAmount(orderAmount, order.getDiscountAmount()));
        order.setOrderNo(generateOrderNo("culture"));

        if (availableStock > 0) {
            deductOrderGoodsStock(heritageGoods, merchantService, specsPayload, itemSpec, quantity);
        }

        iOrderService.save(order);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(order.getId());
        item.setOrderNo(order.getOrderNo());
        item.setItemType("goods");
        item.setItemId(heritageGoods != null ? heritageGoods.getId() : merchantService.getId());
        item.setItemName(heritageGoods != null ? heritageGoods.getName() : merchantService.getName());
        item.setItemSpec(StringUtils.defaultIfBlank(itemSpec,
                heritageGoods != null ? heritageGoods.getDescription() : merchantService.getDescription()));
        item.setCoverImage(heritageGoods != null ? heritageGoods.getCoverImage() : merchantService.getCoverImage());
        item.setQuantity(quantity);
        item.setUnitPrice(unitPrice);
        item.setTotalPrice(goodsAmount);
        iOrderItemService.save(item);
        couponOrderSupport.consumeCoupon(order, userId);

        return toCreateResult(order);
    }

    private HeritageShopShippingVo resolveShopShippingConfig(Long merchantServiceId) {
        if (merchantServiceId == null) {
            return HeritageShippingUtil.defaultConfig();
        }
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsEntity::getMerchantServiceId, merchantServiceId)
                .last("LIMIT 1");
        GoodsEntity heritageGoods = iHeritageGoodsService.getOne(queryWrapper, false);
        if (heritageGoods == null || heritageGoods.getShopId() == null) {
            return HeritageShippingUtil.defaultConfig();
        }
        HeritageShopEntity shop = iHeritageShopService.getById(heritageGoods.getShopId());
        if (shop == null) {
            return HeritageShippingUtil.defaultConfig();
        }
        return HeritageShippingUtil.parse(shop.getShippingConfig());
    }

    @Override
    public IPage<OrderAppVo> appPage(Long userId, OrderListBo listBo) {
        Page<OrderEntity> page = initBoundedPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getUserId, userId)
                .like(StringUtils.isNotBlank(listBo.getOrderNo()), OrderEntity::getOrderNo, listBo.getOrderNo())
                .orderByDesc(OrderEntity::getCreateTime)
                .orderByDesc(OrderEntity::getId);

        applyListCategoryFilter(queryWrapper, listBo.getCategory());
        applyUserListStatusFilter(queryWrapper, listBo);

        IPage<OrderEntity> entityPage = iOrderService.page(page, queryWrapper);
        return toAppPage(entityPage, userId);
    }

    @Override
    public OrderAppVo appDetail(Long userId, String orderKey) {
        OrderEntity order = refreshHotelLifecycle(requireOwnedOrder(userId, orderKey));
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        Set<Long> reviewedOrderIds = loadReviewedOrderIds(userId, Collections.singletonList(order.getId()));
        OrderAppVo vo = toAppVo(order, items, reviewedOrderIds);
        fillOrderDetailExtras(vo, order, items);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAppVo cancel(Long userId, String orderKey) {
        OrderEntity order = requireOwnedOrder(userId, orderKey);
        if (AppOrderStatusSupport.isHotelCheckedIn(order)) {
            throw new BizException(AppError.ORDER_CANCEL_NOT_ALLOWED);
        }
        if ("hotel".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))
                && Objects.equals(order.getStatus(), MerchantOrderStatusGroup.PENDING_USE)) {
            if (!HotelCancelPolicySupport.isFreeCancelAllowed(order, LocalDateTime.now())) {
                throw new BizException(AppError.ORDER_CANCEL_NOT_ALLOWED);
            }
            order.setStatus(OrderStatusMachine.toCancelledStatus());
            order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
            order.setRemark(appendRemark(order.getRemark(), "用户免费取消"));
            iOrderService.updateById(order);
            orderSettlementSupport.settleOnHotelCancelRefund(order);
            instationMessageSupport.sendUserMessage(userId, "订单已取消",
                    "您的订单 " + order.getOrderNo() + " 已取消并退款", order.getId());
        } else if ("route".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            cancelRouteOrder(userId, order);
        } else {
            OrderStatusMachine.assertUserCancelAllowed(order.getStatus());
            order.setStatus(OrderStatusMachine.toCancelledStatus());
            order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
            iOrderService.updateById(order);
            instationMessageSupport.sendUserMessage(userId, "订单已取消",
                    "您的订单 " + order.getOrderNo() + " 已取消", order.getId());
            restoreGoodsStockIfNeeded(order);
        }
        couponOrderSupport.restoreCouponIfNeeded(order);
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        Set<Long> reviewedOrderIds = loadReviewedOrderIds(userId, Collections.singletonList(order.getId()));
        return toAppVo(order, items, reviewedOrderIds);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAppVo appAction(Long userId, String orderKey, OrderAppActionBo actionBo) {
        OrderEntity order = refreshHotelLifecycle(requireOwnedOrder(userId, orderKey));
        String action = normalizeAdminAction(actionBo.getAction());
        switch (action) {
            case "complete":
                OrderStatusMachine.assertUserCompleteAllowed(order.getStatus(), order.getOrderCategory());
                order.setStatus(OrderStatusMachine.toCompletedStatus());
                break;
            case "refund":
                assertOnlinePayRefundAllowed(order);
                OrderStatusMachine.assertUserRefundAllowed(order.getStatus());
                if (OrderTypeSupport.isRouteFlow(OrderTypeSupport.resolve(order))) {
                    assertRouteUserRefundAllowed(order);
                } else {
                    assertHotelUserRefundAllowed(order);
                }
                order.setStatus(OrderStatusMachine.toRefundingStatus());
                if (StringUtils.isNotBlank(actionBo.getReason())) {
                    order.setRemark(appendRemark(order.getRemark(), "用户申请退款：" + StringUtils.trim(actionBo.getReason())));
                }
                break;
            default:
                throw new BizException(AppError.MERCHANT_ORDER_ACTION_INVALID);
        }
        order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
        if ("complete".equals(action)) {
            if (MerchantOrderStatusSupport.isGoodsCategory(order.getOrderCategory())) {
                orderSettlementSupport.settleOnGoodsComplete(order);
            } else {
                iOrderService.updateById(order);
            }
            inviteService.settleCommissionOnOrderCompleted(order);
        } else {
            iOrderService.updateById(order);
        }
        sendUserActionMessage(order, action);
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        Set<Long> reviewedOrderIds = loadReviewedOrderIds(userId, Collections.singletonList(order.getId()));
        return toAppVo(order, items, reviewedOrderIds);
    }

    @Override
    public IPage<OrderAdminVo> adminPage(OrderAdminPageBo pageBo) {
        Page<OrderEntity> page = initBoundedPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        PlatformOrderStatsScope.apply(queryWrapper);
        queryWrapper.eq(Objects.nonNull(pageBo.getId()), OrderEntity::getId, pageBo.getId())
                .like(StringUtils.isNotBlank(pageBo.getOrderNo()), OrderEntity::getOrderNo, pageBo.getOrderNo())
                .like(StringUtils.isNotBlank(pageBo.getContactPhone()), OrderEntity::getContactPhone, pageBo.getContactPhone())
                .eq(pageBo.getMerchantId() != null, OrderEntity::getMerchantId, pageBo.getMerchantId())
                .like(pageBo.getMerchantId() == null && StringUtils.isNotBlank(pageBo.getMerchantName()),
                        OrderEntity::getMerchantName, pageBo.getMerchantName())
                .eq(pageBo.getDestinationId() != null, OrderEntity::getDestinationId, pageBo.getDestinationId())
                .orderByDesc(OrderEntity::getCreateTime)
                .orderByDesc(OrderEntity::getId);

        applyListCategoryFilter(queryWrapper, pageBo.getCategory());
        List<Integer> statusFilter = AppOrderStatusSupport.resolveStatusFilter(pageBo.getStatus());
        if (CollectionUtils.isNotEmpty(statusFilter)) {
            queryWrapper.in(OrderEntity::getStatus, statusFilter);
        }

        IPage<OrderEntity> entityPage = iOrderService.page(page, queryWrapper);
        List<Long> userIds = entityPage.getRecords().stream()
                .map(OrderEntity::getUserId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        Map<Long, UserEntity> userMap = loadUserMap(userIds);

        List<Long> orderIds = entityPage.getRecords().stream().map(OrderEntity::getId).collect(Collectors.toList());
        Map<Long, List<OrderItemEntity>> itemMap = iOrderItemService.mapByOrderIds(orderIds);

        List<OrderAdminVo> voList = new ArrayList<>();
        for (OrderEntity record : entityPage.getRecords()) {
            voList.add(toAdminVo(record, itemMap.getOrDefault(record.getId(), Collections.emptyList()), userMap));
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public OrderAdminVo adminDetail(Long orderId) {
        OrderEntity order = iOrderService.getById(orderId);
        if (order == null) {
            throw new BizException(AppError.ORDER_NOT_FOUND);
        }
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        Map<Long, UserEntity> userMap = loadUserMap(Collections.singletonList(order.getUserId()));
        return toAdminVo(order, items, userMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderAdminVo adminAction(OrderAdminActionBo actionBo) {
        OrderEntity order = iOrderService.getById(actionBo.getOrderId());
        if (order == null) {
            throw new BizException(AppError.ORDER_NOT_FOUND);
        }
        String action = normalizeAdminAction(actionBo.getAction());
        switch (action) {
            case "cancel":
                OrderStatusMachine.assertAdminCancelAllowed(order.getStatus());
                order.setStatus(OrderStatusMachine.toCancelledStatus());
                restoreGoodsStockIfNeeded(order);
                couponOrderSupport.restoreCouponIfNeeded(order);
                refundPaidOrderOnCancel(order);
                break;
            case "complete":
                OrderStatusMachine.assertAdminCompleteAllowed(order.getStatus());
                order.setStatus(OrderStatusMachine.toCompletedStatus());
                break;
            case "refund":
                assertOnlinePayRefundAllowed(order);
                if (Objects.equals(order.getStatus(), MerchantOrderStatusGroup.CANCELLED)) {
                    orderSettlementSupport.refundPaidOrderToUser(order, "平台补退");
                    break;
                }
                if (OrderTypeSupport.isRouteFlow(OrderTypeSupport.resolve(order))) {
                    executeAdminRouteForceRefund(order, actionBo);
                } else {
                    OrderStatusMachine.assertAdminRefundAllowed(order.getStatus());
                    order.setStatus(OrderStatusMachine.toRefundingStatus());
                }
                break;
            default:
                throw new BizException(AppError.MERCHANT_ORDER_ACTION_INVALID);
        }
        order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
        if (StringUtils.isNotBlank(actionBo.getReason())) {
            order.setRemark(appendRemark(order.getRemark(), "运营操作：" + StringUtils.trim(actionBo.getReason())));
        }
        if ("complete".equals(action)) {
            if (MerchantOrderStatusSupport.isGoodsCategory(order.getOrderCategory())) {
                orderSettlementSupport.settleOnGoodsComplete(order);
            } else {
                iOrderService.updateById(order);
            }
            inviteService.settleCommissionOnOrderCompleted(order);
        } else {
            iOrderService.updateById(order);
        }
        sendAdminActionMessage(order, action);
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        Map<Long, UserEntity> userMap = loadUserMap(Collections.singletonList(order.getUserId()));
        return toAdminVo(order, items, userMap);
    }

    private OrderItemEntity buildHotelItem(OrderCreateBo createBo, OrderEntity order) {
        if (createBo.getHotelId() == null || createBo.getRoomId() == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        LocalDate checkIn = parseDate(createBo.getCheckInDate(), "入住日期");
        LocalDate checkOut = parseDate(createBo.getCheckOutDate(), "离店日期");
        if (!checkOut.isAfter(checkIn)) {
            throw new BizException(AppError.ORDER_DATE_INVALID);
        }
        int roomCount = order.getRoomCount() == null || order.getRoomCount() <= 0 ? 1 : order.getRoomCount();

        HotelRoomEntity room = iHotelRoomService.getById(createBo.getRoomId());
        if (room == null || !Objects.equals(room.getHotelId(), createBo.getHotelId())) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        if (room.getStatus() == null || room.getStatus() != ON_SALE) {
            throw new BizException(AppError.ORDER_ITEM_OFF_SALE);
        }

        HotelInventorySupport.QuoteResult quote =
                hotelInventorySupport.quote(room, checkIn, checkOut, roomCount);
        if (createBo.getUnitPrice() != null) {
            assertClientPrice(createBo.getUnitPrice(), quote.averageNightlyPrice());
        }

        HotelEntity hotel = iHotelService.getById(createBo.getHotelId());
        if (hotel == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        MerchantEntity merchant = iMerchantService.getById(hotel.getMerchantId());
        if (merchant == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }

        order.setMerchantId(merchant.getId());
        order.setMerchantName(StringUtils.defaultIfBlank(hotel.getName(), merchant.getStoreName()));
        order.setCheckInDate(checkIn);
        order.setCheckOutDate(checkOut);
        order.setNights(quote.getNights());
        order.setRoomCount(roomCount);
        order.setGuestNames(joinGuestNames(createBo.getGuestNames()));
        order.setCancelPolicyJson(HotelCancelPolicySupport.buildSnapshotJson(hotel, room));

        BigDecimal totalAmount = quote.getTotalAmount();
        order.setPriceSnapshotJson(HotelPriceSnapshotSupport.buildJson(
                hotel, room, checkIn, checkOut, quote.getNights(), roomCount,
                quote.getDailyPrices(), totalAmount, totalAmount));

        OrderItemEntity item = new OrderItemEntity();
        item.setItemType("room");
        item.setItemId(room.getId());
        item.setItemName(room.getName());
        item.setItemSpec(room.getSpec());
        item.setCoverImage(room.getCoverImage());
        item.setQuantity(quote.getNights());
        item.setUnitPrice(quote.averageNightlyPrice());
        item.setTotalPrice(totalAmount);
        return item;
    }

    private String appendRemark(String original, String extra) {
        if (StringUtils.isBlank(original)) {
            return extra;
        }
        return original + "；" + extra;
    }

    private OrderItemEntity buildRouteItem(OrderCreateBo createBo, OrderEntity order) {
        if (createBo.getRouteId() == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        LocalDate travelDate = parseDate(createBo.getTravelDate(), "出行日期");
        int guestCount = order.getGuestCount() == null ? 1 : order.getGuestCount();
        assertRouteInventory(createBo.getRouteId(), travelDate, guestCount);

        RouteEntity route = iRouteService.getById(createBo.getRouteId());
        if (route == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        if (route.getStatus() == null || route.getStatus() != ON_SALE) {
            throw new BizException(AppError.ORDER_ITEM_OFF_SALE);
        }
        assertClientPrice(createBo.getUnitPrice(), route.getPriceFrom());

        MerchantEntity merchant = iMerchantService.getById(route.getMerchantId());
        if (merchant == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }

        order.setMerchantId(merchant.getId());
        order.setMerchantName(merchant.getStoreName());
        order.setTravelDate(travelDate);
        order.setMeetPoint(route.getMeetPoint());
        order.setCancelPolicyJson(routeCancelPolicySupport.buildSnapshotJson());
        applyRouteGuestInfo(createBo, order, guestCount);

        BigDecimal unitPrice = defaultDecimal(route.getPriceFrom());
        BigDecimal totalAmount = unitPrice.multiply(BigDecimal.valueOf(guestCount)).setScale(2, RoundingMode.HALF_UP);

        OrderItemEntity item = new OrderItemEntity();
        item.setItemType("route");
        item.setItemId(route.getId());
        item.setItemName(route.getName());
        item.setItemSpec(route.getDuration());
        item.setCoverImage(route.getCoverImage());
        item.setQuantity(guestCount);
        item.setUnitPrice(unitPrice);
        item.setTotalPrice(totalAmount);
        return item;
    }

    private void assertRouteInventory(Long routeId, LocalDate travelDate, int guestCount) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getOrderCategory, "route")
                .eq(OrderEntity::getTravelDate, travelDate)
                .notIn(OrderEntity::getStatus, MerchantOrderStatusGroup.CANCELLED);
        List<OrderEntity> orders = iOrderService.list(queryWrapper);
        int booked = orders.stream()
                .filter(order -> hasRouteItem(order.getId(), routeId))
                .mapToInt(order -> order.getGuestCount() == null ? 1 : order.getGuestCount())
                .sum();
        if (booked + guestCount > ROUTE_DAILY_CAPACITY) {
            throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
        }
    }

    private boolean hasRouteItem(Long orderId, Long routeId) {
        LambdaQueryWrapper<OrderItemEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderItemEntity::getOrderId, orderId)
                .eq(OrderItemEntity::getItemType, "route")
                .eq(OrderItemEntity::getItemId, routeId)
                .last("LIMIT 1");
        return iOrderItemService.count(queryWrapper) > 0;
    }

    private boolean datesOverlap(LocalDate in1, LocalDate out1, LocalDate in2, LocalDate out2) {
        return in1.isBefore(out2) && in2.isBefore(out1);
    }

    private void applyCoupon(OrderEntity order, Long userId, Long couponId, String category, BigDecimal totalAmount) {
        order.setDiscountAmount(BigDecimal.ZERO);
        if (couponId == null) {
            return;
        }
        UserCouponEntity coupon = userCouponMapper.selectById(couponId);
        if (coupon == null || !Objects.equals(coupon.getUserId(), userId)) {
            throw new BizException(AppError.ORDER_COUPON_INVALID);
        }
        if (coupon.getStatus() == null || coupon.getStatus() != COUPON_UNUSED) {
            throw new BizException(AppError.ORDER_COUPON_INVALID);
        }
        LocalDateTime now = LocalDateTime.now();
        if (coupon.getValidStart() != null && now.isBefore(coupon.getValidStart())) {
            throw new BizException(AppError.ORDER_COUPON_INVALID);
        }
        if (coupon.getValidEnd() != null && now.isAfter(coupon.getValidEnd())) {
            throw new BizException(AppError.ORDER_COUPON_INVALID);
        }
        if (!couponScopeMatch(coupon.getScope(), category)) {
            throw new BizException(AppError.ORDER_COUPON_INVALID);
        }
        BigDecimal minAmount = defaultDecimal(coupon.getMinAmount());
        if (totalAmount.compareTo(minAmount) < 0) {
            throw new BizException(AppError.ORDER_COUPON_INVALID);
        }
        BigDecimal discount = defaultDecimal(coupon.getAmount());
        if (discount.compareTo(totalAmount) > 0) {
            discount = totalAmount;
        }
        order.setCouponId(couponId);
        order.setDiscountAmount(discount);
        order.setPayAmount(calcPayAmount(totalAmount, discount));
    }

    private boolean couponScopeMatch(String scope, String category) {
        if (StringUtils.isBlank(scope) || "all".equalsIgnoreCase(scope)) {
            return true;
        }
        return scope.equalsIgnoreCase(category);
    }

    private BigDecimal calcPayAmount(BigDecimal totalAmount, BigDecimal discountAmount) {
        BigDecimal discount = discountAmount == null ? BigDecimal.ZERO : discountAmount;
        BigDecimal pay = totalAmount.subtract(discount);
        if (pay.compareTo(BigDecimal.ZERO) < 0) {
            pay = BigDecimal.ZERO;
        }
        return pay.setScale(2, RoundingMode.HALF_UP);
    }

    private void restoreGoodsStockIfNeeded(OrderEntity order) {
        if (!MerchantOrderStatusSupport.isGoodsCategory(order.getOrderCategory())) {
            return;
        }
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        for (OrderItemEntity item : items) {
            if (!"goods".equals(item.getItemType()) || item.getItemId() == null) {
                continue;
            }
            int quantity = item.getQuantity() == null ? 1 : item.getQuantity();
            MerchantServiceEntity service = iMerchantServiceItemService.getById(item.getItemId());
            if (service == null) {
                syncHeritageGoodsStock(item.getItemId(), quantity);
                continue;
            }
            GoodsSpecsUtil.GoodsSpecsPayload specsPayload = GoodsSpecsUtil.parseMerchantRaw(
                    service.getGoodsSpecs(), service.getPrice(), service.getStock());
            if (specsPayload.isMulti()) {
                GoodsSpecsUtil.restoreSkuStock(specsPayload, item.getItemSpec(), quantity);
                service.setGoodsSpecs(GoodsSpecsUtil.serializeMerchantPayload(specsPayload));
                service.setStock(GoodsSpecsUtil.totalStock(specsPayload.getSkus(), service.getStock()));
                iMerchantServiceItemService.updateById(service);
                syncHeritageGoodsSpecs(service);
            } else {
                LambdaUpdateWrapper<MerchantServiceEntity> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(MerchantServiceEntity::getId, item.getItemId())
                        .setSql("stock = stock + " + quantity);
                iMerchantServiceItemService.update(updateWrapper);
                syncHeritageGoodsStock(item.getItemId(), quantity);
            }
        }
    }

    /**
     * C 端 goodsId 优先解析为 tb_heritage_goods.id，避免与 tb_merchant_service.id 冲突。
     */
    private GoodsOrderContext resolveGoodsOrderContext(Long goodsId) {
        if (goodsId == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        GoodsEntity heritageGoods = iHeritageGoodsService.getById(goodsId);
        if (heritageGoods != null) {
            MerchantServiceEntity merchantService = null;
            if (heritageGoods.getMerchantServiceId() != null) {
                merchantService = iMerchantServiceItemService.getById(heritageGoods.getMerchantServiceId());
            }
            return new GoodsOrderContext(heritageGoods, merchantService);
        }
        MerchantServiceEntity merchantService = iMerchantServiceItemService.getById(goodsId);
        if (merchantService == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsEntity::getMerchantServiceId, merchantService.getId())
                .last("LIMIT 1");
        heritageGoods = iHeritageGoodsService.getOne(queryWrapper, false);
        return new GoodsOrderContext(heritageGoods, merchantService);
    }

    private void assertGoodsOrderOnSale(GoodsOrderContext orderContext) {
        GoodsEntity heritageGoods = orderContext.getHeritageGoods();
        if (heritageGoods != null) {
            if (!Objects.equals(heritageGoods.getStatus(), HeritageStatus.ONLINE.getCode())) {
                throw new BizException(AppError.ORDER_ITEM_OFF_SALE);
            }
            return;
        }
        MerchantServiceEntity merchantService = orderContext.getMerchantService();
        if (merchantService != null
                && !"on_sale".equalsIgnoreCase(StringUtils.defaultString(merchantService.getStatus()))) {
            throw new BizException(AppError.ORDER_ITEM_OFF_SALE);
        }
    }

    private MerchantEntity resolveGoodsOrderMerchant(GoodsEntity heritageGoods, MerchantServiceEntity merchantService) {
        LinkedHashSet<Long> merchantIds = new LinkedHashSet<>();
        if (merchantService != null && merchantService.getMerchantId() != null) {
            merchantIds.add(merchantService.getMerchantId());
        }
        if (heritageGoods != null && heritageGoods.getShopId() != null) {
            HeritageShopEntity shop = iHeritageShopService.getById(heritageGoods.getShopId());
            if (shop != null && shop.getMerchantId() != null) {
                merchantIds.add(shop.getMerchantId());
            }
        }
        for (Long merchantId : merchantIds) {
            MerchantEntity merchant = iMerchantService.getById(merchantId);
            if (merchant != null) {
                return merchant;
            }
        }
        throw new BizException(AppError.MERCHANT_NOT_FOUND);
    }

    private HeritageShopShippingVo resolveShopShippingForOrder(GoodsEntity heritageGoods,
                                                               MerchantServiceEntity merchantService) {
        if (merchantService != null && merchantService.getId() != null) {
            HeritageShopShippingVo byService = resolveShopShippingConfig(merchantService.getId());
            if (byService != null) {
                return byService;
            }
        }
        if (heritageGoods != null && heritageGoods.getShopId() != null) {
            HeritageShopEntity shop = iHeritageShopService.getById(heritageGoods.getShopId());
            if (shop != null) {
                return HeritageShippingUtil.parse(shop.getShippingConfig());
            }
        }
        return HeritageShippingUtil.defaultConfig();
    }

    private void deductOrderGoodsStock(GoodsEntity heritageGoods,
                                       MerchantServiceEntity merchantService,
                                       GoodsSpecsUtil.GoodsSpecsPayload specsPayload,
                                       String itemSpec,
                                       int quantity) {
        if (merchantService != null) {
            deductGoodsStock(merchantService, specsPayload, null, itemSpec, quantity);
            return;
        }
        if (heritageGoods == null) {
            throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
        }
        if (specsPayload != null && specsPayload.isMulti()) {
            if (!GoodsSpecsUtil.deductSkuStock(specsPayload, itemSpec, quantity)) {
                throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
            }
            heritageGoods.setSpecs(GoodsSpecsUtil.skusToJson(specsPayload.getSkus()));
            heritageGoods.setStock(GoodsSpecsUtil.totalStock(specsPayload.getSkus(), heritageGoods.getStock()));
            heritageGoods.setPrice(GoodsSpecsUtil.minPrice(specsPayload.getSkus(), heritageGoods.getPrice()));
            iHeritageGoodsService.updateById(heritageGoods);
            return;
        }
        LambdaUpdateWrapper<GoodsEntity> stockUpdate = new LambdaUpdateWrapper<>();
        stockUpdate.eq(GoodsEntity::getId, heritageGoods.getId())
                .ge(GoodsEntity::getStock, quantity)
                .setSql("stock = stock - " + quantity);
        if (!iHeritageGoodsService.update(stockUpdate)) {
            throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
        }
    }

    private static final class GoodsOrderContext {
        private final GoodsEntity heritageGoods;
        private final MerchantServiceEntity merchantService;

        private GoodsOrderContext(GoodsEntity heritageGoods, MerchantServiceEntity merchantService) {
            this.heritageGoods = heritageGoods;
            this.merchantService = merchantService;
        }

        private GoodsEntity getHeritageGoods() {
            return heritageGoods;
        }

        private MerchantServiceEntity getMerchantService() {
            return merchantService;
        }
    }

    private GoodsSpecsUtil.GoodsSpecsPayload resolveAppSpecsPayload(GoodsEntity heritageGoods,
                                                                    MerchantServiceEntity merchant) {
        if (heritageGoods != null) {
            List<GoodsSpecSkuVo> skus = GoodsJsonUtil.parseSpecSkus(
                    heritageGoods.getSpecs(), heritageGoods.getPrice(), heritageGoods.getStock());
            GoodsSpecsUtil.GoodsSpecsPayload payload = new GoodsSpecsUtil.GoodsSpecsPayload();
            payload.setSkus(skus == null ? new ArrayList<>() : skus);
            payload.setMode(CollectionUtils.size(payload.getSkus()) > 1
                    ? GoodsSpecsUtil.MODE_MULTI
                    : GoodsSpecsUtil.MODE_SINGLE);
            return payload;
        }
        if (merchant == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        return GoodsSpecsUtil.parseMerchantRaw(
                merchant.getGoodsSpecs(), merchant.getPrice(), merchant.getStock());
    }

    private GoodsSpecSkuVo resolveGoodsSku(GoodsSpecsUtil.GoodsSpecsPayload specsPayload, String itemSpec) {
        if (specsPayload == null || !specsPayload.isMulti()) {
            return null;
        }
        GoodsSpecSkuVo sku = GoodsSpecsUtil.findByName(specsPayload.getSkus(), itemSpec);
        if (sku == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        return sku;
    }

    private BigDecimal resolveGoodsUnitPrice(MerchantServiceEntity goods,
                                             GoodsSpecsUtil.GoodsSpecsPayload specsPayload,
                                             GoodsSpecSkuVo selectedSku,
                                             GoodsEntity heritageGoods) {
        if (specsPayload != null && specsPayload.isMulti()) {
            return defaultDecimal(selectedSku.getPrice());
        }
        if (heritageGoods != null) {
            return defaultDecimal(heritageGoods.getPrice());
        }
        if (goods == null) {
            throw new BizException(AppError.ORDER_ITEM_NOT_FOUND);
        }
        return defaultDecimal(goods.getPrice());
    }

    private int resolveGoodsAvailableStock(MerchantServiceEntity goods,
                                           GoodsSpecsUtil.GoodsSpecsPayload specsPayload,
                                           GoodsSpecSkuVo selectedSku,
                                           GoodsEntity heritageGoods) {
        if (specsPayload != null && specsPayload.isMulti()) {
            return selectedSku.getStock() == null ? 0 : selectedSku.getStock();
        }
        if (heritageGoods != null && heritageGoods.getStock() != null) {
            return heritageGoods.getStock();
        }
        if (goods == null) {
            return 0;
        }
        return goods.getStock() == null ? 0 : goods.getStock();
    }

    private void deductGoodsStock(MerchantServiceEntity goods,
                                  GoodsSpecsUtil.GoodsSpecsPayload specsPayload,
                                  GoodsSpecSkuVo selectedSku,
                                  String itemSpec,
                                  int quantity) {
        if (specsPayload != null && specsPayload.isMulti()) {
            if (!GoodsSpecsUtil.deductSkuStock(specsPayload, itemSpec, quantity)) {
                throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
            }
            goods.setGoodsSpecs(GoodsSpecsUtil.serializeMerchantPayload(specsPayload));
            goods.setStock(GoodsSpecsUtil.totalStock(specsPayload.getSkus(), goods.getStock()));
            goods.setPrice(GoodsSpecsUtil.minPrice(specsPayload.getSkus(), goods.getPrice()));
            iMerchantServiceItemService.updateById(goods);
            syncHeritageGoodsSpecs(goods);
            return;
        }
        LambdaUpdateWrapper<MerchantServiceEntity> stockUpdate = new LambdaUpdateWrapper<>();
        stockUpdate.eq(MerchantServiceEntity::getId, goods.getId())
                .ge(MerchantServiceEntity::getStock, quantity)
                .setSql("stock = stock - " + quantity);
        if (!iMerchantServiceItemService.update(stockUpdate)) {
            throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
        }
        syncHeritageGoodsStock(goods.getId(), -quantity);
    }

    private void syncHeritageGoodsSpecs(MerchantServiceEntity service) {
        if (service == null || service.getId() == null) {
            return;
        }
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsEntity::getMerchantServiceId, service.getId()).last("LIMIT 1");
        GoodsEntity heritageGoods = iHeritageGoodsService.getOne(queryWrapper, false);
        if (heritageGoods == null) {
            return;
        }
        GoodsSpecsUtil.GoodsSpecsPayload specsPayload = GoodsSpecsUtil.parseMerchantRaw(
                service.getGoodsSpecs(), service.getPrice(), service.getStock());
        if (specsPayload.isMulti()) {
            heritageGoods.setSpecs(GoodsSpecsUtil.skusToJson(specsPayload.getSkus()));
            heritageGoods.setStock(GoodsSpecsUtil.totalStock(specsPayload.getSkus(), service.getStock()));
            heritageGoods.setPrice(GoodsSpecsUtil.minPrice(specsPayload.getSkus(), service.getPrice()));
        } else {
            heritageGoods.setStock(service.getStock());
            heritageGoods.setPrice(service.getPrice());
        }
        iHeritageGoodsService.updateById(heritageGoods);
    }

    private void syncHeritageGoodsStock(Long merchantServiceId, int delta) {
        if (merchantServiceId == null || delta == 0) {
            return;
        }
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsEntity::getMerchantServiceId, merchantServiceId)
                .last("LIMIT 1");
        GoodsEntity heritageGoods = iHeritageGoodsService.getOne(queryWrapper, false);
        if (heritageGoods == null) {
            return;
        }
        int quantity = Math.abs(delta);
        LambdaUpdateWrapper<GoodsEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(GoodsEntity::getId, heritageGoods.getId());
        if (delta < 0) {
            updateWrapper.ge(GoodsEntity::getStock, quantity)
                    .setSql("stock = stock - " + quantity);
        } else {
            updateWrapper.setSql("stock = stock + " + quantity);
        }
        iHeritageGoodsService.update(updateWrapper);
    }

    private OrderEntity requireOwnedOrder(Long userId, String orderKey) {
        OrderEntity order = findOrderByKey(orderKey);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(AppError.ORDER_NOT_FOUND);
        }
        return order;
    }

    private OrderEntity findOrderByKey(String orderKey) {
        if (StringUtils.isBlank(orderKey)) {
            return null;
        }
        if (StringUtils.isNumeric(orderKey)) {
            OrderEntity byId = iOrderService.getById(Long.valueOf(orderKey));
            if (byId != null) {
                return byId;
            }
        }
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getOrderNo, orderKey).last("LIMIT 1");
        return iOrderService.getOne(queryWrapper, false);
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        return user;
    }

    private Long resolveDestinationId(UserEntity user) {
        if (user.getCurrentDestinationId() != null) {
            DestinationEntity destination = iDestinationService.getById(user.getCurrentDestinationId());
            if (destination != null) {
                return destination.getId();
            }
        }
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getIsDefault, 1).last("LIMIT 1");
        DestinationEntity defaultDestination = iDestinationService.getOne(queryWrapper, false);
        if (defaultDestination != null) {
            return defaultDestination.getId();
        }
        throw new BizException(AppError.DESTINATION_NOT_FOUND);
    }

    private UserAddressEntity requireAddress(Long userId, Long addressId) {
        UserAddressEntity address = userAddressMapper.selectById(addressId);
        if (address == null || !Objects.equals(address.getUserId(), userId)) {
            throw new BizException(AppError.ORDER_ADDRESS_NOT_FOUND);
        }
        return address;
    }

    private String formatAddress(UserAddressEntity address) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, address.getProvince());
        appendPart(builder, address.getCity());
        appendPart(builder, address.getDistrict());
        appendPart(builder, address.getDetailAddress());
        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String part) {
        if (StringUtils.isBlank(part)) {
            return;
        }
        builder.append(part);
    }

    private IPage<OrderAppVo> toAppPage(IPage<OrderEntity> entityPage, Long userId) {
        List<Long> orderIds = entityPage.getRecords().stream().map(OrderEntity::getId).collect(Collectors.toList());
        Map<Long, List<OrderItemEntity>> itemMap = iOrderItemService.mapByOrderIds(orderIds);
        Set<Long> reviewedOrderIds = loadReviewedOrderIds(userId, orderIds);
        List<OrderAppVo> voList = new ArrayList<>();
        for (OrderEntity record : entityPage.getRecords()) {
            voList.add(toAppVo(record, itemMap.getOrDefault(record.getId(), Collections.emptyList()), reviewedOrderIds));
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    private OrderAppVo toAppVo(OrderEntity order, List<OrderItemEntity> items, Set<Long> reviewedOrderIds) {
        OrderAppVo vo = new OrderAppVo();
        OrderEntity displayOrder = resolveDisplayOrder(order, items);
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        OrderType resolvedType = OrderTypeSupport.resolve(displayOrder, items);
        vo.setOrderType(resolvedType.getCode());
        vo.setOrderTypeLabel(OrderTypeSupport.labelOf(resolvedType));
        vo.setUserTabType(OrderTypeSupport.userTabType(resolvedType));
        vo.setOrderCategory(MerchantOrderStatusSupport.normalizeCategory(resolvedType.getCode()));
        String apiStatus = AppOrderStatusSupport.toApiStatus(displayOrder);
        vo.setStatusCode(apiStatus);
        vo.setStatus(AppOrderStatusSupport.toDisplayStatus(displayOrder));
        vo.setStatusColor(AppOrderStatusSupport.statusColor(vo.getUserTabType(), apiStatus));
        vo.setMerchantId(order.getMerchantId());
        vo.setMerchantName(order.getMerchantName());
        vo.setAmount(defaultDecimal(order.getPayAmount()));
        vo.setTotalAmount(defaultDecimal(order.getTotalAmount()));
        vo.setDiscountAmount(defaultDecimal(order.getDiscountAmount()));
        vo.setDate(formatDateTime(order.getCreateTime()));
        vo.setPayMethod(order.getPayMethod());
        vo.setContactName(StringUtils.defaultIfBlank(order.getContactName(), order.getReceiverName()));
        vo.setPhone(StringUtils.defaultIfBlank(order.getContactPhone(), order.getReceiverPhone()));
        vo.setRemark(order.getRemark());
        vo.setDestinationId(order.getDestinationId());
        vo.setCheckIn(formatDate(order.getCheckInDate()));
        vo.setCheckOut(formatDate(order.getCheckOutDate()));
        vo.setNights(order.getNights());
        vo.setRoomCount(order.getRoomCount());
        vo.setGuestNames(order.getGuestNames());
        vo.setExpectedArrivalTime(order.getExpectedArrivalTime());
        vo.setPayExpireTime(formatDateTime(order.getPayExpireTime()));
        if (OrderTypeSupport.isRouteFlow(resolvedType)) {
            vo.setCancelPolicySummary(routeCancelPolicySupport.policySummary(order));
        } else {
            vo.setCancelPolicySummary(HotelCancelPolicySupport.policySummary(order));
        }
        vo.setTripDate(formatDate(order.getTravelDate()));
        vo.setGuestCount(order.getGuestCount());
        vo.setMeetPoint(order.getMeetPoint());
        vo.setAddressText(order.getReceiverAddress());
        vo.setVerifiedAt(formatDateTime(order.getVerifyTime()));
        vo.setVerified(Boolean.valueOf(order.getVerifyTime() != null));
        vo.setVerifyCode(OrderVerifyCodeSupport.resolveVerifyCode(order.getOrderNo(), order.getVerifyCode()));
        vo.setRefundAmount(defaultDecimal(order.getRefundAmount()));
        vo.setMerchantSettleAmount(defaultDecimal(order.getMerchantSettleAmount()));
        vo.setPlatformFeeAmount(defaultDecimal(order.getPlatformFeeAmount()));
        vo.setSettleTime(formatDateTime(order.getSettleTime()));
        if (OrderTypeSupport.isRouteFlow(resolvedType) && StringUtils.isNotBlank(order.getGuestInfoJson())) {
            vo.setGuestList(OrderGuestInfoSupport.toVoList(order.getGuestInfoJson(), true));
        }

        if (CollectionUtils.isNotEmpty(items)) {
            vo.setItems(toItemVos(items));
            OrderItemEntity first = items.get(0);
            if (StringUtils.isNotBlank(first.getCoverImage())) {
                vo.setGoodsCover(first.getCoverImage());
            }
            if ("room".equals(first.getItemType())) {
                vo.setRoomName(first.getItemName());
            } else if ("route".equals(first.getItemType())) {
                vo.setRouteName(first.getItemName());
                vo.setRouteId(first.getItemId());
                RouteEntity route = iRouteService.getById(first.getItemId());
                if (route != null) {
                    vo.setDuration(route.getDuration());
                    vo.setSpots(route.getSpots());
                    if (StringUtils.isBlank(vo.getMeetPoint())) {
                        vo.setMeetPoint(route.getMeetPoint());
                    }
                    applyRouteMerchantName(vo, route.getMerchantId(), order.getMerchantId());
                }
            } else if ("goods".equals(first.getItemType())) {
                vo.setGoodsName(first.getItemName());
                vo.setGoodsSpec(first.getItemSpec());
                vo.setQuantity(first.getQuantity());
                vo.setGoodsId(first.getItemId());
            }
        }
        if (OrderTypeSupport.isGoodsFlow(resolvedType)) {
            vo.setShippingFee(calcGoodsShippingFee(order, items));
        }
        fillActionFlags(vo, displayOrder, reviewedOrderIds);
        return vo;
    }

    /** 好物订单运费 = 订单总额 - 商品明细合计 */
    private BigDecimal calcGoodsShippingFee(OrderEntity order, List<OrderItemEntity> items) {
        BigDecimal total = defaultDecimal(order.getTotalAmount());
        BigDecimal goodsAmount = BigDecimal.ZERO;
        if (CollectionUtils.isNotEmpty(items)) {
            for (OrderItemEntity item : items) {
                if ("goods".equals(item.getItemType()) && item.getTotalPrice() != null) {
                    goodsAmount = goodsAmount.add(item.getTotalPrice());
                }
            }
        }
        goodsAmount = goodsAmount.setScale(2, RoundingMode.HALF_UP);
        BigDecimal fee = total.subtract(goodsAmount);
        if (fee.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return fee.setScale(2, RoundingMode.HALF_UP);
    }

    private void fillOrderDetailExtras(OrderAppVo vo, OrderEntity order, List<OrderItemEntity> items) {
        if (vo == null || order == null) {
            return;
        }
        Long merchantId = order.getMerchantId();
        HotelEntity hotel = null;
        HotelRoomEntity roomEntity = null;

        OrderItemEntity roomItem = findOrderItem(items, "room");
        OrderItemEntity routeItem = findOrderItem(items, "route");
        OrderItemEntity goodsItem = findOrderItem(items, "goods");

        if (roomItem != null && roomItem.getItemId() != null) {
            roomEntity = iHotelRoomService.getById(roomItem.getItemId());
            if (roomEntity != null) {
                vo.setRoomId(roomEntity.getId());
                if (StringUtils.isNotBlank(roomEntity.getSpec())) {
                    vo.setRoomSpec(roomEntity.getSpec());
                } else if (StringUtils.isNotBlank(roomItem.getItemSpec())) {
                    vo.setRoomSpec(roomItem.getItemSpec());
                }
                if (merchantId == null && roomEntity.getMerchantId() != null) {
                    merchantId = roomEntity.getMerchantId();
                }
                if (roomEntity.getHotelId() != null) {
                    hotel = iHotelService.getById(roomEntity.getHotelId());
                }
            }
        }

        if (routeItem != null && routeItem.getItemId() != null) {
            RouteEntity route = iRouteService.getById(routeItem.getItemId());
            if (route != null && merchantId == null && route.getMerchantId() != null) {
                merchantId = route.getMerchantId();
            }
        }

        if (goodsItem != null && goodsItem.getItemId() != null) {
            MerchantServiceEntity goods = iMerchantServiceItemService.getById(goodsItem.getItemId());
            if (goods != null && merchantId == null && goods.getMerchantId() != null) {
                merchantId = goods.getMerchantId();
            }
        }

        if (hotel != null) {
            applyHotelToVo(vo, hotel);
            if (merchantId == null) {
                merchantId = hotel.getMerchantId();
            }
        } else if (merchantId != null && isHotelOrder(vo, order, items)) {
            hotel = findHotelByMerchantId(merchantId);
            if (hotel != null) {
                applyHotelToVo(vo, hotel);
            }
        }

        if (merchantId != null) {
            vo.setMerchantId(merchantId);
            MerchantEntity merchant = iMerchantService.getById(merchantId);
            if (merchant != null) {
                applyMerchantToVo(vo, merchant);
            }
        }

        if (routeItem != null) {
            applyRouteMerchantName(vo, merchantId, order.getMerchantId());
        }

        fillDestinationName(vo, order.getDestinationId());
    }

    private OrderItemEntity findOrderItem(List<OrderItemEntity> items, String itemType) {
        if (CollectionUtils.isEmpty(items) || StringUtils.isBlank(itemType)) {
            return null;
        }
        return items.stream()
                .filter(item -> itemType.equals(item.getItemType()))
                .findFirst()
                .orElse(null);
    }

    private boolean isHotelOrder(OrderAppVo vo, OrderEntity order, List<OrderItemEntity> items) {
        if (vo != null && "hotel".equals(vo.getOrderType())) {
            return true;
        }
        if (order != null && order.getCheckInDate() != null) {
            return true;
        }
        return findOrderItem(items, "room") != null;
    }

    private HotelEntity findHotelByMerchantId(Long merchantId) {
        if (merchantId == null) {
            return null;
        }
        LambdaQueryWrapper<HotelEntity> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HotelEntity::getMerchantId, merchantId)
                .orderByDesc(HotelEntity::getSort)
                .orderByDesc(HotelEntity::getId)
                .last("LIMIT 1");
        return iHotelService.getOne(wrapper, false);
    }

    private void applyHotelToVo(OrderAppVo vo, HotelEntity hotel) {
        if (vo == null || hotel == null) {
            return;
        }
        vo.setHotelId(hotel.getId());
        if (StringUtils.isNotBlank(hotel.getCoverImage())) {
            vo.setHotelCover(hotel.getCoverImage());
        }
        if (StringUtils.isNotBlank(hotel.getName())) {
            vo.setMerchantName(hotel.getName());
        }
        if (StringUtils.isNotBlank(hotel.getAddress())) {
            vo.setMerchantAddress(hotel.getAddress());
        }
        if (StringUtils.isNotBlank(hotel.getPhone())) {
            vo.setMerchantPhone(hotel.getPhone());
        }
        if (hotel.getLatitude() != null) {
            vo.setLatitude(hotel.getLatitude());
        }
        if (hotel.getLongitude() != null) {
            vo.setLongitude(hotel.getLongitude());
        }
    }

    private void applyRouteMerchantName(OrderAppVo vo, Long routeMerchantId, Long orderMerchantId) {
        if (vo == null) {
            return;
        }
        Long merchantId = routeMerchantId != null ? routeMerchantId : orderMerchantId;
        if (merchantId == null) {
            merchantId = vo.getMerchantId();
        }
        if (merchantId == null) {
            return;
        }
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant == null) {
            return;
        }
        vo.setMerchantId(merchantId);
        vo.setMerchantName(StringUtils.defaultIfBlank(merchant.getStoreName(), merchant.getContactName()));
    }

    private void applyMerchantToVo(OrderAppVo vo, MerchantEntity merchant) {
        if (vo == null || merchant == null) {
            return;
        }
        if (StringUtils.isBlank(vo.getMerchantName())) {
            vo.setMerchantName(merchant.getStoreName());
        }
        if (StringUtils.isBlank(vo.getMerchantPhone())) {
            vo.setMerchantPhone(merchant.getContactPhone());
        }
        if (StringUtils.isNotBlank(merchant.getContactName())) {
            vo.setMerchantContactName(merchant.getContactName());
        }
        if (StringUtils.isBlank(vo.getMerchantAddress())) {
            vo.setMerchantAddress(merchant.getStoreAddress());
        }
        if (StringUtils.isBlank(vo.getMerchantRegion())) {
            vo.setMerchantRegion(merchant.getStoreRegion());
        }
        if (vo.getLatitude() == null) {
            vo.setLatitude(merchant.getLatitude());
        }
        if (vo.getLongitude() == null) {
            vo.setLongitude(merchant.getLongitude());
        }
        if (StringUtils.isBlank(vo.getMerchantCover())) {
            String cover = GooseDecorUtil.resolveCover(loadMerchantDecorMap(merchant.getId()));
            if (StringUtils.isNotBlank(cover)) {
                vo.setMerchantCover(cover);
            }
        }
    }

    private Map<String, Object> loadMerchantDecorMap(Long merchantId) {
        if (merchantId == null) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<MerchantStoreDecorEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantStoreDecorEntity::getMerchantId, merchantId)
                .last("LIMIT 1");
        MerchantStoreDecorEntity entity = iMerchantStoreDecorService.getOne(queryWrapper, false);
        if (entity == null) {
            return Collections.emptyMap();
        }
        return GooseDecorUtil.parseDecor(entity.getDecorJson());
    }

    private void fillDestinationName(OrderAppVo vo, Long destinationId) {
        if (vo == null) {
            return;
        }
        DestinationEntity destination = destinationId == null ? null : iDestinationService.getById(destinationId);
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destination, vo.getMeetPoint(), vo.getAddressText()));
    }

    private OrderEntity resolveDisplayOrder(OrderEntity order, List<OrderItemEntity> items) {
        if (order == null) {
            return new OrderEntity();
        }
        OrderType type = OrderTypeSupport.resolve(order, items);
        OrderTypeSupport.apply(order, type);
        return order;
    }

    private OrderAdminVo toAdminVo(OrderEntity order, List<OrderItemEntity> items, Map<Long, UserEntity> userMap) {
        OrderAppVo base = toAppVo(order, items, Collections.emptySet());
        fillOrderDetailExtras(base, order, items);
        OrderAdminVo vo = new OrderAdminVo();
        BeanUtils.copyProperties(base, vo);
        vo.setUserId(order.getUserId());
        UserEntity user = userMap.get(order.getUserId());
        if (user != null) {
            vo.setUserName(user.getUserName());
            vo.setUserPhone(user.getPhone());
        }
        vo.setPayTime(formatDateTime(order.getPayTime()));
        vo.setCreateTime(formatDateTime(order.getCreateTime()));
        vo.setUpdateTime(formatDateTime(order.getUpdateTime()));
        if (OrderTypeSupport.isRouteFlow(OrderTypeSupport.resolve(order, items))
                && StringUtils.isNotBlank(order.getGuestInfoJson())) {
            vo.setGuestList(OrderGuestInfoSupport.toVoList(order.getGuestInfoJson(), false));
        }
        return vo;
    }

    private List<OrderAppItemVo> toItemVos(List<OrderItemEntity> items) {
        List<OrderAppItemVo> voList = new ArrayList<>();
        for (OrderItemEntity item : items) {
            OrderAppItemVo vo = new OrderAppItemVo();
            vo.setName(item.getItemName());
            vo.setSpec(item.getItemSpec());
            vo.setCover(item.getCoverImage());
            vo.setQuantity(item.getQuantity());
            vo.setUnitPrice(defaultDecimal(item.getUnitPrice()));
            vo.setTotalPrice(defaultDecimal(item.getTotalPrice()));
            voList.add(vo);
        }
        return voList;
    }

    private OrderCreateResultVo toCreateResult(OrderEntity order) {
        OrderCreateResultVo vo = new OrderCreateResultVo();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setPayAmount(defaultDecimal(order.getPayAmount()));
        OrderType type = OrderTypeSupport.resolve(order);
        vo.setOrderType(type.getCode());
        vo.setStatusCode(AppOrderStatusSupport.toApiStatus(order));
        vo.setStatus(AppOrderStatusSupport.toDisplayStatus(order));
        return vo;
    }

    private Map<Long, UserEntity> loadUserMap(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, UserEntity> map = new HashMap<>();
        for (UserEntity user : users) {
            map.put(user.getUserId(), user);
        }
        return map;
    }

    private Page<OrderEntity> initBoundedPage(Long page, Long limit) {
        Page<OrderEntity> pageObj = PageUtil.initPage(page, limit);
        if (pageObj.getSize() > MAX_PAGE_LIMIT) {
            pageObj.setSize(MAX_PAGE_LIMIT);
        }
        return pageObj;
    }

    private String generateOrderNo(String category) {
        String prefix;
        switch (category) {
            case "hotel":
                prefix = "HT";
                break;
            case "route":
                prefix = "RT";
                break;
            case "goods":
                prefix = "GD";
                break;
            default:
                prefix = "OD";
        }
        return prefix + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(100, 999);
    }

    private LocalDate parseDate(String text, String label) {
        if (StringUtils.isBlank(text)) {
            throw new BizException(AppError.ORDER_DATE_INVALID);
        }
        try {
            return LocalDate.parse(text.trim(), DATE_FMT);
        } catch (Exception ex) {
            throw new BizException(AppError.ORDER_DATE_INVALID);
        }
    }

    private void assertClientPrice(BigDecimal clientPrice, BigDecimal serverPrice) {
        if (clientPrice == null || serverPrice == null) {
            return;
        }
        if (clientPrice.setScale(2, RoundingMode.HALF_UP)
                .compareTo(serverPrice.setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new BizException(AppError.ORDER_PRICE_CHANGED);
        }
    }

    private String joinGuestNames(List<String> guestNames) {
        if (CollectionUtils.isEmpty(guestNames)) {
            return null;
        }
        return guestNames.stream().filter(StringUtils::isNotBlank).collect(Collectors.joining("、"));
    }

    private String normalizeAdminAction(String action) {
        if (StringUtils.isBlank(action)) {
            return action;
        }
        return action.trim().toLowerCase();
    }

    private BigDecimal defaultDecimal(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FMT);
    }

    private String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FMT);
    }

    private Set<Long> loadReviewedOrderIds(Long userId, List<Long> orderIds) {
        if (userId == null || CollectionUtils.isEmpty(orderIds)) {
            return Collections.emptySet();
        }
        Set<Long> reviewedOrderIds = new HashSet<>();
        LambdaQueryWrapper<ReviewEntity> goodsQuery = new LambdaQueryWrapper<>();
        goodsQuery.eq(ReviewEntity::getUserId, userId)
                .in(ReviewEntity::getOrderId, orderIds)
                .select(ReviewEntity::getOrderId);
        iReviewService.list(goodsQuery).stream()
                .map(ReviewEntity::getOrderId)
                .filter(Objects::nonNull)
                .forEach(reviewedOrderIds::add);

        LambdaQueryWrapper<HotelReviewEntity> hotelQuery = new LambdaQueryWrapper<>();
        hotelQuery.eq(HotelReviewEntity::getUserId, userId)
                .in(HotelReviewEntity::getOrderId, orderIds)
                .select(HotelReviewEntity::getOrderId);
        iHotelReviewService.list(hotelQuery).stream()
                .map(HotelReviewEntity::getOrderId)
                .filter(Objects::nonNull)
                .forEach(reviewedOrderIds::add);

        LambdaQueryWrapper<RouteReviewEntity> routeQuery = new LambdaQueryWrapper<>();
        routeQuery.eq(RouteReviewEntity::getUserId, userId)
                .in(RouteReviewEntity::getOrderId, orderIds)
                .select(RouteReviewEntity::getOrderId);
        iRouteReviewService.list(routeQuery).stream()
                .map(RouteReviewEntity::getOrderId)
                .filter(Objects::nonNull)
                .forEach(reviewedOrderIds::add);
        return reviewedOrderIds;
    }

    private OrderEntity refreshHotelLifecycle(OrderEntity order) {
        if (order == null) {
            return null;
        }
        if (orderLifecycleService.applyHotelNoShowIfNeeded(order)
                || orderLifecycleService.applyHotelAutoCompleteIfNeeded(order)) {
            return iOrderService.getById(order.getId());
        }
        return order;
    }

    private void refreshHotelLifecycle(List<OrderEntity> orders) {
        if (CollectionUtils.isEmpty(orders)) {
            return;
        }
        for (int i = 0; i < orders.size(); i++) {
            orders.set(i, refreshHotelLifecycle(orders.get(i)));
        }
    }

    private void assertOnlinePayRefundAllowed(OrderEntity order) {
        OrderType type = OrderTypeSupport.resolve(order);
        if (!OrderTypeSupport.isOnlinePayFlow(type)) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
    }

    /** 已支付订单取消时原路退实付（优惠券单独退还） */
    private void refundPaidOrderOnCancel(OrderEntity order) {
        if (order == null || order.getPayTime() == null) {
            return;
        }
        OrderType type = OrderTypeSupport.resolve(order);
        if (!OrderTypeSupport.isOnlinePayFlow(type)) {
            return;
        }
        String title = OrderTypeSupport.isHotelFlow(type) ? "酒店订单取消退款" : "订单取消退款";
        orderSettlementSupport.refundPaidOrderToUser(order, title);
    }

    private void assertHotelUserRefundAllowed(OrderEntity order) {
        if (!"hotel".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            return;
        }
        if (AppOrderStatusSupport.isHotelCheckedIn(order)) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
        if (!HotelCancelPolicySupport.isUserRefundAllowed(order, LocalDateTime.now())) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
    }

    private void assertRouteUserRefundAllowed(OrderEntity order) {
        if (order.getVerifyTime() != null) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
        if (!routeCancelPolicySupport.canApplyRefund(order, LocalDateTime.now())) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
    }

    private void cancelRouteOrder(Long userId, OrderEntity order) {
        if (routeCancelPolicySupport.isLegacyRouteBooking(order)) {
            if (!Objects.equals(order.getStatus(), MerchantOrderStatusGroup.PENDING_CONFIRM)
                    && !Objects.equals(order.getStatus(), MerchantOrderStatusGroup.PENDING_USE)) {
                throw new BizException(AppError.ORDER_CANCEL_NOT_ALLOWED);
            }
            order.setStatus(OrderStatusMachine.toCancelledStatus());
            order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
            order.setRemark(appendRemark(order.getRemark(), "用户取消预约"));
            iOrderService.updateById(order);
            instationMessageSupport.sendUserMessage(userId, "线路预约已取消",
                    "您的线路预约 " + order.getOrderNo() + " 已取消", order.getId());
            return;
        }
        if (Objects.equals(order.getStatus(), MerchantOrderStatusGroup.UNPAID)) {
            OrderStatusMachine.assertUserCancelAllowed(order.getStatus());
            order.setStatus(OrderStatusMachine.toCancelledStatus());
            order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
            iOrderService.updateById(order);
            instationMessageSupport.sendUserMessage(userId, "订单已取消",
                    "您的订单 " + order.getOrderNo() + " 已取消", order.getId());
            return;
        }
        if (order.getVerifyTime() != null) {
            throw new BizException(AppError.ORDER_CANCEL_NOT_ALLOWED);
        }
        if (!routeCancelPolicySupport.canDirectCancel(order, LocalDateTime.now())) {
            throw new BizException(AppError.ORDER_CANCEL_NOT_ALLOWED);
        }
        RouteCancelPolicySupport.RefundQuote quote = routeCancelPolicySupport.quoteRefund(order, LocalDateTime.now());
        order.setStatus(OrderStatusMachine.toCancelledStatus());
        order.setStatusText(AppOrderStatusSupport.toDisplayStatus(order));
        order.setRemark(appendRemark(order.getRemark(), "用户取消并退款"));
        iOrderService.updateById(order);
        orderSettlementSupport.settleOnRouteRefundComplete(order, quote);
        instationMessageSupport.sendUserMessage(userId, "订单已取消",
                "您的线路订单 " + order.getOrderNo() + " 已取消并退款", order.getId());
    }

    private void applyRouteGuestInfo(OrderCreateBo createBo, OrderEntity order, int guestCount) {
        List<OrderGuestBo> guestList = createBo.getGuestList();
        if (CollectionUtils.isNotEmpty(guestList)) {
            try {
                OrderGuestInfoSupport.validateGuestList(guestList, guestCount);
            } catch (IllegalArgumentException ex) {
                throw new BizException(AppError.ORDER_GUEST_INVALID);
            }
            order.setGuestInfoJson(OrderGuestInfoSupport.toJson(guestList));
            order.setGuestNames(joinGuestNames(OrderGuestInfoSupport.extractNames(guestList)));
            return;
        }
        order.setGuestNames(joinGuestNames(createBo.getGuestNames()));
    }

    @Override
    public OrderRefundPreviewVo adminRouteRefundPreview(Long orderId, Integer refundRatio) {
        OrderEntity order = iOrderService.getById(orderId);
        if (order == null) {
            throw new BizException(AppError.ORDER_NOT_FOUND);
        }
        if (!OrderTypeSupport.isRouteFlow(OrderTypeSupport.resolve(order))) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
        RouteCancelPolicySupport.RefundQuote quote = buildAdminRouteRefundQuote(order, refundRatio);
        return toRefundPreviewVo(order, quote);
    }

    @Override
    public OrderRefundPreviewVo refundPreview(Long userId, String orderKey) {
        OrderEntity order = requireOwnedOrder(userId, orderKey);
        if (!OrderTypeSupport.isRouteFlow(OrderTypeSupport.resolve(order))) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
        RouteCancelPolicySupport.RefundQuote quote = routeCancelPolicySupport.quoteRefund(order, LocalDateTime.now());
        return toUserRefundPreviewVo(order, quote);
    }

    private void executeAdminRouteForceRefund(OrderEntity order, OrderAdminActionBo actionBo) {
        assertAdminRouteForceRefundAllowed(order);
        if (routeCancelPolicySupport.isLegacyRouteBooking(order)) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
        if (!routeCancelPolicySupport.isPaidRouteOrder(order)) {
            throw new BizException(AppError.ORDER_REFUND_NOT_ALLOWED);
        }
        RouteCancelPolicySupport.RefundQuote quote = buildAdminRouteRefundQuote(order, actionBo.getRefundRatio());
        order.setStatus(OrderStatusMachine.toCancelledStatus());
        String remark = "平台强制退款（退用户" + quote.getUserRefundRatio() + "%）";
        if (StringUtils.isNotBlank(actionBo.getReason())) {
            remark += "：" + StringUtils.trim(actionBo.getReason());
        }
        order.setRemark(appendRemark(order.getRemark(), remark));
        couponOrderSupport.restoreCouponIfNeeded(order);
        orderSettlementSupport.settleOnRouteRefundComplete(order, quote);
        if (order.getUserId() != null) {
            instationMessageSupport.sendUserMessage(order.getUserId(), "订单已退款",
                    "您的线路订单 " + order.getOrderNo() + " 已由平台处理退款", order.getId());
        }
    }

    private RouteCancelPolicySupport.RefundQuote buildAdminRouteRefundQuote(OrderEntity order, Integer refundRatio) {
        if (refundRatio == null) {
            throw new BizException(AppError.ORDER_REFUND_RATIO_REQUIRED);
        }
        int ratio = Math.max(0, Math.min(100, refundRatio));
        int commissionRate = orderSettlementSupport.resolveCommissionRate(order);
        RouteCancelPolicySupport.RefundQuote quote = orderSettlementSupport.calcRefundSettlement(
                order.getPayAmount(), ratio, commissionRate);
        quote.setRuleLabel("平台强制退款");
        return quote;
    }

    private void assertAdminRouteForceRefundAllowed(OrderEntity order) {
        int current = order.getStatus() == null ? MerchantOrderStatusGroup.UNPAID : order.getStatus();
        if (current == MerchantOrderStatusGroup.UNPAID
                || current == MerchantOrderStatusGroup.CANCELLED) {
            throw new BizException(AppError.ORDER_STATUS_INVALID);
        }
    }

    private OrderRefundPreviewVo toRefundPreviewVo(OrderEntity order, RouteCancelPolicySupport.RefundQuote quote) {
        if (quote == null) {
            return null;
        }
        OrderRefundPreviewVo vo = new OrderRefundPreviewVo();
        vo.setPayAmount(quote.getPayAmount());
        vo.setRuleLabel(quote.getRuleLabel());
        vo.setRefundRatio(Integer.valueOf(quote.getRefundRatio()));
        vo.setUserRefundRatio(Integer.valueOf(quote.getUserRefundRatio()));
        vo.setCommissionRate(Integer.valueOf(quote.getCommissionRate()));
        vo.setUserRefund(quote.getUserRefund());
        vo.setPlatformFee(quote.getPlatformFee());
        vo.setMerchantIncome(quote.getMerchantIncome());
        if (order != null) {
            vo.setPolicySummary(routeCancelPolicySupport.policySummary(order));
        }
        return vo;
    }

    /** C 端退款预览：仅返回用户可见字段 */
    private OrderRefundPreviewVo toUserRefundPreviewVo(OrderEntity order, RouteCancelPolicySupport.RefundQuote quote) {
        if (quote == null) {
            return null;
        }
        OrderRefundPreviewVo vo = new OrderRefundPreviewVo();
        vo.setPayAmount(quote.getPayAmount());
        vo.setRuleLabel(quote.getRuleLabel());
        vo.setRefundRatio(Integer.valueOf(quote.getRefundRatio()));
        vo.setUserRefundRatio(Integer.valueOf(quote.getUserRefundRatio()));
        vo.setUserRefund(quote.getUserRefund());
        if (order != null) {
            vo.setPolicySummary(routeCancelPolicySupport.policySummary(order));
        }
        return vo;
    }

    private void fillActionFlags(OrderAppVo vo, OrderEntity order, Set<Long> reviewedOrderIds) {
        String apiStatus = vo.getStatusCode();
        OrderType type = OrderType.fromCode(vo.getOrderType());
        if (type == null) {
            type = OrderType.GOODS;
        }
        vo.setCanConfirmReceive(Boolean.valueOf(
                OrderTypeSupport.isGoodsFlow(type) && AppOrderStatusSupport.PENDING_RECEIVE.equals(apiStatus)));
        boolean hotelCheckedIn = AppOrderStatusSupport.isHotelCheckedIn(order);
        boolean canFreeCancelHotel = OrderTypeSupport.isHotelFlow(type)
                && AppOrderStatusSupport.PENDING_USE.equals(apiStatus)
                && !hotelCheckedIn
                && HotelCancelPolicySupport.isFreeCancelAllowed(order, LocalDateTime.now());
        boolean canCancelRoute = false;
        boolean canApplyRefundRoute = false;
        if (OrderTypeSupport.isRouteFlow(type)) {
            if (routeCancelPolicySupport.isLegacyRouteBooking(order)) {
                canCancelRoute = AppOrderStatusSupport.PENDING_CONFIRM.equals(apiStatus)
                        || AppOrderStatusSupport.PENDING_USE.equals(apiStatus);
            } else if (AppOrderStatusSupport.PENDING_PAY.equals(apiStatus)) {
                canCancelRoute = true;
            } else if (AppOrderStatusSupport.PENDING_USE.equals(apiStatus) && order.getVerifyTime() == null) {
                canCancelRoute = routeCancelPolicySupport.canDirectCancel(order, LocalDateTime.now());
                canApplyRefundRoute = routeCancelPolicySupport.canApplyRefund(order, LocalDateTime.now());
            }
        }
        vo.setCanCancelOrder(Boolean.valueOf(
                !hotelCheckedIn && (AppOrderStatusSupport.PENDING_PAY.equals(apiStatus)
                        || canFreeCancelHotel
                        || canCancelRoute)));
        boolean canApplyRefund = OrderTypeSupport.isOnlinePayFlow(type)
                && !hotelCheckedIn
                && (AppOrderStatusSupport.PENDING_CONFIRM.equals(apiStatus)
                || AppOrderStatusSupport.PENDING_USE.equals(apiStatus)
                || AppOrderStatusSupport.PENDING_RECEIVE.equals(apiStatus))
                && !canFreeCancelHotel;
        if (canApplyRefund && OrderTypeSupport.isHotelFlow(type)) {
            canApplyRefund = HotelCancelPolicySupport.isUserRefundAllowed(order, LocalDateTime.now());
        }
        if (OrderTypeSupport.isRouteFlow(type)) {
            canApplyRefund = canApplyRefundRoute;
        }
        vo.setCanApplyRefund(Boolean.valueOf(canApplyRefund));
        boolean reviewed = reviewedOrderIds != null && reviewedOrderIds.contains(order.getId());
        vo.setReviewed(Boolean.valueOf(reviewed));
        boolean canReview = AppOrderStatusSupport.COMPLETED.equals(apiStatus) && !reviewed;
        if (canReview && OrderTypeSupport.isHotelFlow(type)) {
            canReview = hotelCheckedIn;
        } else if (canReview && OrderTypeSupport.isRouteFlow(type)) {
            canReview = order.getVerifyTime() != null || hotelCheckedIn;
        } else if (canReview && !OrderTypeSupport.isGoodsFlow(type)) {
            canReview = false;
        }
        vo.setCanReview(Boolean.valueOf(canReview));
    }

    /** C 端「好物」Tab 含 goods / culture / service */
    private void applyListCategoryFilter(LambdaQueryWrapper<OrderEntity> queryWrapper, String categoryRaw) {
        if (StringUtils.isBlank(categoryRaw) || "all".equalsIgnoreCase(categoryRaw)) {
            return;
        }
        String category = AppOrderStatusSupport.normalizeCategory(categoryRaw);
        if ("goods".equals(category)) {
            queryWrapper.in(OrderEntity::getOrderCategory, Arrays.asList("goods", "culture", "service"));
        } else {
            queryWrapper.eq(OrderEntity::getOrderCategory, category);
        }
    }

    private void applyUserListStatusFilter(LambdaQueryWrapper<OrderEntity> queryWrapper, OrderListBo listBo) {
        String status = listBo.getStatus();
        if (StringUtils.isBlank(status) || "all".equalsIgnoreCase(status)) {
            return;
        }
        if (AppOrderStatusSupport.isRefundedStatusFilter(status)) {
            queryWrapper.eq(OrderEntity::getStatus, MerchantOrderStatusGroup.CANCELLED)
                    .and(wrapper -> wrapper.like(OrderEntity::getRemark, "退款")
                            .or()
                            .like(OrderEntity::getRemark, "免费取消"));
            return;
        }
        List<Integer> statusFilter = AppOrderStatusSupport.resolveStatusFilter(status, listBo.getCategory());
        if (CollectionUtils.isEmpty(statusFilter)) {
            return;
        }
        queryWrapper.in(OrderEntity::getStatus, statusFilter);
        if (AppOrderStatusSupport.isCancelledStatusFilter(status)) {
            queryWrapper.and(wrapper -> wrapper.isNull(OrderEntity::getRemark)
                    .or(sub -> sub.notLike(OrderEntity::getRemark, "退款")
                            .notLike(OrderEntity::getRemark, "免费取消")));
        }
        if (AppOrderStatusSupport.isHotelPendingUseFilter(status, listBo.getCategory())) {
            queryWrapper.isNull(OrderEntity::getVerifyTime);
        }
    }

    private void sendUserActionMessage(OrderEntity order, String action) {
        if (order.getUserId() == null) {
            return;
        }
        String orderNo = order.getOrderNo();
        switch (action) {
            case "complete":
                instationMessageSupport.sendUserMessage(order.getUserId(), "确认收货成功",
                        "您的订单 " + orderNo + " 已确认收货", order.getId());
                break;
            case "refund":
                instationMessageSupport.sendUserMessage(order.getUserId(), "退款申请已提交",
                        "您的订单 " + orderNo + " 退款申请已提交，请等待处理", order.getId());
                break;
            default:
                break;
        }
    }

    private void sendAdminActionMessage(OrderEntity order, String action) {
        if (order.getUserId() == null) {
            return;
        }
        String orderNo = order.getOrderNo();
        switch (action) {
            case "cancel":
                instationMessageSupport.sendUserMessage(order.getUserId(), "订单已取消",
                        "您的订单 " + orderNo + " 已被取消", order.getId());
                break;
            case "complete":
                instationMessageSupport.sendUserMessage(order.getUserId(), "订单已完成",
                        "您的订单 " + orderNo + " 已完成", order.getId());
                break;
            case "refund":
                instationMessageSupport.sendUserMessage(order.getUserId(), "订单退款处理中",
                        "您的订单 " + orderNo + " 正在退款处理", order.getId());
                break;
            default:
                break;
        }
    }
}

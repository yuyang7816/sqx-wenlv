package com.sqx.app.lvyou.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.messages.support.InstationMessageSupport;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantOrderActionBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantOrderListBo;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantOrderItemVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantOrderVo;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.canteen.service.CanteenReservationService;
import com.sqx.app.lvyou.coupon.support.CouponOrderSupport;
import com.sqx.app.lvyou.invite.service.InviteService;
import com.sqx.app.lvyou.merchant.service.MerchantOrderService;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.merchant.support.MerchantAuthSupport;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.enums.OrderType;
import com.sqx.app.lvyou.order.support.AppOrderStatusSupport;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.app.lvyou.order.support.OrderGuestInfoSupport;
import com.sqx.app.lvyou.order.support.OrderSettlementSupport;
import com.sqx.app.lvyou.order.support.OrderTypeSupport;
import com.sqx.app.lvyou.order.domain.vo.OrderLogisticsVo;
import com.sqx.app.lvyou.order.service.OrderLogisticsService;
import com.sqx.app.lvyou.order.support.OrderVerifyCodeSupport;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 商户订单业务服务实现
 */
@Service
public class MerchantOrderServiceImpl implements MerchantOrderService {

    private static final DateTimeFormatter DATETIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private MerchantAuthSupport merchantAuthSupport;

    @Resource
    private MerchantWalletService merchantWalletService;

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private CanteenReservationService canteenReservationService;

    @Resource
    private InstationMessageSupport instationMessageSupport;

    @Resource
    private InviteService inviteService;

    @Resource
    private IMerchantServiceItemService iMerchantServiceItemService;

    @Resource
    private CouponOrderSupport couponOrderSupport;

    @Resource
    private OrderSettlementSupport orderSettlementSupport;

    @Resource
    private RouteCancelPolicySupport routeCancelPolicySupport;

    @Resource
    private OrderLogisticsService orderLogisticsService;

    @Override
    public IPage<MerchantOrderVo> pageOrders(Long userId, MerchantOrderListBo listBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        merchantWalletService.assertDepositPaid(merchant.getId());
        String tab = StringUtils.defaultIfBlank(listBo.getTab(), "all");

        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchant.getId())
                .ne(OrderEntity::getStatus, MerchantOrderStatusGroup.UNPAID)
                .like(StringUtils.isNotBlank(listBo.getOrderNo()), OrderEntity::getOrderNo, listBo.getOrderNo());

        applyTabFilter(queryWrapper, tab);

        if ("verify".equalsIgnoreCase(tab)) {
            queryWrapper.last("ORDER BY COALESCE(travel_date, check_in_date) ASC, create_time DESC");
        } else {
            queryWrapper.orderByDesc(OrderEntity::getCreateTime)
                    .orderByDesc(OrderEntity::getId);
        }

        Page<OrderEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        IPage<OrderEntity> entityPage = iOrderService.page(page, queryWrapper);

        List<OrderEntity> records = entityPage.getRecords();
        if (StringUtils.isNotBlank(listBo.getStatus())) {
            records = records.stream()
                    .filter(order -> listBo.getStatus().equals(MerchantOrderStatusSupport.toApiStatus(order)))
                    .collect(Collectors.toList());
        }

        List<Long> orderIds = records.stream().map(OrderEntity::getId).collect(Collectors.toList());
        Map<Long, List<OrderItemEntity>> itemMap = iOrderItemService.mapByOrderIds(orderIds);

        List<MerchantOrderVo> voList = new ArrayList<>();
        for (OrderEntity record : records) {
            voList.add(toVo(record, itemMap.getOrDefault(record.getId(), Collections.emptyList())));
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public MerchantOrderVo getOrderDetail(Long userId, String orderKey) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        merchantWalletService.assertDepositPaid(merchant.getId());
        OrderEntity order = requireOwnedOrder(merchant.getId(), orderKey);
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        return toVo(order, items);
    }

    @Override
    public OrderLogisticsVo queryOrderLogistics(Long userId, String orderKey) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        merchantWalletService.assertDepositPaid(merchant.getId());
        OrderEntity order = requireOwnedOrder(merchant.getId(), orderKey);
        return orderLogisticsService.queryLogistics(null, order.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantOrderVo executeAction(Long userId, String orderKey, MerchantOrderActionBo actionBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        merchantWalletService.assertDepositPaid(merchant.getId());
        OrderEntity order = requireOwnedOrder(merchant.getId(), orderKey);
        String action = normalizeAction(actionBo.getAction());
        int currentStatus = order.getStatus() == null ? MerchantOrderStatusGroup.PENDING_CONFIRM : order.getStatus();
        String category = MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory());

        switch (action) {
            case "confirm":
                confirmOrder(order, currentStatus, category);
                break;
            case "ship":
                shipOrder(order, currentStatus, category, actionBo);
                break;
            case "complete":
                completeOrder(order, currentStatus, category);
                break;
            case "verify":
                verifyOrder(order, currentStatus, category);
                break;
            case "cancel":
            case "reject":
                cancelOrder(order, currentStatus, actionBo.getReason());
                break;
            case "refund_agree":
                agreeRefund(order, currentStatus);
                break;
            case "refund_weather":
                refundWeather(order, currentStatus, category);
                break;
            default:
                throw new BizException(AppError.MERCHANT_ORDER_ACTION_INVALID);
        }

        if ("verify".equals(action)) {
            orderSettlementSupport.settleOnVerifyComplete(order);
        } else if ("complete".equals(action) && MerchantOrderStatusSupport.isGoodsCategory(category)) {
            orderSettlementSupport.settleOnGoodsComplete(order);
        } else if ("route".equals(category)
                && ("refund_agree".equals(action) || "refund_weather".equals(action))) {
            completeRouteRefund(order, action);
        }
        iOrderService.updateById(order);
        if ("complete".equals(action) || "verify".equals(action)) {
            inviteService.settleCommissionOnOrderCompleted(order);
        }
        if ("restaurant".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            canteenReservationService.syncStatusByOrderId(order.getId(), action);
        }
        sendMerchantActionMessage(order, action);
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        return toVo(order, items);
    }

    @Override
    public long countPendingOrders(Long merchantId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .ne(OrderEntity::getStatus, MerchantOrderStatusGroup.UNPAID)
                .and(wrapper -> wrapper
                        .and(inner -> inner.eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_CONFIRM)
                                .ne(OrderEntity::getOrderCategory, "hotel"))
                        .or(inner -> inner.eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_USE)
                                .in(OrderEntity::getOrderCategory, "goods", "culture")));
        return iOrderService.count(queryWrapper);
    }

    @Override
    public long countUnprocessedOrders(Long merchantId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_CONFIRM,
                        MerchantOrderStatusGroup.PENDING_USE,
                        MerchantOrderStatusGroup.PENDING_RECEIVE,
                        MerchantOrderStatusGroup.REFUNDING)
                .ne(OrderEntity::getOrderCategory, AppOrderStatusSupport.RESTAURANT_SHADOW_CATEGORY);
        return iOrderService.count(queryWrapper);
    }

    @Override
    public long countPendingVerifyOrders(Long merchantId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .in(OrderEntity::getOrderCategory, "hotel", "route")
                .eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_USE)
                .isNull(OrderEntity::getVerifyTime);
        return iOrderService.count(queryWrapper);
    }

    @Override
    public List<MerchantOrderVo> listPendingVerifyOrders(Long merchantId, int limit) {
        int size = limit <= 0 ? 5 : Math.min(limit, 20);
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .in(OrderEntity::getOrderCategory, "hotel", "route")
                .eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_USE)
                .isNull(OrderEntity::getVerifyTime)
                .last("ORDER BY COALESCE(travel_date, check_in_date) ASC, create_time DESC LIMIT " + size);
        List<OrderEntity> records = iOrderService.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<Long> orderIds = records.stream().map(OrderEntity::getId).collect(Collectors.toList());
        Map<Long, List<OrderItemEntity>> itemMap = iOrderItemService.mapByOrderIds(orderIds);
        List<MerchantOrderVo> voList = new ArrayList<>();
        for (OrderEntity record : records) {
            voList.add(toVo(record, itemMap.getOrDefault(record.getId(), Collections.emptyList())));
        }
        return voList;
    }

    @Override
    public long countPendingShipOrders(Long merchantId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .in(OrderEntity::getOrderCategory, "goods", "culture", "service")
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_CONFIRM,
                        MerchantOrderStatusGroup.PENDING_USE);
        return iOrderService.count(queryWrapper);
    }

    @Override
    public List<MerchantOrderVo> listPendingShipOrders(Long merchantId, int limit) {
        int size = limit <= 0 ? 5 : Math.min(limit, 20);
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .in(OrderEntity::getOrderCategory, "goods", "culture", "service")
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_CONFIRM,
                        MerchantOrderStatusGroup.PENDING_USE)
                .orderByDesc(OrderEntity::getCreateTime)
                .last("LIMIT " + size);
        List<OrderEntity> records = iOrderService.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<Long> orderIds = records.stream().map(OrderEntity::getId).collect(Collectors.toList());
        Map<Long, List<OrderItemEntity>> itemMap = iOrderItemService.mapByOrderIds(orderIds);
        List<MerchantOrderVo> voList = new ArrayList<>();
        for (OrderEntity record : records) {
            voList.add(toVo(record, itemMap.getOrDefault(record.getId(), Collections.emptyList())));
        }
        return voList;
    }

    @Override
    public long countPendingRouteBookings(Long merchantId) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .eq(OrderEntity::getOrderCategory, "route")
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_CONFIRM,
                        MerchantOrderStatusGroup.PENDING_USE);
        return iOrderService.count(queryWrapper);
    }

    @Override
    public List<MerchantOrderVo> listPendingRouteBookings(Long merchantId, int limit) {
        int size = limit <= 0 ? 5 : Math.min(limit, 20);
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .eq(OrderEntity::getOrderCategory, "route")
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_CONFIRM,
                        MerchantOrderStatusGroup.PENDING_USE)
                .orderByAsc(OrderEntity::getTravelDate)
                .orderByDesc(OrderEntity::getCreateTime)
                .last("LIMIT " + size);
        List<OrderEntity> records = iOrderService.list(queryWrapper);
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        List<Long> orderIds = records.stream().map(OrderEntity::getId).collect(Collectors.toList());
        Map<Long, List<OrderItemEntity>> itemMap = iOrderItemService.mapByOrderIds(orderIds);
        List<MerchantOrderVo> voList = new ArrayList<>();
        for (OrderEntity record : records) {
            voList.add(toVo(record, itemMap.getOrDefault(record.getId(), Collections.emptyList())));
        }
        return voList;
    }

    private void confirmOrder(OrderEntity order, int currentStatus, String category) {
        if (currentStatus != MerchantOrderStatusGroup.PENDING_CONFIRM) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
        order.setStatusText(MerchantOrderStatusSupport.statusTextAfterTransition(category, MerchantOrderStatusGroup.PENDING_USE));
    }

    private void shipOrder(OrderEntity order, int currentStatus, String category, MerchantOrderActionBo actionBo) {
        if (!MerchantOrderStatusSupport.isGoodsCategory(category)) {
            throw new BizException(AppError.MERCHANT_ORDER_ACTION_INVALID);
        }
        if (currentStatus != MerchantOrderStatusGroup.PENDING_USE
                && !(currentStatus == MerchantOrderStatusGroup.PENDING_CONFIRM
                && MerchantOrderStatusSupport.isGoodsCategory(category))) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        order.setStatus(MerchantOrderStatusGroup.PENDING_RECEIVE);
        order.setStatusText(MerchantOrderStatusSupport.statusTextAfterTransition(category, MerchantOrderStatusGroup.PENDING_RECEIVE));
        appendLogisticsRemark(order, actionBo);
    }

    private void completeOrder(OrderEntity order, int currentStatus, String category) {
        if (MerchantOrderStatusSupport.isGoodsCategory(category)) {
            if (currentStatus != MerchantOrderStatusGroup.PENDING_USE
                    && currentStatus != MerchantOrderStatusGroup.PENDING_RECEIVE) {
                throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
            }
        } else if ("hotel".equals(MerchantOrderStatusSupport.normalizeCategory(category))) {
            if (currentStatus != MerchantOrderStatusGroup.PENDING_USE) {
                throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
            }
            if (order.getVerifyTime() == null) {
                throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
            }
        } else if (currentStatus != MerchantOrderStatusGroup.PENDING_USE
                && currentStatus != MerchantOrderStatusGroup.PENDING_RECEIVE) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        order.setStatus(MerchantOrderStatusGroup.COMPLETED);
        if ("restaurant".equals(MerchantOrderStatusSupport.normalizeCategory(category))) {
            order.setStatusText(MerchantOrderStatusSupport.restaurantLabelOfApiStatus(
                    MerchantOrderStatusSupport.COMPLETED));
        } else {
            order.setStatusText(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.COMPLETED));
        }
    }

    private void verifyOrder(OrderEntity order, int currentStatus, String category) {
        String normalized = MerchantOrderStatusSupport.normalizeCategory(category);
        if (!"hotel".equals(normalized) && !"route".equals(normalized)) {
            throw new BizException(AppError.MERCHANT_ORDER_ACTION_INVALID);
        }
        if (currentStatus != MerchantOrderStatusGroup.PENDING_USE) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        if (order.getVerifyTime() != null) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        order.setVerifyTime(LocalDateTime.now());
        order.setStatus(MerchantOrderStatusGroup.COMPLETED);
        if ("route".equals(normalized)) {
            order.setStatusText(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.COMPLETED));
        } else {
            order.setStatusText(AppOrderStatusSupport.hotelCheckedInLabel());
        }
    }

    private void cancelOrder(OrderEntity order, int currentStatus, String reason) {
        if (currentStatus == MerchantOrderStatusGroup.COMPLETED
                || currentStatus == MerchantOrderStatusGroup.CANCELLED) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        order.setStatus(MerchantOrderStatusGroup.CANCELLED);
        order.setStatusText(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.CANCELLED));
        if (StringUtils.isNotBlank(reason)) {
            order.setRemark(appendRemark(order.getRemark(), "取消原因：" + StringUtils.trim(reason)));
        }
        couponOrderSupport.restoreCouponIfNeeded(order);
        if ("hotel".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))
                && currentStatus != MerchantOrderStatusGroup.UNPAID) {
            orderSettlementSupport.refundPaidOrderToUser(order, "酒店订单取消退款");
        }
    }

    private void agreeRefund(OrderEntity order, int currentStatus) {
        if (currentStatus != MerchantOrderStatusGroup.REFUNDING) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        order.setStatus(MerchantOrderStatusGroup.CANCELLED);
        order.setStatusText(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.CANCELLED));
        order.setRemark(appendRemark(order.getRemark(), "退款已完成"));
        restoreGoodsStockIfNeeded(order);
        couponOrderSupport.restoreCouponIfNeeded(order);
        if ("hotel".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            orderSettlementSupport.refundPaidOrderToUser(order, "酒店订单退款");
        }
    }

    private String resolveMerchantStatusLabel(OrderEntity order, String apiStatus) {
        if (AppOrderStatusSupport.isHotelCheckedIn(order)) {
            return AppOrderStatusSupport.hotelCheckedInLabel();
        }
        if ("restaurant".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            return MerchantOrderStatusSupport.restaurantLabelOfApiStatus(apiStatus);
        }
        return MerchantOrderStatusSupport.labelOfApiStatus(apiStatus);
    }

    private void appendLogisticsRemark(OrderEntity order, MerchantOrderActionBo actionBo) {
        String company = StringUtils.trimToNull(actionBo.getLogisticsCompany());
        String logisticsNo = StringUtils.trimToNull(actionBo.getLogisticsNo());
        if (company == null && logisticsNo == null) {
            return;
        }
        StringBuilder builder = new StringBuilder();
        builder.append("物流信息：");
        if (company != null) {
            builder.append(company);
        }
        if (logisticsNo != null) {
            if (company != null) {
                builder.append(" ");
            }
            builder.append(logisticsNo);
        }
        order.setRemark(appendRemark(order.getRemark(), builder.toString()));
    }

    private String appendRemark(String original, String extra) {
        if (StringUtils.isBlank(original)) {
            return extra;
        }
        return original + "；" + extra;
    }

    private OrderEntity requireOwnedOrder(Long merchantId, String orderKey) {
        OrderEntity order = findOrderByKey(orderKey);
        if (order == null || !Objects.equals(order.getMerchantId(), merchantId)) {
            throw new BizException(AppError.MERCHANT_ORDER_NOT_FOUND);
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
        LambdaQueryWrapper<OrderEntity> byCodeWrapper = new LambdaQueryWrapper<>();
        byCodeWrapper.eq(OrderEntity::getVerifyCode, orderKey.trim()).last("LIMIT 1");
        OrderEntity byVerifyCode = iOrderService.getOne(byCodeWrapper, false);
        if (byVerifyCode != null) {
            return byVerifyCode;
        }
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getOrderNo, orderKey).last("LIMIT 1");
        return iOrderService.getOne(queryWrapper, false);
    }

    private void applyTabFilter(LambdaQueryWrapper<OrderEntity> queryWrapper, String tab) {
        if ("pending".equalsIgnoreCase(tab)) {
            queryWrapper.and(wrapper -> wrapper
                    .and(inner -> inner.eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_CONFIRM)
                            .ne(OrderEntity::getOrderCategory, "hotel"))
                    .or(inner -> inner.eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_USE)
                            .in(OrderEntity::getOrderCategory, "goods", "culture")));
            return;
        }
        if ("processing".equalsIgnoreCase(tab)) {
            queryWrapper.and(wrapper -> wrapper.in(OrderEntity::getStatus,
                            MerchantOrderStatusGroup.PENDING_RECEIVE, MerchantOrderStatusGroup.REFUNDING)
                    .or(inner -> inner.eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_USE)
                            .in(OrderEntity::getOrderCategory, "goods", "culture", "restaurant"))
                    .or(inner -> inner.eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_CONFIRM)
                            .eq(OrderEntity::getOrderCategory, "hotel")));
            return;
        }
        if ("completed".equalsIgnoreCase(tab)) {
            queryWrapper.in(OrderEntity::getStatus,
                    MerchantOrderStatusGroup.COMPLETED, MerchantOrderStatusGroup.CANCELLED);
            return;
        }
        if ("verify".equalsIgnoreCase(tab)) {
            queryWrapper.in(OrderEntity::getOrderCategory, "hotel", "route")
                    .eq(OrderEntity::getStatus, MerchantOrderStatusGroup.PENDING_USE)
                    .isNull(OrderEntity::getVerifyTime);
        }
    }

    private void completeRouteRefund(OrderEntity order, String action) {
        if (!OrderTypeSupport.isRouteFlow(OrderTypeSupport.resolve(order))) {
            return;
        }
        RouteCancelPolicySupport.RefundScene scene = "refund_weather".equals(action)
                ? RouteCancelPolicySupport.RefundScene.WEATHER_FULL
                : routeCancelPolicySupport.resolveScene(order, LocalDateTime.now());
        RouteCancelPolicySupport.RefundQuote quote = routeCancelPolicySupport.quoteRefund(order, scene, LocalDateTime.now());
        orderSettlementSupport.settleOnRouteRefundComplete(order, quote);
    }

    private void refundWeather(OrderEntity order, int currentStatus, String category) {
        if (!"route".equals(MerchantOrderStatusSupport.normalizeCategory(category))) {
            throw new BizException(AppError.MERCHANT_ORDER_ACTION_INVALID);
        }
        if (currentStatus != MerchantOrderStatusGroup.PENDING_USE) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        if (order.getVerifyTime() != null) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        if (!routeCancelPolicySupport.isPaidRouteOrder(order)) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
        order.setStatus(MerchantOrderStatusGroup.CANCELLED);
        order.setStatusText(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.CANCELLED));
        order.setRemark(appendRemark(order.getRemark(), "商户天气原因全额退款"));
        couponOrderSupport.restoreCouponIfNeeded(order);
    }

    private MerchantOrderVo toVo(OrderEntity order, List<OrderItemEntity> items) {
        MerchantOrderVo vo = new MerchantOrderVo();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        String apiStatus = MerchantOrderStatusSupport.toApiStatus(order);
        vo.setStatus(apiStatus);
        vo.setStatusLabel(resolveMerchantStatusLabel(order, apiStatus));
        vo.setCustomerName(order.getContactName());
        vo.setCustomerPhone(order.getContactPhone());
        vo.setAmount(defaultDecimal(order.getPayAmount()));
        vo.setTotalAmount(defaultDecimal(order.getTotalAmount()));
        vo.setDiscountAmount(defaultDecimal(order.getDiscountAmount()));
        vo.setGuestNames(order.getGuestNames());
        vo.setRoomCount(order.getRoomCount());
        vo.setExpectedArrivalTime(order.getExpectedArrivalTime());
        vo.setMeetPoint(order.getMeetPoint());
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setCreatedAt(formatDateTime(order.getCreateTime()));
        vo.setPaidAt(formatDateTime(order.getPayTime()));
        vo.setRemark(order.getRemark());
        OrderType resolvedType = OrderTypeSupport.resolve(order, items);
        vo.setOrderType(resolvedType.getCode());
        vo.setOrderTypeLabel(OrderTypeSupport.labelOf(resolvedType));
        vo.setOrderCategory(resolvedType.getCode());
        vo.setVerifiedAt(formatDateTime(order.getVerifyTime()));
        vo.setVerified(order.getVerifyTime() != null);
        vo.setVerifyCode(OrderVerifyCodeSupport.resolveVerifyCode(order.getOrderNo(), order.getVerifyCode()));
        vo.setItems(toItemVos(items));
        vo.setCustomer(order.getContactName());
        vo.setTime(vo.getCreatedAt());

        if (CollectionUtils.isNotEmpty(vo.getItems())) {
            String title = vo.getItems().get(0).getName();
            if (vo.getItems().size() > 1) {
                title = title + " 等";
            }
            vo.setTitle(title);
        } else {
            vo.setTitle(order.getOrderNo());
        }

        fillCategoryFields(vo, order);
        if (OrderTypeSupport.isRouteFlow(resolvedType) && StringUtils.isNotBlank(order.getGuestInfoJson())) {
            vo.setGuestList(OrderGuestInfoSupport.toVoList(order.getGuestInfoJson(), false));
        }
        if (OrderTypeSupport.isGoodsFlow(resolvedType)) {
            vo.setShippingFee(calcGoodsShippingFee(order, items));
        }
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

    private void fillCategoryFields(MerchantOrderVo vo, OrderEntity order) {
        OrderType type = OrderType.fromCode(vo.getOrderType());
        if (type == null) {
            type = OrderTypeSupport.resolve(order);
        }
        if (OrderTypeSupport.isHotelFlow(type)) {
            vo.setCheckIn(formatDate(order.getCheckInDate()));
            vo.setCheckOut(formatDate(order.getCheckOutDate()));
            vo.setNights(order.getNights());
            vo.setGuestCount(order.getGuestCount());
            return;
        }
        if (OrderTypeSupport.isRouteFlow(type)) {
            vo.setTourDate(formatDate(order.getTravelDate()));
            vo.setGuestCount(order.getGuestCount());
            return;
        }
        if (type == OrderType.RESTAURANT) {
            vo.setReservationDate(formatDate(order.getTravelDate()));
            vo.setGuestCount(order.getGuestCount());
            parseReservationTime(order.getRemark(), vo);
            return;
        }
        if (OrderTypeSupport.isGoodsFlow(type)) {
            vo.setAddress(order.getReceiverAddress());
        }
    }

    private void parseReservationTime(String remark, MerchantOrderVo vo) {
        if (StringUtils.isBlank(remark) || !remark.contains("预约时间：")) {
            return;
        }
        int index = remark.indexOf("预约时间：");
        String tail = remark.substring(index + "预约时间：".length());
        int end = tail.indexOf('；');
        vo.setReservationTime(end >= 0 ? tail.substring(0, end) : tail);
    }

    private List<MerchantOrderItemVo> toItemVos(List<OrderItemEntity> items) {
        if (CollectionUtils.isEmpty(items)) {
            return Collections.emptyList();
        }
        List<MerchantOrderItemVo> voList = new ArrayList<>();
        for (OrderItemEntity item : items) {
            MerchantOrderItemVo vo = new MerchantOrderItemVo();
            vo.setName(item.getItemName());
            vo.setSpec(item.getItemSpec());
            vo.setPrice(defaultDecimal(item.getUnitPrice()));
            vo.setQty(item.getQuantity() == null ? 1 : item.getQuantity());
            vo.setCover(StringUtils.defaultString(item.getCoverImage()));
            voList.add(vo);
        }
        return voList;
    }

    private String normalizeAction(String action) {
        if (StringUtils.isBlank(action)) {
            return action;
        }
        String normalized = action.trim().toLowerCase();
        if ("checkout".equals(normalized) || "confirmcomplete".equals(normalized)) {
            return "complete";
        }
        if ("verify".equals(normalized) || "verifyorder".equals(normalized) || "confirmcheckin".equals(normalized)) {
            return "verify";
        }
        if (Arrays.asList("confirmtour", "confirmreservation", "confirmorder").contains(normalized)) {
            return "confirm";
        }
        if ("confirmship".equals(normalized)) {
            return "ship";
        }
        if ("refund_agree".equals(normalized) || "refundagree".equals(normalized) || "handlerefund".equals(normalized)) {
            return "refund_agree";
        }
        return normalized;
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

    private void restoreGoodsStockIfNeeded(OrderEntity order) {
        if (!MerchantOrderStatusSupport.isGoodsCategory(order.getOrderCategory())) {
            return;
        }
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        for (OrderItemEntity item : items) {
            if (!"goods".equals(item.getItemType()) || item.getItemId() == null) {
                continue;
            }
            LambdaUpdateWrapper<MerchantServiceEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MerchantServiceEntity::getId, item.getItemId())
                    .setSql("stock = stock + " + (item.getQuantity() == null ? 1 : item.getQuantity()));
            iMerchantServiceItemService.update(updateWrapper);
        }
    }

    private void sendMerchantActionMessage(OrderEntity order, String action) {
        if (order.getUserId() == null) {
            return;
        }
        String orderNo = order.getOrderNo();
        switch (action) {
            case "confirm":
                instationMessageSupport.sendUserMessage(order.getUserId(), "订单已确认",
                        "您的订单 " + orderNo + " 已确认", order.getId());
                break;
            case "ship":
                instationMessageSupport.sendUserMessage(order.getUserId(), "订单已发货",
                        "您的订单 " + orderNo + " 已发货", order.getId());
                break;
            case "complete":
                instationMessageSupport.sendUserMessage(order.getUserId(), "订单已完成",
                        "您的订单 " + orderNo + " 已完成", order.getId());
                break;
            case "verify":
                instationMessageSupport.sendUserMessage(order.getUserId(), "入住办理成功",
                        "您的订单 " + orderNo + " 已办理入住", order.getId());
                break;
            case "cancel":
            case "reject":
                instationMessageSupport.sendUserMessage(order.getUserId(), "订单已取消",
                        "您的订单 " + orderNo + " 已被取消", order.getId());
                break;
            case "refund_agree":
                instationMessageSupport.sendUserMessage(order.getUserId(), "退款已同意",
                        "您的订单 " + orderNo + " 退款已同意", order.getId());
                break;
            default:
                break;
        }
    }

    @Override
    public long countPaidOrders(Long merchantId, LocalDateTime start, LocalDateTime end) {
        if (merchantId == null) {
            return 0L;
        }
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getMerchantId, merchantId)
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PAID);
        if (start != null) {
            queryWrapper.ge(OrderEntity::getCreateTime, start);
        }
        if (end != null) {
            queryWrapper.le(OrderEntity::getCreateTime, end);
        }
        return iOrderService.count(queryWrapper);
    }

    @Override
    public BigDecimal sumPaidAmount(Long merchantId, LocalDateTime start, LocalDateTime end) {
        if (merchantId == null) {
            return BigDecimal.ZERO;
        }
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("IFNULL(SUM(pay_amount),0) AS total")
                .eq("merchant_id", merchantId)
                .in("status", MerchantOrderStatusGroup.PAID);
        if (start != null) {
            queryWrapper.ge("pay_time", start);
        }
        if (end != null) {
            queryWrapper.le("pay_time", end);
        }
        List<Map<String, Object>> maps = iOrderService.listMaps(queryWrapper);
        if (CollectionUtils.isEmpty(maps)) {
            return BigDecimal.ZERO;
        }
        Object total = maps.get(0).get("total");
        if (total == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(total));
    }
}

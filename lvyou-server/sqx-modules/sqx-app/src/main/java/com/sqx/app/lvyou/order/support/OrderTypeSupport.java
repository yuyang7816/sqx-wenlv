package com.sqx.app.lvyou.order.support;

import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.enums.OrderType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 订单类型解析与写入（orderType 为权威字段，orderCategory 与之同步以兼容旧逻辑）
 */
public final class OrderTypeSupport {

    private static final Set<String> USER_TAB_TYPES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList(
                    OrderType.HOTEL.getCode(),
                    OrderType.GOODS.getCode(),
                    OrderType.ROUTE.getCode())));

    private OrderTypeSupport() {
    }

    public static OrderType resolve(OrderEntity order) {
        return resolve(order, null);
    }

    public static OrderType resolve(OrderEntity order, List<OrderItemEntity> items) {
        if (order == null) {
            return OrderType.GOODS;
        }
        OrderType fromType = OrderType.fromCode(order.getOrderType());
        if (fromType != null) {
            return fromType;
        }
        OrderType fromCategory = OrderType.fromCode(order.getOrderCategory());
        if (fromCategory != null) {
            return fromCategory;
        }
        if (order.getCheckInDate() != null) {
            return OrderType.HOTEL;
        }
        if (order.getTravelDate() != null && order.getGuestCount() != null) {
            return OrderType.ROUTE;
        }
        if (CollectionUtils.isNotEmpty(items)) {
            String itemType = items.get(0).getItemType();
            if ("room".equals(itemType)) {
                return OrderType.HOTEL;
            }
            if ("route".equals(itemType)) {
                return OrderType.ROUTE;
            }
            if ("goods".equals(itemType)) {
                return OrderType.GOODS;
            }
        }
        return OrderType.GOODS;
    }

    public static void apply(OrderEntity order, OrderType type) {
        if (order == null || type == null) {
            return;
        }
        order.setOrderType(type.getCode());
        order.setOrderCategory(type.getCode());
    }

    public static String labelOf(OrderType type) {
        return type == null ? "" : type.getLabel();
    }

    /** C 端订单 Tab 分类（goods Tab 含 goods/culture/service） */
    public static String userTabType(OrderType type) {
        if (type == null) {
            return OrderType.GOODS.getCode();
        }
        switch (type) {
            case HOTEL:
                return OrderType.HOTEL.getCode();
            case ROUTE:
                return OrderType.ROUTE.getCode();
            case CULTURE:
            case SERVICE:
            case GOODS:
                return OrderType.GOODS.getCode();
            default:
                return "";
        }
    }

    public static boolean isUserTabType(String tabType) {
        return USER_TAB_TYPES.contains(StringUtils.defaultString(tabType).toLowerCase());
    }

    public static boolean isGoodsFlow(OrderType type) {
        return type == OrderType.GOODS || type == OrderType.CULTURE || type == OrderType.SERVICE;
    }

    public static boolean isHotelFlow(OrderType type) {
        return type == OrderType.HOTEL;
    }

    public static boolean isRouteFlow(OrderType type) {
        return type == OrderType.ROUTE;
    }

    public static boolean isRestaurantFlow(OrderType type) {
        return type == OrderType.RESTAURANT;
    }

    /** 需在线支付的订单类型 */
    public static boolean isOnlinePayFlow(OrderType type) {
        return isGoodsFlow(type) || isHotelFlow(type) || isRouteFlow(type);
    }
}

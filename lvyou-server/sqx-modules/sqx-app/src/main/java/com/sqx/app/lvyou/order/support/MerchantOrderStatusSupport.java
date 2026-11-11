package com.sqx.app.lvyou.order.support;

import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 商户订单状态：数据库整型 ↔ 前端 API 字符串映射
 */
public final class MerchantOrderStatusSupport {

    public static final String PENDING_CONFIRM = "pending_confirm";
    public static final String PENDING_SHIP = "pending_ship";
    public static final String SHIPPED = "shipped";
    public static final String PENDING_CHECKIN = "pending_checkin";
    public static final String ON_TOUR = "on_tour";
    /** 餐饮预约：已确认，等待客人到店（非备餐） */
    public static final String PENDING_ARRIVAL = "pending_arrival";
    /** @deprecated 旧版 API 状态，餐饮已改为 {@link #PENDING_ARRIVAL} */
    public static final String PREPARING = "preparing";
    public static final String COMPLETED = "completed";
    public static final String CANCELLED = "cancelled";
    public static final String REFUNDING = "refunding";

    private static final Set<String> GOODS_CATEGORIES = Collections.unmodifiableSet(
            new HashSet<>(Arrays.asList("goods", "culture", "service")));

    private MerchantOrderStatusSupport() {
    }

    public static String toApiStatus(OrderEntity order) {
        if (order == null || order.getStatus() == null) {
            return PENDING_CONFIRM;
        }
        int status = order.getStatus();
        String category = normalizeCategory(order.getOrderCategory());
        if (status == MerchantOrderStatusGroup.PENDING_CONFIRM) {
            if ("hotel".equals(category)) {
                return PENDING_CHECKIN;
            }
            if (isGoodsCategory(category)) {
                return PENDING_SHIP;
            }
            return PENDING_CONFIRM;
        }
        if (status == MerchantOrderStatusGroup.PENDING_USE) {
            if (isGoodsCategory(category)) {
                return PENDING_SHIP;
            }
            if ("hotel".equals(category)) {
                if (order.getVerifyTime() != null) {
                    return COMPLETED;
                }
                return PENDING_CHECKIN;
            }
            if ("route".equals(category)) {
                return ON_TOUR;
            }
            if ("restaurant".equals(category)) {
                return PENDING_ARRIVAL;
            }
            return PENDING_CHECKIN;
        }
        if (status == MerchantOrderStatusGroup.PENDING_RECEIVE) {
            return SHIPPED;
        }
        if (status == MerchantOrderStatusGroup.COMPLETED) {
            return COMPLETED;
        }
        if (status == MerchantOrderStatusGroup.CANCELLED) {
            return CANCELLED;
        }
        if (status == MerchantOrderStatusGroup.REFUNDING) {
            return REFUNDING;
        }
        return PENDING_CONFIRM;
    }

    public static String labelOfApiStatus(String apiStatus) {
        if (StringUtils.isBlank(apiStatus)) {
            return "待确认";
        }
        switch (apiStatus) {
            case PENDING_CONFIRM:
                return "待确认";
            case PENDING_SHIP:
                return "待发货";
            case SHIPPED:
                return "已发货";
            case PENDING_CHECKIN:
                return "待入住";
            case ON_TOUR:
                return "待出行";
            case PENDING_ARRIVAL:
            case PREPARING:
                return "待到店";
            case COMPLETED:
                return "已完成";
            case CANCELLED:
                return "已取消";
            case REFUNDING:
                return "退款中";
            default:
                return apiStatus;
        }
    }

    public static String restaurantLabelOfApiStatus(String apiStatus) {
        if (StringUtils.isBlank(apiStatus)) {
            return "待确认";
        }
        switch (apiStatus) {
            case PENDING_CONFIRM:
                return "待确认";
            case PENDING_ARRIVAL:
            case PREPARING:
                return "待到店";
            case COMPLETED:
                return "已到店";
            case CANCELLED:
                return "已取消";
            case REFUNDING:
                return "退款中";
            default:
                return labelOfApiStatus(apiStatus);
        }
    }

    public static String statusTextAfterTransition(String category, int nextStatus) {
        String apiStatus = toApiStatusFromParts(category, nextStatus);
        if ("restaurant".equals(normalizeCategory(category))) {
            return restaurantLabelOfApiStatus(apiStatus);
        }
        return labelOfApiStatus(apiStatus);
    }

    public static String toApiStatusFromParts(String orderCategory, int status) {
        OrderEntity entity = new OrderEntity();
        entity.setOrderCategory(orderCategory);
        entity.setStatus(status);
        return toApiStatus(entity);
    }

    public static boolean isGoodsCategory(String category) {
        return GOODS_CATEGORIES.contains(normalizeCategory(category));
    }

    public static String normalizeCategory(String category) {
        if (StringUtils.isBlank(category)) {
            return "goods";
        }
        String normalized = category.toLowerCase();
        if ("travel".equals(normalized)) {
            return "route";
        }
        return normalized;
    }

    public static String categoryFromStoreType(String storeType) {
        String normalized = MerchantStoreType.normalize(storeType);
        if ("travel".equals(normalized)) {
            return "route";
        }
        if ("culture".equals(normalized)) {
            return "culture";
        }
        if ("restaurant".equals(normalized)) {
            return "restaurant";
        }
        return "hotel";
    }

    public static List<Integer> tabPendingStatuses() {
        return Collections.singletonList(MerchantOrderStatusGroup.PENDING_CONFIRM);
    }

    public static boolean matchesTab(String tab, OrderEntity order) {
        String normalizedTab = StringUtils.defaultIfBlank(tab, "all");
        if ("all".equalsIgnoreCase(normalizedTab)) {
            return true;
        }
        String apiStatus = toApiStatus(order);
        if ("pending".equalsIgnoreCase(normalizedTab)) {
            return PENDING_CONFIRM.equals(apiStatus) || PENDING_SHIP.equals(apiStatus);
        }
        if ("processing".equalsIgnoreCase(normalizedTab)) {
            return SHIPPED.equals(apiStatus) || PENDING_CHECKIN.equals(apiStatus)
                    || ON_TOUR.equals(apiStatus) || PENDING_ARRIVAL.equals(apiStatus)
                    || PREPARING.equals(apiStatus) || REFUNDING.equals(apiStatus);
        }
        if ("completed".equalsIgnoreCase(normalizedTab)) {
            return COMPLETED.equals(apiStatus) || CANCELLED.equals(apiStatus);
        }
        return true;
    }
}

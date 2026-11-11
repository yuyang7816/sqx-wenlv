package com.sqx.app.lvyou.order.support;

import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户端订单状态：数据库整型 ↔ API 字符串 ↔ 中文展示
 */
public final class AppOrderStatusSupport {

    public static final String PENDING_PAY = "pending_pay";
    public static final String PENDING_CONFIRM = "pending_confirm";
    public static final String PENDING_USE = "pending_use";
    public static final String PENDING_RECEIVE = "pending_receive";
    public static final String COMPLETED = "completed";
    public static final String CANCELLED = "cancelled";
    public static final String REFUNDING = "refunding";
    public static final String REFUNDED = "refunded";
    public static final String HOTEL_CHECKED_IN_LABEL = "已入住";

    private AppOrderStatusSupport() {
    }

    public static String hotelCheckedInLabel() {
        return HOTEL_CHECKED_IN_LABEL;
    }

    public static boolean isHotelCheckedIn(OrderEntity order) {
        if (order == null || !"hotel".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            return false;
        }
        if (order.getVerifyTime() == null) {
            return false;
        }
        Integer status = order.getStatus();
        return status != null && (status == MerchantOrderStatusGroup.COMPLETED
                || status == MerchantOrderStatusGroup.PENDING_USE
                || status == MerchantOrderStatusGroup.PENDING_CONFIRM);
    }

    public static boolean isRefundedOrder(OrderEntity order) {
        if (order == null || order.getStatus() == null) {
            return false;
        }
        if (order.getStatus() != MerchantOrderStatusGroup.CANCELLED) {
            return false;
        }
        String remark = StringUtils.defaultString(order.getRemark());
        return remark.contains("退款") || remark.contains("免费取消");
    }

    public static String toApiStatus(OrderEntity order) {
        if (order == null || order.getStatus() == null) {
            return PENDING_PAY;
        }
        if (isHotelCheckedIn(order)) {
            return COMPLETED;
        }
        String category = MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory());
        switch (order.getStatus()) {
            case MerchantOrderStatusGroup.UNPAID:
                return PENDING_PAY;
            case MerchantOrderStatusGroup.PENDING_CONFIRM:
                if ("hotel".equals(category)) {
                    return PENDING_USE;
                }
                if (MerchantOrderStatusSupport.isGoodsCategory(category)) {
                    return PENDING_USE;
                }
                return PENDING_CONFIRM;
            case MerchantOrderStatusGroup.PENDING_USE:
                return PENDING_USE;
            case MerchantOrderStatusGroup.PENDING_RECEIVE:
                return PENDING_RECEIVE;
            case MerchantOrderStatusGroup.COMPLETED:
                return COMPLETED;
            case MerchantOrderStatusGroup.CANCELLED:
                return CANCELLED;
            case MerchantOrderStatusGroup.REFUNDING:
                return REFUNDING;
            default:
                return PENDING_PAY;
        }
    }

    public static String toDisplayStatus(OrderEntity order) {
        if (isHotelCheckedIn(order)) {
            return HOTEL_CHECKED_IN_LABEL;
        }
        String apiStatus = toApiStatus(order);
        String category = MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory());
        if (PENDING_PAY.equals(apiStatus)) {
            return "待支付";
        }
        if (PENDING_CONFIRM.equals(apiStatus)) {
            if ("hotel".equals(category)) {
                return "待入住";
            }
            if (MerchantOrderStatusSupport.isGoodsCategory(category)) {
                return "待发货";
            }
            return "待确认";
        }
        if (PENDING_USE.equals(apiStatus)) {
            if ("hotel".equals(category)) {
                return "待入住";
            }
            if ("route".equals(category)) {
                return "待出行";
            }
            if (MerchantOrderStatusSupport.isGoodsCategory(category)) {
                return "待发货";
            }
            return "待使用";
        }
        if (PENDING_RECEIVE.equals(apiStatus)) {
            return "待收货";
        }
        if (COMPLETED.equals(apiStatus)) {
            if ("hotel".equals(category)) {
                return HOTEL_CHECKED_IN_LABEL;
            }
            return "已完成";
        }
        if (CANCELLED.equals(apiStatus)) {
            if (isRefundedOrder(order)) {
                return "已退款";
            }
            return "已取消";
        }
        if (REFUNDING.equals(apiStatus)) {
            return "退款中";
        }
        return apiStatus;
    }

    public static String statusColor(String category, String apiStatus) {
        if ("hotel".equals(category)) {
            return "#2979FF";
        }
        if ("route".equals(category)) {
            return "#FF6600";
        }
        if (MerchantOrderStatusSupport.isGoodsCategory(category)) {
            return "#B45D2D";
        }
        return "#666666";
    }

    public static List<Integer> resolveStatusFilter(String status) {
        return resolveStatusFilter(status, null);
    }

    public static List<Integer> resolveStatusFilter(String status, String category) {
        if (StringUtils.isBlank(status) || "all".equalsIgnoreCase(status)) {
            return Collections.emptyList();
        }
        String normalized = status.trim().toLowerCase();
        String normalizedCategory = normalizeCategory(category);
        switch (normalized) {
            case PENDING_PAY:
            case "pendingpay":
            case "待支付":
                return Collections.singletonList(MerchantOrderStatusGroup.UNPAID);
            case PENDING_CONFIRM:
            case "pendingconfirm":
            case "待确认":
                return Collections.singletonList(MerchantOrderStatusGroup.PENDING_CONFIRM);
            case PENDING_USE:
            case "pendinguse":
            case "待入住":
            case "待出行":
            case "待发货":
            case "待使用":
                if ("hotel".equals(normalizedCategory)
                        || MerchantOrderStatusSupport.isGoodsCategory(normalizedCategory)) {
                    return Stream.of(MerchantOrderStatusGroup.PENDING_USE, MerchantOrderStatusGroup.PENDING_CONFIRM)
                            .distinct()
                            .collect(Collectors.toList());
                }
                return Collections.singletonList(MerchantOrderStatusGroup.PENDING_USE);
            case PENDING_RECEIVE:
            case "pendingreceive":
            case "待收货":
                return Collections.singletonList(MerchantOrderStatusGroup.PENDING_RECEIVE);
            case COMPLETED:
            case "已完成":
                return Collections.singletonList(MerchantOrderStatusGroup.COMPLETED);
            case CANCELLED:
            case "已取消":
                return Collections.singletonList(MerchantOrderStatusGroup.CANCELLED);
            case REFUNDING:
            case "退款中":
                return Collections.singletonList(MerchantOrderStatusGroup.REFUNDING);
            case REFUNDED:
            case "已退款":
                return Collections.singletonList(MerchantOrderStatusGroup.CANCELLED);
            default:
                return Collections.emptyList();
        }
    }

    public static boolean isRefundedStatusFilter(String status) {
        return StringUtils.isNotBlank(status)
                && (REFUNDED.equalsIgnoreCase(status.trim()) || "已退款".equals(status.trim()));
    }

    public static boolean isCancelledStatusFilter(String status) {
        return StringUtils.isNotBlank(status)
                && (CANCELLED.equalsIgnoreCase(status.trim()) || "已取消".equals(status.trim()));
    }

    public static boolean isHotelPendingUseFilter(String status, String category) {
        return StringUtils.isNotBlank(status)
                && PENDING_USE.equalsIgnoreCase(status.trim())
                && "hotel".equals(normalizeCategory(category));
    }

    /** 餐饮预约同步商户端的影子单分类，归属 canteen 域，不计入用户端订单 Tab 与平台订单统计 */
    public static final String RESTAURANT_SHADOW_CATEGORY = "restaurant";

    public static Set<String> allowedCategories() {
        return Collections.unmodifiableSet(new HashSet<>(Arrays.asList("hotel", "route", "goods", "culture", "service")));
    }

    /**
     * 是否排除在平台订单统计之外（餐饮预约影子单等）
     */
    public static boolean excludedFromPlatformOrderStats(String category) {
        return RESTAURANT_SHADOW_CATEGORY.equals(normalizeCategory(category));
    }

    public static String normalizeCategory(String category) {
        return MerchantOrderStatusSupport.normalizeCategory(category);
    }
}

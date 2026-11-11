package com.sqx.app.lvyou.hotel.support;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 酒店退改规则引擎
 */
public final class HotelCancelPolicySupport {

    public static final String POLICY_FREE_BEFORE_CHECKIN = "free_before_checkin_18";

    private HotelCancelPolicySupport() {
    }

    public static Map<String, Object> defaultPolicyMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("policyCode", POLICY_FREE_BEFORE_CHECKIN);
        map.put("freeCancelHour", HotelBookingConstants.FREE_CANCEL_HOUR);
        map.put("summary", "入住日18:00前可免费取消");
        map.put("holdUntilHour", HotelBookingConstants.HOLD_UNTIL_HOUR);
        return map;
    }

    public static String toJson(Map<String, Object> policy) {
        Map<String, Object> merged = defaultPolicyMap();
        if (policy != null) {
            merged.putAll(policy);
        }
        return JSON.toJSONString(merged);
    }

    public static Map<String, Object> parse(String json) {
        if (StringUtils.isBlank(json)) {
            return defaultPolicyMap();
        }
        try {
            Map<String, Object> map = JSON.parseObject(json, new TypeReference<Map<String, Object>>() {
            });
            Map<String, Object> merged = defaultPolicyMap();
            if (map != null) {
                merged.putAll(map);
            }
            return merged;
        } catch (Exception ex) {
            return defaultPolicyMap();
        }
    }

    public static String buildSnapshotJson(HotelEntity hotel, HotelRoomEntity room) {
        return toJson(defaultPolicyMap());
    }

    public static LocalDateTime freeCancelDeadline(OrderEntity order) {
        if (order == null || order.getCheckInDate() == null) {
            return null;
        }
        Map<String, Object> policy = parse(order.getCancelPolicyJson());
        int hour = readInt(policy, "freeCancelHour", HotelBookingConstants.FREE_CANCEL_HOUR);
        return order.getCheckInDate().atTime(hour, 0);
    }

    public static LocalDateTime holdUntilDeadline(OrderEntity order) {
        if (order == null || order.getCheckInDate() == null) {
            return null;
        }
        Map<String, Object> policy = parse(order.getCancelPolicyJson());
        int hour = readInt(policy, "holdUntilHour", HotelBookingConstants.HOLD_UNTIL_HOUR);
        return order.getCheckInDate().atTime(hour, 0);
    }

    /** 是否处于免费取消期 */
    public static boolean isFreeCancelAllowed(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getCheckInDate() == null) {
            return false;
        }
        LocalDateTime deadline = freeCancelDeadline(order);
        return deadline != null && (now == null || now.isBefore(deadline));
    }

    /** 是否已过入住保留时间且未核销（No-show 判定） */
    public static boolean isNoShowCandidate(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getVerifyTime() != null || order.getCheckInDate() == null) {
            return false;
        }
        LocalDateTime holdUntil = holdUntilDeadline(order);
        if (holdUntil == null || now == null || now.isBefore(holdUntil)) {
            return false;
        }
        // 保留时刻之后才完成支付的订单不应立刻判定 No-show（常见于当日晚间下单）
        if (order.getPayTime() != null && !order.getPayTime().isBefore(holdUntil)) {
            return false;
        }
        return true;
    }

    /** 酒店订单是否允许用户申请退款（No-show / 已过离店保留时刻不可退） */
    public static boolean isUserRefundAllowed(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getCheckInDate() == null) {
            return true;
        }
        if (isNoShowCandidate(order, now)) {
            return false;
        }
        if (order.getCheckOutDate() != null && now != null) {
            LocalDateTime checkoutDeadline = order.getCheckOutDate().atTime(HotelBookingConstants.AUTO_COMPLETE_HOUR, 0);
            if (!now.isBefore(checkoutDeadline)) {
                return false;
            }
        }
        return true;
    }

    /** 是否应自动完成（已入住且过离店日 14:00） */
    public static boolean shouldAutoComplete(OrderEntity order, LocalDateTime now) {
        if (order == null || order.getVerifyTime() == null || order.getCheckOutDate() == null) {
            return false;
        }
        LocalDateTime completeAt = order.getCheckOutDate().atTime(HotelBookingConstants.AUTO_COMPLETE_HOUR, 0);
        return now != null && !now.isBefore(completeAt);
    }

    public static String policySummary(OrderEntity order) {
        Map<String, Object> policy = parse(order == null ? null : order.getCancelPolicyJson());
        Object summary = policy.get("summary");
        return summary == null ? "入住日18:00前可免费取消" : String.valueOf(summary);
    }

    private static int readInt(Map<String, Object> map, String key, int fallback) {
        Object value = map.get(key);
        if (value == null) {
            return fallback;
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return fallback;
        }
    }
}

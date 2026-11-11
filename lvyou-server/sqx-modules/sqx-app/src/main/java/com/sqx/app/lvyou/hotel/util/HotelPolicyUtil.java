package com.sqx.app.lvyou.hotel.util;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.app.lvyou.hotel.domain.vo.HotelPolicyVo;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 酒店政策 JSON 解析与默认值
 */
public final class HotelPolicyUtil {

    private HotelPolicyUtil() {
    }

    public static Map<String, Object> defaultPolicyMap() {
        Map<String, Object> policy = new LinkedHashMap<>();
        policy.put("bookingNotice", "支付成功后订单生效，请按预约日期携带有效证件办理入住。");
        policy.put("checkInNotice", "前台 24 小时服务，请于入住时在柜台办理登记。");
        policy.put("depositPolicy", "不收取押金");
        policy.put("breakfastInfo", "");
        return policy;
    }

    public static HotelPolicyVo parse(String policyJson) {
        HotelPolicyVo defaults = toVo(defaultPolicyMap());
        if (StringUtils.isBlank(policyJson)) {
            return defaults;
        }
        try {
            Map<String, Object> map = JSON.parseObject(policyJson, new TypeReference<Map<String, Object>>() {
            });
            return mergeVo(defaults, map);
        } catch (Exception ex) {
            return defaults;
        }
    }

    public static HotelPolicyVo fromDecorMap(Map<String, Object> decor) {
        HotelPolicyVo defaults = toVo(defaultPolicyMap());
        if (decor == null || decor.isEmpty()) {
            return defaults;
        }
        Object value = decor.get("hotelPolicy");
        if (value instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) value;
            return mergeVo(defaults, map);
        }
        return defaults;
    }

    public static String toJson(Map<String, Object> policyMap) {
        Map<String, Object> merged = new LinkedHashMap<>(defaultPolicyMap());
        if (policyMap != null) {
            merged.putAll(policyMap);
        }
        return JSON.toJSONString(merged);
    }

    private static HotelPolicyVo mergeVo(HotelPolicyVo base, Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return base;
        }
        base.setBookingNotice(readText(map, "bookingNotice", base.getBookingNotice()));
        base.setCheckInNotice(readText(map, "checkInNotice", base.getCheckInNotice()));
        base.setPetPolicy(readText(map, "petPolicy", base.getPetPolicy()));
        base.setDepositPolicy(readText(map, "depositPolicy", base.getDepositPolicy()));
        base.setBreakfastInfo(readText(map, "breakfastInfo", base.getBreakfastInfo()));
        return base;
    }

    private static HotelPolicyVo toVo(Map<String, Object> map) {
        HotelPolicyVo vo = new HotelPolicyVo();
        vo.setBookingNotice(readText(map, "bookingNotice", ""));
        vo.setCheckInNotice(readText(map, "checkInNotice", ""));
        vo.setPetPolicy(readText(map, "petPolicy", ""));
        vo.setDepositPolicy(readText(map, "depositPolicy", ""));
        vo.setBreakfastInfo(readText(map, "breakfastInfo", ""));
        return vo;
    }

    private static String readText(Map<String, Object> map, String key, String fallback) {
        Object value = map.get(key);
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return fallback;
        }
        return String.valueOf(value).trim();
    }
}

package com.sqx.app.lvyou.heritage.shop.support;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopShippingVo;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 非遗好店运费配置解析与计算
 */
public final class HeritageShippingUtil {

    private HeritageShippingUtil() {
    }

    public static HeritageShopShippingVo defaultConfig() {
        HeritageShopShippingVo vo = new HeritageShopShippingVo();
        vo.setFreeShipping(Boolean.TRUE);
        vo.setFixedFee(BigDecimal.ZERO);
        vo.setFreeAmountThreshold(BigDecimal.ZERO);
        vo.setShippingNote("");
        return vo;
    }

    public static HeritageShopShippingVo parse(String raw) {
        HeritageShopShippingVo defaults = defaultConfig();
        if (StringUtils.isBlank(raw)) {
            return defaults;
        }
        try {
            JSONObject object = JSON.parseObject(raw.trim());
            if (object == null || object.isEmpty()) {
                return defaults;
            }
            return fromMap(object.getInnerMap(), defaults);
        } catch (Exception ignored) {
            return defaults;
        }
    }

    public static HeritageShopShippingVo fromMap(Map<String, Object> source, HeritageShopShippingVo defaults) {
        HeritageShopShippingVo target = defaults == null ? defaultConfig() : defaults;
        if (source == null || source.isEmpty()) {
            return target;
        }
        Object freeShipping = source.get("freeShipping");
        if (freeShipping instanceof Boolean) {
            target.setFreeShipping((Boolean) freeShipping);
        } else if (freeShipping != null) {
            target.setFreeShipping(Boolean.parseBoolean(String.valueOf(freeShipping)));
        }
        target.setFixedFee(readDecimal(source.get("fixedFee"), target.getFixedFee()));
        target.setFreeAmountThreshold(readDecimal(source.get("freeAmountThreshold"), target.getFreeAmountThreshold()));
        Object shippingNote = source.get("shippingNote");
        if (shippingNote != null) {
            target.setShippingNote(String.valueOf(shippingNote).trim());
        }
        return normalize(target);
    }

    public static String toJson(HeritageShopShippingVo config) {
        return JSON.toJSONString(toMap(normalize(config)));
    }

    public static String toJson(Map<String, Object> config) {
        return JSON.toJSONString(toMap(fromMap(config, defaultConfig())));
    }

    public static Map<String, Object> toMap(HeritageShopShippingVo config) {
        HeritageShopShippingVo normalized = normalize(config);
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("freeShipping", Boolean.TRUE.equals(normalized.getFreeShipping()));
        map.put("fixedFee", normalized.getFixedFee());
        map.put("freeAmountThreshold", normalized.getFreeAmountThreshold());
        map.put("shippingNote", StringUtils.defaultString(normalized.getShippingNote()));
        return map;
    }

    public static HeritageShopShippingVo normalize(HeritageShopShippingVo config) {
        HeritageShopShippingVo target = config == null ? defaultConfig() : config;
        if (target.getFreeShipping() == null) {
            target.setFreeShipping(Boolean.TRUE);
        }
        target.setFixedFee(normalizeMoney(target.getFixedFee()));
        target.setFreeAmountThreshold(normalizeMoney(target.getFreeAmountThreshold()));
        if (Boolean.TRUE.equals(target.getFreeShipping())) {
            target.setFixedFee(BigDecimal.ZERO);
        } else if (target.getFixedFee().compareTo(BigDecimal.ZERO) <= 0) {
            target.setFixedFee(BigDecimal.ZERO);
            target.setFreeShipping(Boolean.TRUE);
        }
        if (target.getShippingNote() == null) {
            target.setShippingNote("");
        } else {
            target.setShippingNote(target.getShippingNote().trim());
        }
        return target;
    }

    public static BigDecimal calculateFee(HeritageShopShippingVo config, BigDecimal goodsAmount) {
        HeritageShopShippingVo normalized = normalize(config);
        BigDecimal amount = normalizeMoney(goodsAmount);
        if (Boolean.TRUE.equals(normalized.getFreeShipping())) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        BigDecimal threshold = normalized.getFreeAmountThreshold();
        if (threshold.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(threshold) >= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return normalized.getFixedFee().setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal readDecimal(Object value, BigDecimal defaultValue) {
        if (value == null) {
            return defaultValue == null ? BigDecimal.ZERO : defaultValue;
        }
        if (value instanceof BigDecimal) {
            return normalizeMoney((BigDecimal) value);
        }
        if (value instanceof Number) {
            return normalizeMoney(BigDecimal.valueOf(((Number) value).doubleValue()));
        }
        try {
            return normalizeMoney(new BigDecimal(String.valueOf(value).trim()));
        } catch (NumberFormatException ex) {
            return defaultValue == null ? BigDecimal.ZERO : defaultValue;
        }
    }

    private static BigDecimal normalizeMoney(BigDecimal value) {
        if (value == null || value.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}

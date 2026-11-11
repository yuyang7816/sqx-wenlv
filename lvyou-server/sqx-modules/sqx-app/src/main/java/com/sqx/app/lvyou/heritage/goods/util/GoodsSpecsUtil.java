package com.sqx.app.lvyou.heritage.goods.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsSpecSkuVo;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 文创商品规格工具：单规格 / 多规格 SKU
 */
public final class GoodsSpecsUtil {

    public static final String MODE_SINGLE = "single";
    public static final String MODE_MULTI = "multi";

    private GoodsSpecsUtil() {
    }

    public static class GoodsSpecsPayload {
        private String mode = MODE_SINGLE;
        private List<GoodsSpecSkuVo> skus = new ArrayList<>();

        public String getMode() {
            return mode;
        }

        public void setMode(String mode) {
            this.mode = mode;
        }

        public List<GoodsSpecSkuVo> getSkus() {
            return skus;
        }

        public void setSkus(List<GoodsSpecSkuVo> skus) {
            this.skus = skus == null ? new ArrayList<>() : skus;
        }

        public boolean isMulti() {
            return MODE_MULTI.equalsIgnoreCase(mode) && CollectionUtils.size(skus) > 1;
        }
    }

    public static GoodsSpecsPayload parseMerchantRaw(String raw, BigDecimal fallbackPrice, Integer fallbackStock) {
        GoodsSpecsPayload payload = new GoodsSpecsPayload();
        BigDecimal price = fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice;
        int stock = fallbackStock == null ? 0 : fallbackStock;
        if (StringUtils.isBlank(raw)) {
            return payload;
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("{")) {
            try {
                JSONObject json = JSON.parseObject(trimmed);
                payload.setMode(MODE_MULTI.equalsIgnoreCase(json.getString("mode")) ? MODE_MULTI : MODE_SINGLE);
                payload.setSkus(parseSkuArray(json.getJSONArray("skus"), price, stock));
                if (MODE_MULTI.equalsIgnoreCase(payload.getMode()) && CollectionUtils.isNotEmpty(payload.getSkus())) {
                    return payload;
                }
                if (CollectionUtils.size(payload.getSkus()) == 1) {
                    payload.setMode(MODE_SINGLE);
                }
                return payload;
            } catch (Exception ignored) {
                // fall through
            }
        }
        List<String> legacyNames = splitLegacyNames(trimmed);
        if (legacyNames.size() > 1) {
            payload.setMode(MODE_MULTI);
            List<GoodsSpecSkuVo> skus = new ArrayList<>();
            for (String name : legacyNames) {
                skus.add(buildSku(name, price, stock));
            }
            payload.setSkus(skus);
            return payload;
        }
        if (legacyNames.size() == 1) {
            payload.setMode(MODE_SINGLE);
            payload.setSkus(Collections.singletonList(buildSku(legacyNames.get(0), price, stock)));
        }
        return payload;
    }

    public static List<GoodsSpecSkuVo> parseGoodsSpecs(String raw, BigDecimal fallbackPrice, Integer fallbackStock) {
        if (StringUtils.isBlank(raw)) {
            return Collections.emptyList();
        }
        String trimmed = raw.trim();
        if (!trimmed.startsWith("[")) {
            return Collections.emptyList();
        }
        try {
            JSONArray array = JSON.parseArray(trimmed);
            return parseSkuArray(array, fallbackPrice, fallbackStock);
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }

    public static String serializeMerchantPayload(GoodsSpecsPayload payload) {
        if (payload == null || !payload.isMulti()) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("mode", MODE_MULTI);
        json.put("skus", payload.getSkus());
        return json.toJSONString();
    }

    public static String skusToJson(List<GoodsSpecSkuVo> skus) {
        if (CollectionUtils.isEmpty(skus)) {
            return "[]";
        }
        return JSON.toJSONString(skus);
    }

    public static BigDecimal minPrice(List<GoodsSpecSkuVo> skus, BigDecimal fallbackPrice) {
        if (CollectionUtils.isEmpty(skus)) {
            return fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice;
        }
        BigDecimal min = null;
        for (GoodsSpecSkuVo sku : skus) {
            if (sku == null || sku.getPrice() == null) {
                continue;
            }
            if (min == null || sku.getPrice().compareTo(min) < 0) {
                min = sku.getPrice();
            }
        }
        return min == null ? (fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice) : min;
    }

    public static int totalStock(List<GoodsSpecSkuVo> skus, Integer fallbackStock) {
        if (CollectionUtils.isEmpty(skus)) {
            return fallbackStock == null ? 0 : fallbackStock;
        }
        int total = 0;
        for (GoodsSpecSkuVo sku : skus) {
            if (sku != null && sku.getStock() != null) {
                total += Math.max(0, sku.getStock());
            }
        }
        return total;
    }

    public static GoodsSpecSkuVo findByName(List<GoodsSpecSkuVo> skus, String name) {
        if (CollectionUtils.isEmpty(skus) || StringUtils.isBlank(name)) {
            return null;
        }
        String target = name.trim();
        for (GoodsSpecSkuVo sku : skus) {
            if (sku != null && target.equals(StringUtils.trimToEmpty(sku.getName()))) {
                return sku;
            }
        }
        return null;
    }

    public static List<String> toNameList(List<GoodsSpecSkuVo> skus) {
        if (CollectionUtils.isEmpty(skus)) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        for (GoodsSpecSkuVo sku : skus) {
            if (sku != null && StringUtils.isNotBlank(sku.getName())) {
                names.add(sku.getName().trim());
            }
        }
        return names;
    }

    private static List<GoodsSpecSkuVo> parseSkuArray(JSONArray array, BigDecimal fallbackPrice, Integer fallbackStock) {
        if (array == null || array.isEmpty()) {
            return new ArrayList<>();
        }
        Object first = array.get(0);
        if (first instanceof String) {
            List<GoodsSpecSkuVo> skus = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                String name = array.getString(i);
                if (StringUtils.isNotBlank(name)) {
                    skus.add(buildSku(name.trim(), fallbackPrice, fallbackStock));
                }
            }
            return skus;
        }
        List<GoodsSpecSkuVo> skus = new ArrayList<>();
        for (int i = 0; i < array.size(); i++) {
            JSONObject item = array.getJSONObject(i);
            GoodsSpecSkuVo sku = parseSkuObject(item, fallbackPrice, fallbackStock);
            if (sku != null) {
                skus.add(sku);
            }
        }
        return skus;
    }

    private static GoodsSpecSkuVo parseSkuObject(JSONObject item, BigDecimal fallbackPrice, Integer fallbackStock) {
        if (item == null) {
            return null;
        }
        String name = StringUtils.trimToNull(item.getString("name"));
        if (name == null) {
            return null;
        }
        GoodsSpecSkuVo sku = new GoodsSpecSkuVo();
        sku.setName(name);
        BigDecimal price = item.getBigDecimal("price");
        sku.setPrice(price == null ? (fallbackPrice == null ? BigDecimal.ZERO : fallbackPrice) : price);
        Integer stock = item.getInteger("stock");
        sku.setStock(stock == null ? (fallbackStock == null ? 0 : fallbackStock) : stock);
        return sku;
    }

    private static GoodsSpecSkuVo buildSku(String name, BigDecimal price, Integer stock) {
        GoodsSpecSkuVo sku = new GoodsSpecSkuVo();
        sku.setName(name);
        sku.setPrice(price == null ? BigDecimal.ZERO : price);
        sku.setStock(stock == null ? 0 : stock);
        return sku;
    }

    private static List<String> splitLegacyNames(String raw) {
        String[] parts = raw.split("[,，、]");
        List<String> names = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                names.add(part.trim());
            }
        }
        return names;
    }

    public static boolean deductSkuStock(GoodsSpecsPayload payload, String specName, int quantity) {
        if (payload == null || !payload.isMulti() || quantity <= 0) {
            return false;
        }
        GoodsSpecSkuVo sku = findByName(payload.getSkus(), specName);
        if (sku == null || sku.getStock() == null || sku.getStock() < quantity) {
            return false;
        }
        sku.setStock(sku.getStock() - quantity);
        return true;
    }

    public static boolean restoreSkuStock(GoodsSpecsPayload payload, String specName, int quantity) {
        if (payload == null || !payload.isMulti() || quantity <= 0 || StringUtils.isBlank(specName)) {
            return false;
        }
        GoodsSpecSkuVo sku = findByName(payload.getSkus(), specName);
        if (sku == null) {
            return false;
        }
        int current = sku.getStock() == null ? 0 : sku.getStock();
        sku.setStock(current + quantity);
        return true;
    }

    public static boolean sameSkuList(List<GoodsSpecSkuVo> left, List<GoodsSpecSkuVo> right) {
        return Objects.equals(JSON.toJSONString(left), JSON.toJSONString(right));
    }
}

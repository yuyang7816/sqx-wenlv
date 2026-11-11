package com.sqx.app.lvyou.merchant.support;

import com.alibaba.fastjson.JSON;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 商户入驻资质校验（与 C 端 merchant-onboarding.js 对齐）
 */
public final class MerchantQualificationSupport {

    private static final List<QualItem> HOTEL = Arrays.asList(
            item("storeFront", "门店门头照", true),
            item("specialPermit", "特种行业许可证", false),
            item("healthPermit", "卫生许可证", false)
    );

    private static final List<QualItem> RESTAURANT = Arrays.asList(
            item("storeFront", "门店门头照", true),
            item("foodPermit", "食品经营许可证", false),
            multi("diningEnv", "就餐环境照片", false, 0, 6)
    );

    private static final List<QualItem> CULTURE = Arrays.asList(
            item("storeFront", "门店/网店展示照", true),
            multi("productSample", "产品样品照片", false, 0, 9)
    );

    private static final List<QualItem> TRAVEL = Arrays.asList(
            multi("officePhoto", "经营场所照片", true, 1, 6),
            item("travelPermit", "旅行社经营许可证", false)
    );

    private static final Map<String, List<QualItem>> BY_TYPE = new HashMap<>();

    static {
        BY_TYPE.put(MerchantStoreType.HOTEL.getCode(), HOTEL);
        BY_TYPE.put(MerchantStoreType.RESTAURANT.getCode(), RESTAURANT);
        BY_TYPE.put(MerchantStoreType.LEGACY_GOOSE, RESTAURANT);
        BY_TYPE.put(MerchantStoreType.CULTURE.getCode(), CULTURE);
        BY_TYPE.put(MerchantStoreType.TRAVEL.getCode(), TRAVEL);
    }

    private MerchantQualificationSupport() {
    }

    public static List<QualItem> requiredItems(String storeType) {
        String normalized = MerchantStoreType.normalize(storeType);
        return BY_TYPE.getOrDefault(normalized, HOTEL);
    }

    public static void validateQualImages(String storeType, Map<String, String> qualImages) {
        for (QualItem item : requiredItems(storeType)) {
            List<String> urls = resolveQualUrls(qualImages, item.getKey());
            if (item.isRequired() && urls.size() < item.getMinCount()) {
                throw new BizException("请上传" + item.getLabel());
            }
            if (item.isMultiple() && urls.size() > item.getMaxCount()) {
                throw new BizException(item.getLabel() + "最多上传" + item.getMaxCount() + "张");
            }
        }
    }

    public static List<String> resolveQualUrls(Map<String, String> qualImages, String key) {
        if (qualImages == null || StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        return parseQualImageUrls(qualImages.get(key));
    }

    public static List<String> parseQualImageUrls(String raw) {
        if (StringUtils.isBlank(raw)) {
            return Collections.emptyList();
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                List<String> parsed = JSON.parseArray(trimmed, String.class);
                if (parsed == null) {
                    return Collections.emptyList();
                }
                return parsed.stream()
                        .filter(StringUtils::isNotBlank)
                        .map(String::trim)
                        .collect(Collectors.toList());
            } catch (Exception ex) {
                return Collections.singletonList(trimmed);
            }
        }
        return Collections.singletonList(trimmed);
    }

    private static QualItem item(String key, String label, boolean required) {
        return new QualItem(key, label, required, false, required ? 1 : 0, 1);
    }

    private static QualItem multi(String key, String label, boolean required, int minCount, int maxCount) {
        return new QualItem(key, label, required, true, minCount, maxCount);
    }

    public static final class QualItem {
        private final String key;
        private final String label;
        private final boolean required;
        private final boolean multiple;
        private final int minCount;
        private final int maxCount;

        public QualItem(String key, String label, boolean required, boolean multiple, int minCount, int maxCount) {
            this.key = key;
            this.label = label;
            this.required = required;
            this.multiple = multiple;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }

        public String getKey() {
            return key;
        }

        public String getLabel() {
            return label;
        }

        public boolean isRequired() {
            return required;
        }

        public boolean isMultiple() {
            return multiple;
        }

        public int getMinCount() {
            return minCount;
        }

        public int getMaxCount() {
            return maxCount;
        }
    }
}

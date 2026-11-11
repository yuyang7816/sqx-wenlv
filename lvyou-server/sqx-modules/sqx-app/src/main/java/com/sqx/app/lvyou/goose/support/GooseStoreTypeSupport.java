package com.sqx.app.lvyou.goose.support;

import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 灰鹅美食门店类型判定（goose 与 restaurant 均视为餐饮）
 */
public final class GooseStoreTypeSupport {

    private static final List<String> FOOD_STORE_TYPES = Collections.unmodifiableList(
            Arrays.asList(MerchantStoreType.LEGACY_GOOSE, MerchantStoreType.RESTAURANT.getCode()));

    private GooseStoreTypeSupport() {
    }

    public static boolean isFoodStore(String storeType) {
        if (StringUtils.isBlank(storeType)) {
            return false;
        }
        String normalized = MerchantStoreType.normalize(storeType);
        return FOOD_STORE_TYPES.contains(normalized);
    }

    public static List<String> foodStoreTypes() {
        return FOOD_STORE_TYPES;
    }
}

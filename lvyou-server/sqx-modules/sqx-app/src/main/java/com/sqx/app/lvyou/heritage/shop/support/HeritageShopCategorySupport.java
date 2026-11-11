package com.sqx.app.lvyou.heritage.shop.support;

import com.sqx.app.lvyou.heritage.craft.domain.entity.HeritageCraftEntity;
import org.apache.commons.lang3.StringUtils;

/**
 * 非遗好店分类：优先取商户入驻时选择的好店分类，其次取店铺/工艺上维护的分类。
 */
public final class HeritageShopCategorySupport {

    public static final String DEFAULT = "culture";

    private HeritageShopCategorySupport() {
    }

    public static String resolveFromMerchant(String merchantShopCategory, Long craftId,
                                             java.util.function.Function<Long, HeritageCraftEntity> loader) {
        if (StringUtils.isNotBlank(merchantShopCategory)) {
            return merchantShopCategory.trim();
        }
        return resolveFromCraftId(craftId, loader);
    }

    public static String resolveFromCraft(HeritageCraftEntity craft) {
        if (craft == null || StringUtils.isBlank(craft.getShopCategory())) {
            return DEFAULT;
        }
        return craft.getShopCategory().trim();
    }

    public static String resolveFromCraftId(Long craftId, java.util.function.Function<Long, HeritageCraftEntity> loader) {
        if (craftId == null || loader == null) {
            return DEFAULT;
        }
        return resolveFromCraft(loader.apply(craftId));
    }
}

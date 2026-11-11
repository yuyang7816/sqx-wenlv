package com.sqx.app.lvyou.merchant.support;

import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import org.apache.commons.lang3.StringUtils;

/**
 * 商户门店类型与首页推荐 Tab 映射
 */
public final class MerchantHomeTabSupport {

    private MerchantHomeTabSupport() {
    }

    /**
     * 根据门店类型解析默认首页推荐 Tab 键
     */
    public static String resolveTabKey(String storeType) {
        String normalized = MerchantStoreType.normalize(storeType);
        if (StringUtils.isBlank(normalized)) {
            return null;
        }
        switch (normalized) {
            case "hotel":
                return "hotel";
            case "travel":
                return "route";
            case "culture":
                return "heritage";
            case "restaurant":
                return "activity";
            default:
                if (MerchantStoreType.isLegacyGoose(normalized)) {
                    return "activity";
                }
                return "activity";
        }
    }
}

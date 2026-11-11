package com.sqx.app.lvyou.merchant.enums;

import com.sqx.app.lvyou.merchant.service.MerchantStoreTypeConfigService;
import com.sqx.common.core.utils.SpringContextHolder;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 门店类型
 */
@Getter
public enum MerchantStoreType {

    HOTEL("hotel", "酒店"),
    RESTAURANT("restaurant", "餐饮"),
    TRAVEL("travel", "旅游"),
    CULTURE("culture", "文创");

    /** 历史门店类型（不可新入驻） */
    public static final String LEGACY_GOOSE = "goose";

    private final String code;
    private final String label;

    MerchantStoreType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public static boolean isValid(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        if (MerchantStoreTypeConfigServiceHolder.isValid(code)) {
            return true;
        }
        for (MerchantStoreType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return true;
            }
        }
        return "goods".equalsIgnoreCase(code);
    }

    public static boolean isLegacyGoose(String code) {
        return LEGACY_GOOSE.equalsIgnoreCase(StringUtils.trimToEmpty(code));
    }

    public static String normalize(String code) {
        if (StringUtils.isBlank(code)) {
            return code;
        }
        if ("goods".equalsIgnoreCase(code)) {
            return CULTURE.code;
        }
        return code.toLowerCase();
    }

    public static String labelOf(String code) {
        if (isLegacyGoose(code)) {
            return RESTAURANT.label;
        }
        String fromConfig = MerchantStoreTypeConfigServiceHolder.labelOf(code);
        if (StringUtils.isNotBlank(fromConfig)) {
            return fromConfig;
        }
        return labelOfFallback(normalize(code));
    }

    public static String labelOfFallback(String normalized) {
        for (MerchantStoreType type : values()) {
            if (type.code.equals(normalized)) {
                return type.label;
            }
        }
        return normalized;
    }

    private static final class MerchantStoreTypeConfigServiceHolder {
        private MerchantStoreTypeConfigServiceHolder() {
        }

        private static MerchantStoreTypeConfigService service() {
            try {
                return SpringContextHolder.getBean(MerchantStoreTypeConfigService.class);
            } catch (Exception ex) {
                return null;
            }
        }

        private static boolean isValid(String code) {
            MerchantStoreTypeConfigService service = service();
            return service != null && service.isValid(code);
        }

        private static String labelOf(String code) {
            MerchantStoreTypeConfigService service = service();
            return service == null ? null : service.labelOf(code);
        }
    }
}

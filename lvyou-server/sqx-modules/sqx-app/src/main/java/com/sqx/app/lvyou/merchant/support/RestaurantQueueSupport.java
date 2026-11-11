package com.sqx.app.lvyou.merchant.support;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.goose.support.GooseStoreTypeSupport;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.common.core.exception.BizException;

import java.util.Objects;

/**
 * 餐饮现场排队状态（仅 restaurant / goose 使用）
 */
public final class RestaurantQueueSupport {

    public static final int QUEUE_ENABLED = 1;
    public static final int QUEUE_DISABLED = 0;
    public static final int MERCHANT_OPEN = 1;
    public static final int MIN_TABLE_COUNT = 1;
    public static final int MAX_TABLE_COUNT = 99;

    private RestaurantQueueSupport() {
    }

    public static boolean isRestaurant(MerchantEntity merchant) {
        return merchant != null && GooseStoreTypeSupport.isFoodStore(merchant.getStoreType());
    }

    public static void assertRestaurant(MerchantEntity merchant) {
        if (!isRestaurant(merchant)) {
            throw new BizException(AppError.MERCHANT_QUEUE_RESTAURANT_ONLY);
        }
    }

    public static void validateUpdate(MerchantEntity merchant, Boolean queueEnabled, Integer queueTableCount) {
        assertRestaurant(merchant);
        if (!Objects.equals(merchant.getIsOpen(), MERCHANT_OPEN)) {
            throw new BizException(AppError.MERCHANT_QUEUE_REQUIRE_OPEN);
        }
        if (!Boolean.TRUE.equals(queueEnabled)) {
            return;
        }
        int count = queueTableCount == null ? 0 : queueTableCount;
        if (count < MIN_TABLE_COUNT || count > MAX_TABLE_COUNT) {
            throw new BizException(AppError.MERCHANT_QUEUE_COUNT_INVALID);
        }
    }

    public static int normalizeEnabled(Boolean enabled) {
        return Boolean.TRUE.equals(enabled) ? QUEUE_ENABLED : QUEUE_DISABLED;
    }

    public static int normalizeTableCount(Boolean enabled, Integer count) {
        if (!Boolean.TRUE.equals(enabled)) {
            return 0;
        }
        return count == null ? 0 : count;
    }

    public static void applyQueueFields(MerchantEntity merchant, Boolean queueEnabled, Integer queueTableCount) {
        int enabled = normalizeEnabled(queueEnabled);
        int tableCount = normalizeTableCount(queueEnabled, queueTableCount);
        merchant.setQueueEnabled(enabled);
        merchant.setQueueTableCount(tableCount);
    }

    public static void clearQueue(MerchantEntity merchant) {
        if (merchant == null) {
            return;
        }
        merchant.setQueueEnabled(QUEUE_DISABLED);
        merchant.setQueueTableCount(0);
    }

    public static String buildDisplayText(Integer isOpen, Integer queueEnabled, Integer tableCount) {
        if (!Objects.equals(isOpen, MERCHANT_OPEN)) {
            return null;
        }
        if (!Objects.equals(queueEnabled, QUEUE_ENABLED)) {
            return "无需排队";
        }
        int count = tableCount == null ? 0 : tableCount;
        if (count <= 0) {
            return "无需排队";
        }
        return "前面约 " + count + " 桌";
    }

    public static boolean isQueuing(Integer isOpen, Integer queueEnabled, Integer tableCount) {
        return Objects.equals(isOpen, MERCHANT_OPEN)
                && Objects.equals(queueEnabled, QUEUE_ENABLED)
                && tableCount != null
                && tableCount > 0;
    }
}

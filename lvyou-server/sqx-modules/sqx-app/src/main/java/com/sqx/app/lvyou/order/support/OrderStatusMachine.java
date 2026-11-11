package com.sqx.app.lvyou.order.support;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.common.core.exception.BizException;

/**
 * 订单状态机：校验合法流转
 */
public final class OrderStatusMachine {

    private OrderStatusMachine() {
    }

    /**
     * 新建订单初始状态
     */
    public static int initialStatus() {
        return MerchantOrderStatusGroup.UNPAID;
    }

    /**
     * 用户取消：仅待支付可取消
     */
    public static void assertUserCancelAllowed(Integer status) {
        int current = status == null ? MerchantOrderStatusGroup.UNPAID : status;
        if (current != MerchantOrderStatusGroup.UNPAID) {
            throw new BizException(AppError.ORDER_STATUS_INVALID);
        }
    }

    /**
     * 管理端取消：待支付/待确认/待使用可干预取消
     */
    public static void assertAdminCancelAllowed(Integer status) {
        int current = status == null ? MerchantOrderStatusGroup.UNPAID : status;
        if (current == MerchantOrderStatusGroup.COMPLETED
                || current == MerchantOrderStatusGroup.CANCELLED
                || current == MerchantOrderStatusGroup.REFUNDING) {
            throw new BizException(AppError.ORDER_STATUS_INVALID);
        }
    }

    /**
     * 管理端强制完成
     */
    public static void assertAdminCompleteAllowed(Integer status) {
        int current = status == null ? MerchantOrderStatusGroup.UNPAID : status;
        if (current == MerchantOrderStatusGroup.COMPLETED
                || current == MerchantOrderStatusGroup.CANCELLED) {
            throw new BizException(AppError.ORDER_STATUS_INVALID);
        }
    }

    /**
     * 管理端发起退款
     */
    public static void assertAdminRefundAllowed(Integer status) {
        int current = status == null ? MerchantOrderStatusGroup.UNPAID : status;
        if (current == MerchantOrderStatusGroup.UNPAID
                || current == MerchantOrderStatusGroup.CANCELLED
                || current == MerchantOrderStatusGroup.REFUNDING
                || current == MerchantOrderStatusGroup.COMPLETED) {
            throw new BizException(AppError.ORDER_STATUS_INVALID);
        }
    }

    public static int toCancelledStatus() {
        return MerchantOrderStatusGroup.CANCELLED;
    }

    public static int toCompletedStatus() {
        return MerchantOrderStatusGroup.COMPLETED;
    }

    public static int toRefundingStatus() {
        return MerchantOrderStatusGroup.REFUNDING;
    }

    /**
     * 支付成功：仅待支付可流转至待确认
     */
    public static void assertPayAllowed(Integer status) {
        int current = status == null ? MerchantOrderStatusGroup.UNPAID : status;
        if (current != MerchantOrderStatusGroup.UNPAID) {
            throw new BizException(AppError.ORDER_ALREADY_PAID);
        }
    }

    /**
     * 支付成功后订单状态（默认待确认，酒店支付后直接待入住）
     */
    public static int toPaidStatus() {
        return MerchantOrderStatusGroup.PENDING_CONFIRM;
    }

    /**
     * 支付成功后订单状态（按业务分类）
     */
    public static int toPaidStatus(String orderCategory) {
        String category = MerchantOrderStatusSupport.normalizeCategory(orderCategory);
        if ("hotel".equals(category) || "route".equals(category)) {
            return MerchantOrderStatusGroup.PENDING_USE;
        }
        if (MerchantOrderStatusSupport.isGoodsCategory(category)) {
            return MerchantOrderStatusGroup.PENDING_USE;
        }
        return toPaidStatus();
    }

    /**
     * 用户确认收货：仅商品类待收货可完成
     */
    public static void assertUserCompleteAllowed(Integer status, String orderCategory) {
        int current = status == null ? MerchantOrderStatusGroup.UNPAID : status;
        if (current != MerchantOrderStatusGroup.PENDING_RECEIVE) {
            throw new BizException(AppError.ORDER_STATUS_INVALID);
        }
        if (!MerchantOrderStatusSupport.isGoodsCategory(orderCategory)) {
            throw new BizException(AppError.ORDER_STATUS_INVALID);
        }
    }

    /**
     * 用户申请退款：已支付且未完成/未取消/未在退款中
     */
    public static void assertUserRefundAllowed(Integer status) {
        int current = status == null ? MerchantOrderStatusGroup.UNPAID : status;
        if (current == MerchantOrderStatusGroup.UNPAID
                || current == MerchantOrderStatusGroup.COMPLETED
                || current == MerchantOrderStatusGroup.CANCELLED
                || current == MerchantOrderStatusGroup.REFUNDING) {
            throw new BizException(AppError.ORDER_STATUS_INVALID);
        }
    }
}

package com.sqx.app.lvyou.merchant.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantOrderActionBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantOrderListBo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantOrderVo;
import com.sqx.app.lvyou.order.domain.vo.OrderLogisticsVo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户订单业务服务
 */
public interface MerchantOrderService {

    IPage<MerchantOrderVo> pageOrders(Long userId, MerchantOrderListBo listBo);

    MerchantOrderVo getOrderDetail(Long userId, String orderKey);

    MerchantOrderVo executeAction(Long userId, String orderKey, MerchantOrderActionBo actionBo);

    OrderLogisticsVo queryOrderLogistics(Long userId, String orderKey);

    long countPendingOrders(Long merchantId);

    /** 未完结订单数（退保/关店前校验） */
    long countUnprocessedOrders(Long merchantId);

    long countPendingVerifyOrders(Long merchantId);

    java.util.List<MerchantOrderVo> listPendingVerifyOrders(Long merchantId, int limit);

    long countPendingShipOrders(Long merchantId);

    java.util.List<MerchantOrderVo> listPendingShipOrders(Long merchantId, int limit);

    long countPendingRouteBookings(Long merchantId);

    java.util.List<MerchantOrderVo> listPendingRouteBookings(Long merchantId, int limit);

    /** 已支付订单数（可选时间范围，均为 null 表示累计） */
    long countPaidOrders(Long merchantId, LocalDateTime start, LocalDateTime end);

    /** 已支付订单实付金额合计（可选时间范围，均为 null 表示累计） */
    BigDecimal sumPaidAmount(Long merchantId, LocalDateTime start, LocalDateTime end);
}

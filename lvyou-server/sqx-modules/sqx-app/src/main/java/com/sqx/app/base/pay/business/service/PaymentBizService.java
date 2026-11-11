package com.sqx.app.base.pay.business.service;

import com.sqx.app.base.pay.domain.BusinessParam;

/**
 * 支付业务编排：预校验、回调落库、消息通知
 */
public interface PaymentBizService {

    /**
     * 统一支付前业务校验与参数对齐
     */
    void validateBeforePay(BusinessParam param);

    /**
     * 第三方支付回调成功
     */
    void onChannelPaymentSuccess(String channel, String outTradeNo, String tradeNo);

    /**
     * 第三方支付回调失败
     */
    void onChannelPaymentFailed(String channel, String outTradeNo, String tradeNo);
}

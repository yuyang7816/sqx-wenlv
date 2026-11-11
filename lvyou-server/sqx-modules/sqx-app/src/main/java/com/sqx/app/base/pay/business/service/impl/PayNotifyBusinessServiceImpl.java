package com.sqx.app.base.pay.business.service.impl;

import com.sqx.app.base.pay.business.service.PaymentBizService;
import com.sqx.common.pay.notify.IPayNotifyBusinessService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chengming
 * @since 2025/4/21
 *
 * 回调业务
 */
@Slf4j
@Service
public class PayNotifyBusinessServiceImpl implements IPayNotifyBusinessService {

    @Resource
    private PaymentBizService paymentBizService;

    @Override
    public void onPaymentSuccess(String channel, String outTradeNo, String tradeNo) {
        paymentBizService.onChannelPaymentSuccess(channel, outTradeNo, tradeNo);
    }

    @Override
    public void onPaymentFailed(String channel, String outTradeNo, String tradeNo) {
        paymentBizService.onChannelPaymentFailed(channel, outTradeNo, tradeNo);
    }
}

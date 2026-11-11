package com.sqx.app.base.pay.business.service.impl;

import com.sqx.app.base.pay.business.service.BusinessService;
import com.sqx.app.base.pay.business.service.PaymentBizService;
import com.sqx.app.base.pay.domain.BusinessParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author chengming
 * @since 2025/4/21
 */
@Slf4j
@Service
public class BusinessServiceImpl implements BusinessService {

    @Resource
    private PaymentBizService paymentBizService;

    @Override
    public void process(BusinessParam param) {
        paymentBizService.validateBeforePay(param);
    }
}

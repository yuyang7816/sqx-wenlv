package com.sqx.app.base.pay.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.pay.domain.bo.PaymentAdminPageBo;
import com.sqx.app.base.pay.domain.bo.PaymentConfigUpdateBo;
import com.sqx.app.base.pay.domain.vo.PaymentAdminVo;
import com.sqx.app.base.pay.domain.vo.PaymentConfigAdminVo;

import java.util.List;

/**
 * 管理端支付服务
 */
public interface PaymentAdminService {

    IPage<PaymentAdminVo> page(PaymentAdminPageBo pageBo);

    PaymentAdminVo info(String payNo, String businessType);

    List<PaymentConfigAdminVo> listConfig();

    void updateConfig(PaymentConfigUpdateBo updateBo);
}

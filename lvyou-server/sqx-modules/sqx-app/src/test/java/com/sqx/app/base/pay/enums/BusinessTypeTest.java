package com.sqx.app.base.pay.enums;

import org.junit.Assert;
import org.junit.Test;

/**
 * 支付业务类型枚举测试
 */
public class BusinessTypeTest {

    @Test
    public void fromCode_tourism_ok() {
        Assert.assertEquals(BusinessType.TOURISM, BusinessType.fromCode("tourism"));
    }

    @Test
    public void fromCode_walletRecharge_ok() {
        Assert.assertEquals(BusinessType.WALLET_RECHARGE, BusinessType.fromCode("wallet_recharge"));
    }

    @Test
    public void fromCode_merchantDeposit_ok() {
        Assert.assertEquals(BusinessType.MERCHANT_DEPOSIT, BusinessType.fromCode("merchant_deposit"));
    }
}

package com.sqx.app.lvyou.merchant.service;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

/**
 * 商户保证金状态判定单元测试
 */
public class MerchantWalletDepositLogicTest {

    @Test
    public void depositStatus_shouldBePaidWhenPaidGteRequired() {
        Assert.assertEquals("paid", resolveDepositStatus(new BigDecimal("5000.00"), new BigDecimal("5000.00")));
        Assert.assertEquals("paid", resolveDepositStatus(new BigDecimal("6000.00"), new BigDecimal("5000.00")));
    }

    @Test
    public void depositStatus_shouldBeUnpaidWhenPaidLtRequired() {
        Assert.assertEquals("unpaid", resolveDepositStatus(new BigDecimal("4999.99"), new BigDecimal("5000.00")));
        Assert.assertEquals("unpaid", resolveDepositStatus(BigDecimal.ZERO, new BigDecimal("5000.00")));
    }

    private String resolveDepositStatus(BigDecimal depositPaid, BigDecimal depositRequired) {
        return depositPaid.compareTo(depositRequired) >= 0 ? "paid" : "unpaid";
    }
}

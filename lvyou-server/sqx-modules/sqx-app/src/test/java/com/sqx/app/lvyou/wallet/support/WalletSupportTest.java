package com.sqx.app.lvyou.wallet.support;

import com.sqx.app.lvyou.wallet.enums.WithdrawMethod;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class WalletSupportTest {

    @Test
    public void formatMoney_shouldKeepTwoDecimals() {
        Assert.assertEquals("10.50", WalletSupport.formatMoney(new BigDecimal("10.5")));
    }

    @Test
    public void maskAccount_shouldMaskPhoneAndEmail() {
        Assert.assertEquals("138****8888", WalletSupport.maskAccount("13812348888"));
        Assert.assertTrue(WalletSupport.maskAccount("test@example.com").contains("@example.com"));
    }

    @Test
    public void txStatusRoundTrip_shouldMatch() {
        Assert.assertEquals("pending", WalletSupport.txStatusToCode(WalletSupport.txStatusToInt("pending")));
        Assert.assertEquals("failed", WalletSupport.txStatusToCode(WalletSupport.txStatusToInt("failed")));
        Assert.assertEquals("success", WalletSupport.txStatusToCode(WalletSupport.txStatusToInt("success")));
    }

    @Test
    public void resolveWithdrawAccountDisplay_wechatShouldUseLabel() {
        Assert.assertEquals("微信收款码",
                WalletSupport.resolveWithdrawAccountDisplay(WithdrawMethod.WECHAT, null, "https://img/a.png"));
    }
}

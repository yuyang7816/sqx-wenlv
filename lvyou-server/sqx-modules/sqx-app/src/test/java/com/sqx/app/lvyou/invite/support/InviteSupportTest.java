package com.sqx.app.lvyou.invite.support;

import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class InviteSupportTest {

    @Test
    public void parseRate_shouldAcceptDecimalAndPercent() {
        Assert.assertEquals(new BigDecimal("0.0500"), InviteSupport.parseRate("0.05", InviteSupport.defaultRateLevel1()));
        Assert.assertEquals(new BigDecimal("0.0500"), InviteSupport.parseRate("5", InviteSupport.defaultRateLevel1()));
    }

    @Test
    public void calcCommission_shouldRoundToTwoDecimals() {
        Assert.assertEquals(new BigDecimal("12.50"),
                InviteSupport.calcCommission(new BigDecimal("250.00"), new BigDecimal("0.05")));
    }

    @Test
    public void buildQrContent_shouldAppendInviteCode() {
        Assert.assertEquals("https://example.com/register?code=ABC123",
                InviteSupport.buildQrContent("https://example.com/register", "ABC123"));
        Assert.assertEquals("https://example.com/register?x=1&code=ABC123",
                InviteSupport.buildQrContent("https://example.com/register?x=1", "ABC123"));
    }

    @Test
    public void maskPhone_shouldMaskMiddleDigits() {
        Assert.assertEquals("138****8888", InviteSupport.maskPhone("13812348888"));
    }

    @Test
    public void parseStoredRate_shouldAllowBlankToClearOverride() {
        Assert.assertNull(InviteSupport.parseStoredRate(""));
        Assert.assertEquals(new BigDecimal("0.0600"), InviteSupport.parseStoredRate("0.06"));
        Assert.assertEquals(new BigDecimal("0.0300"), InviteSupport.parseStoredRate("3"));
    }

    @Test
    public void resolveCommissionRate_shouldPreferUserOverride() {
        Assert.assertEquals(new BigDecimal("0.0800"),
                InviteSupport.resolveCommissionRate(new BigDecimal("0.08"), "0.05", InviteSupport.defaultRateLevel1()));
        Assert.assertEquals(new BigDecimal("0.0500"),
                InviteSupport.resolveCommissionRate(null, "0.05", InviteSupport.defaultRateLevel1()));
    }
}

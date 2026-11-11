package com.sqx.common.pay.enums;

import org.junit.Assert;
import org.junit.Test;

/**
 * PayChannel 枚举渠道码校验
 */
public class PayChannelTest {

    @Test
    public void fromCode_wx_returnsWx() {
        Assert.assertEquals(PayChannel.WX, PayChannel.fromCode("wx"));
    }

    @Test
    public void fromCode_ali_returnsAli() {
        Assert.assertEquals(PayChannel.ALI, PayChannel.fromCode("ali"));
    }

    @Test
    public void wxNotifyChannelMatchesHandlerSupport() {
        Assert.assertEquals("wx", PayChannel.WX.getValue());
    }
}

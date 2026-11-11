package com.sqx.app.lvyou.order.util;

import org.junit.Assert;
import org.junit.Test;

public class AddressRegionUtilTest {

    @Test
    public void parseRegion_twoParts() {
        AddressRegionUtil.RegionParts parts = AddressRegionUtil.parseRegion("重庆市 荣昌区");
        Assert.assertEquals("重庆市", parts.getProvince());
        Assert.assertNull(parts.getCity());
        Assert.assertEquals("荣昌区", parts.getDistrict());
    }

    @Test
    public void formatRegion_joinsNonBlankParts() {
        String region = AddressRegionUtil.formatRegion("重庆市", null, "荣昌区");
        Assert.assertEquals("重庆市 荣昌区", region);
    }
}

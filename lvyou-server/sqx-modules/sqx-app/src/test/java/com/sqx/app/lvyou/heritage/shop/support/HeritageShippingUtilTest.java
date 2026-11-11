package com.sqx.app.lvyou.heritage.shop.support;

import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopShippingVo;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class HeritageShippingUtilTest {

    @Test
    public void calculateFeeReturnsZeroWhenFreeShipping() {
        HeritageShopShippingVo config = HeritageShippingUtil.defaultConfig();
        config.setFreeShipping(true);
        Assert.assertEquals(0, HeritageShippingUtil.calculateFee(config, new BigDecimal("100")).compareTo(BigDecimal.ZERO));
    }

    @Test
    public void calculateFeeUsesFixedFeeWhenNotFree() {
        HeritageShopShippingVo config = HeritageShippingUtil.defaultConfig();
        config.setFreeShipping(false);
        config.setFixedFee(new BigDecimal("8"));
        Assert.assertEquals(0, HeritageShippingUtil.calculateFee(config, new BigDecimal("20")).compareTo(new BigDecimal("8.00")));
    }

    @Test
    public void calculateFeeUsesThresholdWhenNotFree() {
        HeritageShopShippingVo config = HeritageShippingUtil.defaultConfig();
        config.setFreeShipping(false);
        config.setFixedFee(new BigDecimal("8"));
        config.setFreeAmountThreshold(new BigDecimal("99"));
        Assert.assertEquals(0, HeritageShippingUtil.calculateFee(config, new BigDecimal("100")).compareTo(BigDecimal.ZERO));
        Assert.assertEquals(0, HeritageShippingUtil.calculateFee(config, new BigDecimal("50")).compareTo(new BigDecimal("8.00")));
    }

    @Test
    public void parseDecorMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("freeShipping", false);
        map.put("fixedFee", 6.5);
        map.put("freeAmountThreshold", 88);
        map.put("shippingNote", "偏远地区另议");
        HeritageShopShippingVo config = HeritageShippingUtil.fromMap(map, HeritageShippingUtil.defaultConfig());
        Assert.assertFalse(config.getFreeShipping());
        Assert.assertEquals(0, config.getFixedFee().compareTo(new BigDecimal("6.50")));
        Assert.assertEquals("偏远地区另议", config.getShippingNote());
    }
}

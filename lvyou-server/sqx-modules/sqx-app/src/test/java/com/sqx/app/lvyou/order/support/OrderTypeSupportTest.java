package com.sqx.app.lvyou.order.support;

import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.enums.OrderType;
import org.junit.Assert;
import org.junit.Test;

import java.time.LocalDate;
import java.util.Collections;

public class OrderTypeSupportTest {

    @Test
    public void resolveShouldPreferOrderTypeField() {
        OrderEntity order = new OrderEntity();
        order.setOrderType("hotel");
        order.setOrderCategory("goods");
        Assert.assertEquals(OrderType.HOTEL, OrderTypeSupport.resolve(order));
    }

    @Test
    public void userTabTypeShouldMapCultureToGoods() {
        Assert.assertEquals("goods", OrderTypeSupport.userTabType(OrderType.CULTURE));
    }

    @Test
    public void applyShouldSyncCategoryAndType() {
        OrderEntity order = new OrderEntity();
        OrderTypeSupport.apply(order, OrderType.HOTEL);
        Assert.assertEquals("hotel", order.getOrderType());
        Assert.assertEquals("hotel", order.getOrderCategory());
    }

    @Test
    public void resolveShouldInferHotelFromCheckInDate() {
        OrderEntity order = new OrderEntity();
        order.setCheckInDate(LocalDate.now());
        OrderItemEntity item = new OrderItemEntity();
        item.setItemType("room");
        Assert.assertEquals(OrderType.HOTEL, OrderTypeSupport.resolve(order, Collections.singletonList(item)));
    }
}

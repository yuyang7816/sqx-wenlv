package com.sqx.app.lvyou.canteen.support;

import org.junit.Assert;
import org.junit.Test;

/**
 * 餐饮预约状态文案测试
 */
public class CanteenReservationStatusSupportTest {

    @Test
    public void labelOfPending() {
        Assert.assertEquals("待确认", CanteenReservationStatusSupport.labelOf("pending"));
    }

    @Test
    public void labelOfConfirmed() {
        Assert.assertEquals("待到店", CanteenReservationStatusSupport.labelOf("confirmed"));
    }

    @Test
    public void labelOfCompleted() {
        Assert.assertEquals("已完成", CanteenReservationStatusSupport.labelOf("completed"));
    }

    @Test
    public void normalizeBlankToPending() {
        Assert.assertEquals("pending", CanteenReservationStatusSupport.normalize(null));
    }
}

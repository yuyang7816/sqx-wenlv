package com.sqx.app.lvyou.canteen.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.lvyou.canteen.domain.entity.CanteenReservationEntity;

/**
 * 餐饮预约数据服务
 */
public interface ICanteenReservationService extends IService<CanteenReservationEntity> {

    /**
     * 按关联订单ID查询预约
     */
    CanteenReservationEntity getByOrderId(Long orderId);
}

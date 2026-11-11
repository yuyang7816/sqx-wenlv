package com.sqx.app.lvyou.canteen.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.canteen.db.mapper.CanteenReservationMapper;
import com.sqx.app.lvyou.canteen.db.service.ICanteenReservationService;
import com.sqx.app.lvyou.canteen.domain.entity.CanteenReservationEntity;
import org.springframework.stereotype.Service;

/**
 * 餐饮预约数据服务实现
 */
@Service
public class ICanteenReservationServiceImpl extends ServiceImpl<CanteenReservationMapper, CanteenReservationEntity>
        implements ICanteenReservationService {

    @Override
    public CanteenReservationEntity getByOrderId(Long orderId) {
        if (orderId == null) {
            return null;
        }
        LambdaQueryWrapper<CanteenReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CanteenReservationEntity::getOrderId, orderId).last("LIMIT 1");
        return getOne(queryWrapper, false);
    }
}

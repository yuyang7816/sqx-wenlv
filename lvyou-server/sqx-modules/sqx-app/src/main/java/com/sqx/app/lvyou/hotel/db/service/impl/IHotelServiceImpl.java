package com.sqx.app.lvyou.hotel.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.hotel.db.mapper.HotelMapper;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import org.springframework.stereotype.Service;

/**
 * 酒店数据服务实现
 */
@Service
public class IHotelServiceImpl extends ServiceImpl<HotelMapper, HotelEntity> implements IHotelService {
}

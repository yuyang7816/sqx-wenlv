package com.sqx.app.lvyou.hotel.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.hotel.db.mapper.HotelRoomMapper;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomEntity;
import org.springframework.stereotype.Service;

/**
 * 酒店房型数据服务实现
 */
@Service
public class IHotelRoomServiceImpl extends ServiceImpl<HotelRoomMapper, HotelRoomEntity> implements IHotelRoomService {
}

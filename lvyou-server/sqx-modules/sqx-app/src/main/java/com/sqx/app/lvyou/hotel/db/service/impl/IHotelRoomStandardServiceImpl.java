package com.sqx.app.lvyou.hotel.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.hotel.db.mapper.HotelRoomStandardMapper;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomStandardService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomStandardEntity;
import org.springframework.stereotype.Service;

@Service
public class IHotelRoomStandardServiceImpl extends ServiceImpl<HotelRoomStandardMapper, HotelRoomStandardEntity>
        implements IHotelRoomStandardService {
}

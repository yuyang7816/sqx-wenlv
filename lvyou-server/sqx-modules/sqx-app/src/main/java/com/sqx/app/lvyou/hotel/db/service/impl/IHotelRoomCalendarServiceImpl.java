package com.sqx.app.lvyou.hotel.db.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.hotel.db.mapper.HotelRoomCalendarMapper;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomCalendarService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomCalendarEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class IHotelRoomCalendarServiceImpl extends ServiceImpl<HotelRoomCalendarMapper, HotelRoomCalendarEntity>
        implements IHotelRoomCalendarService {

    @Override
    public Map<LocalDate, HotelRoomCalendarEntity> mapByRoomAndDates(Long roomId, LocalDate start, LocalDate end) {
        List<HotelRoomCalendarEntity> list = listByRoomAndRange(roomId, start, end);
        Map<LocalDate, HotelRoomCalendarEntity> map = new LinkedHashMap<>();
        for (HotelRoomCalendarEntity entity : list) {
            map.put(entity.getCalendarDate(), entity);
        }
        return map;
    }

    @Override
    public List<HotelRoomCalendarEntity> listByRoomAndRange(Long roomId, LocalDate start, LocalDate end) {
        if (roomId == null || start == null || end == null || end.isBefore(start)) {
            return Collections.emptyList();
        }
        LambdaQueryWrapper<HotelRoomCalendarEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomCalendarEntity::getRoomId, roomId)
                .ge(HotelRoomCalendarEntity::getCalendarDate, start)
                .lt(HotelRoomCalendarEntity::getCalendarDate, end)
                .orderByAsc(HotelRoomCalendarEntity::getCalendarDate);
        return list(queryWrapper);
    }
}

package com.sqx.app.lvyou.hotel.db.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomCalendarEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface IHotelRoomCalendarService extends IService<HotelRoomCalendarEntity> {

    Map<LocalDate, HotelRoomCalendarEntity> mapByRoomAndDates(Long roomId, LocalDate start, LocalDate end);

    List<HotelRoomCalendarEntity> listByRoomAndRange(Long roomId, LocalDate start, LocalDate end);
}

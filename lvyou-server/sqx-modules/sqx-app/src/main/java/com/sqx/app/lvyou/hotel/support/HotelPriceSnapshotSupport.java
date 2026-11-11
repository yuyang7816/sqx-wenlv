package com.sqx.app.lvyou.hotel.support;

import com.alibaba.fastjson2.JSON;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomEntity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 下单价格快照
 */
public final class HotelPriceSnapshotSupport {

    private HotelPriceSnapshotSupport() {
    }

    public static String buildJson(HotelEntity hotel,
                                   HotelRoomEntity room,
                                   LocalDate checkIn,
                                   LocalDate checkOut,
                                   int nights,
                                   int roomCount,
                                   List<DailyPrice> dailyPrices,
                                   BigDecimal totalAmount,
                                   BigDecimal payAmount) {
        Map<String, Object> snapshot = new LinkedHashMap<>();
        if (hotel != null) {
            snapshot.put("hotelId", hotel.getId());
            snapshot.put("hotelName", hotel.getName());
            snapshot.put("hotelAddress", hotel.getAddress());
            snapshot.put("hotelPhone", hotel.getPhone());
        }
        if (room != null) {
            snapshot.put("roomId", room.getId());
            snapshot.put("roomName", room.getName());
            snapshot.put("roomSpec", room.getSpec());
            snapshot.put("basePrice", room.getPrice());
        }
        snapshot.put("checkInDate", checkIn == null ? null : checkIn.toString());
        snapshot.put("checkOutDate", checkOut == null ? null : checkOut.toString());
        snapshot.put("nights", nights);
        snapshot.put("roomCount", roomCount);
        List<Map<String, Object>> dailyList = new ArrayList<>();
        if (dailyPrices != null) {
            for (DailyPrice dailyPrice : dailyPrices) {
                Map<String, Object> row = new LinkedHashMap<>();
                row.put("date", dailyPrice.getDate().toString());
                row.put("unitPrice", dailyPrice.getUnitPrice());
                dailyList.add(row);
            }
        }
        snapshot.put("dailyPrices", dailyList);
        snapshot.put("totalAmount", totalAmount);
        snapshot.put("payAmount", payAmount);
        return JSON.toJSONString(snapshot);
    }

    public static class DailyPrice {
        private final LocalDate date;
        private final BigDecimal unitPrice;

        public DailyPrice(LocalDate date, BigDecimal unitPrice) {
            this.date = date;
            this.unitPrice = unitPrice;
        }

        public LocalDate getDate() {
            return date;
        }

        public BigDecimal getUnitPrice() {
            return unitPrice;
        }
    }
}

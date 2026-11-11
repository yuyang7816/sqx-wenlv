package com.sqx.app.lvyou.hotel.support;

import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomCalendarService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomCalendarEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.common.core.exception.BizException;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 酒店库存与报价
 */
@Component
public class HotelInventorySupport {

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IHotelRoomCalendarService iHotelRoomCalendarService;

    public QuoteResult quote(HotelRoomEntity room, LocalDate checkIn, LocalDate checkOut, int roomCount) {
        if (room == null || checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new BizException(AppError.ORDER_DATE_INVALID);
        }
        int nights = (int) java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
        int normalizedRoomCount = roomCount <= 0 ? 1 : roomCount;
        Map<LocalDate, HotelRoomCalendarEntity> calendarMap =
                iHotelRoomCalendarService.mapByRoomAndDates(room.getId(), checkIn, checkOut);
        List<HotelPriceSnapshotSupport.DailyPrice> dailyPrices = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        LocalDate night = checkIn;
        while (night.isBefore(checkOut)) {
            int stock = resolveDayStock(room, calendarMap.get(night));
            if (stock <= 0) {
                throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
            }
            int booked = countBookedRooms(room.getId(), night);
            if (booked + normalizedRoomCount > stock) {
                throw new BizException(AppError.ORDER_STOCK_INSUFFICIENT);
            }
            BigDecimal dayPrice = resolveDayPrice(room, calendarMap.get(night));
            dailyPrices.add(new HotelPriceSnapshotSupport.DailyPrice(night, dayPrice));
            total = total.add(dayPrice.multiply(BigDecimal.valueOf(normalizedRoomCount)));
            night = night.plusDays(1);
        }
        total = total.setScale(2, RoundingMode.HALF_UP);
        return new QuoteResult(nights, normalizedRoomCount, dailyPrices, total);
    }

    public void assertAvailable(HotelRoomEntity room, LocalDate checkIn, LocalDate checkOut, int roomCount) {
        quote(room, checkIn, checkOut, roomCount);
    }

    /** 指定日期剩余可售间数（总库存 - 有效订单占用） */
    public int resolveAvailableForNight(HotelRoomEntity room, LocalDate night) {
        if (room == null || room.getId() == null || night == null) {
            return 0;
        }
        Map<LocalDate, HotelRoomCalendarEntity> calendarMap =
                iHotelRoomCalendarService.mapByRoomAndDates(room.getId(), night, night.plusDays(1));
        int stock = resolveDayStock(room, calendarMap.get(night));
        int booked = countBookedRooms(room.getId(), night);
        return Math.max(0, stock - booked);
    }

    /** 日期区间内最小剩余可售间数 */
    public int resolveAvailableForStay(HotelRoomEntity room, LocalDate checkIn, LocalDate checkOut) {
        if (room == null || checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return 0;
        }
        int min = Integer.MAX_VALUE;
        LocalDate night = checkIn;
        while (night.isBefore(checkOut)) {
            min = Math.min(min, resolveAvailableForNight(room, night));
            night = night.plusDays(1);
        }
        return min == Integer.MAX_VALUE ? 0 : min;
    }

    public int resolveDayStock(HotelRoomEntity room, HotelRoomCalendarEntity calendar) {
        if (calendar != null && calendar.getStatus() != null
                && calendar.getStatus() == HotelRoomCalendarEntity.STATUS_CLOSED) {
            return 0;
        }
        if (calendar != null && calendar.getStock() != null && calendar.getStock() >= 0) {
            return calendar.getStock();
        }
        Integer stock = room.getStock();
        if (stock == null || stock <= 0) {
            return 1;
        }
        return stock;
    }

    public BigDecimal resolveDayPrice(HotelRoomEntity room, HotelRoomCalendarEntity calendar) {
        if (calendar != null && calendar.getPrice() != null && calendar.getPrice().signum() > 0) {
            return calendar.getPrice();
        }
        return room.getPrice() == null ? BigDecimal.ZERO : room.getPrice();
    }

    private int countBookedRooms(Long roomId, LocalDate night) {
        List<OrderEntity> activeBookings = listActiveHotelBookings(roomId);
        int booked = 0;
        for (OrderEntity existing : activeBookings) {
            if (occupiesNight(existing, night)) {
                booked += resolveRoomCount(existing);
            }
        }
        return booked;
    }

    private int resolveRoomCount(OrderEntity order) {
        if (order.getRoomCount() != null && order.getRoomCount() > 0) {
            return order.getRoomCount();
        }
        return 1;
    }

    private List<OrderEntity> listActiveHotelBookings(Long roomId) {
        List<OrderItemEntity> roomItems = iOrderItemService.lambdaQuery()
                .eq(OrderItemEntity::getItemType, "room")
                .eq(OrderItemEntity::getItemId, roomId)
                .list();
        if (roomItems == null || roomItems.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> orderIds = new ArrayList<>();
        for (OrderItemEntity item : roomItems) {
            if (item.getOrderId() != null) {
                orderIds.add(item.getOrderId());
            }
        }
        if (orderIds.isEmpty()) {
            return Collections.emptyList();
        }
        return iOrderService.lambdaQuery()
                .in(OrderEntity::getId, orderIds)
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PAID)
                .list();
    }

    private boolean occupiesNight(OrderEntity order, LocalDate night) {
        if (order.getCheckInDate() == null || order.getCheckOutDate() == null) {
            return false;
        }
        return !night.isBefore(order.getCheckInDate()) && night.isBefore(order.getCheckOutDate());
    }

    public static class QuoteResult {
        private final int nights;
        private final int roomCount;
        private final List<HotelPriceSnapshotSupport.DailyPrice> dailyPrices;
        private final BigDecimal totalAmount;

        public QuoteResult(int nights, int roomCount,
                           List<HotelPriceSnapshotSupport.DailyPrice> dailyPrices,
                           BigDecimal totalAmount) {
            this.nights = nights;
            this.roomCount = roomCount;
            this.dailyPrices = dailyPrices;
            this.totalAmount = totalAmount;
        }

        public int getNights() {
            return nights;
        }

        public int getRoomCount() {
            return roomCount;
        }

        public List<HotelPriceSnapshotSupport.DailyPrice> getDailyPrices() {
            return dailyPrices;
        }

        public BigDecimal getTotalAmount() {
            return totalAmount;
        }

        public BigDecimal averageNightlyPrice() {
            if (nights <= 0 || roomCount <= 0) {
                return BigDecimal.ZERO;
            }
            return totalAmount.divide(BigDecimal.valueOf((long) nights * roomCount), 2, RoundingMode.HALF_UP);
        }
    }
}

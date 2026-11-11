package com.sqx.app.lvyou.hotel.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 房型日历房态
 */
@Getter
@Setter
@TableName("tb_hotel_room_calendar")
@Schema(name = "HotelRoomCalendarEntity", description = "房型日历房态")
public class HotelRoomCalendarEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    public static final int STATUS_OPEN = 1;
    public static final int STATUS_CLOSED = 0;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("room_id")
    private Long roomId;

    @TableField("hotel_id")
    private Long hotelId;

    @TableField("merchant_id")
    private Long merchantId;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @TableField("calendar_date")
    private LocalDate calendarDate;

    @TableField("price")
    private BigDecimal price;

    @TableField("stock")
    private Integer stock;

    @TableField("status")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

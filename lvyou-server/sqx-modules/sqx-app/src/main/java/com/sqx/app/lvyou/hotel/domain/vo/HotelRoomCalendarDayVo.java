package com.sqx.app.lvyou.hotel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "房型日历单日")
public class HotelRoomCalendarDayVo {

    @Schema(description = "日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "当日价格")
    private BigDecimal price;

    @Schema(description = "可售间数")
    private Integer stock;

    @Schema(description = "1开放 0关房")
    private Integer status;
}

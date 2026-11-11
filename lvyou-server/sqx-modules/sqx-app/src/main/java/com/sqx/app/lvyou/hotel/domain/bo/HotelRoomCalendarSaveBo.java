package com.sqx.app.lvyou.hotel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;

@Data
@Schema(description = "批量保存房型日历")
public class HotelRoomCalendarSaveBo {

    @NotBlank(message = "开始日期不能为空")
    private String startDate;

    @NotBlank(message = "结束日期不能为空")
    private String endDate;

    @Schema(description = "统一价格，可选")
    private BigDecimal price;

    @Schema(description = "统一库存，可选")
    private Integer stock;

    @Schema(description = "1开放 0关房，可选")
    private Integer status;

    @Schema(description = "按日明细，优先级高于统一值")
    private List<DayItem> days;

    @Data
    public static class DayItem {
        private String date;
        private BigDecimal price;
        private Integer stock;
        private Integer status;
    }
}

package com.sqx.app.lvyou.hotel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端酒店设施项
 */
@Data
@Schema(description = "酒店设施项")
public class HotelFacilityVo {

    @Schema(description = "设施ID")
    private Long id;

    @Schema(description = "图标名")
    private String icon;

    @Schema(description = "设施名称")
    private String label;
}

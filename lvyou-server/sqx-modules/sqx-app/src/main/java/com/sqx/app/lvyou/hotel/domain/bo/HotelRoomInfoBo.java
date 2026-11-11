package com.sqx.app.lvyou.hotel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 房型主键入参
 */
@Data
@Schema(description = "房型主键")
public class HotelRoomInfoBo {

    @Schema(description = "房型ID")
    @NotNull(message = "房型ID不能为空")
    private Long id;
}

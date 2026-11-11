package com.sqx.app.lvyou.hotel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新房型
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新房型")
public class HotelRoomUpdateBo extends HotelRoomSaveBo {

    @Schema(description = "房型ID")
    @NotNull(message = "房型ID不能为空")
    private Long id;
}

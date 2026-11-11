package com.sqx.app.lvyou.hotel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 酒店主键入参
 */
@Data
@Schema(description = "酒店主键")
public class HotelInfoBo {

    @Schema(description = "酒店ID")
    @NotNull(message = "酒店ID不能为空")
    private Long id;
}

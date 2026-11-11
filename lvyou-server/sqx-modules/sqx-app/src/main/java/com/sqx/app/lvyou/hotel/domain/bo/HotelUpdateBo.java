package com.sqx.app.lvyou.hotel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端更新酒店
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "更新酒店")
public class HotelUpdateBo extends HotelSaveBo {

    @Schema(description = "酒店ID")
    @NotNull(message = "酒店ID不能为空")
    private Long id;
}

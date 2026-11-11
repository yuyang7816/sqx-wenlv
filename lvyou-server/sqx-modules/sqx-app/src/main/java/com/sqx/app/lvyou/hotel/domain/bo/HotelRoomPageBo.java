package com.sqx.app.lvyou.hotel.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotNull;

/**
 * 管理端房型分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "房型分页查询")
public class HotelRoomPageBo extends PageParam {

    @Schema(description = "酒店ID")
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;

    @Schema(description = "房型名称（模糊）")
    private String name;

    @Schema(description = "状态：1在售 0下架")
    private Integer status;
}

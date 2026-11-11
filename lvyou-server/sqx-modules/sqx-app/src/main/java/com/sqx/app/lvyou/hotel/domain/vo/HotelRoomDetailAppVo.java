package com.sqx.app.lvyou.hotel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端房型详情
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "用户端房型详情")
public class HotelRoomDetailAppVo extends HotelRoomAppVo {

    @Schema(description = "酒店ID")
    private Long hotelId;

    @Schema(description = "酒店名称")
    private String hotelName;

    @Schema(description = "酒店联系电话")
    private String hotelPhone;

    @Schema(description = "入住时间")
    private String checkInTime;

    @Schema(description = "退房时间")
    private String checkOutTime;
}

package com.sqx.app.lvyou.hotel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端酒店政策
 */
@Data
@Schema(description = "酒店政策")
public class HotelPolicyVo {

    @Schema(description = "预订提示")
    private String bookingNotice;

    @Schema(description = "入住提示")
    private String checkInNotice;

    @Schema(description = "宠物政策")
    private String petPolicy;

    @Schema(description = "押金说明")
    private String depositPolicy;

    @Schema(description = "早餐说明")
    private String breakfastInfo;
}

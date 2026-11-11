package com.sqx.app.lvyou.destination.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 当前目的地下已审核商户 VO
 */
@Data
@Schema(description = "目的地商户")
public class DestinationMerchantVo {

    @Schema(description = "商户业务编码（前端 merchantId）")
    private String id;

    @Schema(description = "门店类型键：hotel/restaurant/travel/culture")
    private String typeKey;

    @Schema(description = "门店名称")
    private String name;

    @Schema(description = "门店类型中文")
    private String type;

    @Schema(description = "门店地址")
    private String address;

    @Schema(description = "服务说明")
    private String service;
}

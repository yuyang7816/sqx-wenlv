package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端收货地址
 */
@Data
@Schema(description = "用户端收货地址")
public class AddressVo {

    @Schema(description = "地址ID")
    private Long id;

    @Schema(description = "收货人姓名")
    private String name;

    @Schema(description = "收货人电话")
    private String phone;

    @Schema(description = "省市区")
    private String region;

    @Schema(description = "详细地址")
    private String detail;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;
}

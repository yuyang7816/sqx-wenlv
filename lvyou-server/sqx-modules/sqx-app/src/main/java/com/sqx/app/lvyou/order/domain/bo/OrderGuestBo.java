package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 线路出行人信息
 */
@Data
@Schema(description = "线路出行人")
public class OrderGuestBo {

    @Schema(description = "角色：booker/companion")
    private String role;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "联系电话（预约人必填）")
    private String phone;
}

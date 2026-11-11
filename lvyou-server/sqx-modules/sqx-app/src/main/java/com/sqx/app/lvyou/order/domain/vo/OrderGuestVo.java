package com.sqx.app.lvyou.order.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "出行人信息")
public class OrderGuestVo {

    @Schema(description = "角色：booker/companion")
    private String role;

    @Schema(description = "角色展示")
    private String roleLabel;

    @Schema(description = "姓名")
    private String name;

    @Schema(description = "身份证号")
    private String idCard;

    @Schema(description = "联系电话")
    private String phone;
}

package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户端保存收货地址
 */
@Data
@Schema(description = "保存收货地址")
public class AddressSaveBo {

    @Schema(description = "地址ID（编辑时传）")
    private Long id;

    @Schema(description = "收货人姓名")
    @NotBlank(message = "收货人姓名不能为空")
    private String name;

    @Schema(description = "收货人电话")
    @NotBlank(message = "收货人电话不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    private String phone;

    @Schema(description = "省市区（如：重庆市 荣昌区）")
    @NotBlank(message = "所在地区不能为空")
    private String region;

    @Schema(description = "详细地址")
    @NotBlank(message = "详细地址不能为空")
    private String detail;

    @Schema(description = "是否默认地址")
    private Boolean isDefault;
}

package com.sqx.app.lvyou.canteen.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 用户修改餐饮预约
 */
@Data
@Schema(description = "餐饮预约修改")
public class CanteenReservationUpdateBo {

    @Schema(description = "门店名称")
    private String shopName;

    @Schema(description = "预约菜品")
    private String dishName;

    @NotBlank(message = "联系人不能为空")
    @Schema(description = "联系人姓名")
    private String contactName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "联系电话")
    private String phone;

    @Min(value = 1, message = "用餐人数至少1人")
    @Schema(description = "用餐人数")
    private Integer guestCount;

    @NotBlank(message = "预约时间不能为空")
    @Schema(description = "预约时间 yyyy-MM-dd HH:mm")
    private String reserveTime;

    @NotBlank(message = "用餐地址不能为空")
    @Schema(description = "用餐地址")
    private String address;

    @Schema(description = "备注")
    private String remark;
}

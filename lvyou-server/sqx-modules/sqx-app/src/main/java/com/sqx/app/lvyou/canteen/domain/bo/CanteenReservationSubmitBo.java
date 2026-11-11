package com.sqx.app.lvyou.canteen.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * 餐饮预约提交
 */
@Data
@Schema(description = "餐饮预约提交")
public class CanteenReservationSubmitBo {

    @Schema(description = "商户/门店业务编码（shopId）")
    private String merchantId;

    @Schema(description = "门店业务编码别名")
    private String shopId;

    @Schema(description = "门店名称")
    private String shopName;

    @Schema(description = "预约菜品")
    private String dishName;

    @Schema(description = "目的地业务编码（历史兼容，新客户端请传 lat/lng）")
    private String destinationId;

    @Schema(description = "用户当前纬度（GCJ-02，用于匹配所在地区）")
    private String lat;

    @Schema(description = "用户当前经度（GCJ-02，用于匹配所在地区）")
    private String lng;

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

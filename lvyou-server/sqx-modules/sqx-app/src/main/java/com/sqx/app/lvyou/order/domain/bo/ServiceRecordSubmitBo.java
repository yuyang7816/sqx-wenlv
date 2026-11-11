package com.sqx.app.lvyou.order.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * C 端服务预约提交
 */
@Data
@Schema(description = "服务预约提交")
public class ServiceRecordSubmitBo {

    @NotBlank(message = "商户不能为空")
    @Schema(description = "商户业务编码")
    private String merchantId;

    @Schema(description = "目的地业务编码，空则取当前目的地")
    private String destinationId;

    @NotBlank(message = "联系人不能为空")
    @Schema(description = "联系人姓名")
    private String contactName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1\\d{10}$", message = "手机号格式不正确")
    @Schema(description = "联系电话")
    private String phone;

    @NotBlank(message = "预约意向不能为空")
    @Schema(description = "预约意向")
    private String intent;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "预约/入住日期 yyyy-MM-dd")
    private String date;

    @Schema(description = "人数")
    private Integer guestCount;
}

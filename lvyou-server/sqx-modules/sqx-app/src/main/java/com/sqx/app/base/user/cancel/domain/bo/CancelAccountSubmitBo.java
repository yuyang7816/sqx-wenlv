package com.sqx.app.base.user.cancel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 用户提交注销申请
 */
@Data
@Schema(description = "用户注销申请")
public class CancelAccountSubmitBo {

    @Size(max = 500, message = "注销原因不能超过500字")
    @Schema(description = "注销原因")
    private String reason;
}

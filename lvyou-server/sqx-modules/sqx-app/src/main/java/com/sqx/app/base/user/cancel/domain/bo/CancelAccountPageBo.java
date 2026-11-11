package com.sqx.app.base.user.cancel.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端注销申请分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "注销申请分页查询")
public class CancelAccountPageBo extends PageParam {

    @Schema(description = "状态：pending/approved/rejected")
    private String status;

    @Schema(description = "用户手机号（模糊）")
    private String userPhone;

    @Schema(description = "用户昵称（模糊）")
    private String userName;
}

package com.sqx.app.lvyou.kefu.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "在线客服会话分页查询")
public class KefuImSessionPageBo extends PageParam {

    @Schema(description = "会话状态：open/closed")
    private String status;

    @Schema(description = "用户手机号（模糊）")
    private String userPhone;

    @Schema(description = "仅看客服未读")
    private Boolean unreadOnly;
}

package com.sqx.app.lvyou.invite.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端邀请关系分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理端邀请关系分页查询")
public class InviteRelationAdminPageBo extends PageParam {

    @Schema(description = "注册邀请码（模糊）")
    private String inviteCode;

    @Schema(description = "被邀请用户手机号（模糊）")
    private String memberPhone;

    @Schema(description = "一级邀请人手机号（模糊）")
    private String inviterPhone;

    @Schema(description = "二级邀请人手机号（模糊）")
    private String inviterLevel2Phone;
}

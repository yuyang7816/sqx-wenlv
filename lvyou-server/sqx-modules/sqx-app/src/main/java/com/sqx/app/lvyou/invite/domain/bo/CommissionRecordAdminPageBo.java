package com.sqx.app.lvyou.invite.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端佣金记录分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理端佣金记录分页查询")
public class CommissionRecordAdminPageBo extends PageParam {

    @Schema(description = "佣金记录ID")
    private Long id;

    @Schema(description = "订单号（模糊）")
    private String orderNo;

    @Schema(description = "受益人手机号（模糊）")
    private String beneficiaryPhone;

    @Schema(description = "下单用户手机号（模糊）")
    private String sourcePhone;

    @Schema(description = "佣金层级：1一级 2二级")
    private Integer commissionLevel;

    @Schema(description = "状态：settled")
    private String status;
}

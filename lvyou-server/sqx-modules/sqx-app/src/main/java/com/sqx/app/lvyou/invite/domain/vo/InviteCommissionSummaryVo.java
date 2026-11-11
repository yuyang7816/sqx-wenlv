package com.sqx.app.lvyou.invite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 收益概览
 */
@Data
public class InviteCommissionSummaryVo {

    @Schema(description = "总收益")
    private String total;

    @Schema(description = "一级收益")
    private String level1;

    @Schema(description = "二级收益")
    private String level2;

    @Schema(description = "一级收益笔数")
    private Integer level1Count;

    @Schema(description = "二级收益笔数")
    private Integer level2Count;
}

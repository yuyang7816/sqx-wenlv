package com.sqx.app.lvyou.stats.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 统计分布项（品类/状态等）
 */
@Getter
@Setter
@Schema(name = "StatsBreakdownItemVo", description = "统计分布项")
public class StatsBreakdownItemVo {

    @Schema(description = "业务编码")
    private String code;

    @Schema(description = "展示名称")
    private String label;

    @Schema(description = "数量")
    private Long count;

    @Schema(description = "已支付 GMV 汇总（可选）")
    private BigDecimal amount;
}

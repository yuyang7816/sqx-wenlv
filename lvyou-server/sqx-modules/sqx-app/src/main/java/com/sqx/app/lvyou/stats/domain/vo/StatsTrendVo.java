package com.sqx.app.lvyou.stats.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 管理端业务趋势统计
 */
@Getter
@Setter
@Schema(name = "StatsTrendVo", description = "管理端业务趋势统计")
public class StatsTrendVo {

    @Schema(description = "近 N 日订单数折线")
    private List<StatsTrendPointVo> orderTrend;

    @Schema(description = "近 N 日 GMV 折线")
    private List<StatsTrendPointVo> gmvTrend;

    @Schema(description = "近 N 日新增用户折线")
    private List<StatsTrendPointVo> userTrend;

    @Schema(description = "订单品类分布")
    private List<StatsBreakdownItemVo> categoryBreakdown;

    @Schema(description = "订单状态分布")
    private List<StatsBreakdownItemVo> statusBreakdown;
}

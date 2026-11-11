package com.sqx.app.lvyou.stats.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 趋势折线单点数据
 */
@Getter
@Setter
@Schema(name = "StatsTrendPointVo", description = "趋势折线单点数据")
public class StatsTrendPointVo {

    @Schema(description = "统计日期，格式 yyyy-MM-dd")
    private String date;

    @Schema(description = "订单数（orderTrend 使用）")
    private Long count;

    @Schema(description = "GMV 金额（gmvTrend 使用）")
    private BigDecimal amount;
}

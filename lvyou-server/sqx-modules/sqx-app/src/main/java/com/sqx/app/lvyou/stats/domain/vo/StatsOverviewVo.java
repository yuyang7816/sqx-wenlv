package com.sqx.app.lvyou.stats.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/**
 * 管理端业务概览统计
 */
@Getter
@Setter
@Schema(name = "StatsOverviewVo", description = "管理端业务概览统计")
public class StatsOverviewVo {

    @Schema(description = "今日 GMV（已支付订单实付金额汇总）")
    private BigDecimal todayGmv;

    @Schema(description = "本月 GMV（已支付订单实付金额汇总）")
    private BigDecimal monthGmv;

    @Schema(description = "平台订单总量（全状态，排除 restaurant 影子单，见 PlatformOrderStatsScope）")
    private Long orderCount;

    @Schema(description = "待审核商户申请数")
    private Long pendingMerchantCount;

    @Schema(description = "今日新增用户数")
    private Long newUserCount;

    @Schema(description = "用户总数")
    private Long userCount;

    @Schema(description = "商户总数")
    private Long merchantCount;

    @Schema(description = "本月 GMV（与 monthGmv 一致，供仪表盘兼容）")
    private BigDecimal gmv;

    @Schema(description = "待审核商户数（与 pendingMerchantCount 一致，供仪表盘兼容）")
    private Long pendingAudit;

    @Schema(description = "今日下单量")
    private Long todayOrderCount;

    @Schema(description = "待处理订单数（待支付/待确认/待使用/待收货）")
    private Long pendingOrderCount;

    @Schema(description = "退款中订单数")
    private Long refundingOrderCount;

    @Schema(description = "已上线目的地数")
    private Long destinationOnlineCount;

    @Schema(description = "上架景区数")
    private Long scenicOnlineCount;

    @Schema(description = "上架活动数")
    private Long activityOnlineCount;
}

package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商户中心概览
 */
@Getter
@Setter
@Schema(description = "商户中心概览")
public class MerchantDashboardVo {

    @Schema(description = "商户摘要")
    private MerchantProfileVo merchant;

    @Schema(description = "服务统计")
    private ServiceStats service;

    @Schema(description = "订单统计")
    private OrderStats order;

    @Schema(description = "营收统计")
    private RevenueStats revenue;

    @Schema(description = "钱包摘要")
    private WalletSummary wallet;

    @Schema(description = "待处理订单数")
    private Long pendingCount;

    @Getter
    @Setter
    @Schema(description = "服务统计")
    public static class ServiceStats {
        @Schema(description = "服务总数")
        private Long total;
        @Schema(description = "上架中数量")
        private Long onSale;
        @Schema(description = "上架标签文案")
        private String onSaleLabel;
    }

    @Getter
    @Setter
    @Schema(description = "订单统计")
    public static class OrderStats {
        @Schema(description = "订单总数")
        private Long total;
        @Schema(description = "待处理数")
        private Long pending;
        @Schema(description = "进行中数")
        private Long processing;
        @Schema(description = "已完结数")
        private Long closed;
        @Schema(description = "今日订单数")
        private Long todayCount;
        @Schema(description = "待核销数（酒店）")
        private Long pendingVerify;

        @Schema(description = "待发货数（文创好物）")
        private Long pendingShip;

        @Schema(description = "待跟进预约数（线路）")
        private Long pendingRouteBooking;
    }

    @Schema(description = "待核销订单（酒店，首页展示）")
    private List<MerchantOrderVo> pendingVerifyOrders;

    @Schema(description = "待发货订单（文创商家，首页展示）")
    private List<MerchantOrderVo> pendingShipOrders;

    @Schema(description = "待跟进线路预约（旅游商家首页展示）")
    private List<MerchantOrderVo> pendingRouteBookings;

    @Getter
    @Setter
    @Schema(description = "营收统计")
    public static class RevenueStats {
        @Schema(description = "今日营收/预约")
        private BigDecimal today;
        @Schema(description = "本月营收/预约")
        private BigDecimal month;
        @Schema(description = "累计营收")
        private BigDecimal total;
        @Schema(description = "是否金额展示")
        private Boolean isMoney;
    }

    @Getter
    @Setter
    @Schema(description = "钱包摘要")
    public static class WalletSummary {
        @Schema(description = "可提现余额")
        private BigDecimal balance;
        @Schema(description = "待结算")
        private BigDecimal pendingSettle;
        @Schema(description = "累计收入")
        private BigDecimal totalIncome;

        @Schema(description = "应缴保证金")
        private BigDecimal depositRequired;

        @Schema(description = "已缴保证金")
        private BigDecimal depositPaid;

        @Schema(description = "保证金状态：paid/unpaid")
        private String depositStatus;
    }
}

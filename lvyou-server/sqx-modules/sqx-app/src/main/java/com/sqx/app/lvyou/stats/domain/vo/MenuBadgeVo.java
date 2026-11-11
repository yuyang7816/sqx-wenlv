package com.sqx.app.lvyou.stats.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理端侧边栏菜单角标统计
 */
@Getter
@Setter
@Schema(name = "MenuBadgeVo", description = "管理端侧边栏菜单角标统计")
public class MenuBadgeVo {

    @Schema(description = "待审核商户入驻申请数")
    private Long pendingMerchantCount;

    @Schema(description = "待处理订单数（待支付/待确认/待使用/待收货）")
    private Long pendingOrderCount;

    @Schema(description = "退款中订单数")
    private Long refundingOrderCount;

    @Schema(description = "待审核提现申请数")
    private Long pendingWithdrawCount;

    @Schema(description = "待审核用户注销申请数")
    private Long pendingCancelAccountCount;

    @Schema(description = "待确认餐饮预约数")
    private Long pendingCanteenReservationCount;
}

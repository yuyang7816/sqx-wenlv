package com.sqx.app.base.pay.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理端支付流水分页查询
 */
@Getter
@Setter
@Schema(description = "管理端支付流水分页查询")
public class PaymentAdminPageBo extends PageParam {

    @Schema(description = "支付记录ID（订单ID或充值单ID）")
    private Long id;

    @Schema(description = "支付单号（订单号/充值单号）模糊筛选")
    private String payNo;

    @Schema(description = "业务类型：tourism/wallet_recharge")
    private String businessType;

    @Schema(description = "支付方式：wx/ali/wallet")
    private String payMethod;

    @Schema(description = "用户手机号模糊筛选")
    private String userPhone;

    @Schema(description = "支付开始时间 yyyy-MM-dd HH:mm:ss")
    private String payTimeStart;

    @Schema(description = "支付结束时间 yyyy-MM-dd HH:mm:ss")
    private String payTimeEnd;
}

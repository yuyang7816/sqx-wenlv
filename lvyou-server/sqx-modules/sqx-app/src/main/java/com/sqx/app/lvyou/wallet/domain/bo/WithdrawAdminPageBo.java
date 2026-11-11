package com.sqx.app.lvyou.wallet.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端提现分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理端提现分页查询")
public class WithdrawAdminPageBo extends PageParam {

    @Schema(description = "提现记录ID")
    private Long id;

    @Schema(description = "提现单号模糊筛选")
    private String withdrawNo;

    @Schema(description = "主体类型：user/merchant")
    private String ownerType;

    @Schema(description = "状态：pending/success/failed")
    private String status;

    @Schema(description = "提现方式：alipay/wechat")
    private String method;

    @Schema(description = "用户手机号模糊筛选")
    private String userPhone;

    @Schema(description = "商户名称模糊筛选")
    private String merchantName;
}

package com.sqx.app.lvyou.canteen.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端餐饮预约分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "管理端餐饮预约分页查询")
public class CanteenReservationAdminPageBo extends PageParam {

    @Schema(description = "预约ID")
    private Long id;

    @Schema(description = "预约单号（模糊）")
    private String reservationNo;

    @Schema(description = "门店名称（模糊）")
    private String shopName;

    @Schema(description = "联系人手机号（模糊）")
    private String contactPhone;

    @Schema(description = "用户手机号（模糊，关联用户表）")
    private String userPhone;

    @Schema(description = "商户名称（模糊，关联商户表）")
    private String merchantName;

    @Schema(description = "预约状态：pending/confirmed/completed/cancelled")
    private String status;

    @Schema(description = "目的地ID")
    private Long destinationId;
}

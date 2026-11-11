package com.sqx.app.lvyou.canteen.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 餐饮预约列表查询
 */
@Data
@Schema(description = "餐饮预约列表查询")
public class CanteenReservationListBo {

    @Schema(description = "页码")
    private Long page;

    @Schema(description = "每页条数")
    private Long limit;

    @Schema(description = "状态：pending/confirmed/completed/cancelled")
    private String status;
}

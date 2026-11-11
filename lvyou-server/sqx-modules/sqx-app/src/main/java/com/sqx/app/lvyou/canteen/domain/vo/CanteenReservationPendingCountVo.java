package com.sqx.app.lvyou.canteen.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 餐饮预约待处理数量
 */
@Data
@Schema(description = "餐饮预约待处理数量")
public class CanteenReservationPendingCountVo {

    @Schema(description = "待确认数量")
    private Long count;
}

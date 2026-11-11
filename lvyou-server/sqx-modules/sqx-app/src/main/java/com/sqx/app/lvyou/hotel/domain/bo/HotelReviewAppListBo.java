package com.sqx.app.lvyou.hotel.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端酒店评价列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "酒店评价列表查询")
public class HotelReviewAppListBo extends PageParam {

    @Schema(description = "酒店ID")
    private Long hotelId;
}

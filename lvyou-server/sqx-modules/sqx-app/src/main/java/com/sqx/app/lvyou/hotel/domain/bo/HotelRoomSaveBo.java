package com.sqx.app.lvyou.hotel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * 管理端新增房型
 */
@Data
@Schema(description = "新增房型")
public class HotelRoomSaveBo {

    @Schema(description = "酒店ID")
    @NotNull(message = "酒店ID不能为空")
    private Long hotelId;

    @Schema(description = "商户ID")
    @NotNull(message = "商户ID不能为空")
    private Long merchantId;

    @Schema(description = "房型名称")
    @NotBlank(message = "房型名称不能为空")
    private String name;

    @Schema(description = "规格描述")
    private String spec;

    @Schema(description = "每晚价格")
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "状态：1在售 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;
}

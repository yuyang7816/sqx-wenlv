package com.sqx.app.lvyou.hotel.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 管理端房型
 */
@Data
@Schema(description = "管理端房型")
public class HotelRoomVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "酒店ID")
    private Long hotelId;

    @Schema(description = "酒店名称")
    private String hotelName;

    @Schema(description = "商户ID")
    private Long merchantId;

    @Schema(description = "商户名称")
    private String merchantName;

    @Schema(description = "房型名称")
    private String name;

    @Schema(description = "规格描述")
    private String spec;

    @Schema(description = "每晚价格")
    private BigDecimal price;

    @Schema(description = "库存")
    private Integer stock;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

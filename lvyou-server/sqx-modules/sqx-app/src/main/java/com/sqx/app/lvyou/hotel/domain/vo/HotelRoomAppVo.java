package com.sqx.app.lvyou.hotel.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户端房型
 */
@Data
@Schema(description = "用户端房型")
public class HotelRoomAppVo {

    @Schema(description = "房型ID")
    private Long id;

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

    @Schema(description = "房型说明")
    private String desc;

    @Schema(description = "房型相册")
    private List<String> gallery = new ArrayList<>();

    @Schema(description = "面积（㎡）")
    private String area;

    @Schema(description = "楼层")
    private String floor;

    @Schema(description = "可住人数")
    private Integer occupancy;

    @Schema(description = "床型")
    private String bedType;

    @Schema(description = "床数量")
    private Integer bedCount;

    @Schema(description = "床宽（米）")
    private String bedSize;

    @Schema(description = "加床政策")
    private String extraBedPolicy;

    @Schema(description = "宠物政策")
    private String petPolicy;

    @Schema(description = "窗户")
    private String windowType;

    @Schema(description = "吸烟政策")
    private String smokingPolicy;

    @Schema(description = "房型设施")
    private List<HotelFacilityVo> facilities = new ArrayList<>();

    @Schema(description = "卖点标签")
    private List<String> tags = new ArrayList<>();

    @Schema(description = "连住总价（传入日期时返回）")
    private BigDecimal quoteTotal;

    @Schema(description = "连住可售间数（传入日期时返回）")
    private Integer quoteAvailable;
}

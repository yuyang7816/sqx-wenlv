package com.sqx.app.lvyou.hotel.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端酒店分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "酒店分页查询")
public class HotelPageBo extends PageParam {

    @Schema(description = "酒店ID")
    private Long id;

    @Schema(description = "关联商户ID")
    private Long merchantId;

    @Schema(description = "酒店名称（模糊）")
    private String name;

    @Schema(description = "分类：luxury/boutique/business/resort")
    private String category;

    @Schema(description = "关联目的地ID")
    private Long destinationId;

    @Schema(description = "关联商户名称（模糊）")
    private String merchantName;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;
}

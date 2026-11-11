package com.sqx.app.lvyou.coupon.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * 管理端优惠券模板分页查询
 */
@Getter
@Setter
@Schema(description = "优惠券模板分页查询")
public class CouponTemplatePageBo extends PageParam {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "模板业务编码（模糊）")
    private String templateCode;

    @Schema(description = "优惠券名称（模糊）")
    private String name;

    @Schema(description = "适用范围：all/hotel/route/goods")
    private String scope;

    @Schema(description = "状态：1可领取 0下架")
    private Integer status;
}

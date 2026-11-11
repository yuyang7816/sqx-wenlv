package com.sqx.app.lvyou.heritage.shop.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 非遗好店运费配置
 */
@Data
@Schema(description = "非遗好店运费配置")
public class HeritageShopShippingVo {

    @Schema(description = "是否全国包邮")
    private Boolean freeShipping;

    @Schema(description = "统一运费（元）")
    private BigDecimal fixedFee;

    @Schema(description = "满额包邮门槛（元，0 表示不启用）")
    private BigDecimal freeAmountThreshold;

    @Schema(description = "运费说明")
    private String shippingNote;
}

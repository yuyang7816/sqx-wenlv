package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 门店装修保存
 */
@Getter
@Setter
@Schema(description = "门店装修保存")
public class MerchantStoreDecorSaveBo {

    @Schema(description = "装修内容（部分更新）")
    private Map<String, Object> decor;
}

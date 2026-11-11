package com.sqx.app.lvyou.dict.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 字典项（C 端 / 管理端筛选项）
 */
@Data
@Schema(name = "DictItemVo", description = "字典项")
public class DictItemVo {

    @Schema(description = "业务编码")
    private String code;

    @Schema(description = "展示文案")
    private String label;

    @Schema(description = "字典值（通常与 code 相同）")
    private String value;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "应缴保证金（元，仅商户入驻类型）")
    private BigDecimal depositAmount;
}

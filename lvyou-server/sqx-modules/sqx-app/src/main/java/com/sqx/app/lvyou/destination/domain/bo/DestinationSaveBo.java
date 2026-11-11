package com.sqx.app.lvyou.destination.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 管理端新增目的地
 */
@Data
@Schema(description = "新增目的地")
public class DestinationSaveBo {

    @Schema(description = "目的地业务编码")
    @NotBlank(message = "目的地业务编码不能为空")
    private String destinationCode;

    @Schema(description = "目的地全称")
    @NotBlank(message = "目的地名称不能为空")
    private String name;

    @Schema(description = "简称")
    private String shortName;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "状态：1已上线 2即将上线 0禁用")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "是否默认目的地：1是 0否")
    private Integer isDefault;

    @Schema(description = "商户数量")
    private Integer merchantCount;

    @Schema(description = "资源数量")
    private Integer resourceCount;

    @Schema(description = "排序")
    private Integer sort;
}

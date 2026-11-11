package com.sqx.app.lvyou.destination.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端目的地分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "目的地分页查询")
public class DestinationPageBo extends PageParam {

    @Schema(description = "目的地ID")
    private Long id;

    @Schema(description = "目的地业务编码")
    private String destinationCode;

    @Schema(description = "目的地名称（模糊）")
    private String name;

    @Schema(description = "状态：1已上线 2即将上线 0禁用")
    private Integer status;
}

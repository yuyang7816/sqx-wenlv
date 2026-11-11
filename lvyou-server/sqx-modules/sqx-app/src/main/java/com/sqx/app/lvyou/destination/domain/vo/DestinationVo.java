package com.sqx.app.lvyou.destination.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 管理端目的地详情
 */
@Data
@Schema(description = "管理端目的地")
public class DestinationVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "目的地业务编码")
    private String destinationCode;

    @Schema(description = "目的地全称")
    private String name;

    @Schema(description = "简称")
    private String shortName;

    @Schema(description = "简介")
    private String intro;

    @Schema(description = "状态：1已上线 2即将上线 0禁用")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "是否默认目的地：1是 0否")
    private Integer isDefault;

    @Schema(description = "商户数量")
    private Integer merchantCount;

    @Schema(description = "资源数量")
    private Integer resourceCount;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

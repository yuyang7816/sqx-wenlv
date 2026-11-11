package com.sqx.app.lvyou.activity.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端活动详情
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端活动详情")
public class ActivityAppDetailVo extends ActivityAppVo {

    @Schema(description = "活动介绍")
    private String desc;

    @Schema(description = "活动亮点")
    private List<ActivityHighlightVo> highlights;

    @Schema(description = "参与须知")
    private List<String> notices;

    @Schema(description = "报名上限，0表示不限")
    private Integer enrollLimit;

    @Schema(description = "当前用户是否已报名")
    private Boolean enrolled;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;
}

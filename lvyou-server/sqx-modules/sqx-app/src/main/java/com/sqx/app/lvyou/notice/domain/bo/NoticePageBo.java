package com.sqx.app.lvyou.notice.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端公告分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "公告分页查询")
public class NoticePageBo extends PageParam {

    @Schema(description = "公告标题（模糊）")
    private String title;

    @Schema(description = "分类：activity/scenic/traffic/policy")
    private String category;

    @Schema(description = "关联目的地ID")
    private Long destinationId;

    @Schema(description = "状态：1已发布 0草稿")
    private Integer state;
}

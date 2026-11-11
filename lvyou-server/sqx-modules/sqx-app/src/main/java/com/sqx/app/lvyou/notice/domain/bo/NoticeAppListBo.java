package com.sqx.app.lvyou.notice.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户端公告列表查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "用户端公告列表查询")
public class NoticeAppListBo extends PageParam {

    @Schema(description = "关联目的地ID，筛选该目的地及全平台公告")
    private Long destinationId;

    @Schema(description = "分类：all/activity/scenic/traffic/policy")
    private String category;
}

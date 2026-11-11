package com.sqx.app.lvyou.notice.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 公告主键操作
 */
@Data
@Schema(description = "公告主键")
public class NoticeInfoBo {

    @Schema(description = "主键")
    @NotNull(message = "id不能为空")
    private Long id;
}

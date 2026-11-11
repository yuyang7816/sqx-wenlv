package com.sqx.app.lvyou.notice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 公告跑马灯
 */
@Data
@Schema(description = "公告跑马灯")
public class NoticeMarqueeVo {

    @Schema(description = "最新公告列表（最多3条）")
    private List<NoticeAppVo> items;

    @Schema(description = "跑马灯展示文案，多条以空格分隔")
    private String text;
}

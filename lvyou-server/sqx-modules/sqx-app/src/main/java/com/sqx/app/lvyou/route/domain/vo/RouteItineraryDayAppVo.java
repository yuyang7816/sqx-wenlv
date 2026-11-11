package com.sqx.app.lvyou.route.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户端按天行程
 */
@Data
@Schema(description = "用户端按天行程")
public class RouteItineraryDayAppVo {

    @Schema(description = "天序号，从 1 开始")
    private Integer dayNo;

    @Schema(description = "天标签，如第1天")
    private String dayLabel;

    @Schema(description = "当天标题")
    private String title;

    @Schema(description = "当天详细内容")
    private String content;

    @Schema(description = "当天图片")
    private List<String> images;
}

package com.sqx.app.lvyou.activity.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 用户端活动列表项
 */
@Data
@Schema(description = "用户端活动列表项")
public class ActivityAppVo {

    @Schema(description = "活动ID")
    private Long id;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "活动地址")
    private String address;

    @Schema(description = "活动时间展示")
    private String time;

    @Schema(description = "活动阶段：ongoing/upcoming/ended")
    private String status;

    @Schema(description = "活动阶段文案")
    private String statusLabel;

    @Schema(description = "已参与人数")
    private Integer participants;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "参考价格（线下签到支付，线上仅报名）")
    private BigDecimal price;

    @Schema(description = "费用说明")
    private String priceTip;
}

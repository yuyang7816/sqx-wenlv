package com.sqx.app.lvyou.activity.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端活动
 */
@Data
@Schema(description = "管理端活动")
public class ActivityVo {

    @Schema(description = "主键")
    private Long id;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "目的地名称")
    private String destinationName;

    @Schema(description = "活动名称")
    private String name;

    @Schema(description = "活动地址")
    private String address;

    @Schema(description = "纬度（地图选点）")
    private BigDecimal latitude;

    @Schema(description = "经度（地图选点）")
    private BigDecimal longitude;

    @Schema(description = "参考价格（线下签到支付，线上仅报名）")
    private BigDecimal priceFrom;

    @Schema(description = "标签（JSON 数组或单标签）")
    private String tag;

    @Schema(description = "标签列表")
    private List<String> tags;

    @Schema(description = "封面图")
    private String coverImage;

    @Schema(description = "活动介绍")
    private String description;

    @Schema(description = "活动开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;

    @Schema(description = "活动结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;

    @Schema(description = "报名上限，0表示不限")
    private Integer enrollLimit;

    @Schema(description = "已报名人数")
    private Integer enrollCount;

    @Schema(description = "活动亮点")
    private List<ActivityHighlightVo> highlights;

    @Schema(description = "参与须知")
    private List<String> notices;

    @Schema(description = "活动阶段")
    private String phase;

    @Schema(description = "活动阶段文案")
    private String phaseLabel;

    @Schema(description = "状态：1上架 0下架")
    private Integer status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
}

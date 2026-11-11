package com.sqx.app.lvyou.activity.domain.bo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sqx.app.lvyou.activity.domain.vo.ActivityHighlightVo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 管理端新增活动
 */
@Data
@Schema(description = "新增活动")
public class ActivitySaveBo {

    @Schema(description = "目的地ID（服务端按经纬度自动解析，客户端勿传）")
    private Long destinationId;

    @Schema(description = "活动名称")
    @NotBlank(message = "活动名称不能为空")
    private String name;

    @Schema(description = "活动地址（地图选点回填，勿手填）")
    private String address;

    @Schema(description = "发布位置纬度（地图选点，用于自动匹配目的地）")
    @NotNull(message = "请在地图上搜索并选择活动地址")
    private BigDecimal latitude;

    @Schema(description = "发布位置经度（地图选点，用于自动匹配目的地）")
    @NotNull(message = "请在地图上搜索并选择活动地址")
    private BigDecimal longitude;

    @Schema(description = "参考价格（线下签到支付说明，线上仅报名不收费）")
    private BigDecimal priceFrom;

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

    @Schema(description = "活动亮点")
    private List<ActivityHighlightVo> highlights;

    @Schema(description = "参与须知")
    private List<String> notices;

    @Schema(description = "状态：1上架 0下架")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sort;
}

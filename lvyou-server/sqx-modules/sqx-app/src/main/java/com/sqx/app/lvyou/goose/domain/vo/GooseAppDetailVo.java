package com.sqx.app.lvyou.goose.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * 灰鹅美食商家详情
 */
@Getter
@Setter
@Schema(description = "灰鹅美食商家详情")
public class GooseAppDetailVo {

    @Schema(description = "商家ID")
    private Long id;

    @Schema(description = "商户业务编码")
    private String merchantCode;

    @Schema(description = "门店名称")
    private String name;

    @Schema(description = "角标文案")
    private String badgeText;

    @Schema(description = "评分（餐饮预约不使用，固定为空）")
    private String rating;

    @Schema(description = "已预约数（不含已取消）")
    private Integer monthlySales;

    @Schema(description = "门店地址")
    private String address;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "标签")
    private List<String> tags;

    @Schema(description = "起售价格（元）")
    private BigDecimal priceFrom;

    @Schema(description = "店铺介绍")
    private String intro;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "轮播图")
    private List<String> bannerList;

    @Schema(description = "招牌菜品预览（前若干条）")
    private List<GooseMenuItemVo> specialtyList;

    @Schema(description = "营业时间")
    private List<GooseBusinessHourVo> businessHours;

    @Schema(description = "目的地ID")
    private Long destinationId;

    @Schema(description = "营业状态：1营业中 0休息中")
    private Integer isOpen;

    @Schema(description = "当前是否排队")
    private Boolean queueEnabled;

    @Schema(description = "前面排队桌数")
    private Integer queueTableCount;

    @Schema(description = "排队展示文案")
    private String queueDisplayText;
}

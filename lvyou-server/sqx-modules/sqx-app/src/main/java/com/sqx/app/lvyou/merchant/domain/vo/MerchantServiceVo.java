package com.sqx.app.lvyou.merchant.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 商户服务
 */
@Getter
@Setter
@Schema(description = "商户服务")
public class MerchantServiceVo {

    @Schema(description = "服务ID")
    private Long id;

    @Schema(description = "服务业务编码")
    private String serviceCode;

    @Schema(description = "服务名称")
    private String name;

    @Schema(description = "服务价格")
    private BigDecimal price;

    @Schema(description = "库存/名额")
    private Integer stock;

    @Schema(description = "服务说明")
    private String desc;

    @Schema(description = "规格描述")
    private String spec;

    @Schema(description = "集合地点")
    private String meetPoint;

    @Schema(description = "途经景点")
    private String spots;

    @Schema(description = "旅行城市")
    private String travelCity;

    @Schema(description = "行程天数")
    private Integer dayCount;

    @Schema(description = "每日行程")
    private List<Map<String, Object>> itineraryDays;

    @Schema(description = "商品规格")
    private String goodsSpecs;

    @Schema(description = "详情介绍")
    private String detail;

    @Schema(description = "酒店房型扩展信息")
    private Map<String, Object> roomDetail;

    @Schema(description = "关联客房标准ID")
    private Long roomStandardId;

    @Schema(description = "关联客房标准名称")
    private String roomStandardName;

    @Schema(description = "是否继承客房标准")
    private Boolean inheritStandard;

    @Schema(description = "月售数量")
    private Integer monthlySales;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "轮播图")
    private List<String> banners;

    @Schema(description = "商品详情图")
    private List<String> detailImages;

    @Schema(description = "状态：on_sale/off_sale/draft")
    private String status;

    @Schema(description = "状态文案")
    private String statusLabel;

    @Schema(description = "首页推荐Tab业务键")
    private String homeTabKey;

    @Schema(description = "首页推荐Tab名称")
    private String homeTabLabel;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime updatedAt;
}

package com.sqx.app.lvyou.merchant.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 商户服务保存
 */
@Getter
@Setter
@Schema(description = "商户服务保存")
public class MerchantServiceSaveBo {

    @Schema(description = "服务ID，为空则新建")
    private Long id;

    @NotBlank(message = "请填写服务名称")
    @Schema(description = "服务名称")
    private String name;

    @Schema(description = "服务价格，0表示免费预约")
    private BigDecimal price;

    @Schema(description = "库存/名额")
    private Integer stock;

    @Schema(description = "服务说明")
    private String desc;

    @Schema(description = "规格描述（酒店房型规格等）")
    private String spec;

    @Schema(description = "集合地点（旅游线路服务）")
    private String meetPoint;

    @Schema(description = "途经景点（逗号分隔，旅游线路服务）")
    private String spots;

    @Schema(description = "旅行城市")
    private String travelCity;

    @Schema(description = "行程天数")
    private Integer dayCount;

    @Schema(description = "每日行程（标题/正文/图片）")
    private List<Map<String, Object>> itineraryDays;

    @Schema(description = "商品规格（逗号分隔，文创）")
    private String goodsSpecs;

    @Schema(description = "详情介绍（文创商品等）")
    private String detail;

    @Schema(description = "酒店房型扩展信息")
    private Map<String, Object> roomDetail;

    @Schema(description = "关联客房标准ID")
    private Long roomStandardId;

    @Schema(description = "是否继承客房标准（设施/默认参数/共用相册标签）")
    private Boolean inheritStandard;

    @Schema(description = "月售数量（餐饮菜品）")
    private Integer monthlySales;

    @Schema(description = "封面图URL")
    private String cover;

    @Schema(description = "轮播图URL列表")
    private List<String> banners;

    @Schema(description = "商品详情图URL列表（文创）")
    private List<String> detailImages;

    @Schema(description = "状态：on_sale/off_sale/draft")
    private String status;

    @Schema(description = "首页推荐Tab业务键")
    private String homeTabKey;
}

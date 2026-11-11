package com.sqx.app.lvyou.heritage.goods.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 提交商品评价入参（须关联已完成订单）
 */
@Data
@Schema(description = "提交商品评价")
public class GoodsReviewSubmitBo {

    @NotNull(message = "订单ID不能为空")
    @Schema(description = "已完成订单ID（防刷评价）")
    private Long orderId;

    @NotBlank(message = "评价内容不能为空")
    @Size(max = 512, message = "评价内容不能超过512字")
    @Schema(description = "评价内容")
    private String content;

    @NotNull(message = "评分不能为空")
    @Min(value = 1, message = "评分最低1分")
    @Max(value = 5, message = "评分最高5分")
    @Schema(description = "评分1-5")
    private Integer rating;

    @Size(max = 6, message = "评价图片最多6张")
    @Schema(description = "评价图片URL列表（上传接口返回，最多6张）")
    private List<String> images;
}

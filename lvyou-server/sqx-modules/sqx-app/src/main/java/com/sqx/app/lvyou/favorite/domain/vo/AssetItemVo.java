package com.sqx.app.lvyou.favorite.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 收藏/足迹列表项（含关联业务展示字段）
 */
@Data
@Schema(description = "收藏/足迹列表项")
public class AssetItemVo {

    @Schema(description = "收藏/浏览对象类型")
    private String targetType;

    @Schema(description = "与 targetType 同义，兼容用户端 moduleType 字段")
    private String moduleType;

    @Schema(description = "收藏/浏览对象业务ID")
    private Long targetId;

    @Schema(description = "与 targetId 同义，兼容用户端 id 字段")
    private Long id;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "地址/位置")
    private String address;

    @Schema(description = "用户端跳转路径")
    private String path;

    @Schema(description = "封面图")
    private String cover;

    @Schema(description = "简介")
    private String desc;

    @Schema(description = "价格/起步价")
    private BigDecimal price;

    @Schema(description = "收藏时间戳（毫秒）")
    private Long favoritedAt;

    @Schema(description = "浏览时间戳（毫秒）")
    private Long visitedAt;
}

package com.sqx.app.lvyou.favorite.support;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 收藏/足迹关联对象快照（展示字段）
 */
@Data
public class AssetTargetSnapshot {

    private String targetType;

    private Long targetId;

    private String name;

    private String address;

    private String cover;

    private String desc;

    private BigDecimal price;

    private String path;
}

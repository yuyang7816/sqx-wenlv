package com.sqx.app.base.map.domain.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 地图 POI（停车/交通）
 */
@Getter
@Setter
@TableName("tb_map_poi")
@Schema(name = "MapPoiEntity", description = "地图POI")
public class MapPoiEntity implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("destination_id")
    private Long destinationId;

    @TableField("poi_type")
    private String poiType;

    @TableField("name")
    private String name;

    @TableField("address")
    private String address;

    @TableField("latitude")
    private BigDecimal latitude;

    @TableField("longitude")
    private BigDecimal longitude;

    @TableField("tag")
    private String tag;

    @TableField("spaces")
    private Integer spaces;

    @TableField("total_spaces")
    private Integer totalSpaces;

    @TableField("fee_desc")
    private String feeDesc;

    @TableField("status")
    private Integer status;

    @TableField("sort")
    private Integer sort;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "create_time", fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField(value = "update_time", fill = FieldFill.UPDATE)
    private LocalDateTime updateTime;
}

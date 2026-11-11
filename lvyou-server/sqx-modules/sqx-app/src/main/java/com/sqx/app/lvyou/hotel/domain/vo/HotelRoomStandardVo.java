package com.sqx.app.lvyou.hotel.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "客房标准")
public class HotelRoomStandardVo {

    @Schema(description = "模板ID")
    private Long id;

    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "适用楼层备注")
    private String floorHint;

    @Schema(description = "房间设施")
    private List<Map<String, Object>> facilities = new ArrayList<>();

    @Schema(description = "默认参数")
    private Map<String, Object> defaults = new HashMap<>();

    @Schema(description = "共用相册")
    private List<String> gallery = new ArrayList<>();

    @Schema(description = "共用标签")
    private List<String> tags = new ArrayList<>();

    @Schema(description = "关联房型数")
    private Integer linkedRoomCount;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
}

package com.sqx.app.lvyou.hotel.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Data
@Schema(description = "保存客房标准")
public class HotelRoomStandardSaveBo {

    @Schema(description = "模板ID，为空则新建")
    private Long id;

    @NotBlank(message = "请填写模板名称")
    @Schema(description = "模板名称")
    private String name;

    @Schema(description = "适用楼层备注")
    private String floorHint;

    @Schema(description = "房间设施")
    private List<Map<String, Object>> facilities;

    @Schema(description = "默认参数")
    private Map<String, Object> defaults;

    @Schema(description = "共用相册")
    private List<String> gallery;

    @Schema(description = "共用标签")
    private List<String> tags;

    @Schema(description = "排序")
    private Integer sort;

    @Schema(description = "状态：1启用 0停用")
    private Integer status;
}

package com.sqx.app.lvyou.scenic.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 用户端景区标签
 */
@Data
@Schema(description = "景区标签")
public class ScenicTagVo {

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签类型：green/orange")
    private String type;

    @Schema(description = "标签文案")
    private String label;
}

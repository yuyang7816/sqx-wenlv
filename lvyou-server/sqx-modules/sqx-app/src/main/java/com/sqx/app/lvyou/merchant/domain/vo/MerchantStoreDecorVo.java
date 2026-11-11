package com.sqx.app.lvyou.merchant.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * 门店装修
 */
@Getter
@Setter
@Schema(description = "门店装修")
public class MerchantStoreDecorVo {

    @Schema(description = "门店类型键")
    private String typeKey;

    @Schema(description = "门店类型元信息")
    private StoreTypeMeta typeMeta;

    @Schema(description = "商户资料摘要")
    private MerchantProfileVo profile;

    @Schema(description = "装修内容")
    private Map<String, Object> decor;

    @Schema(description = "用户端预览目标ID")
    private Long userPreviewId;

    @Schema(description = "用户端预览路径（含 id 参数）")
    private String userPreviewPath;

    @Getter
    @Setter
    @Schema(description = "门店类型元信息")
    public static class StoreTypeMeta {
        @Schema(description = "类型键")
        private String key;
        @Schema(description = "类型名称")
        private String label;
        @Schema(description = "图标")
        private String icon;
        @Schema(description = "主题色")
        private String color;
        @Schema(description = "背景色")
        private String bgColor;
    }
}

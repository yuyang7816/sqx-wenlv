package com.sqx.app.lvyou.scenic.domain.bo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 景区分类主键
 */
@Data
@Schema(description = "景区分类主键")
public class ScenicCategoryInfoBo {

    @NotNull(message = "id不能为空")
    private Long id;
}

package com.sqx.app.lvyou.feedback.domain.bo;

import com.sqx.common.mybatis.domain.PageParam;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 管理端意见反馈分页查询
 */
@EqualsAndHashCode(callSuper = true)
@Data
@Schema(description = "意见反馈分页查询")
public class FeedbackPageBo extends PageParam {

    @Schema(description = "反馈类型")
    private String type;

    @Schema(description = "处理状态：pending/processed")
    private String status;

    @Schema(description = "联系电话（模糊）")
    private String contactPhone;

    @Schema(description = "用户手机号（模糊，关联用户表）")
    private String userPhone;
}

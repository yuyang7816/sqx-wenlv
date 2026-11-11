package com.sqx.app.lvyou.feedback.db.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.sqx.app.lvyou.feedback.domain.entity.FeedbackEntity;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户意见反馈 Mapper
 */
@Mapper
public interface FeedbackMapper extends BaseMapper<FeedbackEntity> {
}

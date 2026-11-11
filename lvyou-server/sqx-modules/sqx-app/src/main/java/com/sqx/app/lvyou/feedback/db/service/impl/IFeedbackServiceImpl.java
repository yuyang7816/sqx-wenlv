package com.sqx.app.lvyou.feedback.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.feedback.db.mapper.FeedbackMapper;
import com.sqx.app.lvyou.feedback.db.service.IFeedbackService;
import com.sqx.app.lvyou.feedback.domain.entity.FeedbackEntity;
import org.springframework.stereotype.Service;

/**
 * 用户意见反馈数据服务实现
 */
@Service
public class IFeedbackServiceImpl extends ServiceImpl<FeedbackMapper, FeedbackEntity>
        implements IFeedbackService {
}

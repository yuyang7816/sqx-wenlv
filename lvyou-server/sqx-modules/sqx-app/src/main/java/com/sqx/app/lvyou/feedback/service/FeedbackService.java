package com.sqx.app.lvyou.feedback.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackPageBo;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackProcessBo;
import com.sqx.app.lvyou.feedback.domain.bo.FeedbackSubmitBo;
import com.sqx.app.lvyou.feedback.domain.vo.FeedbackSubmitVo;
import com.sqx.app.lvyou.feedback.domain.vo.FeedbackVo;

/**
 * 意见反馈业务服务
 */
public interface FeedbackService {

    /**
     * 用户提交反馈
     */
    FeedbackSubmitVo submit(Long userId, FeedbackSubmitBo submitBo);

    /**
     * 管理端分页列表
     */
    IPage<FeedbackVo> pageInfo(FeedbackPageBo pageBo);

    /**
     * 管理端标记已处理
     */
    void process(FeedbackProcessBo processBo);
}

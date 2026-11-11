package com.sqx.app.lvyou.invite.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.appConfig.domain.vo.InviteConfigVO;
import com.sqx.app.lvyou.invite.domain.vo.InviteCommissionDetailVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteCommissionSummaryVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteSummaryVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteTeamMemberVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteTeamSummaryVo;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;

/**
 * 邀请分销业务服务
 */
public interface InviteService {

    InviteConfigVO getConfig();

    InviteSummaryVo getSummary(Long userId);

    InviteTeamSummaryVo getTeamSummary(Long userId);

    IPage<InviteTeamMemberVo> getTeamMembers(Long userId, Integer level, Long page, Long limit);

    InviteCommissionSummaryVo getCommissionSummary(Long userId);

    IPage<InviteCommissionDetailVo> getCommissionDetails(Long userId, Integer level, Long page, Long limit);

    void bindRelationOnRegister(Long userId, String inviteCode);

    void settleCommissionOnOrderCompleted(OrderEntity order);
}

package com.sqx.app.lvyou.invite.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.invite.domain.bo.CommissionRecordAdminPageBo;
import com.sqx.app.lvyou.invite.domain.bo.InviteRelationAdminPageBo;
import com.sqx.app.lvyou.invite.domain.vo.CommissionRecordAdminVo;
import com.sqx.app.lvyou.invite.domain.vo.InviteRelationAdminVo;

/**
 * 管理端邀请分销服务
 */
public interface InviteAdminService {

    IPage<InviteRelationAdminVo> relationPage(InviteRelationAdminPageBo pageBo);

    IPage<CommissionRecordAdminVo> commissionPage(CommissionRecordAdminPageBo pageBo);
}

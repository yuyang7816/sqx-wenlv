package com.sqx.app.lvyou.invite.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.invite.db.mapper.InviteRelationMapper;
import com.sqx.app.lvyou.invite.db.service.IInviteRelationService;
import com.sqx.app.lvyou.invite.domain.entity.InviteRelationEntity;
import org.springframework.stereotype.Service;

/**
 * 邀请关系数据服务实现
 */
@Service
public class IInviteRelationServiceImpl extends ServiceImpl<InviteRelationMapper, InviteRelationEntity>
        implements IInviteRelationService {
}

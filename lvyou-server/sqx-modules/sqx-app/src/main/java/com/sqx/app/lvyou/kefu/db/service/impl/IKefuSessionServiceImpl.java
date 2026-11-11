package com.sqx.app.lvyou.kefu.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.kefu.db.mapper.KefuSessionMapper;
import com.sqx.app.lvyou.kefu.db.service.IKefuSessionService;
import com.sqx.app.lvyou.kefu.domain.entity.KefuSessionEntity;
import org.springframework.stereotype.Service;

@Service
public class IKefuSessionServiceImpl extends ServiceImpl<KefuSessionMapper, KefuSessionEntity>
        implements IKefuSessionService {
}

package com.sqx.app.lvyou.kefu.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.kefu.db.mapper.KefuMessageMapper;
import com.sqx.app.lvyou.kefu.db.service.IKefuMessageService;
import com.sqx.app.lvyou.kefu.domain.entity.KefuMessageEntity;
import org.springframework.stereotype.Service;

@Service
public class IKefuMessageServiceImpl extends ServiceImpl<KefuMessageMapper, KefuMessageEntity>
        implements IKefuMessageService {
}

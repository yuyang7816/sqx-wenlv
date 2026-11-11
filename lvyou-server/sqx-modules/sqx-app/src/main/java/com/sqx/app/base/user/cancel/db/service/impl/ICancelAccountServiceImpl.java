package com.sqx.app.base.user.cancel.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.base.user.cancel.db.mapper.CancelAccountMapper;
import com.sqx.app.base.user.cancel.db.service.ICancelAccountService;
import com.sqx.app.base.user.cancel.domain.entity.CancelAccountEntity;
import org.springframework.stereotype.Service;

@Service
public class ICancelAccountServiceImpl extends ServiceImpl<CancelAccountMapper, CancelAccountEntity>
        implements ICancelAccountService {
}

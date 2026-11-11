package com.sqx.app.lvyou.heritage.craft.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.heritage.craft.db.mapper.HeritageCraftMapper;
import com.sqx.app.lvyou.heritage.craft.db.service.IHeritageCraftService;
import com.sqx.app.lvyou.heritage.craft.domain.entity.HeritageCraftEntity;
import org.springframework.stereotype.Service;

/**
 * 非遗工艺 DB 服务实现
 */
@Service
public class IHeritageCraftServiceImpl extends ServiceImpl<HeritageCraftMapper, HeritageCraftEntity>
        implements IHeritageCraftService {
}

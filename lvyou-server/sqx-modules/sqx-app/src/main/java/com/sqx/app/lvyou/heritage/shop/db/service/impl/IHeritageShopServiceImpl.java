package com.sqx.app.lvyou.heritage.shop.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.heritage.shop.db.mapper.HeritageShopMapper;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import org.springframework.stereotype.Service;

/**
 * 非遗店铺 DB 服务实现
 */
@Service
public class IHeritageShopServiceImpl extends ServiceImpl<HeritageShopMapper, HeritageShopEntity>
        implements IHeritageShopService {
}

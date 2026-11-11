package com.sqx.app.lvyou.heritage.goods.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.heritage.goods.db.mapper.GoodsMapper;
import com.sqx.app.lvyou.heritage.goods.db.service.IGoodsService;
import com.sqx.app.lvyou.heritage.goods.domain.entity.GoodsEntity;
import org.springframework.stereotype.Service;

/**
 * 非遗商品 DB 服务实现
 */
@Service
public class IGoodsServiceImpl extends ServiceImpl<GoodsMapper, GoodsEntity> implements IGoodsService {
}

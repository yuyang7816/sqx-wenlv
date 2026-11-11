package com.sqx.app.lvyou.heritage.shop.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.heritage.shop.db.mapper.HeritageShopCategoryMapper;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopCategoryService;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopCategoryEntity;
import org.springframework.stereotype.Service;

/**
 * 非遗好店分类数据服务实现
 */
@Service
public class IHeritageShopCategoryServiceImpl extends ServiceImpl<HeritageShopCategoryMapper, HeritageShopCategoryEntity>
        implements IHeritageShopCategoryService {
}

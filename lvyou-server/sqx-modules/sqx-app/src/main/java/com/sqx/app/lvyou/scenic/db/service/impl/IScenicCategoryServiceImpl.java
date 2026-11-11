package com.sqx.app.lvyou.scenic.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.scenic.db.mapper.ScenicCategoryMapper;
import com.sqx.app.lvyou.scenic.db.service.IScenicCategoryService;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicCategoryEntity;
import org.springframework.stereotype.Service;

/**
 * 景区分类数据服务实现
 */
@Service
public class IScenicCategoryServiceImpl extends ServiceImpl<ScenicCategoryMapper, ScenicCategoryEntity>
        implements IScenicCategoryService {
}

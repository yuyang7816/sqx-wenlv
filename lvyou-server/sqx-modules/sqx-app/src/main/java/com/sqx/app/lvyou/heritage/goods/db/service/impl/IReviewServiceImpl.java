package com.sqx.app.lvyou.heritage.goods.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.heritage.goods.db.mapper.ReviewMapper;
import com.sqx.app.lvyou.heritage.goods.db.service.IReviewService;
import com.sqx.app.lvyou.heritage.goods.domain.entity.ReviewEntity;
import org.springframework.stereotype.Service;

/**
 * 非遗商品评价 DB 服务实现
 */
@Service
public class IReviewServiceImpl extends ServiceImpl<ReviewMapper, ReviewEntity> implements IReviewService {
}

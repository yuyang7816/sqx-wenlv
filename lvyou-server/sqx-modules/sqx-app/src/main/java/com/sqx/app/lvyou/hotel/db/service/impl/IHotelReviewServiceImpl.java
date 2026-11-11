package com.sqx.app.lvyou.hotel.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.hotel.db.mapper.HotelReviewMapper;
import com.sqx.app.lvyou.hotel.db.service.IHotelReviewService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelReviewEntity;
import org.springframework.stereotype.Service;

/**
 * 酒店评价数据服务实现
 */
@Service
public class IHotelReviewServiceImpl extends ServiceImpl<HotelReviewMapper, HotelReviewEntity>
        implements IHotelReviewService {
}

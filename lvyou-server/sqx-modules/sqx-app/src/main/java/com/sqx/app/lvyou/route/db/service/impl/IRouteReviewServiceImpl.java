package com.sqx.app.lvyou.route.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.route.db.mapper.RouteReviewMapper;
import com.sqx.app.lvyou.route.db.service.IRouteReviewService;
import com.sqx.app.lvyou.route.domain.entity.RouteReviewEntity;
import org.springframework.stereotype.Service;

/**
 * 线路评价数据服务实现
 */
@Service
public class IRouteReviewServiceImpl extends ServiceImpl<RouteReviewMapper, RouteReviewEntity>
        implements IRouteReviewService {
}

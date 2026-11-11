package com.sqx.app.lvyou.route.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.route.db.mapper.RouteItineraryMapper;
import com.sqx.app.lvyou.route.db.service.IRouteItineraryService;
import com.sqx.app.lvyou.route.domain.entity.RouteItineraryEntity;
import org.springframework.stereotype.Service;

/**
 * 线路行程节点数据服务实现
 */
@Service
public class IRouteItineraryServiceImpl extends ServiceImpl<RouteItineraryMapper, RouteItineraryEntity>
        implements IRouteItineraryService {
}

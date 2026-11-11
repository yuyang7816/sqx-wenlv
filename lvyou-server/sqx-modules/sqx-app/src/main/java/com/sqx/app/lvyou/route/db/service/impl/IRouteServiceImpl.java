package com.sqx.app.lvyou.route.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.route.db.mapper.RouteMapper;
import com.sqx.app.lvyou.route.db.service.IRouteService;
import com.sqx.app.lvyou.route.domain.entity.RouteEntity;
import org.springframework.stereotype.Service;

/**
 * 旅游线路数据服务实现
 */
@Service
public class IRouteServiceImpl extends ServiceImpl<RouteMapper, RouteEntity> implements IRouteService {
}

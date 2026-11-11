package com.sqx.app.lvyou.activity.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.activity.db.mapper.ActivityMapper;
import com.sqx.app.lvyou.activity.db.service.IActivityService;
import com.sqx.app.lvyou.activity.domain.entity.ActivityEntity;
import org.springframework.stereotype.Service;

/**
 * 精彩活动数据服务实现
 */
@Service
public class IActivityServiceImpl extends ServiceImpl<ActivityMapper, ActivityEntity> implements IActivityService {
}

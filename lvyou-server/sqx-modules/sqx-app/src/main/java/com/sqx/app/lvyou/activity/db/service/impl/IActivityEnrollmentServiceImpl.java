package com.sqx.app.lvyou.activity.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.activity.db.mapper.ActivityEnrollmentMapper;
import com.sqx.app.lvyou.activity.db.service.IActivityEnrollmentService;
import com.sqx.app.lvyou.activity.domain.entity.ActivityEnrollmentEntity;
import org.springframework.stereotype.Service;

/**
 * 活动报名 DB 服务实现
 */
@Service
public class IActivityEnrollmentServiceImpl extends ServiceImpl<ActivityEnrollmentMapper, ActivityEnrollmentEntity>
        implements IActivityEnrollmentService {
}

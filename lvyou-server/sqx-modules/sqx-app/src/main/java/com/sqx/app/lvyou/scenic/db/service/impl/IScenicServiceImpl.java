package com.sqx.app.lvyou.scenic.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.scenic.db.mapper.ScenicMapper;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import org.springframework.stereotype.Service;

/**
 * 旅游景区数据服务实现
 */
@Service
public class IScenicServiceImpl extends ServiceImpl<ScenicMapper, ScenicEntity> implements IScenicService {
}

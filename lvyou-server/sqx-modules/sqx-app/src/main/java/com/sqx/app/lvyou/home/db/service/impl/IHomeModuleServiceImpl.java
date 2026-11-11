package com.sqx.app.lvyou.home.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.home.db.mapper.HomeModuleMapper;
import com.sqx.app.lvyou.home.db.service.IHomeModuleService;
import com.sqx.app.lvyou.home.domain.entity.HomeModuleEntity;
import org.springframework.stereotype.Service;

/**
 * 首页模块 DB 服务实现
 */
@Service
public class IHomeModuleServiceImpl extends ServiceImpl<HomeModuleMapper, HomeModuleEntity> implements IHomeModuleService {
}

package com.sqx.app.lvyou.help.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.help.db.mapper.HelpFaqMapper;
import com.sqx.app.lvyou.help.db.service.IHelpFaqService;
import com.sqx.app.lvyou.help.domain.entity.HelpFaqEntity;
import org.springframework.stereotype.Service;

/**
 * 帮助中心 FAQ 数据服务实现
 */
@Service
public class IHelpFaqServiceImpl extends ServiceImpl<HelpFaqMapper, HelpFaqEntity>
        implements IHelpFaqService {
}

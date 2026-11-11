package com.sqx.app.lvyou.notice.db.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.sqx.app.lvyou.notice.db.mapper.NoticeMapper;
import com.sqx.app.lvyou.notice.db.service.INoticeService;
import com.sqx.app.lvyou.notice.domain.entity.NoticeEntity;
import org.springframework.stereotype.Service;

/**
 * 公告数据服务实现
 */
@Service
public class INoticeServiceImpl extends ServiceImpl<NoticeMapper, NoticeEntity> implements INoticeService {
}

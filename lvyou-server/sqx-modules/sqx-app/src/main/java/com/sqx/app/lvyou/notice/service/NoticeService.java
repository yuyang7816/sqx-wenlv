package com.sqx.app.lvyou.notice.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.notice.domain.bo.*;
import com.sqx.app.lvyou.notice.domain.vo.NoticeAppVo;
import com.sqx.app.lvyou.notice.domain.vo.NoticeMarqueeVo;
import com.sqx.app.lvyou.notice.domain.vo.NoticeVo;

/**
 * 公告业务服务
 */
public interface NoticeService {

    IPage<NoticeAppVo> appPage(NoticeAppListBo listBo);

    NoticeMarqueeVo marquee(Long destinationId);

    NoticeAppVo appDetail(Long id);

    IPage<NoticeVo> pageInfo(NoticePageBo pageBo);

    void saveInfo(NoticeSaveBo saveBo);

    void updateInfo(NoticeUpdateBo updateBo);

    void delete(NoticeInfoBo infoBo);

    void updateState(NoticeUpdateStateBo updateStateBo);

    void updatePinned(NoticeUpdatePinnedBo updatePinnedBo);
}

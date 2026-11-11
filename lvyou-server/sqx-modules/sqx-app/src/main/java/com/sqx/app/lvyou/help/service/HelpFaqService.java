package com.sqx.app.lvyou.help.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqInfoBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqPageBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqSaveBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqUpdateBo;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqListVo;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqVo;

/**
 * 帮助中心 FAQ 业务服务
 */
public interface HelpFaqService {

    HelpFaqListVo appList();

    IPage<HelpFaqVo> pageInfo(HelpFaqPageBo pageBo);

    void saveInfo(HelpFaqSaveBo saveBo);

    void updateInfo(HelpFaqUpdateBo updateBo);

    void delete(HelpFaqInfoBo infoBo);
}

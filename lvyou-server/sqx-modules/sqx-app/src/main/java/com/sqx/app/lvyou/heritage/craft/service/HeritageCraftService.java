package com.sqx.app.lvyou.heritage.craft.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.heritage.craft.domain.bo.*;
import com.sqx.app.lvyou.heritage.craft.domain.vo.HeritageCraftAppVo;
import com.sqx.app.lvyou.heritage.craft.domain.vo.HeritageCraftVo;

/**
 * 非遗工艺业务服务
 */
public interface HeritageCraftService {

    IPage<HeritageCraftAppVo> appPage(HeritageCraftAppListBo listBo);

    HeritageCraftAppVo appDetail(Long id);

    IPage<HeritageCraftVo> pageInfo(HeritageCraftPageBo pageBo);

    HeritageCraftVo info(HeritageCraftInfoBo infoBo);

    void saveInfo(HeritageCraftSaveBo saveBo);

    void updateInfo(HeritageCraftUpdateBo updateBo);

    void delete(HeritageCraftInfoBo infoBo);

    void updateState(HeritageCraftUpdateStateBo updateStateBo);
}

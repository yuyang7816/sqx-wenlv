package com.sqx.app.lvyou.heritage.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.heritage.shop.domain.bo.*;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopAppVo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopVo;

/**
 * 非遗店铺业务服务
 */
public interface HeritageShopService {

    IPage<HeritageShopAppVo> appPage(HeritageShopAppListBo listBo);

    HeritageShopAppVo appDetail(Long id);

    IPage<HeritageShopVo> pageInfo(HeritageShopPageBo pageBo);

    HeritageShopVo info(HeritageShopInfoBo infoBo);

    void saveInfo(HeritageShopSaveBo saveBo);

    void updateInfo(HeritageShopUpdateBo updateBo);

    void delete(HeritageShopInfoBo infoBo);

    void updateState(HeritageShopUpdateStateBo updateStateBo);

    /**
     * 将商户好店分类同步到已关联的非遗店铺（文创商户）
     */
    void syncCategoryFromMerchant(Long merchantId);
}

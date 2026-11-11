package com.sqx.app.lvyou.heritage.goods.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.heritage.goods.domain.bo.*;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsAppVo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsVo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;

/**
 * 非遗商品业务服务
 */
public interface GoodsService {

    IPage<GoodsAppVo> appPage(GoodsAppListBo listBo);

    GoodsAppVo appDetail(Long id, Long userId);

    IPage<ReviewAppVo> appReviews(GoodsReviewAppListBo listBo);

    ReviewAppVo submitReview(Long userId, Long goodsId, GoodsReviewSubmitBo submitBo);

    IPage<GoodsVo> pageInfo(GoodsPageBo pageBo);

    void saveInfo(GoodsSaveBo saveBo);

    void updateInfo(GoodsUpdateBo updateBo);

    void delete(GoodsInfoBo infoBo);

    void updateState(GoodsUpdateStateBo updateStateBo);
}

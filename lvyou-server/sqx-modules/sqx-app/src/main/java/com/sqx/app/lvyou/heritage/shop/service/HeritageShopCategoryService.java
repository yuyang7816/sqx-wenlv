package com.sqx.app.lvyou.heritage.shop.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryInfoBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryPageBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategorySaveBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryUpdateBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryUpdateStateBo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopCategoryOptionVo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopCategoryVo;

import java.util.List;

/**
 * 非遗好店分类业务服务
 */
public interface HeritageShopCategoryService {

    IPage<HeritageShopCategoryVo> pageInfo(HeritageShopCategoryPageBo pageBo);

    void saveInfo(HeritageShopCategorySaveBo saveBo);

    void updateInfo(HeritageShopCategoryUpdateBo updateBo);

    void updateState(HeritageShopCategoryUpdateStateBo updateStateBo);

    void delete(HeritageShopCategoryInfoBo infoBo);

    /** 启用中的分类选项（管理端下拉 / 用户端筛选） */
    List<HeritageShopCategoryOptionVo> listEnabledOptions();

    /** 筛选参数是否合法（空或 all 视为全部） */
    boolean isValidFilter(String code);

    /** 保存时分类是否合法（空允许；非空须已启用） */
    boolean isValidForSave(String code);

    /** 规范化编码 */
    String normalize(String code);

    /** 分类名称；未知编码返回原值 */
    String labelOf(String code);

    /** 默认分类编码：首个启用分类，否则 culture */
    String resolveDefaultCode();
}

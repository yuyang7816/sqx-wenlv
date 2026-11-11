package com.sqx.app.lvyou.scenic.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryInfoBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryPageBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategorySaveBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryUpdateBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryUpdateStateBo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicCategoryOptionVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicCategoryVo;

import java.util.List;

/**
 * 景区分类业务服务
 */
public interface ScenicCategoryService {

    IPage<ScenicCategoryVo> pageInfo(ScenicCategoryPageBo pageBo);

    void saveInfo(ScenicCategorySaveBo saveBo);

    void updateInfo(ScenicCategoryUpdateBo updateBo);

    void updateState(ScenicCategoryUpdateStateBo updateStateBo);

    void delete(ScenicCategoryInfoBo infoBo);

    /** 启用中的分类选项（管理端下拉 / 用户端筛选） */
    List<ScenicCategoryOptionVo> listEnabledOptions();

    /** 筛选参数是否合法（空或 all 视为全部） */
    boolean isValidFilter(String code);

    /** 保存时分类是否合法（空允许；非空须已启用） */
    boolean isValidForSave(String code);

    /** 规范化编码 */
    String normalize(String code);

    /** 分类名称；未知编码返回原值 */
    String labelOf(String code);
}

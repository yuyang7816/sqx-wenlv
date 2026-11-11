package com.sqx.app.lvyou.home.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleInfoBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModulePageBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleSaveBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleUpdateBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleUpdateStateBo;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleAppVo;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleVo;

import java.util.List;

/**
 * 首页模块配置服务
 */
public interface HomeModuleService {

    IPage<HomeModuleVo> pageInfo(HomeModulePageBo pageBo);

    void saveInfo(HomeModuleSaveBo saveBo);

    void updateInfo(HomeModuleUpdateBo updateBo);

    void updateState(HomeModuleUpdateStateBo updateStateBo);

    void delete(HomeModuleInfoBo infoBo);

    /** C 端：按类型拉取启用模块 */
    List<HomeModuleAppVo> listAppModules(String moduleType, Long destinationId);

    /** 商户发布服务时可选的首页 Tab 列表（全平台启用项） */
    List<HomeModuleAppVo> listSelectableTabs();

    /** 校验 Tab 业务键是否有效 */
    boolean isValidTabKey(String tabKey);

    /** 解析 Tab 展示名称 */
    String resolveTabLabel(String tabKey);
}

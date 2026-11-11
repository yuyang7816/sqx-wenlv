package com.sqx.app.lvyou.destination.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.destination.domain.bo.*;
import com.sqx.app.lvyou.destination.domain.vo.DestinationAppVo;
import com.sqx.app.lvyou.destination.domain.vo.DestinationMerchantVo;
import com.sqx.app.lvyou.destination.domain.vo.DestinationVo;

import java.util.List;

/**
 * 目的地业务服务
 */
public interface DestinationService {

    List<DestinationAppVo> listOnline();

    DestinationAppVo getCurrent(String lat, String lng);

    List<DestinationMerchantVo> listCurrentMerchants(String lat, String lng);

    DestinationAppVo switchDestination(Long userId, DestinationSwitchBo switchBo);

    void saveInfo(DestinationSaveBo saveBo);

    IPage<DestinationVo> pageInfo(DestinationPageBo pageBo);

    void updateInfo(DestinationUpdateBo updateBo);

    void delete(DestinationInfoBo infoBo);

    void updateState(DestinationUpdateStateBo updateStateBo);
}

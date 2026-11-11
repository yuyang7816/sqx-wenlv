package com.sqx.app.lvyou.transport.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.transport.domain.bo.*;
import com.sqx.app.lvyou.transport.domain.vo.*;

/**
 * 公共交通业务服务
 */
public interface TransportService {

    IPage<TransportRouteAppVo> appPage(TransportRouteAppListBo listBo);

    TransportScheduleAppVo appSchedule(String routeKey, String lat, String lng, String city, Integer direction);

    TransportStationAppVo appStations(String routeKey, String lat, String lng, String city, String station, Integer direction);

    TransportTrackAppVo appTrack(String routeKey, String lat, String lng, String city, String station, Integer direction);

    IPage<TransportRouteVo> pageInfo(TransportRoutePageBo pageBo);

    void saveInfo(TransportRouteSaveBo saveBo);

    void updateInfo(TransportRouteUpdateBo updateBo);

    void delete(TransportRouteInfoBo infoBo);

    void updateState(TransportRouteUpdateStateBo updateStateBo);
}

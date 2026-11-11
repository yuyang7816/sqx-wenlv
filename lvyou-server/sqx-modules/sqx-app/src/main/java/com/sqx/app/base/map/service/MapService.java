package com.sqx.app.base.map.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.map.domain.bo.MapDirectionBo;
import com.sqx.app.base.map.domain.bo.MapPlaceSearchBo;
import com.sqx.app.base.map.domain.bo.MapPoiListBo;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.domain.vo.GeocodeVo;
import com.sqx.app.base.map.domain.vo.MapPoiVo;
import com.sqx.app.base.map.domain.vo.MapPickerConfigVo;
import com.sqx.app.base.map.domain.vo.MapRouteVo;
import com.sqx.app.base.map.domain.vo.MapSuggestVo;

import java.util.List;

public interface MapService {

    AddressVo reverseGeocode(String lat, String lng);

    GeocodeVo geocode(String address);

    MapPickerConfigVo pickerConfig();

    IPage<MapPoiVo> pagePois(MapPoiListBo listBo);

    List<MapPoiVo> searchPlaces(MapPlaceSearchBo searchBo);

    List<MapSuggestVo> suggestPlaces(String keyword, String lat, String lng);

    List<MapRouteVo> planRoutes(MapDirectionBo directionBo);
}

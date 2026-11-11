package com.sqx.app.base.map.strategy;

import com.sqx.app.base.map.domain.bo.MapDirectionBo;
import com.sqx.app.base.map.domain.bo.MapPlaceSearchBo;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.domain.vo.GeocodeVo;
import com.sqx.app.base.map.domain.vo.MapPoiVo;
import com.sqx.app.base.map.domain.vo.MapRouteVo;
import com.sqx.app.base.map.domain.vo.MapSuggestVo;

import java.util.List;

/**
 * @author chengming
 * @since 2025/8/11
 */
public interface IMapPlatformStrategy {

    AddressVo reverseGeocode(String lat, String lng);

    default GeocodeVo geocode(String address) {
        throw new UnsupportedOperationException("当前地图平台不支持正向地理编码");
    }

    default List<MapPoiVo> searchPlaces(MapPlaceSearchBo searchBo) {
        throw new UnsupportedOperationException("当前地图平台不支持地点搜索");
    }

    default List<MapSuggestVo> suggestPlaces(String keyword, String lat, String lng) {
        throw new UnsupportedOperationException("当前地图平台不支持地址联想");
    }

    default List<MapRouteVo> planRoutes(MapDirectionBo directionBo) {
        throw new UnsupportedOperationException("当前地图平台不支持路线规划");
    }
}

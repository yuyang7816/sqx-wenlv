package com.sqx.app.base.map.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.map.domain.bo.MapDirectionBo;
import com.sqx.app.base.map.domain.bo.MapPlaceSearchBo;
import com.sqx.app.base.map.domain.bo.MapPoiListBo;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.domain.vo.GeocodeVo;
import com.sqx.app.base.map.domain.vo.MapPoiVo;
import com.sqx.app.base.map.domain.vo.MapRouteVo;
import com.sqx.app.base.map.domain.vo.MapSuggestVo;
import com.sqx.app.base.map.service.MapService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author chengming
 * @since 2025/7/22
 */
@Tag(name = "地图")
@RestController
@PublicAccess
@RequestMapping("app/map")
public class MapController {

    @Resource
    private MapService mapService;

    @Resource
    private DestinationSupport destinationSupport;

    @Operation(summary = "反向地理编码（根据经纬度获取地址信息）")
    @GetMapping("reverseGeocode")
    public Result<AddressVo> reverseGeocode(String lat, String lng) {
        AddressVo vo = mapService.reverseGeocode(lat, lng);
        return Result.success(vo);
    }

    @Operation(summary = "正向地理编码（根据地址获取经纬度）")
    @GetMapping("geocode")
    public Result<GeocodeVo> geocode(@RequestParam("address") String address) {
        return Result.success(mapService.geocode(address));
    }

    @Operation(summary = "地图POI分页列表（聚合景区/美食/停车/交通）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MapPoiVo.class)))
    @GetMapping("pois")
    public Result<IPage<MapPoiVo>> pois(@RequestParam(value = "type", required = false) String type,
                                        @RequestParam(value = "lat", required = false) String lat,
                                        @RequestParam(value = "lng", required = false) String lng,
                                        @RequestParam(value = "keyword", required = false) String keyword,
                                        @RequestParam(value = "page", required = false) Long page,
                                        @RequestParam(value = "limit", required = false) Long limit,
                                        @RequestParam(value = "size", required = false) Long size) {
        MapPoiListBo listBo = new MapPoiListBo();
        listBo.setDestinationId(destinationSupport.resolveAppDestinationId(lat, lng));
        listBo.setType(type);
        listBo.setLat(lat);
        listBo.setLng(lng);
        listBo.setKeyword(keyword);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        return Result.success(mapService.pagePois(listBo));
    }

    @Operation(summary = "地图地点搜索（腾讯地图 POI，非业务库）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MapPoiVo.class)))
    @GetMapping("placeSearch")
    public Result<List<MapPoiVo>> placeSearch(@RequestParam(value = "keyword", required = false) String keyword,
                                              @RequestParam("lat") String lat,
                                              @RequestParam("lng") String lng,
                                              @RequestParam(value = "radius", required = false) Integer radius,
                                              @RequestParam(value = "filter", required = false) String filter,
                                              @RequestParam(value = "autoExtend", required = false) Integer autoExtend,
                                              @RequestParam(value = "mode", required = false) String mode,
                                              @RequestParam(value = "page", required = false) Integer page,
                                              @RequestParam(value = "limit", required = false) Integer limit) {
        MapPlaceSearchBo searchBo = new MapPlaceSearchBo();
        searchBo.setKeyword(keyword);
        searchBo.setLat(lat);
        searchBo.setLng(lng);
        searchBo.setRadius(radius);
        searchBo.setFilter(filter);
        searchBo.setAutoExtend(autoExtend);
        searchBo.setMode(mode);
        searchBo.setPage(page);
        searchBo.setLimit(limit);
        return Result.success(mapService.searchPlaces(searchBo));
    }

    @Operation(summary = "地址联想（腾讯地图 suggestion）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MapSuggestVo.class)))
    @GetMapping("suggest")
    public Result<List<MapSuggestVo>> suggest(@RequestParam("keyword") String keyword,
                                              @RequestParam(value = "lat", required = false) String lat,
                                              @RequestParam(value = "lng", required = false) String lng) {
        return Result.success(mapService.suggestPlaces(keyword, lat, lng));
    }

    @Operation(summary = "路线规划（腾讯地图 direction，支持驾车/步行/骑行/公交地铁多方案）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = MapRouteVo.class)))
    @GetMapping("direction")
    public Result<List<MapRouteVo>> direction(@RequestParam("fromLat") String fromLat,
                                             @RequestParam("fromLng") String fromLng,
                                             @RequestParam("toLat") String toLat,
                                             @RequestParam("toLng") String toLng,
                                             @RequestParam(value = "mode", required = false) String mode,
                                             @RequestParam(value = "multiPlan", required = false) Boolean multiPlan) {
        MapDirectionBo directionBo = new MapDirectionBo();
        directionBo.setFromLat(fromLat);
        directionBo.setFromLng(fromLng);
        directionBo.setToLat(toLat);
        directionBo.setToLng(toLng);
        directionBo.setMode(mode);
        directionBo.setMultiPlan(multiPlan);
        return Result.success(mapService.planRoutes(directionBo));
    }
}

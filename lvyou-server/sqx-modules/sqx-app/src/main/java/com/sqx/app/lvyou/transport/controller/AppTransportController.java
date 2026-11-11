package com.sqx.app.lvyou.transport.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.lvyou.transport.domain.bo.TransportRouteAppListBo;
import com.sqx.app.lvyou.transport.domain.vo.TransportRouteAppVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportRoutePageVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportScheduleAppVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportStationAppVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportTrackAppVo;
import com.sqx.app.lvyou.transport.service.TransportService;
import com.sqx.common.core.domain.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 用户端公共交通
 */
@Tag(name = "用户端-公共交通", description = "用户端-公共交通")
@RestController
@RequestMapping("/app/transport")
@PublicAccess
public class AppTransportController {

    @Resource
    private TransportService transportService;

    @Operation(summary = "公交线路列表（分页，实时公交）")
    @ApiResponse(content = @Content(schema = @Schema(implementation = TransportRoutePageVo.class)))
    @GetMapping("routes")
    public Result<TransportRoutePageVo> routes(@RequestParam(value = "lat", required = false) String lat,
                                                     @RequestParam(value = "lng", required = false) String lng,
                                                     @RequestParam(value = "city", required = false) String city,
                                                     @RequestParam(value = "district", required = false) String district,
                                                     @RequestParam(value = "address", required = false) String address,
                                                     @RequestParam(value = "locationName", required = false) String locationName,
                                                     @RequestParam(value = "keyword", required = false) String keyword,
                                                     @RequestParam(value = "page", required = false) Long page,
                                                     @RequestParam(value = "limit", required = false) Long limit,
                                                     @RequestParam(value = "size", required = false) Long size) {
        TransportRouteAppListBo listBo = new TransportRouteAppListBo();
        listBo.setLat(lat);
        listBo.setLng(lng);
        listBo.setCity(city);
        listBo.setDistrict(district);
        listBo.setAddress(address);
        listBo.setLocationName(locationName);
        listBo.setKeyword(keyword);
        listBo.setPage(page);
        listBo.setLimit(limit != null ? limit : size);
        IPage<TransportRouteAppVo> routePage = transportService.appPage(listBo);
        if (routePage instanceof TransportRoutePageVo) {
            return Result.success((TransportRoutePageVo) routePage);
        }
        TransportRoutePageVo pageVo = new TransportRoutePageVo();
        pageVo.setCurrent(routePage.getCurrent());
        pageVo.setSize(routePage.getSize());
        pageVo.setTotal(routePage.getTotal());
        pageVo.setRecords(routePage.getRecords());
        return Result.success(pageVo);
    }

    @Operation(summary = "公交线路时刻表")
    @ApiResponse(content = @Content(schema = @Schema(implementation = TransportScheduleAppVo.class)))
    @GetMapping("routes/{id}/schedule")
    public Result<TransportScheduleAppVo> schedule(@PathVariable("id") String id,
                                                   @RequestParam(value = "lat", required = false) String lat,
                                                   @RequestParam(value = "lng", required = false) String lng,
                                                   @RequestParam(value = "city", required = false) String city,
                                                   @RequestParam(value = "direction", required = false) Integer direction) {
        return Result.success(transportService.appSchedule(id, lat, lng, city, direction));
    }

    @Operation(summary = "公交线路站点")
    @ApiResponse(content = @Content(schema = @Schema(implementation = TransportStationAppVo.class)))
    @GetMapping("routes/{id}/stations")
    public Result<TransportStationAppVo> stations(@PathVariable("id") String id,
                                                    @RequestParam(value = "lat", required = false) String lat,
                                                    @RequestParam(value = "lng", required = false) String lng,
                                                    @RequestParam(value = "city", required = false) String city,
                                                    @RequestParam(value = "station", required = false) String station,
                                                    @RequestParam(value = "direction", required = false) Integer direction) {
        return Result.success(transportService.appStations(id, lat, lng, city, station, direction));
    }

    @Operation(summary = "公交线路实时轨迹")
    @ApiResponse(content = @Content(schema = @Schema(implementation = TransportTrackAppVo.class)))
    @GetMapping("routes/{id}/track")
    public Result<TransportTrackAppVo> track(@PathVariable("id") String id,
                                             @RequestParam(value = "lat", required = false) String lat,
                                             @RequestParam(value = "lng", required = false) String lng,
                                             @RequestParam(value = "city", required = false) String city,
                                             @RequestParam(value = "station", required = false) String station,
                                             @RequestParam(value = "direction", required = false) Integer direction) {
        return Result.success(transportService.appTrack(id, lat, lng, city, station, direction));
    }
}

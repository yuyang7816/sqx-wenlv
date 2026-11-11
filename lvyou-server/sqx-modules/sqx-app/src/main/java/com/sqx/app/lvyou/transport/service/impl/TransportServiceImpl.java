package com.sqx.app.lvyou.transport.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.transport.db.service.ITransportRouteService;
import com.sqx.app.lvyou.transport.db.service.ITransportScheduleService;
import com.sqx.app.lvyou.transport.db.service.ITransportStationService;
import com.sqx.app.lvyou.transport.domain.bo.*;
import com.sqx.app.lvyou.transport.domain.entity.TransportRouteEntity;
import com.sqx.app.lvyou.transport.domain.entity.TransportScheduleEntity;
import com.sqx.app.lvyou.transport.domain.entity.TransportStationEntity;
import com.sqx.app.lvyou.transport.domain.vo.*;
import com.sqx.app.lvyou.transport.enums.TransportRouteStatus;
import com.sqx.app.lvyou.transport.service.BusRealtimeTransportService;
import com.sqx.app.lvyou.transport.support.BusRealtimeConfigSupport;
import com.sqx.app.lvyou.transport.service.TransportService;
import com.sqx.app.lvyou.transport.util.TransportJsonUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 公共交通业务服务实现
 */
@Service
public class TransportServiceImpl implements TransportService {

    private static final int STATUS_ONLINE = TransportRouteStatus.ONLINE.getCode();

    @Resource
    private ITransportRouteService iTransportRouteService;

    @Resource
    private ITransportStationService iTransportStationService;

    @Resource
    private ITransportScheduleService iTransportScheduleService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Resource
    private BusRealtimeConfigSupport busRealtimeConfigSupport;

    @Resource
    private BusRealtimeTransportService busRealtimeTransportService;

    @Override
    public IPage<TransportRouteAppVo> appPage(TransportRouteAppListBo listBo) {
        if (busRealtimeConfigSupport.isAvailable()) {
            return busRealtimeTransportService.appPage(listBo);
        }
        Page<TransportRouteEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<TransportRouteEntity> queryWrapper = buildOnlineRouteQuery(listBo.getDestinationId(), listBo.getKeyword());
        IPage<TransportRouteEntity> entityPage = iTransportRouteService.page(page, queryWrapper);

        Map<Long, List<String>> stationMap = loadStationNamesByRouteIds(entityPage.getRecords());
        List<TransportRouteAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (TransportRouteEntity record : entityPage.getRecords()) {
                voList.add(toAppVo(record, stationMap.get(record.getId())));
            }
        }
        IPage<TransportRouteAppVo> copied = PageUtil.copyPage(entityPage, voList);
        TransportRoutePageVo pageVo = new TransportRoutePageVo();
        pageVo.setCurrent(copied.getCurrent());
        pageVo.setSize(copied.getSize());
        pageVo.setTotal(copied.getTotal());
        pageVo.setRecords(copied.getRecords());
        pageVo.setNearbyStations(Collections.emptyList());
        return pageVo;
    }

    @Override
    public TransportScheduleAppVo appSchedule(String routeKey, String lat, String lng, String city, Integer direction) {
        if (busRealtimeConfigSupport.isAvailable()) {
            return busRealtimeTransportService.appSchedule(routeKey, lat, lng, city, direction);
        }
        TransportRouteEntity route = resolveOnlineRoute(routeKey);
        TransportScheduleAppVo vo = new TransportScheduleAppVo();
        vo.setId(route.getRouteCode());
        vo.setName(route.getName());
        vo.setRoutes(listOnlineRouteBriefs(route.getDestinationId()));
        vo.setDirections(TransportJsonUtil.parseDirections(route.getDirectionsJson()));
        vo.setScheduleRows(loadScheduleRows(route.getId()));
        return vo;
    }

    @Override
    public TransportStationAppVo appStations(String routeKey, String lat, String lng, String city,
                                               String station, Integer direction) {
        if (busRealtimeConfigSupport.isAvailable()) {
            return busRealtimeTransportService.appStations(routeKey, lat, lng, city, station, direction);
        }
        TransportRouteEntity route = resolveOnlineRoute(routeKey);
        List<String> stations = loadStationNames(route.getId());
        TransportStationAppVo vo = new TransportStationAppVo();
        vo.setId(route.getRouteCode());
        vo.setName(route.getName());
        vo.setStartStation(route.getStartStation());
        vo.setEndStation(route.getEndStation());
        vo.setStationCount(stations.size());
        vo.setCurrentStopIndex(route.getCurrentStopIndex());
        vo.setStations(stations);
        return vo;
    }

    @Override
    public TransportTrackAppVo appTrack(String routeKey, String lat, String lng, String city,
                                          String station, Integer direction) {
        if (busRealtimeConfigSupport.isAvailable()) {
            return busRealtimeTransportService.appTrack(routeKey, lat, lng, city, station, direction);
        }
        TransportRouteEntity route = resolveOnlineRoute(routeKey);
        TransportTrackAppVo vo = new TransportTrackAppVo();
        vo.setRouteName(route.getName());
        vo.setTrackInfo(TransportJsonUtil.parseTrackInfo(route.getTrackInfoJson()));
        vo.setRealtimeUpdates(TransportJsonUtil.parseRealtimeUpdates(route.getRealtimeUpdatesJson()));
        vo.setPolyline(loadPolyline(route.getId()));
        return vo;
    }

    @Override
    public IPage<TransportRouteVo> pageInfo(TransportRoutePageBo pageBo) {
        Page<TransportRouteEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<TransportRouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(pageBo.getId()), TransportRouteEntity::getId, pageBo.getId())
                .like(StringUtils.isNotBlank(pageBo.getRouteCode()), TransportRouteEntity::getRouteCode, pageBo.getRouteCode())
                .like(StringUtils.isNotBlank(pageBo.getName()), TransportRouteEntity::getName, pageBo.getName())
                .like(StringUtils.isNotBlank(pageBo.getStartStation()), TransportRouteEntity::getStartStation, pageBo.getStartStation())
                .like(StringUtils.isNotBlank(pageBo.getEndStation()), TransportRouteEntity::getEndStation, pageBo.getEndStation())
                .eq(Objects.nonNull(pageBo.getDestinationId()), TransportRouteEntity::getDestinationId, pageBo.getDestinationId())
                .eq(Objects.nonNull(pageBo.getStatus()), TransportRouteEntity::getStatus, pageBo.getStatus())
                .orderByAsc(TransportRouteEntity::getSort)
                .orderByDesc(TransportRouteEntity::getId);

        IPage<TransportRouteEntity> entityPage = iTransportRouteService.page(page, queryWrapper);
        Map<Long, String> destinationNameMap = loadDestinationNames(entityPage.getRecords());
        Map<Long, Integer> stationCountMap = loadStationCountByRouteIds(entityPage.getRecords());

        List<TransportRouteVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (TransportRouteEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record,
                        DestinationDisplaySupport.resolveLabel(
                                destinationNameMap, record.getDestinationId(),
                                record.getStartStation(), record.getEndStation()),
                        stationCountMap.getOrDefault(record.getId(), 0)));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(TransportRouteSaveBo saveBo) {
        TransportRouteEntity entity = new TransportRouteEntity();
        fillRouteEntity(entity, saveBo);
        try {
            iTransportRouteService.save(entity);
        } catch (DuplicateKeyException ex) {
            throw new BizException(AppError.TRANSPORT_ROUTE_CODE_DUPLICATE);
        }
        saveStationsAndSchedules(entity.getId(), saveBo.getStations(), saveBo.getScheduleRows());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(TransportRouteUpdateBo updateBo) {
        TransportRouteEntity existing = iTransportRouteService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.TRANSPORT_ROUTE_NOT_FOUND);
        }
        TransportRouteEntity entity = new TransportRouteEntity();
        entity.setId(updateBo.getId());
        fillRouteEntity(entity, updateBo);
        try {
            iTransportRouteService.updateById(entity);
        } catch (DuplicateKeyException ex) {
            throw new BizException(AppError.TRANSPORT_ROUTE_CODE_DUPLICATE);
        }
        deleteStationsAndSchedules(entity.getId());
        saveStationsAndSchedules(entity.getId(), updateBo.getStations(), updateBo.getScheduleRows());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(TransportRouteInfoBo infoBo) {
        TransportRouteEntity existing = iTransportRouteService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.TRANSPORT_ROUTE_NOT_FOUND);
        }
        deleteStationsAndSchedules(infoBo.getId());
        iTransportRouteService.removeById(infoBo.getId());
    }

    @Override
    public void updateState(TransportRouteUpdateStateBo updateStateBo) {
        TransportRouteEntity existing = iTransportRouteService.getById(updateStateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.TRANSPORT_ROUTE_NOT_FOUND);
        }
        TransportRouteEntity entity = new TransportRouteEntity();
        entity.setId(updateStateBo.getId());
        entity.setStatus(updateStateBo.getStatus());
        iTransportRouteService.updateById(entity);
    }

    private LambdaQueryWrapper<TransportRouteEntity> buildOnlineRouteQuery(Long destinationId, String keyword) {
        LambdaQueryWrapper<TransportRouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransportRouteEntity::getStatus, STATUS_ONLINE)
                .eq(Objects.nonNull(destinationId), TransportRouteEntity::getDestinationId, destinationId);
        if (StringUtils.isNotBlank(keyword)) {
            queryWrapper.and(wrapper -> wrapper.like(TransportRouteEntity::getName, keyword)
                    .or().like(TransportRouteEntity::getRouteCode, keyword)
                    .or().like(TransportRouteEntity::getStartStation, keyword)
                    .or().like(TransportRouteEntity::getEndStation, keyword));
        }
        queryWrapper.orderByAsc(TransportRouteEntity::getSort)
                .orderByDesc(TransportRouteEntity::getId);
        return queryWrapper;
    }

    private TransportRouteEntity resolveOnlineRoute(String routeKey) {
        TransportRouteEntity route = findRouteByKey(routeKey);
        if (route == null || !Objects.equals(route.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.TRANSPORT_ROUTE_NOT_FOUND);
        }
        return route;
    }

    private TransportRouteEntity findRouteByKey(String routeKey) {
        if (StringUtils.isBlank(routeKey)) {
            return null;
        }
        LambdaQueryWrapper<TransportRouteEntity> byCode = new LambdaQueryWrapper<>();
        byCode.eq(TransportRouteEntity::getRouteCode, routeKey.trim());
        TransportRouteEntity route = iTransportRouteService.getOne(byCode, false);
        if (route != null) {
            return route;
        }
        if (StringUtils.isNumeric(routeKey)) {
            return iTransportRouteService.getById(Long.valueOf(routeKey));
        }
        return null;
    }

    private List<TransportRouteBriefVo> listOnlineRouteBriefs(Long destinationId) {
        LambdaQueryWrapper<TransportRouteEntity> queryWrapper = buildOnlineRouteQuery(destinationId, null);
        List<TransportRouteEntity> routes = iTransportRouteService.list(queryWrapper);
        List<TransportRouteBriefVo> briefs = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(routes)) {
            for (TransportRouteEntity route : routes) {
                TransportRouteBriefVo brief = new TransportRouteBriefVo();
                brief.setId(route.getRouteCode());
                brief.setName(route.getName());
                briefs.add(brief);
            }
        }
        return briefs;
    }

    private List<String> loadStationNames(Long routeId) {
        LambdaQueryWrapper<TransportStationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransportStationEntity::getRouteId, routeId)
                .orderByAsc(TransportStationEntity::getSortOrder)
                .orderByAsc(TransportStationEntity::getId);
        List<TransportStationEntity> stations = iTransportStationService.list(queryWrapper);
        if (CollectionUtils.isEmpty(stations)) {
            return new ArrayList<>();
        }
        return stations.stream().map(TransportStationEntity::getStationName).collect(Collectors.toList());
    }

    private Map<Long, List<String>> loadStationNamesByRouteIds(List<TransportRouteEntity> routes) {
        Map<Long, List<String>> result = new HashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return result;
        }
        List<Long> routeIds = routes.stream().map(TransportRouteEntity::getId).collect(Collectors.toList());
        LambdaQueryWrapper<TransportStationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TransportStationEntity::getRouteId, routeIds)
                .orderByAsc(TransportStationEntity::getSortOrder)
                .orderByAsc(TransportStationEntity::getId);
        List<TransportStationEntity> stations = iTransportStationService.list(queryWrapper);
        if (CollectionUtils.isEmpty(stations)) {
            return result;
        }
        for (TransportStationEntity station : stations) {
            result.computeIfAbsent(station.getRouteId(), key -> new ArrayList<>()).add(station.getStationName());
        }
        return result;
    }

    private Map<Long, Integer> loadStationCountByRouteIds(List<TransportRouteEntity> routes) {
        Map<Long, Integer> result = new HashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return result;
        }
        List<Long> routeIds = routes.stream().map(TransportRouteEntity::getId).collect(Collectors.toList());
        LambdaQueryWrapper<TransportStationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(TransportStationEntity::getRouteId, routeIds);
        List<TransportStationEntity> stations = iTransportStationService.list(queryWrapper);
        if (CollectionUtils.isEmpty(stations)) {
            return result;
        }
        for (TransportStationEntity station : stations) {
            result.merge(station.getRouteId(), 1, Integer::sum);
        }
        return result;
    }

    private List<TransportScheduleRowVo> loadScheduleRows(Long routeId) {
        LambdaQueryWrapper<TransportScheduleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransportScheduleEntity::getRouteId, routeId)
                .orderByAsc(TransportScheduleEntity::getSortOrder)
                .orderByAsc(TransportScheduleEntity::getId);
        List<TransportScheduleEntity> schedules = iTransportScheduleService.list(queryWrapper);
        List<TransportScheduleRowVo> rows = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(schedules)) {
            for (TransportScheduleEntity schedule : schedules) {
                TransportScheduleRowVo row = new TransportScheduleRowVo();
                row.setStation(schedule.getStationName());
                row.setTimes(TransportJsonUtil.parseTimes(schedule.getTimesJson()));
                rows.add(row);
            }
        }
        return rows;
    }

    private List<TransportPolylinePointVo> loadPolyline(Long routeId) {
        LambdaQueryWrapper<TransportStationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TransportStationEntity::getRouteId, routeId)
                .orderByAsc(TransportStationEntity::getSortOrder)
                .orderByAsc(TransportStationEntity::getId);
        List<TransportStationEntity> stations = iTransportStationService.list(queryWrapper);
        List<TransportPolylinePointVo> points = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(stations)) {
            for (TransportStationEntity station : stations) {
                TransportPolylinePointVo point = new TransportPolylinePointVo();
                point.setLatitude(station.getLatitude());
                point.setLongitude(station.getLongitude());
                point.setStationName(station.getStationName());
                points.add(point);
            }
        }
        return points;
    }

    private Map<Long, String> loadDestinationNames(List<TransportRouteEntity> routes) {
        Map<Long, String> result = new HashMap<>();
        if (CollectionUtils.isEmpty(routes)) {
            return result;
        }
        List<Long> destinationIds = routes.stream()
                .map(TransportRouteEntity::getDestinationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(destinationIds)) {
            return result;
        }
        List<DestinationEntity> destinations = iDestinationService.listByIds(destinationIds);
        if (CollectionUtils.isNotEmpty(destinations)) {
            for (DestinationEntity destination : destinations) {
                result.put(destination.getId(), destination.getName());
            }
        }
        return result;
    }

    private TransportRouteAppVo toAppVo(TransportRouteEntity entity, List<String> stations) {
        TransportRouteAppVo vo = new TransportRouteAppVo();
        vo.setId(entity.getRouteCode());
        vo.setName(entity.getName());
        vo.setFirstBus(entity.getFirstBus());
        vo.setLastBus(entity.getLastBus());
        vo.setStartStation(entity.getStartStation());
        vo.setEndStation(entity.getEndStation());
        vo.setStatusKey(entity.getStatusKey());
        vo.setStatusLabel(entity.getStatusLabel());
        vo.setAccentColor(entity.getAccentColor());
        vo.setAccentLight(entity.getAccentLight());
        vo.setShowRealtimeStops(Objects.equals(entity.getShowRealtimeStops(), 1));
        vo.setCurrentStop(entity.getCurrentStop());
        vo.setNextStop(entity.getNextStop());
        vo.setNextEta(entity.getNextEta());
        vo.setExpandable(Objects.equals(entity.getExpandable(), 1));
        vo.setFooterType(entity.getFooterType());
        vo.setCurrentStopIndex(entity.getCurrentStopIndex());
        vo.setStations(stations == null ? new ArrayList<>() : stations);
        vo.setStationCount(vo.getStations().size());
        vo.setTrackInfo(TransportJsonUtil.parseTrackInfo(entity.getTrackInfoJson()));
        return vo;
    }

    private TransportRouteVo toAdminVo(TransportRouteEntity entity, String destinationName, int stationCount) {
        TransportRouteVo vo = new TransportRouteVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setDestinationName(destinationName);
        vo.setDirections(TransportJsonUtil.parseDirections(entity.getDirectionsJson()));
        vo.setTrackInfo(TransportJsonUtil.parseTrackInfo(entity.getTrackInfoJson()));
        vo.setRealtimeUpdates(TransportJsonUtil.parseRealtimeUpdates(entity.getRealtimeUpdatesJson()));
        vo.setStationCount(stationCount);
        return vo;
    }

    private void fillRouteEntity(TransportRouteEntity entity, TransportRouteSaveBo saveBo) {
        entity.setRouteCode(StringUtils.trim(saveBo.getRouteCode()));
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(saveBo.getLatitude(), saveBo.getLongitude()));
        entity.setLatitude(saveBo.getLatitude());
        entity.setLongitude(saveBo.getLongitude());
        entity.setName(StringUtils.trim(saveBo.getName()));
        entity.setFirstBus(saveBo.getFirstBus());
        entity.setLastBus(saveBo.getLastBus());
        entity.setStartStation(saveBo.getStartStation());
        entity.setEndStation(saveBo.getEndStation());
        entity.setStatus(saveBo.getStatus());
        entity.setStatusKey(StringUtils.defaultIfBlank(saveBo.getStatusKey(), "running"));
        entity.setStatusLabel(StringUtils.defaultIfBlank(saveBo.getStatusLabel(), "运行中"));
        entity.setAccentColor(saveBo.getAccentColor());
        entity.setAccentLight(saveBo.getAccentLight());
        entity.setShowRealtimeStops(saveBo.getShowRealtimeStops() == null ? 0 : saveBo.getShowRealtimeStops());
        entity.setExpandable(saveBo.getExpandable() == null ? 0 : saveBo.getExpandable());
        entity.setFooterType(saveBo.getFooterType());
        entity.setCurrentStopIndex(saveBo.getCurrentStopIndex() == null ? 0 : saveBo.getCurrentStopIndex());
        entity.setCurrentStop(saveBo.getCurrentStop());
        entity.setNextStop(saveBo.getNextStop());
        entity.setNextEta(saveBo.getNextEta());
        entity.setDirectionsJson(TransportJsonUtil.toJson(saveBo.getDirections()));
        entity.setTrackInfoJson(TransportJsonUtil.toJson(saveBo.getTrackInfo()));
        entity.setRealtimeUpdatesJson(TransportJsonUtil.toJson(saveBo.getRealtimeUpdates()));
        entity.setSort(saveBo.getSort() == null ? 0 : saveBo.getSort());
    }

    private void saveStationsAndSchedules(Long routeId, List<String> stations, List<TransportScheduleRowVo> scheduleRows) {
        if (CollectionUtils.isNotEmpty(stations)) {
            int order = 1;
            for (String stationName : stations) {
                if (StringUtils.isBlank(stationName)) {
                    continue;
                }
                TransportStationEntity station = new TransportStationEntity();
                station.setRouteId(routeId);
                station.setStationName(stationName.trim());
                station.setSortOrder(order++);
                iTransportStationService.save(station);
            }
        }
        if (CollectionUtils.isNotEmpty(scheduleRows)) {
            int order = 1;
            for (TransportScheduleRowVo row : scheduleRows) {
                if (row == null || StringUtils.isBlank(row.getStation())) {
                    continue;
                }
                TransportScheduleEntity schedule = new TransportScheduleEntity();
                schedule.setRouteId(routeId);
                schedule.setStationName(row.getStation().trim());
                schedule.setTimesJson(TransportJsonUtil.toJson(row.getTimes()));
                schedule.setSortOrder(order++);
                iTransportScheduleService.save(schedule);
            }
        }
    }

    private void deleteStationsAndSchedules(Long routeId) {
        LambdaQueryWrapper<TransportStationEntity> stationWrapper = new LambdaQueryWrapper<>();
        stationWrapper.eq(TransportStationEntity::getRouteId, routeId);
        iTransportStationService.remove(stationWrapper);

        LambdaQueryWrapper<TransportScheduleEntity> scheduleWrapper = new LambdaQueryWrapper<>();
        scheduleWrapper.eq(TransportScheduleEntity::getRouteId, routeId);
        iTransportScheduleService.remove(scheduleWrapper);
    }
}

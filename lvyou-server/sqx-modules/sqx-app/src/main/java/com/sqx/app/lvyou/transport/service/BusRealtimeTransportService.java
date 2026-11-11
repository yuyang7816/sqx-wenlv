package com.sqx.app.lvyou.transport.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.service.MapService;
import com.sqx.app.base.map.support.MapDistanceUtil;
import com.sqx.app.lvyou.transport.client.BusRealtimeClient;
import com.sqx.app.lvyou.transport.domain.bo.TransportRouteAppListBo;
import com.sqx.app.lvyou.transport.domain.vo.*;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 基于 apizero 的实时公交 C 端服务
 */
@Slf4j
@Service
public class BusRealtimeTransportService {

    private static final int DEFAULT_DIRECTION = 1;
    private static final long CITY_CACHE_MS = 6 * 60 * 60 * 1000L;
    private static final BigDecimal NEARBY_STATION_RADIUS_KM = new BigDecimal("2.0");
    private static final BigDecimal NEARBY_STATION_FALLBACK_RADIUS_KM = new BigDecimal("5.0");
    private static final int MAX_NEARBY_STATIONS = 10;
    private static final int MAX_AGGREGATE_ROUTES = 60;
    private static final int MAX_CITY_GEO_PROBE = 20;
    private static final String[] ACCENT_COLORS = {"#FF6600", "#2196F3", "#4CAF50", "#9C27B0", "#E91E63", "#009688"};
    private static final String ACCENT_LIGHT = "#FFF2E8";

    @Resource
    private BusRealtimeClient busRealtimeClient;

    @Resource
    private MapService mapService;

    private volatile List<String> cachedCityNames = Collections.emptyList();
    private volatile long cityCacheAt = 0L;

    public TransportRoutePageVo appPage(TransportRouteAppListBo listBo) {
        LocationContext ctx = resolveLocationContext(listBo);
        List<TransportRouteAppVo> routes = new ArrayList<>();
        List<StationCandidate> nearbyStations = ctx.nearbyStations;

        TransportRoutePageVo pageVo = new TransportRoutePageVo();
        pageVo.setNearbyStations(toNearbyStationVos(nearbyStations));

        if (StringUtils.isNotBlank(listBo.getKeyword())) {
            routes.addAll(searchByKeyword(ctx, listBo.getKeyword().trim()));
            routes = dedupeRoutes(routes);
            Page<TransportRouteAppVo> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
            IPage<TransportRouteAppVo> pagedRoutes = PageUtil.manualPage(page, routes);
            pageVo.setCurrent(pagedRoutes.getCurrent());
            pageVo.setSize(pagedRoutes.getSize());
            pageVo.setTotal(pagedRoutes.getTotal());
            pageVo.setRecords(pagedRoutes.getRecords());
            return pageVo;
        }

        if (CollectionUtils.isEmpty(nearbyStations)) {
            throw new BizException(AppError.TRANSPORT_BUS_STATION_NOT_FOUND);
        }

        Page<StationCandidate> stationPage = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        IPage<StationCandidate> pagedStations = PageUtil.manualPage(stationPage, nearbyStations);
        for (StationCandidate station : pagedStations.getRecords()) {
            routes.addAll(queryRoutesAtStationSafe(ctx.officialCity, station.name, DEFAULT_DIRECTION, station.name));
            if (routes.size() >= MAX_AGGREGATE_ROUTES) {
                break;
            }
        }
        routes = dedupeRoutesByStation(routes);
        pageVo.setCurrent(pagedStations.getCurrent());
        pageVo.setSize(pagedStations.getSize());
        pageVo.setTotal(pagedStations.getTotal());
        pageVo.setRecords(routes);
        return pageVo;
    }

    public TransportScheduleAppVo appSchedule(String routeKey, String lat, String lng, String city, Integer direction) {
        LocationContext ctx = resolveLocationContext(lat, lng, city);
        int dir = normalizeDirection(direction);
        JSONObject lineData = requireLineData(ctx.officialCity, routeKey, dir);

        TransportScheduleAppVo vo = new TransportScheduleAppVo();
        vo.setId(lineData.getString("line"));
        vo.setName(lineData.getString("line"));
        vo.setRoutes(listRouteBriefs(ctx, lineData));
        vo.setDirections(buildDirections(ctx.officialCity, lineData, dir));
        vo.setScheduleRows(buildScheduleRows(lineData));
        return vo;
    }

    public TransportStationAppVo appStations(String routeKey, String lat, String lng, String city,
                                               String station, Integer direction) {
        LocationContext ctx = resolveLocationContext(lat, lng, city);
        int dir = normalizeDirection(direction);
        JSONObject lineData = requireLineData(ctx.officialCity, routeKey, dir);

        List<String> stopNames = parseStopNames(lineData);
        String refStation = StringUtils.defaultIfBlank(station, ctx.referenceStation);
        int currentIndex = resolveCurrentStopIndex(stopNames, refStation, ctx.lat, ctx.lng, lineData);

        TransportStationAppVo vo = new TransportStationAppVo();
        vo.setId(lineData.getString("line"));
        vo.setName(lineData.getString("line"));
        vo.setStartStation(lineData.getString("start"));
        vo.setEndStation(lineData.getString("end"));
        vo.setStationCount(stopNames.size());
        vo.setCurrentStopIndex(currentIndex);
        vo.setStations(stopNames);
        return vo;
    }

    public TransportTrackAppVo appTrack(String routeKey, String lat, String lng, String city,
                                        String station, Integer direction) {
        LocationContext ctx = resolveLocationContext(lat, lng, city);
        int dir = normalizeDirection(direction);
        JSONObject lineData = requireLineData(ctx.officialCity, routeKey, dir);
        String refStation = StringUtils.defaultIfBlank(station, ctx.referenceStation);
        if (StringUtils.isBlank(refStation)) {
            refStation = lineData.getString("start");
        }

        JSONObject queryData = requireQueryData(ctx.officialCity, refStation, dir);
        JSONObject lineInfo = findLineInQuery(queryData, lineData.getString("line"));

        TransportTrackAppVo vo = new TransportTrackAppVo();
        vo.setRouteName(lineData.getString("line"));
        vo.setTrackInfo(buildTrackInfo(refStation, lineInfo, lineData));
        vo.setRealtimeUpdates(buildRealtimeUpdates(lineInfo, queryData.getString("updated_at")));
        vo.setPolyline(buildPolyline(lineData));
        return vo;
    }

    private List<TransportRouteAppVo> searchByKeyword(LocationContext ctx, String keyword) {
        List<TransportRouteAppVo> routes = new ArrayList<>();

        JSONObject lineResp = busRealtimeClient.line(ctx.officialCity, keyword, DEFAULT_DIRECTION);
        JSONObject lineData = dataOrNull(lineResp);
        if (lineData != null && StringUtils.isNotBlank(lineData.getString("line"))) {
            StationCandidate station = pickNearestStation(ctx.nearbyStations);
            String refStation = station == null ? lineData.getString("start") : station.name;
            routes.add(toRouteVoFromLine(ctx.officialCity, lineData, refStation, DEFAULT_DIRECTION, null));
        }

        List<StationCandidate> stations = searchStations(ctx.officialCity, keyword, ctx.lat, ctx.lng);
        for (StationCandidate candidate : stations) {
            if (!candidate.queryable) {
                continue;
            }
            routes.addAll(queryRoutesAtStation(ctx.officialCity, candidate.name, DEFAULT_DIRECTION, candidate.name));
            if (routes.size() >= 30) {
                break;
            }
        }
        return routes;
    }

    private List<TransportRouteAppVo> queryRoutesAtStation(String city, String station, int direction, String refStation) {
        JSONObject queryResp = busRealtimeClient.query(city, station, direction);
        JSONObject queryData = requireUpstreamData(queryResp);
        return buildRoutesFromQueryData(city, queryData, refStation, direction);
    }

    private List<TransportRouteAppVo> queryRoutesAtStationSafe(String city, String station, int direction, String refStation) {
        try {
            JSONObject queryResp = busRealtimeClient.query(city, station, direction);
            JSONObject queryData = dataOrNull(queryResp);
            if (queryData == null) {
                return Collections.emptyList();
            }
            return buildRoutesFromQueryData(city, queryData, refStation, direction);
        } catch (Exception ex) {
            log.warn("query routes at station failed: city={}, station={}, reason={}", city, station, ex.getMessage());
            return Collections.emptyList();
        }
    }

    private List<TransportRouteAppVo> buildRoutesFromQueryData(String city, JSONObject queryData,
                                                                 String refStation, int direction) {
        JSONArray lines = queryData.getJSONArray("lines");
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }
        List<TransportRouteAppVo> routes = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            JSONObject line = lines.getJSONObject(i);
            routes.add(toRouteVoFromQuery(city, line, refStation, direction, i));
        }
        return routes;
    }

    private TransportRouteAppVo toRouteVoFromQuery(String city, JSONObject line, String refStation, int direction, int index) {
        TransportRouteAppVo vo = new TransportRouteAppVo();
        vo.setId(line.getString("line"));
        vo.setName(line.getString("line"));
        vo.setFirstBus(StringUtils.defaultIfBlank(line.getString("first_time"), "--"));
        vo.setLastBus(StringUtils.defaultIfBlank(line.getString("last_time"), "--"));
        vo.setStartStation(refStation);
        vo.setEndStation(StringUtils.defaultIfBlank(line.getString("terminal"), ""));
        vo.setStatusKey("running");
        vo.setStatusLabel(resolveStatusLabel(line));
        vo.setAccentColor(ACCENT_COLORS[index % ACCENT_COLORS.length]);
        vo.setAccentLight(ACCENT_LIGHT);
        vo.setShowRealtimeStops(true);
        vo.setExpandable(false);
        vo.setCity(city);
        vo.setReferenceStation(refStation);
        vo.setDirection(direction);

        JSONArray buses = line.getJSONArray("buses");
        if (buses != null && !buses.isEmpty()) {
            JSONObject nearest = pickNearestBus(buses);
            vo.setCurrentStop(refStation);
            vo.setNextStop(formatBusStatus(nearest));
            vo.setNextEta(formatEta(nearest));
            TransportTrackInfoVo trackInfo = new TransportTrackInfoVo();
            trackInfo.setCurrentStation(refStation);
            trackInfo.setNextStation(formatBusStatus(nearest));
            trackInfo.setEta(formatEta(nearest));
            trackInfo.setVehicleNo(nearest.getString("bus_id"));
            vo.setTrackInfo(trackInfo);
        } else {
            vo.setCurrentStop(refStation);
            vo.setNextStop("暂无车辆");
        }
        vo.setStationCount(0);
        vo.setCurrentStopIndex(0);
        vo.setStations(Collections.emptyList());
        return vo;
    }

    private TransportRouteAppVo toRouteVoFromLine(String city, JSONObject lineData, String refStation,
                                                  int direction, JSONObject lineInfo) {
        TransportRouteAppVo vo = new TransportRouteAppVo();
        vo.setId(lineData.getString("line"));
        vo.setName(lineData.getString("line"));
        vo.setFirstBus(StringUtils.defaultIfBlank(lineData.getString("first_time"), "--"));
        vo.setLastBus(StringUtils.defaultIfBlank(lineData.getString("last_time"), "--"));
        vo.setStartStation(lineData.getString("start"));
        vo.setEndStation(lineData.getString("end"));
        vo.setStatusKey("running");
        vo.setStatusLabel("运行中");
        vo.setAccentColor(ACCENT_COLORS[0]);
        vo.setAccentLight(ACCENT_LIGHT);
        vo.setCity(city);
        vo.setReferenceStation(refStation);
        vo.setDirection(direction);
        vo.setExpandable(true);
        vo.setShowRealtimeStops(lineInfo != null);

        List<String> stops = parseStopNames(lineData);
        vo.setStations(stops);
        vo.setStationCount(stops.size());
        vo.setCurrentStopIndex(resolveCurrentStopIndex(stops, refStation, null, null, lineData));

        if (lineInfo != null) {
            JSONArray buses = lineInfo.getJSONArray("buses");
            if (buses != null && !buses.isEmpty()) {
                JSONObject nearest = pickNearestBus(buses);
                vo.setShowRealtimeStops(true);
                vo.setCurrentStop(refStation);
                vo.setNextStop(formatBusStatus(nearest));
                vo.setNextEta(formatEta(nearest));
            }
        }
        return vo;
    }

    private List<TransportRouteBriefVo> listRouteBriefs(LocationContext ctx, JSONObject currentLine) {
        List<TransportRouteBriefVo> briefs = new ArrayList<>();
        TransportRouteBriefVo current = new TransportRouteBriefVo();
        current.setId(currentLine.getString("line"));
        current.setName(currentLine.getString("line"));
        briefs.add(current);

        if (StringUtils.isNotBlank(ctx.referenceStation)) {
            JSONObject queryResp = busRealtimeClient.query(ctx.officialCity, ctx.referenceStation, DEFAULT_DIRECTION);
            JSONObject queryData = dataOrNull(queryResp);
            if (queryData != null) {
                JSONArray lines = queryData.getJSONArray("lines");
                if (lines != null) {
                    Set<String> exists = new HashSet<>();
                    exists.add(current.getId());
                    for (int i = 0; i < lines.size(); i++) {
                        String lineName = lines.getJSONObject(i).getString("line");
                        if (StringUtils.isBlank(lineName) || exists.contains(lineName)) {
                            continue;
                        }
                        TransportRouteBriefVo brief = new TransportRouteBriefVo();
                        brief.setId(lineName);
                        brief.setName(lineName);
                        briefs.add(brief);
                        exists.add(lineName);
                    }
                }
            }
        }
        return briefs;
    }

    private List<TransportDirectionVo> buildDirections(String city, JSONObject lineData, int direction) {
        List<TransportDirectionVo> directions = new ArrayList<>();
        directions.add(toDirectionVo(lineData, direction == 1 ? "上行" : "下行"));
        if (Boolean.TRUE.equals(lineData.getBoolean("has_reverse"))) {
            int reverseDir = direction == 1 ? 2 : 1;
            JSONObject reverseLine = dataOrNull(busRealtimeClient.line(city, lineData.getString("line"), reverseDir));
            if (reverseLine != null) {
                directions.add(toDirectionVo(reverseLine, reverseDir == 1 ? "上行" : "下行"));
            }
        }
        return directions;
    }

    private TransportDirectionVo toDirectionVo(JSONObject lineData, String label) {
        TransportDirectionVo vo = new TransportDirectionVo();
        vo.setLabel(label);
        vo.setFrom(lineData.getString("start"));
        vo.setTo(lineData.getString("end"));
        vo.setFirstBus(StringUtils.defaultIfBlank(lineData.getString("first_time"), "--"));
        vo.setLastBus(StringUtils.defaultIfBlank(lineData.getString("last_time"), "--"));
        vo.setInterval("以实际运营为准");
        return vo;
    }

    private List<TransportScheduleRowVo> buildScheduleRows(JSONObject lineData) {
        JSONArray stops = lineData.getJSONArray("stops");
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<TransportScheduleRowVo> rows = new ArrayList<>();
        for (int i = 0; i < stops.size(); i++) {
            JSONObject stop = stops.getJSONObject(i);
            TransportScheduleRowVo row = new TransportScheduleRowVo();
            row.setStation(stop.getString("name"));
            row.setTimes(Collections.emptyList());
            rows.add(row);
        }
        return rows;
    }

    private TransportTrackInfoVo buildTrackInfo(String refStation, JSONObject lineInfo, JSONObject lineData) {
        TransportTrackInfoVo info = new TransportTrackInfoVo();
        info.setCurrentStation(refStation);
        info.setNextStation(resolveNextStationName(refStation, lineData));

        if (lineInfo == null) {
            info.setEta("暂无车辆");
            return info;
        }
        JSONArray buses = lineInfo.getJSONArray("buses");
        if (buses == null || buses.isEmpty()) {
            info.setEta("暂无车辆");
            return info;
        }
        JSONObject nearest = pickNearestBus(buses);
        info.setEta(formatRemainingAndEta(nearest));
        info.setVehicleNo(nearest.getString("bus_id"));
        return info;
    }

    /**
     * 下一站取线路站点列表中当前站的下一站真实站名
     */
    private String resolveNextStationName(String refStation, JSONObject lineData) {
        List<String> stopNames = parseStopNames(lineData);
        if (CollectionUtils.isEmpty(stopNames)) {
            return "";
        }
        int currentIndex = resolveCurrentStopIndex(stopNames, refStation, null, null, lineData);
        if (currentIndex >= 0 && currentIndex < stopNames.size() - 1) {
            return stopNames.get(currentIndex + 1);
        }
        return "";
    }

    /**
     * 剩余站数与预计时间合并展示，例如「还有 7 站 · 约17分钟」
     */
    private String formatRemainingAndEta(JSONObject bus) {
        String remaining = formatRemainingStops(bus);
        String eta = formatEta(bus);
        if (StringUtils.isNotBlank(remaining) && StringUtils.isNotBlank(eta)) {
            return remaining + " · " + eta;
        }
        if (StringUtils.isNotBlank(remaining)) {
            return remaining;
        }
        if (StringUtils.isNotBlank(eta)) {
            return eta;
        }
        return formatBusStatus(bus);
    }

    private String formatRemainingStops(JSONObject bus) {
        if (bus == null) {
            return "";
        }
        Integer stops = bus.getInteger("stops_remaining");
        if (stops != null) {
            return "还有 " + stops + " 站";
        }
        String status = bus.getString("status");
        if (StringUtils.isBlank(status)) {
            return "";
        }
        java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("(\\d+)\\s*站").matcher(status);
        if (matcher.find()) {
            return "还有 " + matcher.group(1) + " 站";
        }
        return status.trim();
    }

    private List<TransportRealtimeUpdateVo> buildRealtimeUpdates(JSONObject lineInfo, String updatedAt) {
        if (lineInfo == null) {
            return Collections.emptyList();
        }
        JSONArray buses = lineInfo.getJSONArray("buses");
        if (buses == null || buses.isEmpty()) {
            return Collections.emptyList();
        }
        List<TransportRealtimeUpdateVo> updates = new ArrayList<>();
        for (int i = 0; i < buses.size(); i++) {
            JSONObject bus = buses.getJSONObject(i);
            TransportRealtimeUpdateVo update = new TransportRealtimeUpdateVo();
            update.setId(bus.getString("bus_id"));
            update.setLabel(StringUtils.defaultIfBlank(bus.getString("bus_id"), "车辆"));
            update.setValue(formatBusStatus(bus));
            update.setTime(StringUtils.defaultIfBlank(formatEta(bus), updatedAt));
            updates.add(update);
        }
        return updates;
    }

    private List<TransportPolylinePointVo> buildPolyline(JSONObject lineData) {
        JSONArray stops = lineData.getJSONArray("stops");
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<TransportPolylinePointVo> points = new ArrayList<>();
        for (int i = 0; i < stops.size(); i++) {
            JSONObject stop = stops.getJSONObject(i);
            if (stop.getDouble("lat") == null || stop.getDouble("lng") == null) {
                continue;
            }
            TransportPolylinePointVo point = new TransportPolylinePointVo();
            point.setLatitude(BigDecimal.valueOf(stop.getDouble("lat")));
            point.setLongitude(BigDecimal.valueOf(stop.getDouble("lng")));
            point.setStationName(stop.getString("name"));
            points.add(point);
        }
        return points;
    }

    private JSONObject requireLineData(String city, String routeKey, int direction) {
        JSONObject resp = busRealtimeClient.line(city, routeKey, direction);
        JSONObject data = dataOrNull(resp);
        if (data == null || StringUtils.isBlank(data.getString("line"))) {
            throw new BizException(AppError.TRANSPORT_ROUTE_NOT_FOUND);
        }
        return data;
    }

    private JSONObject requireQueryData(String city, String station, int direction) {
        JSONObject resp = busRealtimeClient.query(city, station, direction);
        JSONObject data = dataOrNull(resp);
        if (data == null) {
            throw new BizException(AppError.TRANSPORT_BUS_UPSTREAM_ERROR);
        }
        return data;
    }

    private JSONObject requireUpstreamData(JSONObject resp) {
        if (resp == null) {
            throw new BizException(AppError.TRANSPORT_BUS_UPSTREAM_ERROR);
        }
        if (!Objects.equals(resp.getInteger("code"), 0)) {
            throw new BizException(AppError.TRANSPORT_BUS_UPSTREAM_ERROR);
        }
        return resp.getJSONObject("data");
    }

    private JSONObject dataOrNull(JSONObject resp) {
        if (resp == null || !Objects.equals(resp.getInteger("code"), 0)) {
            return null;
        }
        return resp.getJSONObject("data");
    }

    private JSONObject findLineInQuery(JSONObject queryData, String lineName) {
        if (queryData == null || StringUtils.isBlank(lineName)) {
            return null;
        }
        JSONArray lines = queryData.getJSONArray("lines");
        if (lines == null) {
            return null;
        }
        for (int i = 0; i < lines.size(); i++) {
            JSONObject line = lines.getJSONObject(i);
            if (lineName.equals(line.getString("line"))) {
                return line;
            }
        }
        return null;
    }

    private List<String> parseStopNames(JSONObject lineData) {
        JSONArray stops = lineData.getJSONArray("stops");
        if (stops == null || stops.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> names = new ArrayList<>();
        for (int i = 0; i < stops.size(); i++) {
            names.add(stops.getJSONObject(i).getString("name"));
        }
        return names;
    }

    private int resolveCurrentStopIndex(List<String> stopNames, String refStation, String lat, String lng,
                                        JSONObject lineData) {
        if (CollectionUtils.isEmpty(stopNames)) {
            return 0;
        }
        if (StringUtils.isNotBlank(refStation)) {
            for (int i = 0; i < stopNames.size(); i++) {
                if (refStation.equals(stopNames.get(i))) {
                    return i;
                }
            }
        }
        BigDecimal userLat = MapDistanceUtil.parseCoordinate(lat);
        BigDecimal userLng = MapDistanceUtil.parseCoordinate(lng);
        if (userLat == null || userLng == null || lineData == null) {
            return 0;
        }
        JSONArray stops = lineData.getJSONArray("stops");
        if (stops == null || stops.isEmpty()) {
            return 0;
        }
        int nearestIndex = 0;
        BigDecimal nearestDistance = null;
        for (int i = 0; i < stops.size(); i++) {
            JSONObject stop = stops.getJSONObject(i);
            if (stop.getDouble("lat") == null || stop.getDouble("lng") == null) {
                continue;
            }
            BigDecimal distance = MapDistanceUtil.distanceKm(userLat, userLng,
                    BigDecimal.valueOf(stop.getDouble("lat")),
                    BigDecimal.valueOf(stop.getDouble("lng")));
            if (distance != null && (nearestDistance == null || distance.compareTo(nearestDistance) < 0)) {
                nearestDistance = distance;
                nearestIndex = i;
            }
        }
        return nearestIndex;
    }

    private List<StationCandidate> findNearbyStations(LocationContext ctx, List<String> extraKeywords) {
        LinkedHashSet<String> keywords = new LinkedHashSet<>();
        if (CollectionUtils.isNotEmpty(extraKeywords)) {
            keywords.addAll(extraKeywords);
        }
        if (StringUtils.isNotBlank(ctx.locationName)) {
            keywords.add(ctx.locationName.trim());
        }
        if (StringUtils.isNotBlank(ctx.addressKeyword)) {
            keywords.add(ctx.addressKeyword.trim());
        }
        if (StringUtils.isNotBlank(ctx.roadKeyword)) {
            keywords.add(ctx.roadKeyword);
        }
        if (StringUtils.isNotBlank(ctx.townKeyword)) {
            keywords.add(ctx.townKeyword);
        }
        if (StringUtils.isNotBlank(ctx.districtKeyword)) {
            keywords.add(ctx.districtKeyword);
        }
        keywords.add("站");
        keywords.add("公交");

        Map<String, StationCandidate> candidates = new LinkedHashMap<>();
        collectStationCandidates(candidates, searchStations(ctx.officialCity, null, ctx.lat, ctx.lng));
        for (String keyword : keywords) {
            if (StringUtils.isBlank(keyword)) {
                continue;
            }
            collectStationCandidates(candidates, searchStations(ctx.officialCity, keyword, ctx.lat, ctx.lng));
        }

        List<StationCandidate> withinPrimaryRadius = filterNearbyBusStations(candidates.values(), NEARBY_STATION_RADIUS_KM);
        if (withinPrimaryRadius.size() >= 2) {
            return limitNearbyStations(withinPrimaryRadius);
        }
        List<StationCandidate> withinFallbackRadius = filterNearbyBusStations(candidates.values(),
                NEARBY_STATION_FALLBACK_RADIUS_KM);
        if (withinFallbackRadius.size() >= 2) {
            return limitNearbyStations(withinFallbackRadius);
        }
        List<StationCandidate> relaxed = filterNearbyBusStationsRelaxed(candidates.values());
        if (CollectionUtils.isNotEmpty(relaxed)) {
            return limitNearbyStations(relaxed);
        }
        if (CollectionUtils.isNotEmpty(withinFallbackRadius)) {
            return limitNearbyStations(withinFallbackRadius);
        }
        return limitNearbyStations(withinPrimaryRadius);
    }

    private void collectStationCandidates(Map<String, StationCandidate> candidates, List<StationCandidate> found) {
        if (CollectionUtils.isEmpty(found)) {
            return;
        }
        for (StationCandidate candidate : found) {
            if (candidate == null || StringUtils.isBlank(candidate.name)) {
                continue;
            }
            StationCandidate existing = candidates.get(candidate.name);
            if (existing == null) {
                candidates.put(candidate.name, candidate);
                continue;
            }
            if (existing.distanceKm == null && candidate.distanceKm != null) {
                candidates.put(candidate.name, candidate);
                continue;
            }
            if (existing.distanceKm != null && candidate.distanceKm != null
                    && candidate.distanceKm.compareTo(existing.distanceKm) < 0) {
                candidates.put(candidate.name, candidate);
            }
        }
    }

    private List<StationCandidate> filterNearbyBusStations(Collection<StationCandidate> candidates, BigDecimal radiusKm) {
        return candidates.stream()
                .filter(this::isBusStationCandidate)
                .filter(item -> item.distanceKm != null && item.distanceKm.compareTo(radiusKm) <= 0)
                .sorted(Comparator.comparing(item -> item.distanceKm))
                .collect(Collectors.toList());
    }

    private List<StationCandidate> filterNearbyBusStationsRelaxed(Collection<StationCandidate> candidates) {
        return candidates.stream()
                .filter(this::isBusStationCandidate)
                .sorted(Comparator.comparing(item -> item.distanceKm, Comparator.nullsLast(BigDecimal::compareTo)))
                .collect(Collectors.toList());
    }

    private boolean isBusStationCandidate(StationCandidate item) {
        if (item == null || !item.queryable || StringUtils.isBlank(item.name)) {
            return false;
        }
        if (StringUtils.isBlank(item.kind)) {
            return true;
        }
        return "bus".equalsIgnoreCase(item.kind);
    }

    private List<StationCandidate> limitNearbyStations(List<StationCandidate> stations) {
        if (CollectionUtils.isEmpty(stations)) {
            return Collections.emptyList();
        }
        return stations.stream().limit(MAX_NEARBY_STATIONS).collect(Collectors.toList());
    }

    private StationCandidate pickNearestStation(List<StationCandidate> stations) {
        if (CollectionUtils.isEmpty(stations)) {
            return null;
        }
        return stations.get(0);
    }

    private List<TransportNearbyStationVo> toNearbyStationVos(List<StationCandidate> stations) {
        if (CollectionUtils.isEmpty(stations)) {
            return Collections.emptyList();
        }
        List<TransportNearbyStationVo> result = new ArrayList<>();
        for (StationCandidate station : stations) {
            TransportNearbyStationVo vo = new TransportNearbyStationVo();
            vo.setName(station.name);
            vo.setDistanceKm(station.distanceKm);
            result.add(vo);
        }
        return result;
    }

    private List<StationCandidate> searchStations(String city, String keyword, String lat, String lng) {
        JSONObject resp = busRealtimeClient.stations(city, keyword, lat, lng);
        JSONObject data = dataOrNull(resp);
        if (data == null) {
            return Collections.emptyList();
        }
        JSONArray stations = data.getJSONArray("stations");
        if (stations == null || stations.isEmpty()) {
            return Collections.emptyList();
        }
        BigDecimal userLat = MapDistanceUtil.parseCoordinate(lat);
        BigDecimal userLng = MapDistanceUtil.parseCoordinate(lng);
        List<StationCandidate> result = new ArrayList<>();
        for (int i = 0; i < stations.size(); i++) {
            JSONObject station = stations.getJSONObject(i);
            StationCandidate candidate = new StationCandidate();
            candidate.name = station.getString("name");
            candidate.kind = station.getString("kind");
            candidate.queryable = Boolean.TRUE.equals(station.getBoolean("queryable"));
            if (station.getDouble("lat") != null && station.getDouble("lng") != null
                    && userLat != null && userLng != null) {
                candidate.distanceKm = MapDistanceUtil.distanceKm(userLat, userLng,
                        BigDecimal.valueOf(station.getDouble("lat")),
                        BigDecimal.valueOf(station.getDouble("lng")));
            }
            result.add(candidate);
        }
        result.sort(Comparator.comparing(item -> item.distanceKm, Comparator.nullsLast(BigDecimal::compareTo)));
        return result;
    }

    private LocationContext resolveLocationContext(String lat, String lng) {
        TransportRouteAppListBo listBo = new TransportRouteAppListBo();
        listBo.setLat(lat);
        listBo.setLng(lng);
        return resolveLocationContext(listBo);
    }

    private LocationContext resolveLocationContext(String lat, String lng, String cityOverride) {
        TransportRouteAppListBo listBo = new TransportRouteAppListBo();
        listBo.setLat(lat);
        listBo.setLng(lng);
        listBo.setCity(cityOverride);
        return resolveLocationContext(listBo);
    }

    private LocationContext resolveLocationContext(TransportRouteAppListBo listBo) {
        if (listBo == null || StringUtils.isAnyBlank(listBo.getLat(), listBo.getLng())) {
            throw new BizException(AppError.DESTINATION_LOCATION_REQUIRED);
        }
        LocationContext ctx = new LocationContext();
        ctx.lat = listBo.getLat().trim();
        ctx.lng = listBo.getLng().trim();
        ctx.locationName = StringUtils.trimToNull(listBo.getLocationName());
        ctx.addressKeyword = StringUtils.trimToNull(listBo.getAddress());

        String clientCity = StringUtils.trimToNull(listBo.getCity());
        String clientDistrict = StringUtils.trimToNull(listBo.getDistrict());
        boolean hasClientRegion = StringUtils.isNotBlank(clientCity) || StringUtils.isNotBlank(clientDistrict);

        AddressVo address = null;
        if (!hasClientRegion) {
            address = mapService.reverseGeocode(ctx.lat, ctx.lng);
        }

        if (StringUtils.isNotBlank(clientCity)) {
            ctx.officialCity = resolveOfficialCityWithGeoValidation(clientCity, ctx.lat, ctx.lng);
            if (StringUtils.isBlank(ctx.officialCity)) {
                ctx.officialCity = resolveOfficialCity(clientCity);
            }
        } else if (StringUtils.isNotBlank(clientDistrict)) {
            ctx.officialCity = resolveOfficialCityWithGeoValidation(clientDistrict, ctx.lat, ctx.lng);
            if (StringUtils.isBlank(ctx.officialCity)) {
                ctx.officialCity = resolveOfficialCity(clientDistrict);
            }
        } else {
            ctx.officialCity = resolveOfficialCityByCoordinates(ctx.lat, ctx.lng, address);
        }
        if (StringUtils.isBlank(ctx.officialCity)) {
            throw new BizException(AppError.TRANSPORT_BUS_CITY_UNSUPPORTED);
        }

        if (StringUtils.isNotBlank(clientDistrict)) {
            ctx.districtKeyword = normalizePlaceToken(clientDistrict);
        } else {
            ctx.districtKeyword = normalizePlaceToken(addressToken(address == null ? null : address.getDistrict()));
        }
        ctx.townKeyword = normalizePlaceToken(addressToken(address == null ? null : address.getTown()));
        if (StringUtils.isNotBlank(ctx.addressKeyword)) {
            ctx.roadKeyword = normalizePlaceToken(extractRoadKeyword(ctx.addressKeyword));
        } else {
            ctx.roadKeyword = normalizePlaceToken(addressToken(address == null ? null : address.getRoad()));
        }

        ctx.nearbyStations = findNearbyStations(ctx, Collections.emptyList());
        StationCandidate nearest = pickNearestStation(ctx.nearbyStations);
        if (nearest != null) {
            ctx.referenceStation = nearest.name;
        } else if (StringUtils.isNotBlank(ctx.locationName)) {
            ctx.referenceStation = ctx.locationName;
        }
        return ctx;
    }

    private String extractRoadKeyword(String fullAddress) {
        if (StringUtils.isBlank(fullAddress)) {
            return "";
        }
        String text = fullAddress.trim();
        java.util.regex.Matcher matcher = java.util.regex.Pattern
                .compile("(?:区|县|旗)(.+?(?:路|街|道|巷|大道|大街))")
                .matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        matcher = java.util.regex.Pattern.compile("(.+?(?:路|街|道|巷|大道|大街))").matcher(text);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return text.length() > 20 ? text.substring(text.length() - 20) : text;
    }

    private String resolveOfficialCityFromAddress(AddressVo address) {
        List<String> candidates = new ArrayList<>();
        candidates.add(normalizeCityToken(address.getCity()));
        candidates.add(normalizeCityToken(address.getDistrict()));
        candidates.add(normalizeCityToken(address.getProvince()));
        for (String candidate : candidates) {
            if (StringUtils.isBlank(candidate)) {
                continue;
            }
            String official = resolveOfficialCity(candidate);
            if (StringUtils.isNotBlank(official)) {
                return official;
            }
        }
        return null;
    }

    /**
     * 结合逆地理候选 + 坐标附近是否有公交站点，校验官方城市，避免地址解析偏差导致跨城误查。
     */
    private String resolveOfficialCityByCoordinates(String lat, String lng, AddressVo address) {
        LinkedHashSet<String> tried = new LinkedHashSet<>();
        if (address != null) {
            for (String raw : Arrays.asList(address.getCity(), address.getDistrict(), address.getProvince())) {
                String official = resolveOfficialCityWithGeoValidation(raw, lat, lng, tried);
                if (StringUtils.isNotBlank(official)) {
                    return official;
                }
            }
            String geocodeHint = joinAddressHint(address);
            int probeCount = 0;
            for (String city : loadOfficialCities()) {
                if (tried.contains(city)) {
                    continue;
                }
                if (!matchesGeocodeHint(city, geocodeHint, address)) {
                    continue;
                }
                tried.add(city);
                if (cityHasNearbyBusStations(city, lat, lng)) {
                    return city;
                }
                probeCount++;
                if (probeCount >= MAX_CITY_GEO_PROBE) {
                    break;
                }
            }
        }
        return address == null ? null : resolveOfficialCityFromAddress(address);
    }

    private String resolveOfficialCityWithGeoValidation(String rawCity, String lat, String lng) {
        return resolveOfficialCityWithGeoValidation(rawCity, lat, lng, new LinkedHashSet<>());
    }

    private String resolveOfficialCityWithGeoValidation(String rawCity, String lat, String lng, Set<String> tried) {
        if (StringUtils.isBlank(rawCity)) {
            return null;
        }
        String official = resolveOfficialCity(rawCity.trim());
        if (StringUtils.isBlank(official) || tried.contains(official)) {
            return null;
        }
        tried.add(official);
        if (cityHasNearbyBusStations(official, lat, lng)) {
            return official;
        }
        return null;
    }

    private boolean cityHasNearbyBusStations(String city, String lat, String lng) {
        List<StationCandidate> stations = searchStations(city, null, lat, lng);
        if (CollectionUtils.isEmpty(stations)) {
            return false;
        }
        for (StationCandidate station : stations) {
            if (!station.queryable || !"bus".equalsIgnoreCase(station.kind)) {
                continue;
            }
            if (station.distanceKm != null
                    && station.distanceKm.compareTo(NEARBY_STATION_FALLBACK_RADIUS_KM) <= 0) {
                return true;
            }
        }
        return false;
    }

    private String joinAddressHint(AddressVo address) {
        StringBuilder builder = new StringBuilder();
        appendAddressPart(builder, address.getProvince());
        appendAddressPart(builder, address.getCity());
        appendAddressPart(builder, address.getDistrict());
        return builder.toString();
    }

    private void appendAddressPart(StringBuilder builder, String part) {
        if (StringUtils.isBlank(part)) {
            return;
        }
        builder.append(part.trim());
    }

    private boolean matchesGeocodeHint(String officialCity, String geocodeHint, AddressVo address) {
        if (StringUtils.isBlank(officialCity)) {
            return false;
        }
        if (StringUtils.isNotBlank(geocodeHint) && geocodeHint.contains(officialCity)) {
            return true;
        }
        if (address == null) {
            return false;
        }
        String normalizedCity = normalizeCityToken(address.getCity());
        return officialCity.equals(normalizedCity)
                || normalizedCity.contains(officialCity)
                || officialCity.contains(normalizedCity);
    }

    private String resolveOfficialCity(String rawCity) {
        if (StringUtils.isBlank(rawCity)) {
            return null;
        }
        List<String> cities = loadOfficialCities();
        String normalized = normalizeCityToken(rawCity);
        for (String city : cities) {
            if (city.equals(normalized) || normalized.contains(city) || city.contains(normalized)) {
                return city;
            }
        }
        return null;
    }

    private List<String> loadOfficialCities() {
        long now = System.currentTimeMillis();
        if (CollectionUtils.isNotEmpty(cachedCityNames) && now - cityCacheAt < CITY_CACHE_MS) {
            return cachedCityNames;
        }
        synchronized (this) {
            if (CollectionUtils.isNotEmpty(cachedCityNames) && now - cityCacheAt < CITY_CACHE_MS) {
                return cachedCityNames;
            }
            JSONObject resp = busRealtimeClient.cities();
            JSONObject data = dataOrNull(resp);
            if (data == null) {
                throw new BizException(AppError.TRANSPORT_BUS_UPSTREAM_ERROR);
            }
            JSONArray cities = data.getJSONArray("cities");
            if (cities == null || cities.isEmpty()) {
                throw new BizException(AppError.TRANSPORT_BUS_UPSTREAM_ERROR);
            }
            List<String> names = new ArrayList<>();
            for (int i = 0; i < cities.size(); i++) {
                String name = cities.getJSONObject(i).getString("name");
                if (StringUtils.isNotBlank(name)) {
                    names.add(name.trim());
                }
            }
            cachedCityNames = names;
            cityCacheAt = System.currentTimeMillis();
            return cachedCityNames;
        }
    }

    private List<TransportRouteAppVo> dedupeRoutes(List<TransportRouteAppVo> routes) {
        if (CollectionUtils.isEmpty(routes)) {
            return Collections.emptyList();
        }
        Map<String, TransportRouteAppVo> map = new LinkedHashMap<>();
        for (TransportRouteAppVo route : routes) {
            if (route == null || StringUtils.isBlank(route.getId())) {
                continue;
            }
            map.putIfAbsent(route.getId(), route);
        }
        return new ArrayList<>(map.values());
    }

    private List<TransportRouteAppVo> dedupeRoutesByStation(List<TransportRouteAppVo> routes) {
        if (CollectionUtils.isEmpty(routes)) {
            return Collections.emptyList();
        }
        Map<String, TransportRouteAppVo> map = new LinkedHashMap<>();
        for (TransportRouteAppVo route : routes) {
            if (route == null || StringUtils.isBlank(route.getId())) {
                continue;
            }
            String key = route.getId() + "@" + StringUtils.defaultString(route.getReferenceStation());
            map.putIfAbsent(key, route);
        }
        return new ArrayList<>(map.values());
    }

    private JSONObject pickNearestBus(JSONArray buses) {
        JSONObject selected = buses.getJSONObject(0);
        Integer selectedStops = selected.getInteger("stops_remaining");
        for (int i = 1; i < buses.size(); i++) {
            JSONObject bus = buses.getJSONObject(i);
            Integer stops = bus.getInteger("stops_remaining");
            if (stops == null) {
                continue;
            }
            if (selectedStops == null || stops < selectedStops) {
                selected = bus;
                selectedStops = stops;
            }
        }
        return selected;
    }

    private String resolveStatusLabel(JSONObject line) {
        Integer busCount = line.getInteger("bus_count");
        if (busCount != null && busCount > 0) {
            return "运行中";
        }
        return "暂无车辆";
    }

    private String formatBusStatus(JSONObject bus) {
        if (bus == null) {
            return "暂无车辆";
        }
        String status = bus.getString("status");
        if (StringUtils.isNotBlank(status)) {
            return status;
        }
        Integer stops = bus.getInteger("stops_remaining");
        if (stops != null) {
            return stops + "站";
        }
        return "暂无车辆";
    }

    private String formatEta(JSONObject bus) {
        if (bus == null) {
            return "";
        }
        Integer minutes = bus.getInteger("travel_minutes");
        if (minutes != null && minutes > 0) {
            return "约" + minutes + "分钟";
        }
        String arrivalTime = bus.getString("arrival_time");
        if (StringUtils.isNotBlank(arrivalTime)) {
            return arrivalTime;
        }
        return "";
    }

    private int normalizeDirection(Integer direction) {
        if (direction == null || direction <= 0) {
            return DEFAULT_DIRECTION;
        }
        return direction == 2 ? 2 : 1;
    }

    private String normalizeCityToken(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return value.trim()
                .replace("特别行政区", "")
                .replace("自治区", "")
                .replace("自治州", "")
                .replace("地区", "")
                .replace("盟", "")
                .replace("省", "")
                .replace("市", "")
                .replace("区", "")
                .replace("县", "")
                .trim();
    }

    private String normalizePlaceToken(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        return value.trim()
                .replace("街道办事处", "")
                .replace("街道", "")
                .replace("镇", "")
                .replace("乡", "")
                .replace("村", "")
                .replace("区", "")
                .replace("县", "")
                .trim();
    }

    private String addressToken(String value) {
        return StringUtils.defaultString(value).trim();
    }

    private static class LocationContext {
        private String lat;
        private String lng;
        private String officialCity;
        private String referenceStation;
        private String locationName;
        private String addressKeyword;
        private String districtKeyword;
        private String townKeyword;
        private String roadKeyword;
        private List<StationCandidate> nearbyStations = Collections.emptyList();
    }

    private static class StationCandidate {
        private String name;
        private String kind;
        private boolean queryable;
        private BigDecimal distanceKm;
    }
}

package com.sqx.app.base.map.strategy;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.base.map.domain.bo.MapDirectionBo;
import com.sqx.app.base.map.domain.bo.MapPlaceSearchBo;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.domain.vo.GeocodeVo;
import com.sqx.app.base.map.domain.vo.MapLatLngVo;
import com.sqx.app.base.map.domain.vo.MapPoiVo;
import com.sqx.app.base.map.domain.vo.MapRouteStepVo;
import com.sqx.app.base.map.domain.vo.MapRouteVo;
import com.sqx.app.base.map.domain.vo.MapSuggestVo;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/8/11
 * <p>
 * 腾讯地图
 */
@Slf4j
public class TencentMapPlatformStrategy implements IMapPlatformStrategy {

    @Override
    public AddressVo reverseGeocode(String lat, String lng) {
        if (StringUtils.isBlank(lat) || StringUtils.isBlank(lng)) {
            throw new IllegalArgumentException("经纬度不能为空");
        }

        String api = "https://apis.map.qq.com/ws/geocoder/v1/";
        String key = SysConfigUtil.getConfigValue("map_tencent_key");

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("location", lat + "," + lng);
        paramMap.put("coord_type", "5");
        paramMap.put("key", key);

        String resp;
        try {
            resp = OkHttpUtil.doGet(api, paramMap);
            if (StringUtils.isBlank(resp)) {
                throw new BizException("请求腾讯地图失败: empty response");
            }
        } catch (Exception e) {
            throw new BizException("请求腾讯地图失败: " + e.getMessage(), e);
        }

        JSONObject respJson = JSON.parseObject(resp);
        Integer status = respJson.getInteger("status");
        if (!Objects.equals(0, status)) {
            throw new BizException("腾讯地图解析错误: " + respJson.getString("message"));
        }

        JSONObject result = respJson.getJSONObject("result");
        JSONObject addressComponent = result.getJSONObject("address_component");

        String nation = addressComponent.getString("nation");
        String province = addressComponent.getString("province");
        String city = addressComponent.getString("city");
        String district = addressComponent.getString("district");
        String street = addressComponent.getString("street");
        String streetNumber = addressComponent.getString("street_number");

        AddressVo vo = new AddressVo();
        vo.setCountry(nation);
        vo.setProvince(province);
        vo.setCity(city);
        vo.setDistrict(district);
        vo.setTown(resolveTownTitle(result));
        vo.setRoad(street);
        if (StringUtils.isNotBlank(streetNumber)) {
            vo.setRoad(StringUtils.defaultString(street) + streetNumber);
        }
        vo.setAddress(result.getString("address"));

        return vo;
    }

    private String resolveTownTitle(JSONObject result) {
        if (result == null) {
            return null;
        }
        JSONObject addressReference = result.getJSONObject("address_reference");
        if (addressReference == null) {
            return null;
        }
        JSONObject town = addressReference.getJSONObject("town");
        if (town == null) {
            return null;
        }
        return town.getString("title");
    }

    @Override
    public GeocodeVo geocode(String address) {
        if (StringUtils.isBlank(address)) {
            throw new IllegalArgumentException("地址不能为空");
        }

        String api = "https://apis.map.qq.com/ws/geocoder/v1/";
        String key = SysConfigUtil.getConfigValue("map_tencent_key");

        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("address", address);
        paramMap.put("key", key);

        String resp;
        try {
            resp = OkHttpUtil.doGet(api, paramMap);
            if (StringUtils.isBlank(resp)) {
                throw new BizException("请求腾讯地图失败: empty response");
            }
        } catch (Exception e) {
            throw new BizException("请求腾讯地图失败: " + e.getMessage(), e);
        }

        JSONObject respJson = JSON.parseObject(resp);
        Integer status = respJson.getInteger("status");
        if (!Objects.equals(0, status)) {
            throw new BizException("腾讯地图解析错误: " + respJson.getString("message"));
        }

        JSONObject result = respJson.getJSONObject("result");
        JSONObject location = result.getJSONObject("location");
        JSONObject addressComponent = result.getJSONObject("address_components");

        GeocodeVo vo = new GeocodeVo();
        vo.setLatitude(location.getBigDecimal("lat"));
        vo.setLongitude(location.getBigDecimal("lng"));
        vo.setAddress(StringUtils.defaultIfBlank(result.getString("title"), address));
        if (addressComponent != null) {
            vo.setProvince(addressComponent.getString("province"));
            vo.setCity(addressComponent.getString("city"));
            vo.setDistrict(addressComponent.getString("district"));
        }
        return vo;
    }

    @Override
    public List<MapPoiVo> searchPlaces(MapPlaceSearchBo searchBo) {
        if (searchBo == null) {
            throw new IllegalArgumentException("搜索参数不能为空");
        }
        if (StringUtils.isBlank(searchBo.getLat()) || StringUtils.isBlank(searchBo.getLng())) {
            throw new IllegalArgumentException("经纬度不能为空");
        }

        boolean exploreMode = "explore".equalsIgnoreCase(StringUtils.trimToEmpty(searchBo.getMode()));
        if (!exploreMode && StringUtils.isBlank(searchBo.getKeyword()) && StringUtils.isBlank(searchBo.getFilter())) {
            throw new IllegalArgumentException("搜索关键字不能为空");
        }
        if (exploreMode && StringUtils.isBlank(searchBo.getFilter()) && StringUtils.isBlank(searchBo.getKeyword())) {
            throw new IllegalArgumentException("周边探索需要分类或关键字");
        }

        String key = SysConfigUtil.getConfigValue("map_tencent_key");
        if (StringUtils.isBlank(key)) {
            throw new BizException("未配置腾讯地图 Key（map_tencent_key）");
        }

        int radius = searchBo.getRadius() == null ? 1000 : searchBo.getRadius();
        if (radius < 10) {
            radius = 10;
        }
        if (radius > 1000) {
            radius = 1000;
        }
        // 默认不扩范围，避免把远处无关 POI 打到当前视野造成“打点不准”
        int autoExtend = searchBo.getAutoExtend() == null ? 0 : (searchBo.getAutoExtend() == 1 ? 1 : 0);
        int page = searchBo.getPage() == null || searchBo.getPage() < 1 ? 1 : searchBo.getPage();
        int limit = searchBo.getLimit() == null || searchBo.getLimit() < 1 ? 20 : Math.min(searchBo.getLimit(), 20);

        String api = exploreMode
                ? "https://apis.map.qq.com/ws/place/v1/explore"
                : "https://apis.map.qq.com/ws/place/v1/search";
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("boundary", "nearby(" + searchBo.getLat() + "," + searchBo.getLng() + "," + radius + "," + autoExtend + ")");
        paramMap.put("page_size", String.valueOf(limit));
        paramMap.put("page_index", String.valueOf(page));
        paramMap.put("key", key);
        if (!exploreMode) {
            paramMap.put("keyword", StringUtils.defaultIfBlank(searchBo.getKeyword(), "地点").trim());
            paramMap.put("orderby", "_distance");
        }
        if (StringUtils.isNotBlank(searchBo.getFilter())) {
            paramMap.put("filter", searchBo.getFilter().trim());
        }
        if (exploreMode && StringUtils.isNotBlank(searchBo.getKeyword())) {
            paramMap.put("keyword", searchBo.getKeyword().trim());
        }

        String resp;
        try {
            resp = OkHttpUtil.doGet(api, paramMap);
            if (StringUtils.isBlank(resp)) {
                throw new BizException("请求腾讯地图地点搜索失败: empty response");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("请求腾讯地图地点搜索失败: " + e.getMessage(), e);
        }

        JSONObject respJson = JSON.parseObject(resp);
        Integer status = respJson.getInteger("status");
        if (!Objects.equals(0, status)) {
            throw new BizException("腾讯地图地点搜索错误: " + respJson.getString("message"));
        }

        JSONArray data = respJson.getJSONArray("data");
        List<MapPoiVo> result = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return result;
        }

        for (int i = 0; i < data.size(); i++) {
            JSONObject item = data.getJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONObject location = item.getJSONObject("location");
            if (location == null) {
                continue;
            }
            String title = item.getString("title");
            String category = StringUtils.defaultIfBlank(item.getString("category"),
                    StringUtils.defaultIfBlank(searchBo.getKeyword(), "地点"));
            // 服务端兜底：按关键词意图过滤，避免小区/行政地名混入停车场/美食结果
            if (!matchPlaceIntent(searchBo.getKeyword(), searchBo.getFilter(), title, category)) {
                continue;
            }
            MapPoiVo vo = new MapPoiVo();
            vo.setId(StringUtils.defaultIfBlank(item.getString("id"), "tencent-" + i));
            vo.setName(title);
            vo.setAddress(item.getString("address"));
            vo.setLatitude(location.getBigDecimal("lat"));
            vo.setLongitude(location.getBigDecimal("lng"));
            vo.setTag(category);
            vo.setSourceType("tencent");
            BigDecimal distanceMeters = item.getBigDecimal("_distance");
            if (distanceMeters != null) {
                vo.setDistance(distanceMeters.divide(BigDecimal.valueOf(1000), 2, RoundingMode.HALF_UP));
            }
            result.add(vo);
        }
        return result;
    }

    /**
     * 停车场/美食等场景：名称或分类必须命中意图，排除住宅小区/行政地名。
     */
    private boolean matchPlaceIntent(String keyword, String filter, String title, String category) {
        String intent = StringUtils.defaultString(keyword) + " " + StringUtils.defaultString(filter);
        String text = StringUtils.defaultString(title) + " " + StringUtils.defaultString(category);
        if (intent.contains("停车")) {
            if (category != null && (category.contains("住宅") || category.contains("地名") || category.contains("行政区"))) {
                return false;
            }
            return text.contains("停车") || text.contains("车库") || text.contains("车位");
        }
        if (intent.contains("美食")) {
            if (category != null && (category.contains("住宅") || category.contains("地名") || category.contains("行政区") || category.contains("停车"))) {
                return false;
            }
            return category != null && (category.contains("美食") || category.contains("餐") || category.contains("小吃")
                    || category.contains("咖啡") || category.contains("茶") || category.contains("烧烤"));
        }
        return true;
    }

    @Override
    public List<MapSuggestVo> suggestPlaces(String keyword, String lat, String lng) {
        if (StringUtils.isBlank(keyword)) {
            throw new IllegalArgumentException("搜索关键字不能为空");
        }
        String key = requireTencentKey();
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("keyword", keyword.trim());
        paramMap.put("key", key);
        paramMap.put("page_size", "10");
        paramMap.put("page_index", "1");
        if (StringUtils.isNotBlank(lat) && StringUtils.isNotBlank(lng)) {
            paramMap.put("location", lat + "," + lng);
        }
        JSONObject respJson = requestTencentApi("https://apis.map.qq.com/ws/place/v1/suggestion", paramMap, "地址联想");
        JSONArray data = respJson.getJSONArray("data");
        List<MapSuggestVo> result = new ArrayList<>();
        if (data == null || data.isEmpty()) {
            return result;
        }
        for (int i = 0; i < data.size(); i++) {
            JSONObject item = data.getJSONObject(i);
            if (item == null) {
                continue;
            }
            JSONObject location = item.getJSONObject("location");
            if (location == null) {
                continue;
            }
            MapSuggestVo vo = new MapSuggestVo();
            vo.setId(StringUtils.defaultIfBlank(item.getString("id"), "suggest-" + i));
            vo.setName(item.getString("title"));
            String address = StringUtils.defaultIfBlank(item.getString("address"), item.getString("district"));
            vo.setAddress(address);
            vo.setLatitude(location.getBigDecimal("lat"));
            vo.setLongitude(location.getBigDecimal("lng"));
            vo.setTag(item.getString("category"));
            result.add(vo);
        }
        return result;
    }

    @Override
    public List<MapRouteVo> planRoutes(MapDirectionBo directionBo) {
        if (directionBo == null) {
            throw new IllegalArgumentException("路线参数不能为空");
        }
        if (StringUtils.isAnyBlank(directionBo.getFromLat(), directionBo.getFromLng(),
                directionBo.getToLat(), directionBo.getToLng())) {
            throw new IllegalArgumentException("起终点经纬度不能为空");
        }
        String key = requireTencentKey();
        String mode = StringUtils.defaultIfBlank(directionBo.getMode(), "driving").trim().toLowerCase(Locale.ROOT);
        if (!"driving".equals(mode) && !"walking".equals(mode)
                && !"bicycling".equals(mode) && !"transit".equals(mode)) {
            mode = "driving";
        }
        boolean multi = directionBo.getMultiPlan() == null || Boolean.TRUE.equals(directionBo.getMultiPlan());
        String api = "https://apis.map.qq.com/ws/direction/v1/" + mode + "/";
        Map<String, String> baseParams = new HashMap<>();
        baseParams.put("from", directionBo.getFromLat() + "," + directionBo.getFromLng());
        baseParams.put("to", directionBo.getToLat() + "," + directionBo.getToLng());
        baseParams.put("key", key);
        baseParams.put("output", "json");
        if ("driving".equals(mode) && multi) {
            // 驾车多方案（最多 3 条）
            baseParams.put("get_mp", "1");
        }

        // 公交地铁：合并「地铁优先」与「综合推荐」，避免只出纯公交方案
        if ("transit".equals(mode)) {
            List<MapRouteVo> merged = new ArrayList<>();
            merged.addAll(fetchTransitRoutes(api, baseParams, "RECOMMEND,SUBWAY_FIRST"));
            merged.addAll(fetchTransitRoutes(api, baseParams, "RECOMMEND"));
            return dedupeAndSortTransitRoutes(merged, 6);
        }

        JSONObject respJson = requestTencentApi(api, baseParams, "路线规划");
        JSONObject resultObj = respJson.getJSONObject("result");
        List<MapRouteVo> routes = new ArrayList<>();
        if (resultObj == null) {
            return routes;
        }
        JSONArray routeArr = resultObj.getJSONArray("routes");
        if (routeArr == null || routeArr.isEmpty()) {
            return routes;
        }
        for (int i = 0; i < routeArr.size(); i++) {
            JSONObject route = routeArr.getJSONObject(i);
            if (route == null) {
                continue;
            }
            routes.add(parseNormalRoute(route, mode, i));
        }
        return routes;
    }

    private List<MapRouteVo> fetchTransitRoutes(String api, Map<String, String> baseParams, String policy) {
        Map<String, String> paramMap = new HashMap<>(baseParams);
        paramMap.put("policy", policy);
        paramMap.put("price_unit", "1");
        JSONObject respJson = requestTencentApi(api, paramMap, "路线规划");
        JSONObject resultObj = respJson.getJSONObject("result");
        List<MapRouteVo> routes = new ArrayList<>();
        if (resultObj == null) {
            return routes;
        }
        JSONArray routeArr = resultObj.getJSONArray("routes");
        if (routeArr == null || routeArr.isEmpty()) {
            return routes;
        }
        for (int i = 0; i < routeArr.size(); i++) {
            JSONObject route = routeArr.getJSONObject(i);
            if (route == null) {
                continue;
            }
            routes.add(parseTransitRoute(route, i));
        }
        return routes;
    }

    /** 按方案摘要去重，地铁相关方案优先，再按时长排序 */
    private List<MapRouteVo> dedupeAndSortTransitRoutes(List<MapRouteVo> routes, int limit) {
        Map<String, MapRouteVo> unique = new LinkedHashMap<>();
        for (MapRouteVo route : routes) {
            if (route == null) {
                continue;
            }
            String key = StringUtils.defaultIfBlank(route.getSummary(),
                    Objects.toString(route.getDuration(), "") + "|" + Objects.toString(route.getDistance(), ""));
            MapRouteVo existing = unique.get(key);
            if (existing == null || transitScore(route) > transitScore(existing)) {
                unique.put(key, route);
            }
        }
        List<MapRouteVo> sorted = new ArrayList<>(unique.values());
        sorted.sort(Comparator
                .comparingInt((MapRouteVo r) -> -transitScore(r))
                .thenComparing(r -> r.getDuration() == null ? Integer.MAX_VALUE : r.getDuration())
                .thenComparing(r -> r.getWalkDistance() == null ? Integer.MAX_VALUE : r.getWalkDistance()));
        if (sorted.size() > limit) {
            sorted = new ArrayList<>(sorted.subList(0, limit));
        }
        for (int i = 0; i < sorted.size(); i++) {
            sorted.get(i).setIndex(i);
        }
        return sorted;
    }

    private int transitScore(MapRouteVo route) {
        if (route == null || route.getSteps() == null) {
            return 0;
        }
        int score = 0;
        for (MapRouteStepVo step : route.getSteps()) {
            if (step == null || !"TRANSIT".equalsIgnoreCase(step.getMode())) {
                continue;
            }
            String vehicle = StringUtils.defaultString(step.getVehicle()).toUpperCase(Locale.ROOT);
            if ("SUBWAY".equals(vehicle) || "RAIL".equals(vehicle)) {
                score += 2;
            } else {
                score += 1;
            }
        }
        return score;
    }

    private MapRouteVo parseNormalRoute(JSONObject route, String mode, int index) {
        MapRouteVo vo = new MapRouteVo();
        vo.setIndex(index);
        vo.setMode(StringUtils.defaultIfBlank(route.getString("mode"), mode.toUpperCase(Locale.ROOT)));
        JSONArray tags = route.getJSONArray("tags");
        if (tags != null) {
            for (int t = 0; t < tags.size(); t++) {
                vo.getTags().add(tags.getString(t));
            }
        }
        Integer distance = route.getInteger("distance");
        Integer duration = route.getInteger("duration");
        vo.setDistance(distance);
        vo.setDuration(duration);
        vo.setTrafficLightCount(route.getInteger("traffic_light_count"));
        vo.setToll(route.getInteger("toll"));
        vo.setDistanceText(formatDistanceMeters(distance));
        vo.setDurationText(formatDurationMinutes(duration));
        vo.setPoints(decodePolyline(route.getJSONArray("polyline")));
        JSONArray steps = route.getJSONArray("steps");
        if (steps != null) {
            for (int s = 0; s < steps.size(); s++) {
                JSONObject step = steps.getJSONObject(s);
                if (step == null) {
                    continue;
                }
                MapRouteStepVo stepVo = new MapRouteStepVo();
                stepVo.setMode(vo.getMode());
                stepVo.setInstruction(step.getString("instruction"));
                stepVo.setRoadName(step.getString("road_name"));
                stepVo.setDirDesc(step.getString("dir_desc"));
                stepVo.setActDesc(step.getString("act_desc"));
                Integer stepDistance = step.getInteger("distance");
                stepVo.setDistance(stepDistance);
                stepVo.setDistanceText(formatDistanceMeters(stepDistance));
                vo.getSteps().add(stepVo);
            }
        }
        return vo;
    }

    /**
     * 解析公交/地铁换乘方案：steps 为 [步行, 公交, 步行, …]，polyline 分散在各段中需拼接。
     */
    private MapRouteVo parseTransitRoute(JSONObject route, int index) {
        MapRouteVo vo = new MapRouteVo();
        vo.setIndex(index);
        vo.setMode("TRANSIT");
        Integer distance = route.getInteger("distance");
        Integer duration = route.getInteger("duration");
        vo.setDistance(distance);
        vo.setDuration(duration);
        vo.setDistanceText(formatDistanceMeters(distance));
        vo.setDurationText(formatDurationMinutes(duration));

        Integer price = route.getInteger("price");
        if (price != null && price >= 0) {
            vo.setPrice(price);
            vo.setPriceText(formatPriceFen(price));
        }

        List<String> lineNames = new ArrayList<>();
        int walkMeters = 0;
        int transitSegments = 0;
        List<MapLatLngVo> allPoints = new ArrayList<>();

        JSONArray steps = route.getJSONArray("steps");
        if (steps != null) {
            for (int s = 0; s < steps.size(); s++) {
                JSONObject step = steps.getJSONObject(s);
                if (step == null) {
                    continue;
                }
                String stepMode = StringUtils.defaultString(step.getString("mode")).toUpperCase(Locale.ROOT);
                if ("WALKING".equals(stepMode)) {
                    Integer stepDistance = step.getInteger("distance");
                    if (stepDistance != null) {
                        walkMeters += stepDistance;
                    }
                    allPoints.addAll(decodePolyline(step.getJSONArray("polyline")));

                    MapRouteStepVo stepVo = new MapRouteStepVo();
                    stepVo.setMode("WALKING");
                    stepVo.setDistance(stepDistance);
                    stepVo.setDistanceText(formatDistanceMeters(stepDistance));
                    stepVo.setDuration(step.getInteger("duration"));
                    String direction = step.getString("direction");
                    if (StringUtils.isNotBlank(direction)) {
                        stepVo.setInstruction("步行向" + direction
                                + (stepDistance != null ? "约" + formatDistanceMeters(stepDistance) : ""));
                    } else {
                        stepVo.setInstruction("步行"
                                + (stepDistance != null ? formatDistanceMeters(stepDistance) : ""));
                    }
                    vo.getSteps().add(stepVo);
                } else if ("TRANSIT".equals(stepMode)) {
                    JSONArray lines = step.getJSONArray("lines");
                    if (lines == null || lines.isEmpty()) {
                        continue;
                    }
                    JSONObject line = lines.getJSONObject(0);
                    if (line == null) {
                        continue;
                    }
                    transitSegments++;
                    String title = line.getString("title");
                    if (StringUtils.isNotBlank(title)) {
                        lineNames.add(title);
                    }
                    allPoints.addAll(decodePolyline(line.getJSONArray("polyline")));

                    JSONObject geton = line.getJSONObject("geton");
                    JSONObject getoff = line.getJSONObject("getoff");
                    String getOnTitle = geton == null ? null : geton.getString("title");
                    String getOffTitle = getoff == null ? null : getoff.getString("title");
                    Integer stationCount = line.getInteger("station_count");
                    String vehicle = line.getString("vehicle");

                    MapRouteStepVo stepVo = new MapRouteStepVo();
                    stepVo.setMode("TRANSIT");
                    stepVo.setVehicle(vehicle);
                    stepVo.setLineName(title);
                    stepVo.setGetOn(getOnTitle);
                    stepVo.setGetOff(getOffTitle);
                    stepVo.setStationCount(stationCount);
                    stepVo.setDistance(line.getInteger("distance"));
                    stepVo.setDistanceText(formatDistanceMeters(line.getInteger("distance")));
                    stepVo.setDuration(line.getInteger("duration"));
                    stepVo.setRoadName(title);
                    stepVo.setInstruction(buildTransitInstruction(vehicle, title, getOnTitle, getOffTitle, stationCount));
                    vo.getSteps().add(stepVo);
                }
            }
        }

        vo.setWalkDistance(walkMeters);
        vo.setWalkDistanceText(formatDistanceMeters(walkMeters));
        vo.setTransferCount(Math.max(0, transitSegments - 1));
        if (!lineNames.isEmpty()) {
            vo.setSummary(String.join(" → ", lineNames));
            vo.getTags().addAll(lineNames);
        }
        vo.setPoints(allPoints);
        return vo;
    }

    private String buildTransitInstruction(String vehicle, String title, String getOn, String getOff, Integer stationCount) {
        String vehicleLabel = "公交";
        if ("SUBWAY".equalsIgnoreCase(vehicle)) {
            vehicleLabel = "地铁";
        } else if ("RAIL".equalsIgnoreCase(vehicle)) {
            vehicleLabel = "火车";
        } else if ("BUS".equalsIgnoreCase(vehicle)) {
            vehicleLabel = "公交";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("乘坐").append(StringUtils.defaultIfBlank(title, vehicleLabel));
        if (StringUtils.isNotBlank(getOn) && StringUtils.isNotBlank(getOff)) {
            sb.append("，").append(getOn).append("上车 → ").append(getOff).append("下车");
        } else if (StringUtils.isNotBlank(getOn)) {
            sb.append("，").append(getOn).append("上车");
        }
        if (stationCount != null && stationCount > 0) {
            sb.append("（").append(stationCount).append("站）");
        }
        return sb.toString();
    }

    private String formatPriceFen(Integer fen) {
        if (fen == null || fen < 0) {
            return "";
        }
        if (fen % 100 == 0) {
            return "¥" + (fen / 100);
        }
        return "¥" + BigDecimal.valueOf(fen).divide(BigDecimal.valueOf(100), 1, RoundingMode.HALF_UP);
    }

    private String requireTencentKey() {
        String key = SysConfigUtil.getConfigValue("map_tencent_key");
        if (StringUtils.isBlank(key)) {
            throw new BizException("未配置腾讯地图 Key（map_tencent_key）");
        }
        return key;
    }

    private JSONObject requestTencentApi(String api, Map<String, String> paramMap, String action) {
        String resp;
        try {
            resp = OkHttpUtil.doGet(api, paramMap);
            if (StringUtils.isBlank(resp)) {
                throw new BizException("请求腾讯地图" + action + "失败: empty response");
            }
        } catch (BizException e) {
            throw e;
        } catch (Exception e) {
            throw new BizException("请求腾讯地图" + action + "失败: " + e.getMessage(), e);
        }
        JSONObject respJson = JSON.parseObject(resp);
        Integer status = respJson.getInteger("status");
        if (!Objects.equals(0, status)) {
            throw new BizException("腾讯地图" + action + "错误: " + respJson.getString("message"));
        }
        return respJson;
    }

    /**
     * 腾讯 polyline 解压：前两个为起点绝对坐标，其后为相对差值（需 /1e6 累加）
     */
    private List<MapLatLngVo> decodePolyline(JSONArray polyline) {
        List<MapLatLngVo> points = new ArrayList<>();
        if (polyline == null || polyline.size() < 2) {
            return points;
        }
        double[] coors = new double[polyline.size()];
        for (int i = 0; i < polyline.size(); i++) {
            coors[i] = polyline.getDoubleValue(i);
        }
        double kr = 1000000d;
        for (int i = 2; i < coors.length; i++) {
            coors[i] = coors[i - 2] + coors[i] / kr;
        }
        for (int i = 0; i + 1 < coors.length; i += 2) {
            points.add(new MapLatLngVo(
                    BigDecimal.valueOf(coors[i]).setScale(6, RoundingMode.HALF_UP),
                    BigDecimal.valueOf(coors[i + 1]).setScale(6, RoundingMode.HALF_UP)
            ));
        }
        return points;
    }

    private String formatDistanceMeters(Integer meters) {
        if (meters == null) {
            return "";
        }
        if (meters < 1000) {
            return meters + "米";
        }
        return BigDecimal.valueOf(meters).divide(BigDecimal.valueOf(1000), 1, RoundingMode.HALF_UP) + "公里";
    }

    private String formatDurationMinutes(Integer minutes) {
        if (minutes == null) {
            return "";
        }
        if (minutes < 60) {
            return minutes + "分钟";
        }
        int hour = minutes / 60;
        int min = minutes % 60;
        if (min == 0) {
            return hour + "小时";
        }
        return hour + "小时" + min + "分钟";
    }
}

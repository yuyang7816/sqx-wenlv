package com.sqx.app.base.map.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.map.db.service.IMapPoiService;
import com.sqx.app.base.map.domain.bo.MapDirectionBo;
import com.sqx.app.base.map.domain.bo.MapPlaceSearchBo;
import com.sqx.app.base.map.domain.bo.MapPoiListBo;
import com.sqx.app.base.map.domain.entity.MapPoiEntity;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.domain.vo.GeocodeVo;
import com.sqx.app.base.map.domain.vo.MapPickerConfigVo;
import com.sqx.app.base.map.domain.vo.MapPoiVo;
import com.sqx.app.base.map.domain.vo.MapRouteVo;
import com.sqx.app.base.map.domain.vo.MapSuggestVo;
import com.sqx.app.base.map.enums.MapPoiType;
import com.sqx.app.base.map.enums.MapPlatformCodeEnums;
import com.sqx.app.base.map.factory.MapFactory;
import com.sqx.app.base.map.service.MapService;
import com.sqx.app.base.map.strategy.BigDataCloudMapPlatformStrategy;
import com.sqx.app.base.map.strategy.IMapPlatformStrategy;
import com.sqx.app.base.map.strategy.TencentMapPlatformStrategy;
import com.sqx.app.base.map.support.MapDistanceUtil;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import com.sqx.app.lvyou.scenic.enums.ScenicStatus;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.mybatis.utils.PageUtil;
import com.sqx.system.db.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * 地图服务实现
 */
@Slf4j
@Service
public class MapServiceImpl implements MapService {

    private static final int MERCHANT_STATUS_NORMAL = 1;
    private static final int MAP_POI_STATUS_ENABLED = 1;
    private static final String MERCHANT_TYPE_GOOSE = "goose";
    private static final String MERCHANT_TYPE_RESTAURANT = "restaurant";
    private static final String SOURCE_SCENIC = "scenic";
    private static final String SOURCE_MERCHANT = "merchant";
    private static final String SOURCE_MAP_POI = "map_poi";

    @Resource
    private IScenicService iScenicService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMapPoiService iMapPoiService;

    @Resource
    private ISysConfigService sysConfigService;

    @Override
    public AddressVo reverseGeocode(String lat, String lng) {
        validateCoordinates(lat, lng);
        try {
            AddressVo addressVo = invokePlatform(resolveMapPlatformCode(), platform -> platform.reverseGeocode(lat, lng),
                    () -> new BigDataCloudMapPlatformStrategy().reverseGeocode(lat, lng));
            return normalizeAddressVo(addressVo, lat, lng);
        } catch (Exception ex) {
            log.warn("逆地理编码失败，尝试备用平台: lat={}, lng={}, reason={}", lat, lng, ex.getMessage());
            try {
                AddressVo fallback = new BigDataCloudMapPlatformStrategy().reverseGeocode(lat, lng);
                return normalizeAddressVo(fallback, lat, lng);
            } catch (Exception fallbackEx) {
                log.warn("备用逆地理编码失败，返回基础地址: lat={}, lng={}, reason={}", lat, lng, fallbackEx.getMessage());
                return buildCoordinateAddress(lat, lng);
            }
        }
    }

    @Override
    public GeocodeVo geocode(String address) {
        return invokePlatform(resolveMapPlatformCode(), platform -> platform.geocode(address), () -> {
            throw new BizException("当前地图平台不支持地址解析，请直接在地图上选点");
        });
    }

    @Override
    public MapPickerConfigVo pickerConfig() {
        MapPickerConfigVo vo = new MapPickerConfigVo();
        vo.setPlatform(resolveMapPlatformCode());
        vo.setTencentKey(StringUtils.defaultString(getConfigValueOrNull("map_tencent_key")));
        vo.setReferer("lvyou-admin");
        return vo;
    }

    @Override
    public List<MapPoiVo> searchPlaces(MapPlaceSearchBo searchBo) {
        if (searchBo == null) {
            throw new BizException("搜索参数不能为空");
        }
        validateCoordinates(searchBo.getLat(), searchBo.getLng());
        boolean exploreMode = "explore".equalsIgnoreCase(StringUtils.trimToEmpty(searchBo.getMode()));
        if (!exploreMode && StringUtils.isBlank(searchBo.getKeyword()) && StringUtils.isBlank(searchBo.getFilter())) {
            throw new BizException("搜索关键字不能为空");
        }
        return invokePlatform(resolveMapPlatformCode(),
                platform -> platform.searchPlaces(searchBo),
                () -> new TencentMapPlatformStrategy().searchPlaces(searchBo));
    }

    @Override
    public List<MapSuggestVo> suggestPlaces(String keyword, String lat, String lng) {
        if (StringUtils.isBlank(keyword)) {
            throw new BizException("搜索关键字不能为空");
        }
        return invokePlatform(resolveMapPlatformCode(),
                platform -> platform.suggestPlaces(keyword, lat, lng),
                () -> new TencentMapPlatformStrategy().suggestPlaces(keyword, lat, lng));
    }

    @Override
    public List<MapRouteVo> planRoutes(MapDirectionBo directionBo) {
        if (directionBo == null) {
            throw new BizException("路线参数不能为空");
        }
        validateCoordinates(directionBo.getFromLat(), directionBo.getFromLng());
        validateCoordinates(directionBo.getToLat(), directionBo.getToLng());
        return invokePlatform(resolveMapPlatformCode(),
                platform -> platform.planRoutes(directionBo),
                () -> new TencentMapPlatformStrategy().planRoutes(directionBo));
    }

    private <T> T invokePlatform(String mapPlatform, PlatformCall<T> primary, PlatformFallback<T> fallback) {
        MapFactory mapFactory = MapFactory.getInstance();
        IMapPlatformStrategy platform = mapFactory.getPlatform(mapPlatform);
        if (platform == null) {
            throw new BizException("不支持的地图平台: " + mapPlatform);
        }
        try {
            return primary.apply(platform);
        } catch (Exception ex) {
            log.warn("主地图平台 [{}] 调用失败，尝试备用: {}", mapPlatform, ex.getMessage());
            try {
                return fallback.get();
            } catch (Exception fallbackEx) {
                throw new BizException("地图解析失败: " + fallbackEx.getMessage());
            }
        }
    }

    private void validateCoordinates(String lat, String lng) {
        if (StringUtils.isBlank(lat) || StringUtils.isBlank(lng)) {
            throw new BizException("经纬度不能为空");
        }
    }

    private AddressVo normalizeAddressVo(AddressVo addressVo, String lat, String lng) {
        if (addressVo == null) {
            return buildCoordinateAddress(lat, lng);
        }
        if (StringUtils.isBlank(addressVo.getAddress())) {
            addressVo.setAddress(buildFullAddress(addressVo));
        }
        if (StringUtils.isBlank(addressVo.getAddress())) {
            addressVo.setAddress(lat + "," + lng);
        }
        return addressVo;
    }

    private AddressVo buildCoordinateAddress(String lat, String lng) {
        AddressVo vo = new AddressVo();
        vo.setAddress(lat + "," + lng);
        return vo;
    }

    private String buildFullAddress(AddressVo addressVo) {
        StringBuilder builder = new StringBuilder();
        appendAddressPart(builder, addressVo.getProvince());
        appendAddressPart(builder, addressVo.getCity());
        appendAddressPart(builder, addressVo.getDistrict());
        appendAddressPart(builder, addressVo.getTown());
        appendAddressPart(builder, addressVo.getRoad());
        return builder.toString();
    }

    private void appendAddressPart(StringBuilder builder, String part) {
        if (StringUtils.isBlank(part)) {
            return;
        }
        if (builder.length() > 0 && builder.indexOf(part) >= 0) {
            return;
        }
        builder.append(part);
    }

    @FunctionalInterface
    private interface PlatformCall<T> {
        T apply(IMapPlatformStrategy platform);
    }

    @FunctionalInterface
    private interface PlatformFallback<T> {
        T get();
    }

    /**
     * 兼容 map_platform（tencent/tianditu/google）与历史 map_type（1/2/3）配置。
     */
    private String resolveMapPlatformCode() {
        String platform = getConfigValueOrNull("map_platform");
        if (StringUtils.isNotBlank(platform)) {
            return platform.trim();
        }

        String mapType = getConfigValueOrNull("map_type");
        if (StringUtils.isBlank(mapType)) {
            return MapPlatformCodeEnums.TENCENT.getCode();
        }

        switch (mapType.trim()) {
            case "1":
                return MapPlatformCodeEnums.TENCENT.getCode();
            case "2":
                return MapPlatformCodeEnums.TIANDITU.getCode();
            case "3":
                return MapPlatformCodeEnums.GOOGLE.getCode();
            default:
                return mapType.trim();
        }
    }

    private String getConfigValueOrNull(String paramKey) {
        try {
            return sysConfigService.getConfigValue(paramKey);
        } catch (SqxException ex) {
            return null;
        }
    }

    @Override
    public IPage<MapPoiVo> pagePois(MapPoiListBo listBo) {
        MapPoiType typeFilter = MapPoiType.resolve(listBo.getType());
        List<MapPoiVo> pois = new ArrayList<>();

        if (shouldLoadScenic(typeFilter)) {
            pois.addAll(loadScenicPois(listBo.getDestinationId(), listBo.getKeyword()));
        }
        if (shouldLoadFood(typeFilter)) {
            pois.addAll(loadFoodPois(listBo.getDestinationId(), listBo.getKeyword()));
        }
        if (shouldLoadMapPoi(typeFilter, MapPoiType.PARKING)) {
            pois.addAll(loadStoredPois(listBo.getDestinationId(), MapPoiType.PARKING, listBo.getKeyword()));
        }
        if (shouldLoadMapPoi(typeFilter, MapPoiType.TRANSPORT)) {
            pois.addAll(loadStoredPois(listBo.getDestinationId(), MapPoiType.TRANSPORT, listBo.getKeyword()));
        }

        BigDecimal refLat = MapDistanceUtil.parseCoordinate(listBo.getLat());
        BigDecimal refLng = MapDistanceUtil.parseCoordinate(listBo.getLng());
        if (refLat != null && refLng != null) {
            for (MapPoiVo poi : pois) {
                poi.setDistance(MapDistanceUtil.distanceKm(refLat, refLng, poi.getLatitude(), poi.getLongitude()));
            }
            pois.sort(Comparator.comparing(MapPoiVo::getDistance, Comparator.nullsLast(BigDecimal::compareTo)));
        }

        Page<MapPoiVo> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        return PageUtil.manualPage(page, pois);
    }

    private boolean shouldLoadScenic(MapPoiType typeFilter) {
        return typeFilter == null || typeFilter == MapPoiType.SCENIC;
    }

    private boolean shouldLoadFood(MapPoiType typeFilter) {
        return typeFilter == null || typeFilter == MapPoiType.FOOD;
    }

    private boolean shouldLoadMapPoi(MapPoiType typeFilter, MapPoiType poiType) {
        return typeFilter == null || typeFilter == poiType;
    }

    private List<MapPoiVo> loadScenicPois(Long destinationId, String keyword) {
        LambdaQueryWrapper<ScenicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicEntity::getStatus, ScenicStatus.ONLINE.getCode())
                .eq(Objects.nonNull(destinationId), ScenicEntity::getDestinationId, destinationId)
                .isNotNull(ScenicEntity::getLatitude)
                .isNotNull(ScenicEntity::getLongitude)
                .like(StringUtils.isNotBlank(keyword), ScenicEntity::getName, keyword)
                .orderByAsc(ScenicEntity::getSort)
                .orderByDesc(ScenicEntity::getId);

        List<ScenicEntity> records = iScenicService.list(queryWrapper);
        List<MapPoiVo> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        for (ScenicEntity entity : records) {
            MapPoiVo vo = new MapPoiVo();
            vo.setId(buildPoiId(MapPoiType.SCENIC, entity.getId()));
            vo.setType(MapPoiType.SCENIC.getCode());
            vo.setName(entity.getName());
            vo.setAddress(entity.getAddress());
            vo.setLatitude(entity.getLatitude());
            vo.setLongitude(entity.getLongitude());
            vo.setTag(StringUtils.defaultIfBlank(entity.getLevelLabel(), entity.getTag()));
            vo.setSourceType(SOURCE_SCENIC);
            vo.setSourceId(entity.getId());
            vo.setCoverImage(entity.getCoverImage());
            result.add(vo);
        }
        return result;
    }

    private List<MapPoiVo> loadFoodPois(Long destinationId, String keyword) {
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .in(MerchantEntity::getStoreType, MERCHANT_TYPE_GOOSE, MERCHANT_TYPE_RESTAURANT)
                .eq(Objects.nonNull(destinationId), MerchantEntity::getDestinationId, destinationId)
                .isNotNull(MerchantEntity::getLatitude)
                .isNotNull(MerchantEntity::getLongitude)
                .like(StringUtils.isNotBlank(keyword), MerchantEntity::getStoreName, keyword)
                .orderByDesc(MerchantEntity::getId);

        List<MerchantEntity> records = iMerchantService.list(queryWrapper);
        List<MapPoiVo> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        for (MerchantEntity entity : records) {
            MapPoiVo vo = new MapPoiVo();
            vo.setId(buildPoiId(MapPoiType.FOOD, entity.getId()));
            vo.setType(MapPoiType.FOOD.getCode());
            vo.setName(entity.getStoreName());
            vo.setAddress(entity.getStoreAddress());
            vo.setLatitude(entity.getLatitude());
            vo.setLongitude(entity.getLongitude());
            vo.setTag("美食");
            vo.setSourceType(SOURCE_MERCHANT);
            vo.setSourceId(entity.getId());
            result.add(vo);
        }
        return result;
    }

    private List<MapPoiVo> loadStoredPois(Long destinationId, MapPoiType poiType, String keyword) {
        LambdaQueryWrapper<MapPoiEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MapPoiEntity::getStatus, MAP_POI_STATUS_ENABLED)
                .eq(MapPoiEntity::getPoiType, poiType.getCode())
                .eq(Objects.nonNull(destinationId), MapPoiEntity::getDestinationId, destinationId)
                .like(StringUtils.isNotBlank(keyword), MapPoiEntity::getName, keyword)
                .orderByAsc(MapPoiEntity::getSort)
                .orderByDesc(MapPoiEntity::getId);

        List<MapPoiEntity> records = iMapPoiService.list(queryWrapper);
        List<MapPoiVo> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(records)) {
            return result;
        }
        for (MapPoiEntity entity : records) {
            MapPoiVo vo = new MapPoiVo();
            vo.setId(buildPoiId(poiType, entity.getId()));
            vo.setType(poiType.getCode());
            vo.setName(entity.getName());
            vo.setAddress(entity.getAddress());
            vo.setLatitude(entity.getLatitude());
            vo.setLongitude(entity.getLongitude());
            vo.setTag(entity.getTag());
            vo.setSourceType(SOURCE_MAP_POI);
            vo.setSourceId(entity.getId());
            vo.setSpaces(entity.getSpaces());
            vo.setTotalSpaces(entity.getTotalSpaces());
            vo.setFeeDesc(entity.getFeeDesc());
            result.add(vo);
        }
        return result;
    }

    private String buildPoiId(MapPoiType type, Long sourceId) {
        return type.getCode() + "-" + sourceId;
    }
}

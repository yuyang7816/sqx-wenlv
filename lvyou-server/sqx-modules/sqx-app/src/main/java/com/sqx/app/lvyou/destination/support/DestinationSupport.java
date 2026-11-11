package com.sqx.app.lvyou.destination.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.service.MapService;
import com.sqx.app.base.map.support.MapDistanceUtil;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.enums.DestinationStatus;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import com.sqx.app.lvyou.scenic.enums.ScenicStatus;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 目的地参数解析：根据经纬度匹配在线目的地，兼容历史 destinationCode 字符串。
 */
@Component
public class DestinationSupport {

    private static final int STATUS_ONLINE = DestinationStatus.ONLINE.getCode();
    private static final int SCENIC_STATUS_ONLINE = ScenicStatus.ONLINE.getCode();
    private static final int MERCHANT_STATUS_NORMAL = 1;
    /** 逆地理失败时，允许用最近 POI 推断目的地的最大距离（公里） */
    private static final double NEARBY_POI_MAX_KM = 80D;
    /** 用户指定了城市但没有对应目的地。不能返回 null，否则列表会当成不限城市。 */
    private static final long UNMATCHED_DESTINATION_ID = -1L;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private MapService mapService;

    @Resource
    private IScenicService iScenicService;

    @Resource
    private IMerchantService iMerchantService;

    /**
     * C 端列表/首页：必须提供有效经纬度，并按逆地理编码匹配在线目的地。
     */
    public Long resolveAppDestinationId(String lat, String lng) {
        if (!isValidCoordinate(lat, lng)) {
            throw new BizException(AppError.DESTINATION_LOCATION_REQUIRED);
        }
        Long matched = matchDestinationByCoordinates(lat.trim(), lng.trim());
        if (matched == null) {
            throw new BizException(AppError.DESTINATION_LOCATION_UNMATCHED);
        }
        return matched;
    }

    /**
     * C 端线路等「按市」列表：解析同市范围内全部在线目的地（含市内各区县目的地），不是单个区。
     * 直辖市 city 为空时以 province 作为市级范围。
     */
    public List<Long> resolveAppCityDestinationIds(String lat, String lng) {
        if (!isValidCoordinate(lat, lng)) {
            throw new BizException(AppError.DESTINATION_LOCATION_REQUIRED);
        }
        AddressVo address = mapService.reverseGeocode(lat.trim(), lng.trim());
        if (address == null) {
            throw new BizException(AppError.DESTINATION_LOCATION_UNMATCHED);
        }
        String scopeKey = resolveCityScopeKey(address);
        if (StringUtils.isBlank(scopeKey)) {
            throw new BizException(AppError.DESTINATION_LOCATION_UNMATCHED);
        }

        List<DestinationEntity> destinations = listOnlineDestinations();
        if (CollectionUtils.isEmpty(destinations)) {
            throw new BizException(AppError.DESTINATION_LOCATION_UNMATCHED);
        }

        Set<Long> ids = new LinkedHashSet<>();
        Set<Long> onlineIds = new LinkedHashSet<>();
        List<String> scopeTokens = Collections.singletonList(scopeKey);
        for (DestinationEntity destination : destinations) {
            onlineIds.add(destination.getId());
            if (matchesDestination(destination, scopeTokens)) {
                ids.add(destination.getId());
            }
        }
        addDestinationIdsFromMerchantsInCityScope(ids, onlineIds, scopeKey);
        addDestinationIdsFromScenicsInCityScope(ids, onlineIds, scopeKey);

        // 仍无命中时退回单点匹配，避免无商户/景区样本时完全空窗
        if (ids.isEmpty()) {
            Long fallback = matchDestinationByCoordinates(lat.trim(), lng.trim());
            if (fallback != null) {
                ids.add(fallback);
            }
        }
        if (ids.isEmpty()) {
            throw new BizException(AppError.DESTINATION_LOCATION_UNMATCHED);
        }
        return new ArrayList<>(ids);
    }

    /**
     * C 端首页模块/公告等：尽量解析目的地；无法解析时返回 null，仍展示「适用目的地为空」的全平台配置。
     */
    public Long tryResolveAppDestinationId(String lat, String lng) {
        if (!isValidCoordinate(lat, lng)) {
            return null;
        }
        return matchDestinationByCoordinates(lat.trim(), lng.trim());
    }

    /**
     * 管理端/C 端发布：根据经纬度匹配在线目的地，无法匹配时抛错。
     */
    public Long resolvePublishDestination(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            throw new BizException(AppError.DESTINATION_LOCATION_REQUIRED);
        }
        Long matched = matchDestinationByCoordinates(toCoordinateString(latitude), toCoordinateString(longitude));
        if (matched == null) {
            throw new BizException(AppError.DESTINATION_LOCATION_UNMATCHED);
        }
        return matched;
    }

    /**
     * 管理端发布（可选目的地）：能解析则绑定，否则返回 null（全平台）。
     */
    public Long tryResolvePublishDestination(BigDecimal latitude, BigDecimal longitude) {
        if (latitude == null || longitude == null) {
            return null;
        }
        return matchDestinationByCoordinates(toCoordinateString(latitude), toCoordinateString(longitude));
    }

    /**
     * @param destinationKey 数字主键或 destination_code（历史兼容，新客户端勿传）
     * @return 数据库主键 id；无法识别时返回 null
     */
    public Long resolveDestinationId(String destinationKey) {
        if (StringUtils.isBlank(destinationKey)) {
            return null;
        }
        String trimmed = destinationKey.trim();
        if (NumberUtils.isCreatable(trimmed)) {
            DestinationEntity byId = iDestinationService.getById(NumberUtils.createLong(trimmed));
            if (byId != null) {
                return byId.getId();
            }
        }
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getDestinationCode, trimmed);
        DestinationEntity byCode = iDestinationService.getOne(queryWrapper, false);
        return byCode == null ? null : byCode.getId();
    }

    /**
     * 首页浏览城市：手动选择优先。指定了城市但没有对应目的地时，不再退回定位城市。
     */
    public Long resolveBrowseDestinationId(String destinationKey, String lat, String lng) {
        if (StringUtils.isNotBlank(destinationKey)) {
            Long selected = resolveOnlineDestinationId(destinationKey);
            if (selected != null) {
                return selected;
            }
            Long named = matchDestinationIdByPlaceName(destinationKey);
            if (named != null) {
                return named;
            }
            return UNMATCHED_DESTINATION_ID;
        }
        return tryResolveAppDestinationId(lat, lng);
    }

    /**
     * 按省市区名称匹配目的地，不要求已上线。同名时优先已上线记录。
     */
    private Long matchDestinationIdByPlaceName(String destinationKey) {
        if (StringUtils.isBlank(destinationKey)) {
            return null;
        }
        List<String> tokens = new ArrayList<>();
        for (String part : destinationKey.trim().split("\\s+")) {
            addLocationToken(tokens, part);
        }
        if (tokens.isEmpty()) {
            return null;
        }
        List<DestinationEntity> destinations = iDestinationService.list();
        if (CollectionUtils.isEmpty(destinations)) {
            return null;
        }
        Long fallbackId = null;
        for (DestinationEntity destination : destinations) {
            if (!matchesSelectedCity(destination, tokens)) {
                continue;
            }
            if (Objects.equals(destination.getStatus(), STATUS_ONLINE)) {
                return destination.getId();
            }
            if (fallbackId == null) {
                fallbackId = destination.getId();
            }
        }
        return fallbackId;
    }

    /** 手动选城只按城市名完整包含匹配，避免「阿里」这类名称被短词或定位结果带成西安。 */
    private boolean matchesSelectedCity(DestinationEntity destination, List<String> tokens) {
        String name = normalizePlaceName(destination.getName());
        String shortName = normalizePlaceName(destination.getShortName());
        String code = StringUtils.lowerCase(StringUtils.trimToEmpty(destination.getDestinationCode()));
        for (String token : tokens) {
            if (StringUtils.isBlank(token)) {
                continue;
            }
            if (token.equals(name) || token.equals(shortName) || token.equalsIgnoreCase(code)) {
                return true;
            }
            if (token.length() < 2) {
                continue;
            }
            if (StringUtils.isNotBlank(name) && name.contains(token)) {
                return true;
            }
            if (StringUtils.isNotBlank(shortName) && shortName.contains(token)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @param destinationKey 数字主键或 destination_code
     * @return 已上线目的地主键；未上线或不存在时返回 null
     */
    public Long resolveOnlineDestinationId(String destinationKey) {
        Long id = resolveDestinationId(destinationKey);
        if (id == null) {
            return null;
        }
        DestinationEntity destination = iDestinationService.getById(id);
        if (destination == null || !Objects.equals(destination.getStatus(), STATUS_ONLINE)) {
            return null;
        }
        return destination.getId();
    }

    private Long matchDestinationByCoordinates(String lat, String lng) {
        List<DestinationEntity> destinations = listOnlineDestinations();
        if (CollectionUtils.isEmpty(destinations)) {
            return null;
        }

        AddressVo address = mapService.reverseGeocode(lat, lng);
        if (address != null) {
            Long matched = matchDestinationByAddress(address, destinations);
            if (matched != null) {
                return matched;
            }
        }

        return matchDestinationByNearestPoi(lat, lng, destinations);
    }

    private Long matchDestinationByAddress(AddressVo address, List<DestinationEntity> destinations) {
        List<String> tokens = extractLocationTokens(address);
        for (DestinationEntity destination : destinations) {
            if (matchesDestination(destination, tokens)) {
                return destination.getId();
            }
        }
        if (StringUtils.isNotBlank(address.getAddress())) {
            for (DestinationEntity destination : destinations) {
                if (matchesDestinationInText(destination, address.getAddress())) {
                    return destination.getId();
                }
            }
        }
        return null;
    }

    /** 逆地理不可用时，根据最近上架景区/商户坐标推断目的地 */
    private Long matchDestinationByNearestPoi(String lat, String lng, List<DestinationEntity> destinations) {
        BigDecimal refLat = MapDistanceUtil.parseCoordinate(lat);
        BigDecimal refLng = MapDistanceUtil.parseCoordinate(lng);
        if (refLat == null || refLng == null) {
            return null;
        }

        Long bestDestinationId = null;
        double bestDistanceKm = Double.MAX_VALUE;

        LambdaQueryWrapper<ScenicEntity> scenicQuery = new LambdaQueryWrapper<>();
        scenicQuery.eq(ScenicEntity::getStatus, SCENIC_STATUS_ONLINE)
                .isNotNull(ScenicEntity::getDestinationId)
                .isNotNull(ScenicEntity::getLatitude)
                .isNotNull(ScenicEntity::getLongitude);
        for (ScenicEntity scenic : iScenicService.list(scenicQuery)) {
            if (!isOnlineDestination(destinations, scenic.getDestinationId())) {
                continue;
            }
            double distanceKm = distanceKm(refLat, refLng, scenic.getLatitude(), scenic.getLongitude());
            if (distanceKm < bestDistanceKm) {
                bestDistanceKm = distanceKm;
                bestDestinationId = scenic.getDestinationId();
            }
        }

        LambdaQueryWrapper<MerchantEntity> merchantQuery = new LambdaQueryWrapper<>();
        merchantQuery.eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .isNotNull(MerchantEntity::getDestinationId)
                .isNotNull(MerchantEntity::getLatitude)
                .isNotNull(MerchantEntity::getLongitude);
        for (MerchantEntity merchant : iMerchantService.list(merchantQuery)) {
            if (!isOnlineDestination(destinations, merchant.getDestinationId())) {
                continue;
            }
            double distanceKm = distanceKm(refLat, refLng, merchant.getLatitude(), merchant.getLongitude());
            if (distanceKm < bestDistanceKm) {
                bestDistanceKm = distanceKm;
                bestDestinationId = merchant.getDestinationId();
            }
        }

        if (bestDestinationId == null || bestDistanceKm > NEARBY_POI_MAX_KM) {
            return null;
        }
        return bestDestinationId;
    }

    private boolean isOnlineDestination(List<DestinationEntity> destinations, Long destinationId) {
        if (destinationId == null) {
            return false;
        }
        for (DestinationEntity destination : destinations) {
            if (Objects.equals(destination.getId(), destinationId)) {
                return true;
            }
        }
        return false;
    }

    private double distanceKm(BigDecimal refLat, BigDecimal refLng, BigDecimal lat, BigDecimal lng) {
        BigDecimal distance = MapDistanceUtil.distanceKm(refLat, refLng, lat, lng);
        return distance == null ? Double.MAX_VALUE : distance.doubleValue();
    }

    private List<DestinationEntity> listOnlineDestinations() {
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getStatus, STATUS_ONLINE)
                .orderByAsc(DestinationEntity::getSort)
                .orderByAsc(DestinationEntity::getId);
        return iDestinationService.list(queryWrapper);
    }

    /**
     * 市级范围键：优先 city；直辖市常无 city 或 city=province，改用 province。
     * 不用 district，避免线路等列表被收窄到单个区县。
     */
    private String resolveCityScopeKey(AddressVo address) {
        if (address == null) {
            return "";
        }
        String city = normalizePlaceName(address.getCity());
        String province = normalizePlaceName(address.getProvince());
        if (StringUtils.isNotBlank(city) && !Objects.equals(city, province)) {
            return city;
        }
        if (StringUtils.isNotBlank(province)) {
            return province;
        }
        return city;
    }

    private void addDestinationIdsFromMerchantsInCityScope(Set<Long> ids, Set<Long> onlineIds, String scopeKey) {
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .isNotNull(MerchantEntity::getDestinationId);
        for (MerchantEntity merchant : iMerchantService.list(queryWrapper)) {
            Long destinationId = merchant.getDestinationId();
            if (destinationId == null || !onlineIds.contains(destinationId)) {
                continue;
            }
            if (textInCityScope(joinPlaceText(merchant.getStoreRegion(), merchant.getStoreAddress()), scopeKey)) {
                ids.add(destinationId);
            }
        }
    }

    private void addDestinationIdsFromScenicsInCityScope(Set<Long> ids, Set<Long> onlineIds, String scopeKey) {
        LambdaQueryWrapper<ScenicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicEntity::getStatus, SCENIC_STATUS_ONLINE)
                .isNotNull(ScenicEntity::getDestinationId);
        for (ScenicEntity scenic : iScenicService.list(queryWrapper)) {
            Long destinationId = scenic.getDestinationId();
            if (destinationId == null || !onlineIds.contains(destinationId)) {
                continue;
            }
            if (textInCityScope(scenic.getAddress(), scopeKey)) {
                ids.add(destinationId);
            }
        }
    }

    private String joinPlaceText(String left, String right) {
        return StringUtils.trimToEmpty(left) + StringUtils.trimToEmpty(right);
    }

    private boolean textInCityScope(String text, String scopeKey) {
        if (StringUtils.isBlank(text) || StringUtils.isBlank(scopeKey)) {
            return false;
        }
        if (text.contains(scopeKey)) {
            return true;
        }
        String normalized = normalizePlaceName(text);
        return StringUtils.isNotBlank(normalized) && normalized.contains(scopeKey);
    }

    /** 单点匹配：按市级目的地优先；city 为空时补充区县，兼容部分地图平台 */
    private List<String> extractLocationTokens(AddressVo address) {
        List<String> tokens = new ArrayList<>();
        addLocationToken(tokens, address.getCity());
        if (tokens.isEmpty()) {
            addLocationToken(tokens, address.getDistrict());
        }
        addLocationToken(tokens, address.getProvince());
        return tokens;
    }

    private void addLocationToken(List<String> tokens, String raw) {
        String normalized = normalizePlaceName(raw);
        if (StringUtils.isBlank(normalized) || tokens.contains(normalized)) {
            return;
        }
        tokens.add(normalized);
    }

    private String normalizePlaceName(String raw) {
        if (StringUtils.isBlank(raw)) {
            return "";
        }
        return raw.trim()
                .replace("自治区", "")
                .replace("特别行政区", "")
                .replace("省", "")
                .replace("市", "")
                .replace("区", "")
                .replace("县", "");
    }

    private boolean matchesDestinationInText(DestinationEntity destination, String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        String normalizedText = text.trim();
        String name = StringUtils.trimToEmpty(destination.getName());
        String shortName = StringUtils.trimToEmpty(destination.getShortName());
        if (StringUtils.isNotBlank(name) && normalizedText.contains(name)) {
            return true;
        }
        if (StringUtils.isNotBlank(shortName) && normalizedText.contains(shortName)) {
            return true;
        }
        String normalizedName = normalizePlaceName(name);
        String normalizedShortName = normalizePlaceName(shortName);
        return (StringUtils.isNotBlank(normalizedName) && normalizedText.contains(normalizedName))
                || (StringUtils.isNotBlank(normalizedShortName) && normalizedText.contains(normalizedShortName));
    }

    private boolean matchesDestination(DestinationEntity destination, List<String> tokens) {
        String name = normalizePlaceName(destination.getName());
        String shortName = normalizePlaceName(destination.getShortName());
        String code = StringUtils.lowerCase(StringUtils.trimToEmpty(destination.getDestinationCode()));
        for (String token : tokens) {
            if (StringUtils.isBlank(token)) {
                continue;
            }
            if (StringUtils.isNotBlank(name) && (name.contains(token) || token.contains(name))) {
                return true;
            }
            if (StringUtils.isNotBlank(shortName) && (shortName.contains(token) || token.contains(shortName))) {
                return true;
            }
            if (StringUtils.isNotBlank(code) && code.equals(StringUtils.lowerCase(token))) {
                return true;
            }
        }
        return false;
    }

    private String toCoordinateString(BigDecimal value) {
        return value.stripTrailingZeros().toPlainString();
    }

    private boolean isValidCoordinate(String lat, String lng) {
        if (StringUtils.isBlank(lat) || StringUtils.isBlank(lng)) {
            return false;
        }
        if (!NumberUtils.isCreatable(lat.trim()) || !NumberUtils.isCreatable(lng.trim())) {
            return false;
        }
        double latValue = NumberUtils.createDouble(lat.trim());
        double lngValue = NumberUtils.createDouble(lng.trim());
        return latValue >= -90 && latValue <= 90 && lngValue >= -180 && lngValue <= 180;
    }
}

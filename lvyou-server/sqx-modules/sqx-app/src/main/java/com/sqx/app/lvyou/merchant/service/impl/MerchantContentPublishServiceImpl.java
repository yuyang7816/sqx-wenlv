package com.sqx.app.lvyou.merchant.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.lvyou.goose.support.GooseStoreTypeSupport;
import com.sqx.app.lvyou.heritage.goods.db.service.IGoodsService;
import com.sqx.app.lvyou.heritage.goods.domain.entity.GoodsEntity;
import com.sqx.app.lvyou.heritage.craft.db.service.IHeritageCraftService;
import com.sqx.app.lvyou.heritage.craft.domain.entity.HeritageCraftEntity;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import com.sqx.app.lvyou.heritage.shop.support.HeritageShopCategorySupport;
import com.sqx.app.lvyou.heritage.util.HeritageJsonUtil;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomService;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomStandardService;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomStandardEntity;
import com.sqx.app.lvyou.hotel.enums.HotelRoomStatus;
import com.sqx.app.lvyou.hotel.util.HotelPolicyUtil;
import com.sqx.app.lvyou.hotel.util.HotelRoomDetailUtil;
import com.sqx.app.lvyou.hotel.util.HotelStarUtil;
import com.sqx.app.lvyou.heritage.goods.domain.bo.GoodsSaveBo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsSpecSkuVo;
import com.sqx.app.lvyou.heritage.goods.util.GoodsJsonUtil;
import com.sqx.app.lvyou.heritage.goods.util.GoodsSpecsUtil;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantStoreDecorService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantStoreDecorEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantServiceStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.merchant.service.MerchantContentPublishService;
import com.sqx.app.lvyou.route.db.service.IRouteItineraryService;
import com.sqx.app.lvyou.route.db.service.IRouteService;
import com.sqx.app.lvyou.route.domain.entity.RouteEntity;
import com.sqx.app.lvyou.route.domain.entity.RouteItineraryEntity;
import com.sqx.app.lvyou.route.util.RouteSpotsUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 商户发布内容同步至用户端展示表
 */
@Service
public class MerchantContentPublishServiceImpl implements MerchantContentPublishService {

    private static final int STATUS_ONLINE = 1;
    private static final int STATUS_OFFLINE = 0;
    private static final Pattern MEDIA_URL_PATTERN = Pattern.compile("https?://[^\\s\"'\\\\\\],]+");

    @Resource
    private IHotelService iHotelService;

    @Resource
    private IHotelRoomService iHotelRoomService;

    @Resource
    private IHotelRoomStandardService iHotelRoomStandardService;

    @Resource
    private IRouteService iRouteService;

    @Resource
    private IRouteItineraryService iRouteItineraryService;

    @Resource
    private IHeritageShopService iHeritageShopService;

    @Resource
    private IHeritageCraftService iHeritageCraftService;

    @Resource
    private IGoodsService iGoodsService;

    @Resource
    private IMerchantServiceItemService iMerchantServiceItemService;

    @Resource
    private IMerchantStoreDecorService iMerchantStoreDecorService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncStoreDecor(MerchantEntity merchant, Map<String, Object> decor) {
        if (merchant == null || decor == null) {
            return;
        }
        String typeKey = MerchantStoreType.normalize(merchant.getStoreType());
        switch (typeKey) {
            case "hotel":
                syncHotelDecor(merchant, decor);
                syncAllServices(merchant);
                break;
            case "travel":
                syncTravelRoutesFromDecor(merchant, decor);
                syncAllServices(merchant);
                break;
            case "culture":
                syncCultureShopDecor(merchant, decor);
                syncAllServices(merchant);
                break;
            default:
                break;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncService(MerchantEntity merchant, MerchantServiceEntity service) {
        if (merchant == null || service == null) {
            return;
        }
        String typeKey = MerchantStoreType.normalize(merchant.getStoreType());
        switch (typeKey) {
            case "hotel":
                syncHotelRoom(merchant, service);
                break;
            case "travel":
                syncTravelRoute(merchant, service);
                break;
            case "culture":
                syncCultureGoods(merchant, service);
                break;
            default:
                break;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeService(MerchantEntity merchant, Long serviceId) {
        if (merchant == null || serviceId == null) {
            return;
        }
        String typeKey = MerchantStoreType.normalize(merchant.getStoreType());
        switch (typeKey) {
            case "hotel":
                removeHotelRoom(serviceId);
                break;
            case "travel":
                removeTravelRoute(serviceId);
                break;
            case "culture":
                removeCultureGoods(serviceId);
                break;
            default:
                break;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncAllServices(MerchantEntity merchant) {
        if (merchant == null) {
            return;
        }
        LambdaQueryWrapper<MerchantServiceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantServiceEntity::getMerchantId, merchant.getId())
                .orderByAsc(MerchantServiceEntity::getSort)
                .orderByAsc(MerchantServiceEntity::getId);
        List<MerchantServiceEntity> services = iMerchantServiceItemService.list(queryWrapper);
        if (CollectionUtils.isEmpty(services)) {
            return;
        }
        for (MerchantServiceEntity service : services) {
            syncService(merchant, service);
        }
    }

    @Override
    public Long resolveUserPreviewId(MerchantEntity merchant) {
        if (merchant == null) {
            return null;
        }
        String typeKey = MerchantStoreType.normalize(merchant.getStoreType());
        switch (typeKey) {
            case "hotel":
                HotelEntity hotel = findHotelByMerchant(merchant.getId());
                return hotel == null ? null : hotel.getId();
            case "travel":
                RouteEntity route = findFirstRouteByMerchant(merchant.getId());
                return route == null ? null : route.getId();
            case "culture":
                HeritageShopEntity shop = findShopByMerchant(merchant.getId());
                return shop == null ? null : shop.getId();
            case "restaurant":
            default:
                if (GooseStoreTypeSupport.isFoodStore(merchant.getStoreType())) {
                    return merchant.getId();
                }
                return merchant.getId();
        }
    }

    private void syncHotelDecor(MerchantEntity merchant, Map<String, Object> decor) {
        HotelEntity hotel = ensureHotel(merchant);
        hotel.setName(merchant.getStoreName());
        hotel.setAddress(merchant.getStoreAddress());
        hotel.setLatitude(merchant.getLatitude());
        hotel.setLongitude(merchant.getLongitude());
        hotel.setPhone(readString(decor, "phone", merchant.getContactPhone()));
        hotel.setStarLevel(readInteger(decor, "starLevel", hotel.getStarLevel() == null ? 4 : hotel.getStarLevel()));
        hotel.setStarLabel(readString(decor, "starLabel", HotelStarUtil.labelOf(hotel.getStarLevel())));
        hotel.setCheckInTime(readString(decor, "checkInTime", hotel.getCheckInTime()));
        hotel.setCheckOutTime(readString(decor, "checkOutTime", hotel.getCheckOutTime()));
        hotel.setIntro(readString(decor, "intro", hotel.getIntro()));
        String cover = readString(decor, "cover", null);
        if (StringUtils.isBlank(cover)) {
            cover = readString(decor, "coverImage", hotel.getCoverImage());
        }
        if (StringUtils.isNotBlank(cover)) {
            hotel.setCoverImage(cover);
        }
        List<String> banners = readStringList(decor, "bannerImages");
        if (CollectionUtils.isNotEmpty(banners)) {
            hotel.setBannerImages(JSON.toJSONString(banners));
        }
        Object facilities = decor.get("facilities");
        if (facilities != null) {
            hotel.setFacilities(JSON.toJSONString(facilities));
        }
        Object hotelPolicy = decor.get("hotelPolicy");
        if (hotelPolicy instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, Object> policyMap = (Map<String, Object>) hotelPolicy;
            hotel.setPolicyJson(HotelPolicyUtil.toJson(policyMap));
        }
        hotel.setStatus(STATUS_ONLINE);
        iHotelService.updateById(hotel);
    }

    private void syncHotelRoom(MerchantEntity merchant, MerchantServiceEntity service) {
        HotelEntity hotel = ensureHotel(merchant);
        HotelRoomEntity room = findRoomByServiceId(service.getId());
        if (room == null) {
            room = new HotelRoomEntity();
            room.setHotelId(hotel.getId());
            room.setMerchantId(merchant.getId());
            room.setMerchantServiceId(service.getId());
            room.setSort(service.getSort() == null ? 0 : service.getSort());
        }
        room.setRoomStandardId(service.getRoomStandardId());
        room.setRoomDetailJson(service.getRoomDetailJson());
        HotelRoomStandardEntity standard = service.getRoomStandardId() == null
                ? null
                : iHotelRoomStandardService.getById(service.getRoomStandardId());
        Map<String, Object> effectiveDetail = HotelRoomDetailUtil.resolveEffectiveDetail(
                standard, service.getRoomDetailJson(), service.getDescription());
        room.setName(service.getName());
        String specSummary = HotelRoomDetailUtil.buildSpecSummary(effectiveDetail);
        room.setSpec(StringUtils.isNotBlank(specSummary) ? specSummary : StringUtils.defaultString(service.getSpec()));
        room.setPrice(service.getPrice() == null ? BigDecimal.ZERO : service.getPrice());
        room.setStock(service.getStock() == null ? 0 : service.getStock());
        List<String> gallery = HotelRoomDetailUtil.parseGallery(effectiveDetail, service.getCoverImage());
        if (StringUtils.isNotBlank(service.getCoverImage())) {
            room.setCoverImage(service.getCoverImage());
        } else if (CollectionUtils.isNotEmpty(gallery)) {
            room.setCoverImage(gallery.get(0));
        } else {
            room.setCoverImage(null);
        }
        room.setStatus(mapServiceStatus(service.getStatus()));
        if (room.getId() == null) {
            iHotelRoomService.save(room);
        } else {
            iHotelRoomService.updateById(room);
        }
        refreshHotelPriceFrom(hotel.getId());
    }

    private void syncTravelRoutesFromDecor(MerchantEntity merchant, Map<String, Object> decor) {
        if (merchant == null || decor == null || decor.isEmpty()) {
            return;
        }
        LambdaQueryWrapper<RouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteEntity::getMerchantId, merchant.getId());
        List<RouteEntity> routes = iRouteService.list(queryWrapper);
        if (CollectionUtils.isEmpty(routes)) {
            return;
        }
        for (RouteEntity route : routes) {
            if (route.getMerchantServiceId() == null) {
                continue;
            }
            MerchantServiceEntity service = iMerchantServiceItemService.getById(route.getMerchantServiceId());
            if (service != null) {
                syncTravelRoute(merchant, service, decor);
            }
        }
    }

    private void syncTravelRoute(MerchantEntity merchant, MerchantServiceEntity service) {
        syncTravelRoute(merchant, service, loadDecorMap(merchant.getId()));
    }

    private void syncTravelRoute(MerchantEntity merchant, MerchantServiceEntity service, Map<String, Object> decor) {
        RouteEntity route = findRouteByServiceId(service.getId());
        if (route == null) {
            route = new RouteEntity();
            route.setMerchantId(merchant.getId());
            route.setMerchantServiceId(service.getId());
            route.setDestinationId(merchant.getDestinationId());
            route.setSort(service.getSort() == null ? 0 : service.getSort());
        }
        route.setName(service.getName());
        route.setDescription(StringUtils.defaultString(service.getDescription()));
        route.setIntro(StringUtils.defaultIfBlank(service.getDescription(),
                readString(decor, "intro", route.getIntro())));
        route.setPriceFrom(service.getPrice() == null ? BigDecimal.ZERO : service.getPrice());
        Integer dayCount = service.getDayCount();
        if (dayCount == null || dayCount <= 0) {
            dayCount = readInteger(decor, "dayCount", null);
        }
        route.setDayCount(dayCount);
        String duration = StringUtils.defaultIfBlank(service.getSpec(),
                readString(decor, "duration", route.getDuration()));
        if (StringUtils.isBlank(duration) && dayCount != null && dayCount > 0) {
            duration = dayCount + "天";
        }
        route.setDuration(duration);
        route.setMeetPoint(StringUtils.defaultIfBlank(service.getMeetPoint(),
                readString(decor, "meetPoint", merchant.getStoreAddress())));
        route.setTravelCity(StringUtils.defaultIfBlank(service.getTravelCity(),
                readString(decor, "travelCity", route.getTravelCity())));
        List<String> serviceSpots = readStringListFromRaw(service.getSpots());
        route.setSpots(CollectionUtils.isNotEmpty(serviceSpots)
                ? RouteSpotsUtil.joinSpots(serviceSpots)
                : RouteSpotsUtil.joinSpots(readStringList(decor, "spots")));
        route.setTags(JSON.toJSONString(readStringList(decor, "tags")));
        String cover = StringUtils.defaultIfBlank(service.getCoverImage(), readString(decor, "cover", ""));
        route.setCoverImage(cover);
        List<String> banners = readStringListFromRaw(service.getBannerImages());
        if (CollectionUtils.isEmpty(banners) && StringUtils.isNotBlank(cover)) {
            banners = Collections.singletonList(cover);
        }
        route.setBannerImages(CollectionUtils.isEmpty(banners) ? null : JSON.toJSONString(banners));
        route.setStatus(mapServiceStatus(service.getStatus()));
        if (route.getId() == null) {
            iRouteService.save(route);
        } else {
            iRouteService.updateById(route);
        }
        List<Map<String, Object>> serviceItinerary = readServiceItinerary(service);
        syncRouteItinerary(route.getId(), CollectionUtils.isNotEmpty(serviceItinerary)
                ? serviceItinerary
                : readItinerary(decor));
    }

    private void syncCultureShopDecor(MerchantEntity merchant, Map<String, Object> decor) {
        HeritageShopEntity shop = ensureCultureShop(merchant);
        shop.setName(merchant.getStoreName());
        shop.setAddress(merchant.getStoreAddress());
        shop.setLatitude(merchant.getLatitude());
        shop.setLongitude(merchant.getLongitude());
        shop.setPhone(StringUtils.defaultIfBlank(merchant.getContactPhone(), shop.getPhone()));
        shop.setBusinessHours(StringUtils.defaultIfBlank(merchant.getStoreHours(), shop.getBusinessHours()));
        shop.setIntro(readString(decor, "intro", shop.getIntro()));
        shop.setIntroDetail(readString(decor, "introDetail", readString(decor, "intro", shop.getIntroDetail())));
        shop.setCoverFrom(readString(decor, "coverFrom", shop.getCoverFrom()));
        shop.setCoverTo(readString(decor, "coverTo", shop.getCoverTo()));
        shop.setCoverImage(readString(decor, "cover", shop.getCoverImage()));
        List<String> bannerImages = readStringList(decor, "bannerImages");
        shop.setBannerImages(CollectionUtils.isEmpty(bannerImages)
                ? null
                : HeritageJsonUtil.toJsonArray(bannerImages));
        Object shipping = decor.get("shipping");
        if (shipping instanceof Map) {
            shop.setShippingConfig(com.sqx.app.lvyou.heritage.shop.support.HeritageShippingUtil.toJson(
                    (Map<String, Object>) shipping));
        }
        List<String> tags = readStringList(decor, "tags");
        if (CollectionUtils.isNotEmpty(tags)) {
            shop.setTag(tags.get(0));
            shop.setTags(HeritageJsonUtil.toJsonArray(tags));
        }
        List<String> services = readStringList(decor, "services");
        if (CollectionUtils.isNotEmpty(services)) {
            shop.setServices(HeritageJsonUtil.toJsonArray(services));
        }
        shop.setStatus(STATUS_ONLINE);
        iHeritageShopService.updateById(shop);
    }

    private void syncCultureGoods(MerchantEntity merchant, MerchantServiceEntity service) {
        HeritageShopEntity shop = ensureCultureShop(merchant);
        GoodsEntity goods = findGoodsByServiceId(service.getId());
        if (goods == null) {
            goods = new GoodsEntity();
            goods.setShopId(shop.getId());
            goods.setMerchantServiceId(service.getId());
            goods.setSort(service.getSort() == null ? 0 : service.getSort());
            goods.setCategory("featured");
        }
        goods.setName(service.getName());
        goods.setDescription(StringUtils.defaultString(service.getDescription()));
        goods.setDetail(StringUtils.defaultIfBlank(service.getDetail(), service.getDescription()));
        GoodsSpecsUtil.GoodsSpecsPayload specsPayload = GoodsSpecsUtil.parseMerchantRaw(
                service.getGoodsSpecs(), service.getPrice(), service.getStock());
        List<GoodsSpecSkuVo> skus = specsPayload.getSkus();
        if (specsPayload.isMulti()) {
            goods.setPrice(GoodsSpecsUtil.minPrice(skus, service.getPrice()));
            goods.setStock(GoodsSpecsUtil.totalStock(skus, service.getStock()));
            goods.setSpecs(GoodsSpecsUtil.skusToJson(skus));
        } else {
            goods.setPrice(service.getPrice() == null ? BigDecimal.ZERO : service.getPrice());
            goods.setStock(service.getStock() == null ? 0 : service.getStock());
            goods.setSpecs("[]");
        }
        goods.setCoverImage(service.getCoverImage());
        List<String> bannerUrls = readStringListFromRaw(service.getBannerImages());
        if (CollectionUtils.isNotEmpty(bannerUrls)) {
            List<GoodsSaveBo.BannerItem> bannerItems = new ArrayList<>();
            for (String url : bannerUrls) {
                if (StringUtils.isBlank(url)) {
                    continue;
                }
                GoodsSaveBo.BannerItem bannerItem = new GoodsSaveBo.BannerItem();
                bannerItem.setImage(url.trim());
                bannerItems.add(bannerItem);
            }
            goods.setBanners(CollectionUtils.isEmpty(bannerItems)
                    ? null
                    : GoodsJsonUtil.bannersToJson(bannerItems));
        } else if (StringUtils.isNotBlank(service.getCoverImage())) {
            goods.setBanners(GoodsJsonUtil.bannersToJson(buildCoverBanner(service.getCoverImage())));
        }
        goods.setDetailImages(GoodsJsonUtil.detailImagesToJson(readStringListFromRaw(service.getDetailImages())));
        goods.setStatus(mapServiceStatus(service.getStatus()));
        if (goods.getId() == null) {
            iGoodsService.save(goods);
        } else {
            iGoodsService.updateById(goods);
        }
    }

    private HotelEntity ensureHotel(MerchantEntity merchant) {
        HotelEntity hotel = findHotelByMerchant(merchant.getId());
        if (hotel != null) {
            return hotel;
        }
        hotel = new HotelEntity();
        hotel.setMerchantId(merchant.getId());
        hotel.setDestinationId(publishDestinationResolver.requireFromCoordinatesOrMerchant(
                merchant.getLatitude(), merchant.getLongitude(), merchant.getId()));
        hotel.setName(merchant.getStoreName());
        hotel.setAddress(merchant.getStoreAddress());
        hotel.setLatitude(merchant.getLatitude());
        hotel.setLongitude(merchant.getLongitude());
        hotel.setPhone(merchant.getContactPhone());
        hotel.setCategory("all");
        hotel.setStarLevel(4);
        hotel.setStatus(STATUS_ONLINE);
        hotel.setSort(0);
        iHotelService.save(hotel);
        return hotel;
    }

    private HeritageShopEntity ensureCultureShop(MerchantEntity merchant) {
        String category = resolveMerchantShopCategory(merchant);
        HeritageShopEntity shop = findShopByMerchant(merchant.getId());
        if (shop != null) {
            if (!StringUtils.equals(category, shop.getCategory())) {
                shop.setCategory(category);
                iHeritageShopService.updateById(shop);
            }
            return shop;
        }
        shop = new HeritageShopEntity();
        shop.setMerchantId(merchant.getId());
        shop.setDestinationId(publishDestinationResolver.requireFromCoordinatesOrMerchant(
                merchant.getLatitude(), merchant.getLongitude(), merchant.getId()));
        shop.setName(merchant.getStoreName());
        shop.setAddress(merchant.getStoreAddress());
        shop.setLatitude(merchant.getLatitude());
        shop.setLongitude(merchant.getLongitude());
        shop.setPhone(merchant.getContactPhone());
        shop.setBusinessHours(merchant.getStoreHours());
        shop.setCategory(category);
        shop.setReviewCount(0);
        shop.setStatus(STATUS_ONLINE);
        shop.setSort(0);
        iHeritageShopService.save(shop);
        return shop;
    }

    private void removeHotelRoom(Long serviceId) {
        HotelRoomEntity room = findRoomByServiceId(serviceId);
        if (room == null) {
            return;
        }
        Long hotelId = room.getHotelId();
        iHotelRoomService.removeById(room.getId());
        if (hotelId != null) {
            refreshHotelPriceFrom(hotelId);
        }
    }

    private void removeTravelRoute(Long serviceId) {
        RouteEntity route = findRouteByServiceId(serviceId);
        if (route == null) {
            return;
        }
        LambdaQueryWrapper<RouteItineraryEntity> removeQuery = new LambdaQueryWrapper<>();
        removeQuery.eq(RouteItineraryEntity::getRouteId, route.getId());
        iRouteItineraryService.remove(removeQuery);
        iRouteService.removeById(route.getId());
    }

    private void removeCultureGoods(Long serviceId) {
        GoodsEntity goods = findGoodsByServiceId(serviceId);
        if (goods == null) {
            return;
        }
        iGoodsService.removeById(goods.getId());
    }

    private void syncRouteItinerary(Long routeId, List<Map<String, Object>> itinerary) {
        LambdaQueryWrapper<RouteItineraryEntity> removeQuery = new LambdaQueryWrapper<>();
        removeQuery.eq(RouteItineraryEntity::getRouteId, routeId);
        iRouteItineraryService.remove(removeQuery);
        if (CollectionUtils.isEmpty(itinerary)) {
            return;
        }
        int sort = 0;
        for (Map<String, Object> node : itinerary) {
            RouteItineraryEntity entity = new RouteItineraryEntity();
            entity.setRouteId(routeId);
            entity.setSort(sort);
            Integer dayNo = readInteger(node, "dayNo", null);
            if (dayNo == null || dayNo <= 0) {
                dayNo = sort + 1;
            }
            entity.setDayNo(dayNo);
            String dayLabel = readString(node, "dayLabel", "");
            if (StringUtils.isBlank(dayLabel)) {
                dayLabel = "第" + dayNo + "天";
            }
            String title = StringUtils.defaultIfBlank(readString(node, "title", ""),
                    readString(node, "name", ""));
            String content = StringUtils.defaultIfBlank(readString(node, "content", ""),
                    readString(node, "desc", ""));
            List<String> images = readStringListFromNode(node, "images");
            String cover = StringUtils.defaultIfBlank(readString(node, "cover", ""),
                    CollectionUtils.isEmpty(images) ? "" : images.get(0));
            String timeLabel = StringUtils.defaultIfBlank(readString(node, "time", ""), dayLabel);
            entity.setTimeLabel(timeLabel);
            entity.setName(title);
            entity.setLocation(readString(node, "location", ""));
            entity.setDescription(content);
            entity.setCoverImage(cover);
            if (CollectionUtils.isNotEmpty(images)) {
                entity.setImagesJson(JSON.toJSONString(images));
            } else if (StringUtils.isNotBlank(cover)) {
                entity.setImagesJson(JSON.toJSONString(Collections.singletonList(cover)));
            }
            iRouteItineraryService.save(entity);
            sort++;
        }
    }

    private List<Map<String, Object>> readServiceItinerary(MerchantServiceEntity service) {
        if (service == null || StringUtils.isBlank(service.getItineraryJson())) {
            return Collections.emptyList();
        }
        try {
            List<Map<String, Object>> list = JSON.parseObject(service.getItineraryJson(),
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            return list == null ? Collections.emptyList() : list;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    @SuppressWarnings("unchecked")
    private List<String> readStringListFromNode(Map<String, Object> node, String key) {
        if (node == null) {
            return Collections.emptyList();
        }
        Object value = node.get(key);
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (item != null && StringUtils.isNotBlank(String.valueOf(item))) {
                    result.add(String.valueOf(item).trim());
                }
            }
            return result;
        }
        if (value instanceof String && StringUtils.isNotBlank((String) value)) {
            return readStringListFromRaw((String) value);
        }
        return Collections.emptyList();
    }

    private void refreshHotelPriceFrom(Long hotelId) {
        LambdaQueryWrapper<HotelRoomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomEntity::getHotelId, hotelId)
                .eq(HotelRoomEntity::getStatus, HotelRoomStatus.ON_SALE.getCode())
                .orderByAsc(HotelRoomEntity::getPrice)
                .last("LIMIT 1");
        HotelRoomEntity cheapest = iHotelRoomService.getOne(queryWrapper, false);
        HotelEntity update = new HotelEntity();
        update.setId(hotelId);
        update.setPriceFrom(cheapest == null ? null : cheapest.getPrice());
        iHotelService.updateById(update);
    }

    private HotelEntity findHotelByMerchant(Long merchantId) {
        LambdaQueryWrapper<HotelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelEntity::getMerchantId, merchantId)
                .orderByAsc(HotelEntity::getId)
                .last("LIMIT 1");
        return iHotelService.getOne(queryWrapper, false);
    }

    private HotelRoomEntity findRoomByServiceId(Long serviceId) {
        LambdaQueryWrapper<HotelRoomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomEntity::getMerchantServiceId, serviceId)
                .last("LIMIT 1");
        return iHotelRoomService.getOne(queryWrapper, false);
    }

    private RouteEntity findRouteByServiceId(Long serviceId) {
        LambdaQueryWrapper<RouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteEntity::getMerchantServiceId, serviceId)
                .last("LIMIT 1");
        return iRouteService.getOne(queryWrapper, false);
    }

    private RouteEntity findFirstRouteByMerchant(Long merchantId) {
        LambdaQueryWrapper<RouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteEntity::getMerchantId, merchantId)
                .eq(RouteEntity::getStatus, STATUS_ONLINE)
                .orderByAsc(RouteEntity::getSort)
                .orderByAsc(RouteEntity::getId)
                .last("LIMIT 1");
        return iRouteService.getOne(queryWrapper, false);
    }

    private String resolveMerchantShopCategory(MerchantEntity merchant) {
        if (merchant == null) {
            return HeritageShopCategorySupport.DEFAULT;
        }
        return HeritageShopCategorySupport.resolveFromMerchant(
                merchant.getShopCategory(), merchant.getCraftId(), iHeritageCraftService::getById);
    }

    private HeritageShopEntity findShopByMerchant(Long merchantId) {
        LambdaQueryWrapper<HeritageShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HeritageShopEntity::getMerchantId, merchantId)
                .last("LIMIT 1");
        return iHeritageShopService.getOne(queryWrapper, false);
    }

    private GoodsEntity findGoodsByServiceId(Long serviceId) {
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsEntity::getMerchantServiceId, serviceId)
                .last("LIMIT 1");
        return iGoodsService.getOne(queryWrapper, false);
    }

    private Map<String, Object> loadDecorMap(Long merchantId) {
        LambdaQueryWrapper<MerchantStoreDecorEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantStoreDecorEntity::getMerchantId, merchantId)
                .last("LIMIT 1");
        MerchantStoreDecorEntity entity = iMerchantStoreDecorService.getOne(queryWrapper, false);
        if (entity == null || StringUtils.isBlank(entity.getDecorJson())) {
            return Collections.emptyMap();
        }
        try {
            return JSON.parseObject(entity.getDecorJson(), new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private int mapServiceStatus(String status) {
        return MerchantServiceStatus.ON_SALE.getCode().equals(MerchantServiceStatus.normalize(status))
                ? STATUS_ONLINE : STATUS_OFFLINE;
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> readItinerary(Map<String, Object> decor) {
        Object value = decor.get("itinerary");
        if (!(value instanceof List)) {
            return Collections.emptyList();
        }
        List<?> list = (List<?>) value;
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map) {
                result.add((Map<String, Object>) item);
            }
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<String> readStringList(Map<String, Object> decor, String key) {
        Object value = decor.get(key);
        if (value == null) {
            return Collections.emptyList();
        }
        if (value instanceof List) {
            List<String> result = new ArrayList<>();
            for (Object item : (List<?>) value) {
                if (item != null && StringUtils.isNotBlank(String.valueOf(item))) {
                    result.add(String.valueOf(item).trim());
                }
            }
            return result;
        }
        if (value instanceof String && StringUtils.isNotBlank((String) value)) {
            String[] parts = ((String) value).split("[,，、]");
            List<String> result = new ArrayList<>();
            for (String part : parts) {
                if (StringUtils.isNotBlank(part)) {
                    result.add(part.trim());
                }
            }
            return result;
        }
        return Collections.emptyList();
    }

    private List<String> readStringListFromRaw(String raw) {
        if (StringUtils.isBlank(raw)) {
            return Collections.emptyList();
        }
        String trimmed = raw.trim();
        if (trimmed.startsWith("[")) {
            try {
                List<String> parsed = JSON.parseArray(trimmed, String.class);
                if (parsed != null && !parsed.isEmpty()) {
                    List<String> result = sanitizeMediaList(parsed);
                    if (!result.isEmpty()) {
                        return result;
                    }
                }
            } catch (Exception ignored) {
                // 继续按分隔符解析
            }
        }
        List<String> extracted = extractMediaUrls(trimmed);
        if (!extracted.isEmpty()) {
            return extracted;
        }
        String[] parts = trimmed.split("[,，、]");
        List<String> result = new ArrayList<>();
        for (String part : parts) {
            if (StringUtils.isNotBlank(part)) {
                result.add(part.trim());
            }
        }
        return result;
    }

    private List<String> sanitizeMediaList(List<String> values) {
        Set<String> result = new LinkedHashSet<>();
        if (values == null) {
            return new ArrayList<>();
        }
        for (String value : values) {
            if (StringUtils.isBlank(value)) {
                continue;
            }
            List<String> urls = extractMediaUrls(value);
            if (!urls.isEmpty()) {
                result.addAll(urls);
                continue;
            }
            String cleaned = value.trim().replaceAll("^[\\[\\]\"']+|[\\[\\]\"']+$", "");
            if (StringUtils.isNotBlank(cleaned)) {
                result.add(cleaned);
            }
        }
        return new ArrayList<>(result);
    }

    private List<String> extractMediaUrls(String raw) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        Matcher matcher = MEDIA_URL_PATTERN.matcher(raw);
        while (matcher.find()) {
            String url = matcher.group();
            if (!result.contains(url)) {
                result.add(url);
            }
        }
        return result;
    }

    private List<GoodsSaveBo.BannerItem> buildCoverBanner(String coverImage) {
        GoodsSaveBo.BannerItem banner = new GoodsSaveBo.BannerItem();
        banner.setImage(coverImage);
        List<GoodsSaveBo.BannerItem> banners = new ArrayList<>();
        banners.add(banner);
        return banners;
    }

    private String readString(Map<String, Object> decor, String key, String defaultValue) {
        Object value = decor.get(key);
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return defaultValue;
        }
        return String.valueOf(value).trim();
    }

    private Integer readInteger(Map<String, Object> decor, String key, Integer defaultValue) {
        Object value = decor.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private BigDecimal readDecimal(Map<String, Object> decor, String key, BigDecimal defaultValue) {
        Object value = decor.get(key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }
}

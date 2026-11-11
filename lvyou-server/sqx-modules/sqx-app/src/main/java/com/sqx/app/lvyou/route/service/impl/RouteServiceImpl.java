package com.sqx.app.lvyou.route.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.TypeReference;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.utils.AvatarSupport;
import com.sqx.app.lvyou.feedback.util.FeedbackJsonUtil;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.enums.OrderType;
import com.sqx.app.lvyou.order.support.OrderTypeSupport;
import com.sqx.app.lvyou.route.db.service.IRouteReviewService;
import com.sqx.app.lvyou.route.domain.entity.RouteReviewEntity;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.home.util.RecommendTagUtil;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantStoreDecorService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantStoreDecorEntity;
import com.sqx.app.lvyou.route.db.service.IRouteItineraryService;
import com.sqx.app.lvyou.route.db.service.IRouteService;
import com.sqx.app.lvyou.route.domain.bo.*;
import com.sqx.app.lvyou.route.domain.entity.RouteEntity;
import com.sqx.app.lvyou.route.domain.entity.RouteItineraryEntity;
import com.sqx.app.lvyou.route.domain.vo.*;
import com.sqx.app.lvyou.route.enums.RouteStatus;
import com.sqx.app.lvyou.favorite.enums.AssetTargetType;
import com.sqx.app.lvyou.favorite.service.FootprintService;
import com.sqx.app.lvyou.route.service.RouteService;
import com.sqx.app.lvyou.route.util.RouteSpotsUtil;
import com.sqx.app.lvyou.route.util.RouteTagUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 旅游线路业务服务实现
 */
@Service
public class RouteServiceImpl implements RouteService {

    private static final int STATUS_ONLINE = RouteStatus.ONLINE.getCode();
    private static final String TRIP_DATE_RULE = "预约时需选择出行日期，出发前短信通知具体集合时间";
    private static final Pattern MEDIA_URL_PATTERN = Pattern.compile("https?://[^\\s\"'\\\\\\],]+");
    private static final DateTimeFormatter REVIEW_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private IRouteService iRouteService;

    @Resource
    private IRouteItineraryService iRouteItineraryService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantStoreDecorService iMerchantStoreDecorService;

    @Resource
    private FootprintService footprintService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Resource
    private IRouteReviewService iRouteReviewService;

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IUserService iUserService;

    @Override
    public List<HomeRecommendItemVo> listRecommend(Long destinationId, int limit) {
        if (destinationId == null || limit <= 0) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<RouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteEntity::getStatus, STATUS_ONLINE)
                .eq(RouteEntity::getDestinationId, destinationId)
                .orderByAsc(RouteEntity::getSort)
                .orderByDesc(RouteEntity::getId)
                .last("LIMIT " + limit);
        List<RouteEntity> entities = iRouteService.list(queryWrapper);
        List<HomeRecommendItemVo> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (RouteEntity entity : entities) {
                items.add(toRecommendItem(entity));
            }
        }
        return items;
    }

    @Override
    public IPage<RouteAppVo> appPage(RouteAppListBo listBo) {
        Page<RouteEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<RouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteEntity::getStatus, STATUS_ONLINE)
                .like(StringUtils.isNotBlank(listBo.getName()), RouteEntity::getName, listBo.getName())
                .eq(listBo.getMerchantId() != null, RouteEntity::getMerchantId, listBo.getMerchantId())
                .eq(listBo.getDayCount() != null, RouteEntity::getDayCount, listBo.getDayCount())
                .ge(listBo.getMinDayCount() != null, RouteEntity::getDayCount, listBo.getMinDayCount())
                .orderByAsc(RouteEntity::getSort)
                .orderByDesc(RouteEntity::getId);

        if (listBo.getMerchantId() == null) {
            if (CollectionUtils.isNotEmpty(listBo.getDestinationIds())) {
                queryWrapper.in(RouteEntity::getDestinationId, listBo.getDestinationIds());
            } else if (listBo.getDestinationId() != null) {
                queryWrapper.eq(RouteEntity::getDestinationId, listBo.getDestinationId());
            }
        }

        IPage<RouteEntity> entityPage = iRouteService.page(page, queryWrapper);
        List<RouteAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (RouteEntity record : entityPage.getRecords()) {
                voList.add(toAppListVo(record));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public RouteAppDetailVo appDetail(Long id, Long userId) {
        RouteEntity entity = getOnlineRoute(id);
        RouteAppDetailVo vo = toAppDetailVo(entity);
        List<RouteItineraryAppVo> itinerary = listItineraryAppVo(id);
        vo.setItinerary(itinerary);
        vo.setItineraryDays(toItineraryDayAppVos(itinerary));
        fillMerchantContact(vo, entity.getMerchantId());
        footprintService.recordAsync(userId, AssetTargetType.ROUTE.getCode(), id);
        return vo;
    }

    @Override
    public RouteItineraryPageVo appItinerary(Long id) {
        RouteEntity entity = getOnlineRoute(id);
        RouteItineraryPageVo vo = new RouteItineraryPageVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDuration(entity.getDuration());
        vo.setDayCount(entity.getDayCount());
        if (entity.getDayCount() != null && entity.getDayCount() > 0) {
            vo.setDays(entity.getDayCount() + "天");
        } else {
            vo.setDays(entity.getDuration());
        }
        vo.setTravelCity(entity.getTravelCity());
        vo.setMeetPoint(entity.getMeetPoint());
        vo.setTripDateRule(TRIP_DATE_RULE);
        List<RouteItineraryAppVo> itinerary = listItineraryAppVo(id);
        vo.setItinerary(itinerary);
        vo.setItineraryDays(toItineraryDayAppVos(itinerary));
        return vo;
    }

    @Override
    public IPage<RouteVo> pageInfo(RoutePageBo pageBo) {
        Page<RouteEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<RouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), RouteEntity::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getDestinationId()), RouteEntity::getDestinationId, pageBo.getDestinationId())
                .eq(Objects.nonNull(pageBo.getStatus()), RouteEntity::getStatus, pageBo.getStatus());

        List<Long> merchantIds = resolveMerchantIds(pageBo.getMerchantName());
        if (merchantIds != null) {
            if (merchantIds.isEmpty()) {
                return PageUtil.copyPage(page, new ArrayList<>());
            }
            queryWrapper.in(RouteEntity::getMerchantId, merchantIds);
        }

        queryWrapper.orderByAsc(RouteEntity::getSort)
                .orderByDesc(RouteEntity::getCreateTime);

        IPage<RouteEntity> entityPage = iRouteService.page(page, queryWrapper);
        Map<Long, String> destinationNameMap = loadDestinationNameMap(entityPage.getRecords());
        Map<Long, String> merchantNameMap = loadMerchantNameMap(entityPage.getRecords());
        Map<Long, List<RouteItineraryVo>> itineraryMap = loadItineraryMap(entityPage.getRecords());

        List<RouteVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (RouteEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, destinationNameMap, merchantNameMap, itineraryMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(RouteSaveBo saveBo) {
        Long destinationId = publishDestinationResolver.requireFromMerchant(saveBo.getMerchantId());
        validateMerchant(saveBo.getMerchantId(), destinationId);

        RouteEntity entity = new RouteEntity();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setDestinationId(destinationId);
        fillRouteDefaults(entity);
        iRouteService.save(entity);
        saveItineraryList(entity.getId(), saveBo.getItineraryList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(RouteUpdateBo updateBo) {
        RouteEntity existing = iRouteService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.ROUTE_NOT_FOUND);
        }
        Long destinationId = publishDestinationResolver.requireFromMerchant(updateBo.getMerchantId());
        validateMerchant(updateBo.getMerchantId(), destinationId);

        RouteEntity entity = new RouteEntity();
        BeanUtils.copyProperties(updateBo, entity);
        entity.setDestinationId(destinationId);
        iRouteService.updateById(entity);
        removeItineraryByRouteId(updateBo.getId());
        saveItineraryList(updateBo.getId(), updateBo.getItineraryList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(RouteInfoBo infoBo) {
        RouteEntity existing = iRouteService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.ROUTE_NOT_FOUND);
        }
        iRouteService.removeById(infoBo.getId());
        removeItineraryByRouteId(infoBo.getId());
    }

    @Override
    public void updateState(RouteUpdateStateBo updateStateBo) {
        RouteEntity existing = iRouteService.getById(updateStateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.ROUTE_NOT_FOUND);
        }
        LambdaUpdateWrapper<RouteEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(RouteEntity::getId, updateStateBo.getId())
                .set(RouteEntity::getStatus, updateStateBo.getStatus());
        iRouteService.update(updateWrapper);
    }

    private RouteEntity getOnlineRoute(Long id) {
        RouteEntity entity = iRouteService.getById(id);
        if (entity == null || !Objects.equals(entity.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.ROUTE_NOT_FOUND);
        }
        return entity;
    }

    private List<RouteItineraryAppVo> listItineraryAppVo(Long routeId) {
        LambdaQueryWrapper<RouteItineraryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteItineraryEntity::getRouteId, routeId)
                .orderByAsc(RouteItineraryEntity::getSort)
                .orderByAsc(RouteItineraryEntity::getId);
        List<RouteItineraryEntity> entities = iRouteItineraryService.list(queryWrapper);
        List<RouteItineraryAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (RouteItineraryEntity entity : entities) {
                voList.add(toItineraryAppVo(entity));
            }
        }
        return voList;
    }

    private void saveItineraryList(Long routeId, List<RouteItinerarySaveBo> itineraryList) {
        if (CollectionUtils.isEmpty(itineraryList)) {
            return;
        }
        int index = 0;
        for (RouteItinerarySaveBo item : itineraryList) {
            RouteItineraryEntity entity = new RouteItineraryEntity();
            int sort = item.getSort() != null ? item.getSort() : index;
            entity.setRouteId(routeId);
            entity.setSort(sort);
            entity.setDayNo(Math.floorDiv(sort, 100) + 1);
            entity.setTimeLabel(item.getTimeLabel());
            entity.setName(item.getName());
            entity.setLocation(StringUtils.defaultString(item.getLocation()));
            entity.setDescription(item.getDescription());
            List<String> images = item.getImages();
            if (CollectionUtils.isNotEmpty(images)) {
                entity.setImagesJson(JSON.toJSONString(images));
                entity.setCoverImage(images.get(0));
            } else if (StringUtils.isNotBlank(item.getCoverImage())) {
                entity.setCoverImage(item.getCoverImage());
                entity.setImagesJson(JSON.toJSONString(Collections.singletonList(item.getCoverImage())));
            }
            iRouteItineraryService.save(entity);
            index++;
        }
    }

    private void removeItineraryByRouteId(Long routeId) {
        LambdaQueryWrapper<RouteItineraryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteItineraryEntity::getRouteId, routeId);
        iRouteItineraryService.remove(queryWrapper);
    }

    private List<Long> resolveMerchantIds(String merchantName) {
        if (StringUtils.isBlank(merchantName)) {
            return null;
        }
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MerchantEntity::getStoreName, merchantName);
        List<MerchantEntity> merchants = iMerchantService.list(queryWrapper);
        if (CollectionUtils.isEmpty(merchants)) {
            return Collections.emptyList();
        }
        return merchants.stream().map(MerchantEntity::getId).collect(Collectors.toList());
    }

    private void validateDestination(Long destinationId) {
        if (destinationId == null) {
            return;
        }
        DestinationEntity destination = iDestinationService.getById(destinationId);
        if (destination == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
    }

    private void validateMerchant(Long merchantId, Long destinationId) {
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
        if (destinationId != null && !Objects.equals(merchant.getDestinationId(), destinationId)) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
    }

    private void fillRouteDefaults(RouteEntity entity) {
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
    }

    private Map<Long, String> loadDestinationNameMap(List<RouteEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> ids = records.stream()
                .map(RouteEntity::getDestinationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<DestinationEntity> destinations = iDestinationService.listByIds(ids);
        Map<Long, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(destinations)) {
            for (DestinationEntity destination : destinations) {
                map.put(destination.getId(), destination.getName());
            }
        }
        return map;
    }

    private Map<Long, String> loadMerchantNameMap(List<RouteEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> ids = records.stream()
                .map(RouteEntity::getMerchantId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return Collections.emptyMap();
        }
        List<MerchantEntity> merchants = iMerchantService.listByIds(ids);
        Map<Long, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(merchants)) {
            for (MerchantEntity merchant : merchants) {
                map.put(merchant.getId(), merchant.getStoreName());
            }
        }
        return map;
    }

    private Map<Long, List<RouteItineraryVo>> loadItineraryMap(List<RouteEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> routeIds = records.stream().map(RouteEntity::getId).collect(Collectors.toList());
        LambdaQueryWrapper<RouteItineraryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(RouteItineraryEntity::getRouteId, routeIds)
                .orderByAsc(RouteItineraryEntity::getSort)
                .orderByAsc(RouteItineraryEntity::getId);
        List<RouteItineraryEntity> entities = iRouteItineraryService.list(queryWrapper);
        Map<Long, List<RouteItineraryVo>> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (RouteItineraryEntity entity : entities) {
                map.computeIfAbsent(entity.getRouteId(), key -> new ArrayList<>()).add(toItineraryVo(entity));
            }
        }
        return map;
    }

    private HomeRecommendItemVo toRecommendItem(RouteEntity entity) {
        HomeRecommendItemVo vo = new HomeRecommendItemVo();
        vo.setId(entity.getId());
        vo.setTitle(entity.getName());
        vo.setCover(StringUtils.defaultString(entity.getCoverImage()));
        vo.setPrice(entity.getPriceFrom());
        vo.setTag(RecommendTagUtil.resolveTag(entity.getTags()));
        return vo;
    }

    private RouteAppVo toAppListVo(RouteEntity entity) {
        RouteAppVo vo = new RouteAppVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDesc(entity.getDescription());
        vo.setPriceFrom(entity.getPriceFrom());
        vo.setDuration(entity.getDuration());
        vo.setDays(entity.getDuration());
        vo.setTags(RouteTagUtil.parseTags(entity.getTags()));
        vo.setSpots(RouteSpotsUtil.parseSpots(entity.getSpots()));
        vo.setCover(StringUtils.defaultString(entity.getCoverImage()));
        return vo;
    }

    private RouteAppDetailVo toAppDetailVo(RouteEntity entity) {
        RouteAppDetailVo vo = new RouteAppDetailVo();
        vo.setId(entity.getId());
        vo.setMerchantId(entity.getMerchantId());
        vo.setName(entity.getName());
        vo.setDesc(entity.getDescription());
        vo.setIntro(entity.getIntro());
        vo.setPriceFrom(entity.getPriceFrom());
        vo.setPrice(entity.getPriceFrom());
        vo.setDuration(entity.getDuration());
        vo.setDayCount(entity.getDayCount());
        if (entity.getDayCount() != null && entity.getDayCount() > 0) {
            vo.setDays(entity.getDayCount() + "天");
        } else {
            vo.setDays(entity.getDuration());
        }
        vo.setTravelCity(entity.getTravelCity());
        vo.setMeetPoint(entity.getMeetPoint());
        vo.setTripDateRule(TRIP_DATE_RULE);
        vo.setTags(RouteTagUtil.parseTags(entity.getTags()));
        vo.setSpots(RouteSpotsUtil.parseSpots(entity.getSpots()));
        vo.setCover(StringUtils.defaultString(entity.getCoverImage()));
        List<String> banners = parseImagesJson(entity.getBannerImages());
        if (CollectionUtils.isEmpty(banners) && StringUtils.isNotBlank(entity.getCoverImage())) {
            banners = Collections.singletonList(entity.getCoverImage());
        }
        vo.setBanners(banners);
        return vo;
    }

    private RouteItineraryAppVo toItineraryAppVo(RouteItineraryEntity entity) {
        RouteItineraryAppVo vo = new RouteItineraryAppVo();
        vo.setTime(entity.getTimeLabel());
        vo.setName(entity.getName());
        vo.setLocation(entity.getLocation());
        vo.setDesc(entity.getDescription());
        vo.setCover(StringUtils.defaultString(entity.getCoverImage()));
        List<String> images = parseImagesJson(entity.getImagesJson());
        if (CollectionUtils.isEmpty(images) && StringUtils.isNotBlank(entity.getCoverImage())) {
            images = Collections.singletonList(entity.getCoverImage());
        }
        vo.setImages(images);
        Integer dayNo = entity.getDayNo();
        if (dayNo == null || dayNo <= 0) {
            int sort = entity.getSort() == null ? 0 : entity.getSort();
            dayNo = Math.floorDiv(sort, 100) + 1;
        }
        vo.setDayNo(dayNo);
        vo.setDayLabel("第" + dayNo + "天");
        return vo;
    }

    private List<RouteItineraryDayAppVo> toItineraryDayAppVos(List<RouteItineraryAppVo> itinerary) {
        if (CollectionUtils.isEmpty(itinerary)) {
            return Collections.emptyList();
        }
        Map<Integer, RouteItineraryDayAppVo> dayMap = new LinkedHashMap<>();
        for (RouteItineraryAppVo node : itinerary) {
            int dayNo = node.getDayNo() == null || node.getDayNo() <= 0 ? 1 : node.getDayNo();
            RouteItineraryDayAppVo day = dayMap.get(dayNo);
            if (day == null) {
                day = new RouteItineraryDayAppVo();
                day.setDayNo(dayNo);
                day.setDayLabel(StringUtils.defaultIfBlank(node.getDayLabel(), "第" + dayNo + "天"));
                day.setTitle(node.getName());
                day.setContent(node.getDesc());
                day.setImages(node.getImages() == null ? new ArrayList<>() : new ArrayList<>(node.getImages()));
                dayMap.put(dayNo, day);
            } else {
                if (StringUtils.isBlank(day.getTitle()) && StringUtils.isNotBlank(node.getName())) {
                    day.setTitle(node.getName());
                }
                if (StringUtils.isNotBlank(node.getDesc())) {
                    if (StringUtils.isBlank(day.getContent())) {
                        day.setContent(node.getDesc());
                    } else {
                        day.setContent(day.getContent() + "\n" + node.getDesc());
                    }
                }
                if (CollectionUtils.isNotEmpty(node.getImages())) {
                    List<String> merged = day.getImages() == null ? new ArrayList<>() : day.getImages();
                    for (String image : node.getImages()) {
                        if (StringUtils.isNotBlank(image) && !merged.contains(image)) {
                            merged.add(image);
                        }
                    }
                    day.setImages(merged);
                }
            }
        }
        return new ArrayList<>(dayMap.values());
    }

    private void fillMerchantContact(RouteAppDetailVo vo, Long merchantId) {
        if (merchantId == null) {
            return;
        }
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant != null) {
            vo.setContactPhone(StringUtils.defaultIfBlank(merchant.getContactPhone(), vo.getContactPhone()));
            vo.setStoreName(StringUtils.defaultString(merchant.getStoreName()));
            vo.setStoreAddress(StringUtils.defaultString(merchant.getStoreAddress()));
            vo.setStoreHours(StringUtils.defaultString(merchant.getStoreHours()));
            vo.setStoreLatitude(merchant.getLatitude());
            vo.setStoreLongitude(merchant.getLongitude());
        }
        Map<String, Object> decor = loadDecorMap(merchantId);
        Object qr = decor.get("wechatQrCode");
        if (qr == null) {
            qr = decor.get("wechatQr");
        }
        if (qr != null && StringUtils.isNotBlank(String.valueOf(qr))) {
            vo.setWechatQrCode(String.valueOf(qr).trim());
        }
        String intro = readDecorString(decor, "intro");
        if (StringUtils.isBlank(intro)) {
            intro = readDecorString(decor, "introDetail");
        }
        vo.setStoreIntro(StringUtils.defaultString(intro));
        vo.setStoreTags(parseDecorStringList(decor, "tags"));
        vo.setStoreCover(readDecorString(decor, "cover"));
        vo.setStoreBanners(parseDecorStringList(decor, "bannerImages"));
        if (CollectionUtils.isEmpty(vo.getStoreBanners())) {
            vo.setStoreBanners(parseDecorStringList(decor, "bannerList"));
        }
        vo.setGuides(parseDecorGuides(decor));
    }

    private String readDecorString(Map<String, Object> decor, String key) {
        if (decor == null || StringUtils.isBlank(key)) {
            return "";
        }
        Object value = decor.get(key);
        return value == null ? "" : StringUtils.trimToEmpty(String.valueOf(value));
    }

    private List<String> parseDecorStringList(Map<String, Object> decor, String key) {
        if (decor == null || StringUtils.isBlank(key)) {
            return Collections.emptyList();
        }
        Object value = decor.get(key);
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
            return parseImagesJson((String) value);
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    private List<RouteGuideAppVo> parseDecorGuides(Map<String, Object> decor) {
        if (decor == null) {
            return Collections.emptyList();
        }
        List<RouteGuideAppVo> result = new ArrayList<>();
        Object value = decor.get("guides");
        if (value instanceof List) {
            for (Object item : (List<?>) value) {
                if (!(item instanceof Map)) {
                    continue;
                }
                Map<String, Object> map = (Map<String, Object>) item;
                String name = StringUtils.trimToEmpty(Objects.toString(map.get("name"), ""));
                String avatar = StringUtils.trimToEmpty(Objects.toString(
                        map.get("avatar") != null ? map.get("avatar")
                                : (map.get("photo") != null ? map.get("photo") : map.get("cover")),
                        ""));
                if (StringUtils.isBlank(name) && StringUtils.isBlank(avatar)) {
                    continue;
                }
                RouteGuideAppVo guide = new RouteGuideAppVo();
                guide.setName(name);
                guide.setAvatar(avatar);
                result.add(guide);
            }
        }
        if (result.isEmpty()) {
            Object legacy = decor.get("guideName");
            if (legacy != null && StringUtils.isNotBlank(String.valueOf(legacy))) {
                RouteGuideAppVo guide = new RouteGuideAppVo();
                guide.setName(String.valueOf(legacy).trim());
                guide.setAvatar("");
                result.add(guide);
            }
        }
        return result;
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
            Map<String, Object> parsed = JSON.parseObject(entity.getDecorJson(),
                    new TypeReference<Map<String, Object>>() {
                    });
            return parsed == null ? Collections.emptyMap() : parsed;
        } catch (Exception ex) {
            return Collections.emptyMap();
        }
    }

    private List<String> parseImagesJson(String imagesJson) {
        if (StringUtils.isBlank(imagesJson)) {
            return Collections.emptyList();
        }
        List<String> parsed = new ArrayList<>();
        try {
            List<String> list = JSON.parseArray(imagesJson, String.class);
            if (list != null) {
                for (String item : list) {
                    appendMediaUrls(parsed, item);
                }
            }
        } catch (Exception ignored) {
            appendMediaUrls(parsed, imagesJson);
        }
        if (parsed.isEmpty()) {
            appendMediaUrls(parsed, imagesJson);
        }
        return parsed;
    }

    private void appendMediaUrls(List<String> target, String raw) {
        if (target == null || StringUtils.isBlank(raw)) {
            return;
        }
        Matcher matcher = MEDIA_URL_PATTERN.matcher(raw);
        boolean found = false;
        while (matcher.find()) {
            found = true;
            String url = matcher.group();
            if (!target.contains(url)) {
                target.add(url);
            }
        }
        if (!found) {
            String cleaned = raw.trim().replaceAll("^[\\[\\]\"']+|[\\[\\]\"']+$", "");
            if (StringUtils.isNotBlank(cleaned) && !target.contains(cleaned)) {
                target.add(cleaned);
            }
        }
    }

    private RouteItineraryVo toItineraryVo(RouteItineraryEntity entity) {
        RouteItineraryVo vo = new RouteItineraryVo();
        BeanUtils.copyProperties(entity, vo);
        List<String> images = parseImagesJson(entity.getImagesJson());
        if (CollectionUtils.isEmpty(images) && StringUtils.isNotBlank(entity.getCoverImage())) {
            images = Collections.singletonList(entity.getCoverImage());
        }
        vo.setImages(images);
        return vo;
    }

    private RouteVo toAdminVo(RouteEntity entity,
                              Map<Long, String> destinationNameMap,
                              Map<Long, String> merchantNameMap,
                              Map<Long, List<RouteItineraryVo>> itineraryMap) {
        RouteVo vo = new RouteVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destinationNameMap, entity.getDestinationId(), entity.getMeetPoint()));
        vo.setMerchantName(merchantNameMap.get(entity.getMerchantId()));
        vo.setStatusLabel(RouteStatus.labelOf(entity.getStatus()));
        vo.setItineraryList(itineraryMap.getOrDefault(entity.getId(), new ArrayList<>()));
        return vo;
    }

    @Override
    public IPage<ReviewAppVo> appStoreReviews(RouteReviewAppListBo listBo) {
        requireMerchant(listBo.getMerchantId());
        Page<RouteReviewEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<RouteReviewEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteReviewEntity::getMerchantId, listBo.getMerchantId())
                .orderByDesc(RouteReviewEntity::getCreateTime)
                .orderByDesc(RouteReviewEntity::getId);
        IPage<RouteReviewEntity> entityPage = iRouteReviewService.page(page, queryWrapper);
        Map<Long, String> avatarMap = loadReviewAvatarMap(entityPage.getRecords());
        List<ReviewAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (RouteReviewEntity record : entityPage.getRecords()) {
                voList.add(toReviewVo(record, avatarMap.get(record.getUserId())));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewAppVo submitStoreReview(Long userId, Long merchantId, RouteReviewSubmitBo submitBo) {
        requireMerchant(merchantId);
        UserEntity user = requireUser(userId);
        OrderEntity order = requireReviewableRouteOrder(userId, submitBo.getOrderId(), merchantId);
        assertRouteNotReviewed(submitBo.getOrderId(), userId);

        RouteReviewEntity entity = new RouteReviewEntity();
        entity.setMerchantId(merchantId);
        entity.setRouteId(resolveRouteIdFromOrder(submitBo.getOrderId()));
        entity.setOrderId(submitBo.getOrderId());
        entity.setUserId(userId);
        entity.setUserName(StringUtils.defaultIfBlank(user.getUserName(), user.getPhone()));
        entity.setContent(StringUtils.trim(submitBo.getContent()));
        entity.setRating(submitBo.getRating());
        entity.setImages(FeedbackJsonUtil.toJson(normalizeReviewImages(submitBo.getImages())));
        iRouteReviewService.save(entity);
        return toReviewVo(entity, user.getAvatar());
    }

    private MerchantEntity requireMerchant(Long merchantId) {
        if (merchantId == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
        return merchant;
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        return user;
    }

    private OrderEntity requireReviewableRouteOrder(Long userId, Long orderId, Long merchantId) {
        OrderEntity order = iOrderService.getById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(AppError.ORDER_NOT_FOUND);
        }
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(orderId));
        OrderType type = OrderTypeSupport.resolve(order, items);
        if (!OrderTypeSupport.isRouteFlow(type)) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        if (order.getStatus() == null || order.getStatus() != MerchantOrderStatusGroup.COMPLETED) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        if (order.getVerifyTime() == null) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        Long orderMerchantId = order.getMerchantId();
        if (orderMerchantId == null) {
            orderMerchantId = resolveRouteMerchantIdFromItems(items);
        }
        if (orderMerchantId == null || !Objects.equals(orderMerchantId, merchantId)) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        return order;
    }

    private Long resolveRouteIdFromOrder(Long orderId) {
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(orderId));
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        for (OrderItemEntity item : items) {
            if ("route".equals(item.getItemType()) && item.getItemId() != null) {
                return item.getItemId();
            }
        }
        return null;
    }

    private Long resolveRouteMerchantIdFromItems(List<OrderItemEntity> items) {
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        for (OrderItemEntity item : items) {
            if (!"route".equals(item.getItemType()) || item.getItemId() == null) {
                continue;
            }
            RouteEntity route = iRouteService.getById(item.getItemId());
            if (route != null && route.getMerchantId() != null) {
                return route.getMerchantId();
            }
        }
        return null;
    }

    private void assertRouteNotReviewed(Long orderId, Long userId) {
        LambdaQueryWrapper<RouteReviewEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteReviewEntity::getOrderId, orderId)
                .eq(RouteReviewEntity::getUserId, userId)
                .last("LIMIT 1");
        if (iRouteReviewService.count(queryWrapper) > 0) {
            throw new BizException(AppError.ORDER_REVIEW_DUPLICATE);
        }
    }

    private Map<Long, String> loadReviewAvatarMap(List<RouteReviewEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = new HashSet<>();
        for (RouteReviewEntity record : records) {
            if (record != null && record.getUserId() != null) {
                userIds.add(record.getUserId());
            }
        }
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, String> avatarMap = new HashMap<>();
        if (CollectionUtils.isEmpty(users)) {
            return avatarMap;
        }
        for (UserEntity user : users) {
            if (user == null || user.getUserId() == null) {
                continue;
            }
            avatarMap.put(user.getUserId(), user.getAvatar());
        }
        return avatarMap;
    }

    private ReviewAppVo toReviewVo(RouteReviewEntity entity, String avatar) {
        ReviewAppVo vo = new ReviewAppVo();
        vo.setId(entity.getId());
        vo.setUser(entity.getUserName());
        vo.setAvatar(AvatarSupport.resolve(avatar));
        vo.setContent(entity.getContent());
        vo.setRating(entity.getRating());
        vo.setImages(FeedbackJsonUtil.parseList(entity.getImages()));
        if (entity.getCreateTime() != null) {
            vo.setTime(entity.getCreateTime().format(REVIEW_DATE_FORMAT));
        }
        return vo;
    }

    private List<String> normalizeReviewImages(List<String> images) {
        if (CollectionUtils.isEmpty(images)) {
            return Collections.emptyList();
        }
        return images.stream()
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .limit(6)
                .collect(Collectors.toList());
    }
}

package com.sqx.app.lvyou.hotel.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.map.support.MapDistanceUtil;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.utils.AvatarSupport;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.home.util.RecommendTagUtil;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomService;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomStandardService;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.bo.*;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomStandardEntity;
import com.sqx.app.lvyou.hotel.domain.vo.*;
import com.sqx.app.lvyou.hotel.enums.HotelCategory;
import com.sqx.app.lvyou.hotel.enums.HotelRoomStatus;
import com.sqx.app.lvyou.hotel.enums.HotelStatus;
import com.sqx.app.lvyou.favorite.enums.AssetTargetType;
import com.sqx.app.lvyou.favorite.service.FootprintService;
import com.sqx.app.lvyou.feedback.util.FeedbackJsonUtil;
import com.sqx.app.lvyou.hotel.service.HotelService;
import com.sqx.app.lvyou.hotel.util.HotelPolicyUtil;
import com.sqx.app.lvyou.hotel.util.HotelRoomDetailUtil;
import com.sqx.app.lvyou.hotel.util.HotelStarUtil;
import com.sqx.app.lvyou.hotel.support.HotelInventorySupport;
import com.sqx.app.lvyou.hotel.db.service.IHotelReviewService;
import com.sqx.app.lvyou.hotel.db.service.IHotelRoomCalendarService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelReviewEntity;
import com.sqx.app.lvyou.hotel.domain.entity.HotelRoomCalendarEntity;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.enums.OrderType;
import com.sqx.app.lvyou.order.support.AppOrderStatusSupport;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.app.lvyou.order.support.OrderTypeSupport;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.merchant.support.MerchantAuthSupport;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 酒店业务服务实现
 */
@Service
public class HotelServiceImpl implements HotelService {

    private static final int STATUS_ONLINE = HotelStatus.ONLINE.getCode();
    private static final int ROOM_ON_SALE = HotelRoomStatus.ON_SALE.getCode();
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter REVIEW_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private HotelInventorySupport hotelInventorySupport;

    @Resource
    private IHotelRoomCalendarService iHotelRoomCalendarService;

    @Resource
    private MerchantAuthSupport merchantAuthSupport;

    @Resource
    private MerchantWalletService merchantWalletService;

    @Resource
    private IHotelService iHotelService;

    @Resource
    private IHotelRoomService iHotelRoomService;

    @Resource
    private IHotelRoomStandardService iHotelRoomStandardService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantServiceItemService iMerchantServiceItemService;

    @Resource
    private FootprintService footprintService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Resource
    private IHotelReviewService iHotelReviewService;

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
        LambdaQueryWrapper<HotelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelEntity::getStatus, STATUS_ONLINE)
                .eq(HotelEntity::getDestinationId, destinationId)
                .orderByAsc(HotelEntity::getSort)
                .orderByDesc(HotelEntity::getId)
                .last("LIMIT " + limit);
        List<HotelEntity> entities = iHotelService.list(queryWrapper);
        List<HomeRecommendItemVo> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (HotelEntity entity : entities) {
                items.add(toRecommendItem(entity));
            }
        }
        return items;
    }

    @Override
    public IPage<HotelAppVo> appPage(HotelAppListBo listBo) {
        if ("distance".equals(resolveAppListSort(listBo))) {
            return appPageByDistance(listBo);
        }
        Page<HotelEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<HotelEntity> queryWrapper = buildAppListQueryWrapper(listBo);
        applyAppListSort(queryWrapper, listBo);

        IPage<HotelEntity> entityPage = iHotelService.page(page, queryWrapper);
        BigDecimal refLat = MapDistanceUtil.parseCoordinate(listBo.getLat());
        BigDecimal refLng = MapDistanceUtil.parseCoordinate(listBo.getLng());
        List<HotelAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HotelEntity record : entityPage.getRecords()) {
                voList.add(toAppListVo(record, refLat, refLng));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    private LambdaQueryWrapper<HotelEntity> buildAppListQueryWrapper(HotelAppListBo listBo) {
        LambdaQueryWrapper<HotelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelEntity::getStatus, STATUS_ONLINE)
                .eq(Objects.nonNull(listBo.getDestinationId()), HotelEntity::getDestinationId, listBo.getDestinationId());
        if (StringUtils.isNotBlank(listBo.getCategory()) && !"all".equalsIgnoreCase(listBo.getCategory())) {
            queryWrapper.eq(HotelEntity::getCategory, HotelCategory.normalize(listBo.getCategory()));
        }
        return queryWrapper;
    }

    private void applyAppListSort(LambdaQueryWrapper<HotelEntity> queryWrapper, HotelAppListBo listBo) {
        String sort = resolveAppListSort(listBo);
        if ("price".equals(sort)) {
            if ("desc".equalsIgnoreCase(StringUtils.trimToEmpty(listBo.getPriceOrder()))) {
                queryWrapper.orderByDesc(HotelEntity::getPriceFrom)
                        .orderByDesc(HotelEntity::getId);
            } else {
                queryWrapper.orderByAsc(HotelEntity::getPriceFrom)
                        .orderByDesc(HotelEntity::getId);
            }
            return;
        }
        queryWrapper.orderByAsc(HotelEntity::getSort)
                .orderByDesc(HotelEntity::getId);
    }

    private IPage<HotelAppVo> appPageByDistance(HotelAppListBo listBo) {
        BigDecimal refLat = MapDistanceUtil.parseCoordinate(listBo.getLat());
        BigDecimal refLng = MapDistanceUtil.parseCoordinate(listBo.getLng());
        boolean nearestFirst = !"desc".equalsIgnoreCase(StringUtils.trimToEmpty(listBo.getDistanceOrder()));
        List<HotelEntity> entities = iHotelService.list(buildAppListQueryWrapper(listBo));
        Comparator<HotelAppVo> comparator = Comparator.comparing(
                HotelAppVo::getDistance,
                Comparator.nullsLast(BigDecimal::compareTo));
        if (!nearestFirst) {
            comparator = comparator.reversed();
        }
        List<HotelAppVo> sorted = entities.stream()
                .map(entity -> toAppListVo(entity, refLat, refLng))
                .sorted(comparator.thenComparing(HotelAppVo::getId, Comparator.nullsLast(Long::compareTo).reversed()))
                .collect(Collectors.toList());

        long pageNo = listBo.getPage() != null && listBo.getPage() > 0 ? listBo.getPage() : 1L;
        long pageSize = listBo.getLimit() != null && listBo.getLimit() > 0 ? listBo.getLimit() : 10L;
        int fromIndex = (int) ((pageNo - 1) * pageSize);
        int toIndex = Math.min(fromIndex + (int) pageSize, sorted.size());
        List<HotelAppVo> pageRecords = fromIndex < sorted.size()
                ? sorted.subList(fromIndex, toIndex)
                : new ArrayList<>();

        Page<HotelAppVo> page = new Page<>(pageNo, pageSize, sorted.size());
        page.setRecords(pageRecords);
        return page;
    }

    private String resolveAppListSort(HotelAppListBo listBo) {
        String sort = StringUtils.trimToEmpty(listBo.getSort());
        if (StringUtils.isBlank(sort) || "all".equalsIgnoreCase(sort) || "featured".equalsIgnoreCase(sort)) {
            return "featured";
        }
        return sort.toLowerCase();
    }

    @Override
    public HotelAppDetailVo appDetail(Long id, Long userId) {
        HotelEntity entity = getOnlineHotel(id);
        HotelAppDetailVo vo = toAppDetailVo(entity);
        vo.setRoomList(listOnlineRooms(id));
        footprintService.recordAsync(userId, AssetTargetType.HOTEL.getCode(), id);
        return vo;
    }

    @Override
    public List<HotelRoomAppVo> appRooms(Long hotelId, String checkInDate, String checkOutDate) {
        getOnlineHotel(hotelId);
        List<HotelRoomAppVo> rooms = listOnlineRooms(hotelId);
        LocalDate checkIn = parseOptionalDate(checkInDate);
        LocalDate checkOut = parseOptionalDate(checkOutDate);
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return rooms;
        }
        for (HotelRoomAppVo vo : rooms) {
            if (vo.getId() == null) {
                continue;
            }
            HotelRoomEntity room = iHotelRoomService.getById(vo.getId());
            if (room == null) {
                continue;
            }
            try {
                HotelInventorySupport.QuoteResult quote = hotelInventorySupport.quote(room, checkIn, checkOut, 1);
                vo.setQuoteTotal(quote.getTotalAmount());
                vo.setQuoteAvailable(resolveQuoteAvailable(room, checkIn, checkOut));
                vo.setPrice(quote.averageNightlyPrice());
            } catch (BizException ex) {
                vo.setQuoteTotal(null);
                vo.setQuoteAvailable(0);
            }
        }
        return rooms;
    }

    @Override
    public List<HotelRoomCalendarDayVo> merchantRoomCalendar(Long userId, Long roomId, String startDate, String endDate) {
        HotelRoomEntity room = requireOwnedRoom(userId, roomId);
        LocalDate start = parseRequiredDate(startDate, "开始日期");
        LocalDate end = parseRequiredDate(endDate, "结束日期");
        if (end.isBefore(start)) {
            throw new BizException(AppError.HOTEL_CALENDAR_INVALID);
        }
        Map<LocalDate, HotelRoomCalendarEntity> map =
                iHotelRoomCalendarService.mapByRoomAndDates(roomId, start, end.plusDays(1));
        List<HotelRoomCalendarDayVo> result = new ArrayList<>();
        LocalDate day = start;
        while (!day.isAfter(end)) {
            HotelRoomCalendarDayVo vo = new HotelRoomCalendarDayVo();
            vo.setDate(day.format(DATE_FMT));
            HotelRoomCalendarEntity entity = map.get(day);
            if (entity != null) {
                vo.setPrice(entity.getPrice());
                vo.setStock(entity.getStock());
                vo.setStatus(entity.getStatus());
            } else {
                vo.setPrice(room.getPrice());
                vo.setStock(room.getStock());
                vo.setStatus(HotelRoomCalendarEntity.STATUS_OPEN);
            }
            result.add(vo);
            day = day.plusDays(1);
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void merchantSaveRoomCalendar(Long userId, Long roomId, HotelRoomCalendarSaveBo saveBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        merchantWalletService.assertDepositPaid(merchant.getId());
        HotelRoomEntity room = requireOwnedRoom(userId, roomId);
        LocalDate start = parseRequiredDate(saveBo.getStartDate(), "开始日期");
        LocalDate end = parseRequiredDate(saveBo.getEndDate(), "结束日期");
        if (end.isBefore(start)) {
            throw new BizException(AppError.HOTEL_CALENDAR_INVALID);
        }
        if (CollectionUtils.isNotEmpty(saveBo.getDays())) {
            for (HotelRoomCalendarSaveBo.DayItem dayItem : saveBo.getDays()) {
                upsertCalendarDay(room, parseRequiredDate(dayItem.getDate(), "日期"),
                        dayItem.getPrice(), dayItem.getStock(), dayItem.getStatus());
            }
            return;
        }
        LocalDate day = start;
        while (!day.isAfter(end)) {
            upsertCalendarDay(room, day, saveBo.getPrice(), saveBo.getStock(), saveBo.getStatus());
            day = day.plusDays(1);
        }
    }

    private int resolveQuoteAvailable(HotelRoomEntity room, LocalDate checkIn, LocalDate checkOut) {
        return hotelInventorySupport.resolveAvailableForStay(room, checkIn, checkOut);
    }

    private HotelRoomEntity requireOwnedRoom(Long userId, Long roomId) {
        com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity merchant =
                merchantAuthSupport.requireApprovedMerchant(userId);
        HotelRoomEntity room = iHotelRoomService.getById(roomId);
        if (room == null || !Objects.equals(room.getMerchantId(), merchant.getId())) {
            throw new BizException(AppError.HOTEL_ROOM_NOT_FOUND);
        }
        return room;
    }

    private void upsertCalendarDay(HotelRoomEntity room, LocalDate day,
                                   BigDecimal price, Integer stock, Integer status) {
        LambdaQueryWrapper<HotelRoomCalendarEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomCalendarEntity::getRoomId, room.getId())
                .eq(HotelRoomCalendarEntity::getCalendarDate, day)
                .last("LIMIT 1");
        HotelRoomCalendarEntity entity = iHotelRoomCalendarService.getOne(queryWrapper, false);
        if (entity == null) {
            entity = new HotelRoomCalendarEntity();
            entity.setRoomId(room.getId());
            entity.setHotelId(room.getHotelId());
            entity.setMerchantId(room.getMerchantId());
            entity.setCalendarDate(day);
        }
        if (price != null) {
            entity.setPrice(price);
        }
        if (stock != null) {
            entity.setStock(stock);
        }
        if (status != null) {
            entity.setStatus(status);
        } else if (entity.getStatus() == null) {
            entity.setStatus(HotelRoomCalendarEntity.STATUS_OPEN);
        }
        iHotelRoomCalendarService.saveOrUpdate(entity);
    }

    private LocalDate parseOptionalDate(String text) {
        if (StringUtils.isBlank(text)) {
            return null;
        }
        try {
            return LocalDate.parse(text.trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new BizException(AppError.ORDER_DATE_INVALID);
        }
    }

    private LocalDate parseRequiredDate(String text, String label) {
        if (StringUtils.isBlank(text)) {
            throw new BizException(AppError.ORDER_DATE_INVALID);
        }
        try {
            return LocalDate.parse(text.trim(), DATE_FMT);
        } catch (DateTimeParseException ex) {
            throw new BizException(AppError.ORDER_DATE_INVALID);
        }
    }

    @Override
    public List<HotelRoomAppVo> appRooms(Long hotelId) {
        return appRooms(hotelId, null, null);
    }

    @Override
    public HotelRoomDetailAppVo appRoomDetail(Long hotelId, Long roomId, Long userId) {
        HotelEntity hotel = getOnlineHotel(hotelId);
        HotelRoomEntity room = getOnlineRoom(hotelId, roomId);
        HotelRoomDetailAppVo vo = new HotelRoomDetailAppVo();
        fillAppRoomVo(vo, room);
        vo.setHotelId(hotel.getId());
        vo.setHotelName(hotel.getName());
        vo.setHotelPhone(hotel.getPhone());
        vo.setCheckInTime(hotel.getCheckInTime());
        vo.setCheckOutTime(hotel.getCheckOutTime());
        footprintService.recordAsync(userId, AssetTargetType.HOTEL.getCode(), hotelId);
        return vo;
    }

    @Override
    public IPage<HotelVo> pageInfo(HotelPageBo pageBo) {
        Page<HotelEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<HotelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(pageBo.getId()), HotelEntity::getId, pageBo.getId())
                .eq(Objects.nonNull(pageBo.getMerchantId()), HotelEntity::getMerchantId, pageBo.getMerchantId())
                .like(StringUtils.isNotBlank(pageBo.getName()), HotelEntity::getName, pageBo.getName())
                .eq(StringUtils.isNotBlank(pageBo.getCategory()), HotelEntity::getCategory, HotelCategory.normalize(pageBo.getCategory()))
                .eq(Objects.nonNull(pageBo.getDestinationId()), HotelEntity::getDestinationId, pageBo.getDestinationId())
                .eq(Objects.nonNull(pageBo.getStatus()), HotelEntity::getStatus, pageBo.getStatus());

        List<Long> merchantIds = resolveMerchantIds(pageBo.getMerchantName());
        if (merchantIds != null) {
            if (merchantIds.isEmpty()) {
                return PageUtil.copyPage(page, new ArrayList<>());
            }
            queryWrapper.in(HotelEntity::getMerchantId, merchantIds);
        }

        queryWrapper.orderByAsc(HotelEntity::getSort)
                .orderByDesc(HotelEntity::getCreateTime);

        IPage<HotelEntity> entityPage = iHotelService.page(page, queryWrapper);
        Map<Long, String> destinationNameMap = loadDestinationNameMap(entityPage.getRecords());
        Map<Long, String> merchantNameMap = loadMerchantNameMap(entityPage.getRecords());
        List<HotelVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HotelEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, destinationNameMap, merchantNameMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(HotelSaveBo saveBo) {
        validateCategory(saveBo.getCategory());
        Long destinationId = publishDestinationResolver.requireFromCoordinatesOrMerchant(
                saveBo.getLatitude(), saveBo.getLongitude(), saveBo.getMerchantId());
        validateMerchant(saveBo.getMerchantId(), destinationId);

        HotelEntity entity = new HotelEntity();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setDestinationId(destinationId);
        entity.setCategory(HotelCategory.normalize(saveBo.getCategory()));
        fillHotelDefaults(entity);
        iHotelService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(HotelUpdateBo updateBo) {
        HotelEntity existing = iHotelService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.HOTEL_NOT_FOUND);
        }
        validateCategory(updateBo.getCategory());
        Long destinationId = publishDestinationResolver.requireFromCoordinatesOrMerchant(
                updateBo.getLatitude(), updateBo.getLongitude(), updateBo.getMerchantId());
        validateMerchant(updateBo.getMerchantId(), destinationId);

        HotelEntity entity = new HotelEntity();
        BeanUtils.copyProperties(updateBo, entity);
        entity.setDestinationId(destinationId);
        entity.setCategory(HotelCategory.normalize(updateBo.getCategory()));
        iHotelService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(HotelInfoBo infoBo) {
        HotelEntity existing = iHotelService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.HOTEL_NOT_FOUND);
        }
        iHotelService.removeById(infoBo.getId());

        LambdaQueryWrapper<HotelRoomEntity> roomQuery = new LambdaQueryWrapper<>();
        roomQuery.eq(HotelRoomEntity::getHotelId, infoBo.getId());
        iHotelRoomService.remove(roomQuery);
    }

    @Override
    public IPage<HotelRoomVo> roomPageInfo(HotelRoomPageBo pageBo) {
        HotelEntity hotel = iHotelService.getById(pageBo.getHotelId());
        if (hotel == null) {
            throw new BizException(AppError.HOTEL_NOT_FOUND);
        }

        Page<HotelRoomEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<HotelRoomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomEntity::getHotelId, pageBo.getHotelId())
                .like(StringUtils.isNotBlank(pageBo.getName()), HotelRoomEntity::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getStatus()), HotelRoomEntity::getStatus, pageBo.getStatus())
                .orderByAsc(HotelRoomEntity::getSort)
                .orderByDesc(HotelRoomEntity::getId);

        IPage<HotelRoomEntity> entityPage = iHotelRoomService.page(page, queryWrapper);
        MerchantEntity merchant = iMerchantService.getById(hotel.getMerchantId());
        String merchantName = merchant == null ? "" : merchant.getStoreName();
        List<HotelRoomVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HotelRoomEntity record : entityPage.getRecords()) {
                voList.add(toAdminRoomVo(record, hotel.getName(), merchantName));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveRoom(HotelRoomSaveBo saveBo) {
        HotelEntity hotel = iHotelService.getById(saveBo.getHotelId());
        if (hotel == null) {
            throw new BizException(AppError.HOTEL_NOT_FOUND);
        }
        if (!Objects.equals(hotel.getMerchantId(), saveBo.getMerchantId())) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }

        HotelRoomEntity entity = new HotelRoomEntity();
        BeanUtils.copyProperties(saveBo, entity);
        fillRoomDefaults(entity);
        iHotelRoomService.save(entity);
        refreshHotelPriceFrom(hotel.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRoom(HotelRoomUpdateBo updateBo) {
        HotelRoomEntity existing = iHotelRoomService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.HOTEL_ROOM_NOT_FOUND);
        }
        HotelEntity hotel = iHotelService.getById(updateBo.getHotelId());
        if (hotel == null) {
            throw new BizException(AppError.HOTEL_NOT_FOUND);
        }
        if (!Objects.equals(hotel.getMerchantId(), updateBo.getMerchantId())) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }

        HotelRoomEntity entity = new HotelRoomEntity();
        BeanUtils.copyProperties(updateBo, entity);
        iHotelRoomService.updateById(entity);
        refreshHotelPriceFrom(hotel.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRoom(HotelRoomInfoBo infoBo) {
        HotelRoomEntity existing = iHotelRoomService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.HOTEL_ROOM_NOT_FOUND);
        }
        Long hotelId = existing.getHotelId();
        iHotelRoomService.removeById(infoBo.getId());
        refreshHotelPriceFrom(hotelId);
    }

    private HotelEntity getOnlineHotel(Long id) {
        HotelEntity entity = iHotelService.getById(id);
        if (entity == null || !Objects.equals(entity.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.HOTEL_NOT_FOUND);
        }
        return entity;
    }

    private HotelRoomEntity getOnlineRoom(Long hotelId, Long roomId) {
        HotelRoomEntity room = iHotelRoomService.getById(roomId);
        if (room == null
                || !Objects.equals(room.getHotelId(), hotelId)
                || !Objects.equals(room.getStatus(), ROOM_ON_SALE)) {
            throw new BizException(AppError.HOTEL_ROOM_NOT_FOUND);
        }
        return room;
    }

    private List<HotelRoomAppVo> listOnlineRooms(Long hotelId) {
        LambdaQueryWrapper<HotelRoomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomEntity::getHotelId, hotelId)
                .eq(HotelRoomEntity::getStatus, ROOM_ON_SALE)
                .orderByAsc(HotelRoomEntity::getSort)
                .orderByAsc(HotelRoomEntity::getId);
        List<HotelRoomEntity> rooms = iHotelRoomService.list(queryWrapper);
        List<HotelRoomAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(rooms)) {
            for (HotelRoomEntity room : rooms) {
                voList.add(toAppRoomVo(room));
            }
        }
        return voList;
    }

    private void refreshHotelPriceFrom(Long hotelId) {
        LambdaQueryWrapper<HotelRoomEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelRoomEntity::getHotelId, hotelId)
                .eq(HotelRoomEntity::getStatus, ROOM_ON_SALE)
                .orderByAsc(HotelRoomEntity::getPrice)
                .last("LIMIT 1");
        HotelRoomEntity cheapest = iHotelRoomService.getOne(queryWrapper, false);
        HotelEntity update = new HotelEntity();
        update.setId(hotelId);
        update.setPriceFrom(cheapest == null ? null : cheapest.getPrice());
        iHotelService.updateById(update);
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

    private void validateCategory(String category) {
        String normalized = HotelCategory.normalize(category);
        if (StringUtils.isNotBlank(normalized) && !HotelCategory.isValid(normalized)) {
            throw new BizException(AppError.HOTEL_CATEGORY_INVALID);
        }
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

    private void fillHotelDefaults(HotelEntity entity) {
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getScore() == null) {
            entity.setScore(BigDecimal.ZERO);
        }
    }

    private void fillRoomDefaults(HotelRoomEntity entity) {
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getStock() == null) {
            entity.setStock(10);
        }
    }

    private Map<Long, String> loadDestinationNameMap(List<HotelEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> ids = records.stream()
                .map(HotelEntity::getDestinationId)
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

    private Map<Long, String> loadMerchantNameMap(List<HotelEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        List<Long> ids = records.stream()
                .map(HotelEntity::getMerchantId)
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

    private HomeRecommendItemVo toRecommendItem(HotelEntity entity) {
        HomeRecommendItemVo vo = new HomeRecommendItemVo();
        vo.setId(entity.getId());
        vo.setTitle(entity.getName());
        vo.setCover(StringUtils.defaultString(entity.getCoverImage()));
        vo.setPrice(entity.getPriceFrom());
        vo.setTag(RecommendTagUtil.resolveTag(entity.getTag()));
        vo.setAddress(StringUtils.defaultString(entity.getAddress()));
        return vo;
    }

    private HotelAppVo toAppListVo(HotelEntity entity) {
        return toAppListVo(entity, null, null);
    }

    private HotelAppVo toAppListVo(HotelEntity entity, BigDecimal refLat, BigDecimal refLng) {
        HotelAppVo vo = new HotelAppVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setCategory(entity.getCategory());
        vo.setCategoryLabel(HotelCategory.labelOf(entity.getCategory()));
        vo.setStarLevel(entity.getStarLevel());
        vo.setAddress(entity.getAddress());
        vo.setScore(entity.getScore());
        vo.setTag(entity.getTag());
        vo.setPriceFrom(entity.getPriceFrom());
        vo.setCoverImage(entity.getCoverImage());
        if (refLat != null && refLng != null) {
            vo.setDistance(MapDistanceUtil.distanceKm(refLat, refLng, entity.getLatitude(), entity.getLongitude()));
        }
        return vo;
    }

    private HotelAppDetailVo toAppDetailVo(HotelEntity entity) {
        HotelAppDetailVo vo = new HotelAppDetailVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setStarLevel(entity.getStarLevel());
        vo.setStarLabel(StringUtils.isNotBlank(entity.getStarLabel())
                ? entity.getStarLabel()
                : HotelStarUtil.labelOf(entity.getStarLevel()));
        vo.setAddress(entity.getAddress());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setPhone(entity.getPhone());
        vo.setScore(entity.getScore());
        vo.setTag(entity.getTag());
        vo.setPriceFrom(entity.getPriceFrom());
        vo.setBannerImage(entity.getCoverImage());
        vo.setBannerImages(parseBannerImages(entity.getBannerImages(), entity.getCoverImage()));
        vo.setCheckInTime(entity.getCheckInTime());
        vo.setCheckOutTime(entity.getCheckOutTime());
        vo.setFacilities(parseFacilities(entity.getFacilities()));
        vo.setIntro(entity.getIntro());
        vo.setPolicy(HotelPolicyUtil.parse(entity.getPolicyJson()));
        return vo;
    }

    private HotelRoomAppVo toAppRoomVo(HotelRoomEntity entity) {
        HotelRoomAppVo vo = new HotelRoomAppVo();
        fillAppRoomVo(vo, entity);
        return vo;
    }

    private void fillAppRoomVo(HotelRoomAppVo vo, HotelRoomEntity entity) {
        HotelRoomStandardEntity standard = entity.getRoomStandardId() == null
                ? null
                : iHotelRoomStandardService.getById(entity.getRoomStandardId());
        Map<String, Object> detailMap = HotelRoomDetailUtil.resolveEffectiveDetail(
                standard, entity.getRoomDetailJson(), resolveRoomServiceDescription(entity));
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setSpec(entity.getSpec());
        vo.setPrice(entity.getPrice());
        vo.setStock(entity.getStock());
        vo.setCoverImage(entity.getCoverImage());
        vo.setGallery(HotelRoomDetailUtil.parseGallery(detailMap, entity.getCoverImage()));
        vo.setDesc(readDetailString(detailMap, "desc"));
        vo.setArea(readDetailString(detailMap, "area"));
        vo.setFloor(readDetailString(detailMap, "floor"));
        vo.setOccupancy(readDetailInteger(detailMap, "occupancy"));
        vo.setBedType(readDetailString(detailMap, "bedType"));
        vo.setBedCount(readDetailInteger(detailMap, "bedCount"));
        vo.setBedSize(readDetailString(detailMap, "bedSize"));
        vo.setExtraBedPolicy(readDetailString(detailMap, "extraBedPolicy"));
        vo.setPetPolicy(readDetailString(detailMap, "petPolicy"));
        vo.setWindowType(readDetailString(detailMap, "windowType"));
        vo.setSmokingPolicy(readDetailString(detailMap, "smokingPolicy"));
        vo.setFacilities(HotelRoomDetailUtil.parseFacilities(detailMap));
        vo.setTags(HotelRoomDetailUtil.parseTags(detailMap));
    }

    private String readDetailString(Map<String, Object> detailMap, String key) {
        if (detailMap == null || detailMap.get(key) == null) {
            return "";
        }
        return StringUtils.trimToEmpty(String.valueOf(detailMap.get(key)));
    }

    private Integer readDetailInteger(Map<String, Object> detailMap, String key) {
        if (detailMap == null || detailMap.get(key) == null) {
            return null;
        }
        Object value = detailMap.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value).trim());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String resolveRoomServiceDescription(HotelRoomEntity entity) {
        if (entity == null || entity.getMerchantServiceId() == null) {
            return null;
        }
        MerchantServiceEntity service = iMerchantServiceItemService.getById(entity.getMerchantServiceId());
        return service == null ? null : StringUtils.trimToNull(service.getDescription());
    }

    private Boolean readDetailBoolean(Map<String, Object> detailMap, String key) {
        if (detailMap == null || detailMap.get(key) == null) {
            return null;
        }
        Object value = detailMap.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }

    private HotelVo toAdminVo(HotelEntity entity, Map<Long, String> destinationNameMap, Map<Long, String> merchantNameMap) {
        HotelVo vo = new HotelVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setCategoryLabel(HotelCategory.labelOf(entity.getCategory()));
        vo.setStatusLabel(HotelStatus.labelOf(entity.getStatus()));
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destinationNameMap, entity.getDestinationId(), entity.getAddress()));
        vo.setMerchantName(merchantNameMap.getOrDefault(entity.getMerchantId(), ""));
        return vo;
    }

    private HotelRoomVo toAdminRoomVo(HotelRoomEntity entity, String hotelName, String merchantName) {
        HotelRoomVo vo = new HotelRoomVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setHotelName(hotelName);
        vo.setMerchantName(merchantName);
        vo.setStatusLabel(HotelRoomStatus.labelOf(entity.getStatus()));
        return vo;
    }

    private List<String> parseBannerImages(String bannerImagesJson, String coverImage) {
        List<String> images = new ArrayList<>();
        if (StringUtils.isNotBlank(bannerImagesJson)) {
            try {
                List<String> parsed = JSON.parseArray(bannerImagesJson, String.class);
                if (CollectionUtils.isNotEmpty(parsed)) {
                    images.addAll(parsed.stream().filter(StringUtils::isNotBlank).collect(Collectors.toList()));
                }
            } catch (Exception ignored) {
                // 非法 JSON 时忽略，避免详情接口失败
            }
        }
        if (images.isEmpty() && StringUtils.isNotBlank(coverImage)) {
            images.add(coverImage);
        }
        return images;
    }

    @Override
    public IPage<ReviewAppVo> appReviews(HotelReviewAppListBo listBo) {
        getOnlineHotel(listBo.getHotelId());
        Page<HotelReviewEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<HotelReviewEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelReviewEntity::getHotelId, listBo.getHotelId())
                .orderByDesc(HotelReviewEntity::getCreateTime)
                .orderByDesc(HotelReviewEntity::getId);

        IPage<HotelReviewEntity> entityPage = iHotelReviewService.page(page, queryWrapper);
        Map<Long, String> avatarMap = loadReviewAvatarMap(entityPage.getRecords());
        List<ReviewAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HotelReviewEntity record : entityPage.getRecords()) {
                voList.add(toReviewVo(record, avatarMap.get(record.getUserId())));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewAppVo submitReview(Long userId, Long hotelId, HotelReviewSubmitBo submitBo) {
        getOnlineHotel(hotelId);
        UserEntity user = requireUser(userId);
        OrderEntity order = requireReviewableHotelOrder(userId, submitBo.getOrderId(), hotelId);
        assertHotelNotReviewed(submitBo.getOrderId(), userId);

        HotelReviewEntity entity = new HotelReviewEntity();
        entity.setHotelId(hotelId);
        entity.setOrderId(submitBo.getOrderId());
        entity.setUserId(userId);
        entity.setUserName(StringUtils.defaultIfBlank(user.getUserName(), user.getPhone()));
        entity.setContent(StringUtils.trim(submitBo.getContent()));
        entity.setRating(submitBo.getRating());
        entity.setImages(FeedbackJsonUtil.toJson(normalizeReviewImages(submitBo.getImages())));
        iHotelReviewService.save(entity);
        return toReviewVo(entity, user.getAvatar());
    }

    private Map<Long, String> loadReviewAvatarMap(List<HotelReviewEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyMap();
        }
        Set<Long> userIds = new HashSet<>();
        for (HotelReviewEntity record : records) {
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

    private ReviewAppVo toReviewVo(HotelReviewEntity entity, String avatar) {
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

    private UserEntity requireUser(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        return user;
    }

    private OrderEntity requireReviewableHotelOrder(Long userId, Long orderId, Long hotelId) {
        OrderEntity order = iOrderService.getById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(AppError.ORDER_NOT_FOUND);
        }
        OrderType type = OrderTypeSupport.resolve(order, iOrderItemService.listByOrderIds(Collections.singletonList(orderId)));
        if (!OrderTypeSupport.isHotelFlow(type)) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        if (order.getStatus() == null || order.getStatus() != MerchantOrderStatusGroup.COMPLETED) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        if (!AppOrderStatusSupport.isHotelCheckedIn(order)) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        Long orderHotelId = resolveHotelIdFromOrder(orderId);
        if (orderHotelId == null || !Objects.equals(orderHotelId, hotelId)) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        return order;
    }

    private Long resolveHotelIdFromOrder(Long orderId) {
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(orderId));
        if (CollectionUtils.isEmpty(items)) {
            return null;
        }
        for (OrderItemEntity item : items) {
            if (!"room".equals(item.getItemType()) || item.getItemId() == null) {
                continue;
            }
            HotelRoomEntity room = iHotelRoomService.getById(item.getItemId());
            if (room != null && room.getHotelId() != null) {
                return room.getHotelId();
            }
        }
        return null;
    }

    private void assertHotelNotReviewed(Long orderId, Long userId) {
        LambdaQueryWrapper<HotelReviewEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelReviewEntity::getOrderId, orderId)
                .eq(HotelReviewEntity::getUserId, userId)
                .last("LIMIT 1");
        if (iHotelReviewService.count(queryWrapper) > 0) {
            throw new BizException(AppError.ORDER_REVIEW_DUPLICATE);
        }
    }

    private List<HotelFacilityVo> parseFacilities(String facilitiesJson) {
        if (StringUtils.isBlank(facilitiesJson)) {
            return new ArrayList<>();
        }
        try {
            List<HotelFacilityVo> facilities = JSON.parseArray(facilitiesJson, HotelFacilityVo.class);
            return facilities == null ? new ArrayList<>() : facilities;
        } catch (Exception ignored) {
            try {
                List<JSONObject> objects = JSON.parseArray(facilitiesJson, JSONObject.class);
                List<HotelFacilityVo> facilities = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(objects)) {
                    for (JSONObject object : objects) {
                        HotelFacilityVo vo = new HotelFacilityVo();
                        vo.setId(object.getLong("id"));
                        vo.setIcon(object.getString("icon"));
                        vo.setLabel(object.getString("label"));
                        facilities.add(vo);
                    }
                }
                return facilities;
            } catch (Exception innerIgnored) {
                return new ArrayList<>();
            }
        }
    }
}

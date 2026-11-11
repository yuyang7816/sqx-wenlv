package com.sqx.app.lvyou.goose.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.canteen.support.CanteenMerchantStatsSupport;
import com.sqx.app.lvyou.goose.domain.bo.GooseAppListBo;
import com.alibaba.fastjson.JSON;
import com.sqx.app.lvyou.goose.domain.vo.GooseAppDetailVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseAppVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseBusinessHourVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseDishDetailVo;
import com.sqx.app.lvyou.goose.domain.vo.GooseMenuItemVo;
import com.sqx.app.lvyou.favorite.enums.AssetTargetType;
import com.sqx.app.lvyou.favorite.service.FootprintService;
import com.sqx.app.lvyou.goose.service.GooseService;
import com.sqx.app.lvyou.goose.support.GooseStoreTypeSupport;
import com.sqx.app.lvyou.goose.util.GooseDecorUtil;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantStoreDecorService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantStoreDecorEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantServiceStatus;
import com.sqx.app.lvyou.merchant.support.RestaurantQueueSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 灰鹅美食业务服务实现
 */
@Service
public class GooseServiceImpl implements GooseService {

    private static final int MERCHANT_STATUS_NORMAL = 1;
    private static final int SPECIALTY_PREVIEW_LIMIT = 4;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantServiceItemService iMerchantServiceItemService;

    @Resource
    private IMerchantStoreDecorService iMerchantStoreDecorService;

    @Resource
    private FootprintService footprintService;

    @Resource
    private CanteenMerchantStatsSupport canteenMerchantStatsSupport;

    @Override
    public IPage<GooseAppVo> appPage(GooseAppListBo listBo) {
        Page<MerchantEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .in(MerchantEntity::getStoreType, GooseStoreTypeSupport.foodStoreTypes())
                .eq(Objects.nonNull(listBo.getDestinationId()), MerchantEntity::getDestinationId, listBo.getDestinationId())
                .like(StringUtils.isNotBlank(listBo.getKeyword()), MerchantEntity::getStoreName, listBo.getKeyword())
                .orderByDesc(MerchantEntity::getId);

        IPage<MerchantEntity> entityPage = iMerchantService.page(page, queryWrapper);
        List<MerchantEntity> records = entityPage.getRecords();
        Map<Long, Map<String, Object>> decorMap = loadDecorMapBatch(records);
        Map<Long, BigDecimal> minPriceMap = loadMinPriceMap(records);
        List<Long> merchantIds = records.stream().map(MerchantEntity::getId).collect(Collectors.toList());
        Map<Long, Integer> reservationCountMap = canteenMerchantStatsSupport.countReservations(merchantIds);

        List<GooseAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            for (MerchantEntity record : records) {
                voList.add(toListVo(record, decorMap.get(record.getId()), minPriceMap.get(record.getId()),
                        reservationCountMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public GooseAppDetailVo appDetail(Long id, Long userId) {
        MerchantEntity merchant = getOnlineFoodMerchant(id);
        Map<String, Object> decor = loadDecorMap(merchant.getId());
        List<GooseMenuItemVo> menuItems = listOnSaleMenu(merchant.getId());
        Map<Long, Integer> reservationCountMap = canteenMerchantStatsSupport.countReservations(
                Collections.singletonList(merchant.getId()));
        GooseAppDetailVo vo = toDetailVo(merchant, decor, resolveMinPrice(menuItems), reservationCountMap);
        if (menuItems.size() > SPECIALTY_PREVIEW_LIMIT) {
            vo.setSpecialtyList(menuItems.subList(0, SPECIALTY_PREVIEW_LIMIT));
        } else {
            vo.setSpecialtyList(menuItems);
        }
        footprintService.recordAsync(userId, AssetTargetType.GOOSE.getCode(), id);
        return vo;
    }

    @Override
    public List<GooseMenuItemVo> appMenu(Long id) {
        MerchantEntity merchant = getOnlineFoodMerchant(id);
        return listOnSaleMenu(merchant.getId());
    }

    @Override
    public GooseDishDetailVo appDishDetail(Long serviceId) {
        if (serviceId == null) {
            throw new BizException(AppError.MERCHANT_SERVICE_NOT_FOUND);
        }
        MerchantServiceEntity entity = iMerchantServiceItemService.getById(serviceId);
        if (entity == null
                || !MerchantServiceStatus.ON_SALE.getCode().equals(entity.getStatus())) {
            throw new BizException(AppError.MERCHANT_SERVICE_NOT_FOUND);
        }
        MerchantEntity merchant = iMerchantService.getById(entity.getMerchantId());
        if (merchant == null
                || !Objects.equals(MERCHANT_STATUS_NORMAL, merchant.getStatus())
                || !GooseStoreTypeSupport.isFoodStore(merchant.getStoreType())) {
            throw new BizException(AppError.MERCHANT_SERVICE_NOT_FOUND);
        }
        GooseDishDetailVo vo = new GooseDishDetailVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDesc(StringUtils.defaultString(entity.getDescription()));
        vo.setPrice(formatPrice(entity.getPrice()));
        vo.setSold(entity.getMonthlySales() == null ? 0 : entity.getMonthlySales());
        vo.setCover(entity.getCoverImage());
        vo.setBanners(resolveServiceBanners(entity));
        Map<String, Object> decor = loadDecorMap(merchant.getId());
        vo.setShopId(merchant.getId());
        vo.setShopName(merchant.getStoreName());
        vo.setShopCover(GooseDecorUtil.resolveCover(decor));
        vo.setShopAddress(StringUtils.defaultString(merchant.getStoreAddress()));
        return vo;
    }

    private MerchantEntity getOnlineFoodMerchant(Long id) {
        MerchantEntity merchant = iMerchantService.getById(id);
        if (merchant == null
                || !Objects.equals(MERCHANT_STATUS_NORMAL, merchant.getStatus())
                || !GooseStoreTypeSupport.isFoodStore(merchant.getStoreType())) {
            throw new BizException(AppError.GOOSE_NOT_FOUND);
        }
        return merchant;
    }

    private List<GooseMenuItemVo> listOnSaleMenu(Long merchantId) {
        LambdaQueryWrapper<MerchantServiceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantServiceEntity::getMerchantId, merchantId)
                .eq(MerchantServiceEntity::getStatus, MerchantServiceStatus.ON_SALE.getCode())
                .orderByAsc(MerchantServiceEntity::getSort)
                .orderByAsc(MerchantServiceEntity::getId);
        List<MerchantServiceEntity> entities = iMerchantServiceItemService.list(queryWrapper);
        if (CollectionUtils.isEmpty(entities)) {
            return new ArrayList<>();
        }
        List<GooseMenuItemVo> items = new ArrayList<>();
        for (MerchantServiceEntity entity : entities) {
            items.add(toMenuItemVo(entity));
        }
        return items;
    }

    private GooseAppVo toListVo(MerchantEntity merchant, Map<String, Object> decor, BigDecimal priceFrom,
                                Map<Long, Integer> reservationCountMap) {
        Map<String, Object> safeDecor = decor == null ? Collections.emptyMap() : decor;
        GooseAppVo vo = new GooseAppVo();
        vo.setId(merchant.getId());
        vo.setMerchantCode(merchant.getMerchantCode());
        vo.setName(merchant.getStoreName());
        Long merchantId = merchant.getId();
        vo.setMonthlySales(reservationCountMap.getOrDefault(merchantId, 0));
        vo.setRating("");
        vo.setAddress(merchant.getStoreAddress());
        vo.setLatitude(merchant.getLatitude());
        vo.setLongitude(merchant.getLongitude());
        vo.setPhone(StringUtils.defaultIfBlank(merchant.getContactPhone(), ""));
        vo.setTags(GooseDecorUtil.parseTags(safeDecor));
        vo.setPriceFrom(priceFrom);
        vo.setDestinationId(merchant.getDestinationId());
        vo.setCover(GooseDecorUtil.resolveCover(safeDecor));
        applyQueueFields(vo, merchant);
        return vo;
    }

    private void applyQueueFields(GooseAppVo vo, MerchantEntity merchant) {
        if (vo == null || merchant == null) {
            return;
        }
        vo.setIsOpen(merchant.getIsOpen());
        boolean queueEnabled = Objects.equals(merchant.getQueueEnabled(), RestaurantQueueSupport.QUEUE_ENABLED);
        int tableCount = merchant.getQueueTableCount() == null ? 0 : merchant.getQueueTableCount();
        vo.setQueueEnabled(queueEnabled);
        vo.setQueueTableCount(queueEnabled ? tableCount : 0);
        vo.setQueueDisplayText(RestaurantQueueSupport.buildDisplayText(
                merchant.getIsOpen(),
                merchant.getQueueEnabled(),
                merchant.getQueueTableCount()));
    }

    private GooseAppDetailVo toDetailVo(MerchantEntity merchant, Map<String, Object> decor, BigDecimal priceFrom,
                                        Map<Long, Integer> reservationCountMap) {
        GooseAppDetailVo vo = new GooseAppDetailVo();
        GooseAppVo base = toListVo(merchant, decor, priceFrom, reservationCountMap);
        vo.setId(base.getId());
        vo.setMerchantCode(base.getMerchantCode());
        vo.setName(base.getName());
        vo.setBadgeText(GooseDecorUtil.resolveBadgeText(decor, "非遗美食"));
        vo.setRating(base.getRating());
        vo.setMonthlySales(base.getMonthlySales());
        vo.setAddress(base.getAddress());
        vo.setLatitude(base.getLatitude());
        vo.setLongitude(base.getLongitude());
        vo.setPhone(base.getPhone());
        vo.setTags(base.getTags());
        vo.setPriceFrom(base.getPriceFrom());
        vo.setDestinationId(base.getDestinationId());
        vo.setCover(base.getCover());
        vo.setIntro(GooseDecorUtil.resolveIntro(decor, merchant.getStoreName()));
        vo.setBannerList(GooseDecorUtil.parseBannerList(decor));
        vo.setBusinessHours(toBusinessHourVos(GooseDecorUtil.parseBusinessHours(decor, merchant.getStoreHours())));
        vo.setIsOpen(merchant.getIsOpen());
        vo.setQueueEnabled(base.getQueueEnabled());
        vo.setQueueTableCount(base.getQueueTableCount());
        vo.setQueueDisplayText(base.getQueueDisplayText());
        return vo;
    }

    private List<GooseBusinessHourVo> toBusinessHourVos(List<Map<String, Object>> hours) {
        if (CollectionUtils.isEmpty(hours)) {
            return new ArrayList<>();
        }
        List<GooseBusinessHourVo> result = new ArrayList<>();
        for (Map<String, Object> hour : hours) {
            GooseBusinessHourVo vo = new GooseBusinessHourVo();
            Object id = hour.get("id");
            if (id instanceof Number) {
                vo.setId(((Number) id).longValue());
            } else if (id != null) {
                try {
                    vo.setId(Long.parseLong(String.valueOf(id)));
                } catch (NumberFormatException ex) {
                    vo.setId(null);
                }
            }
            vo.setLabel(hour.get("label") == null ? null : String.valueOf(hour.get("label")));
            vo.setTime(hour.get("time") == null ? null : String.valueOf(hour.get("time")));
            result.add(vo);
        }
        return result;
    }

    private GooseMenuItemVo toMenuItemVo(MerchantServiceEntity entity) {
        GooseMenuItemVo vo = new GooseMenuItemVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDesc(StringUtils.defaultString(entity.getDescription()));
        vo.setPrice(formatPrice(entity.getPrice()));
        vo.setSold(entity.getMonthlySales() == null ? 0 : entity.getMonthlySales());
        vo.setCover(entity.getCoverImage());
        vo.setBanners(resolveServiceBanners(entity));
        return vo;
    }

    private List<String> resolveServiceBanners(MerchantServiceEntity entity) {
        List<String> banners = parseBannerImages(entity.getBannerImages());
        if (CollectionUtils.isNotEmpty(banners)) {
            return banners;
        }
        if (StringUtils.isNotBlank(entity.getCoverImage())) {
            return Collections.singletonList(entity.getCoverImage());
        }
        return Collections.emptyList();
    }

    private List<String> parseBannerImages(String bannerImagesJson) {
        if (StringUtils.isBlank(bannerImagesJson)) {
            return Collections.emptyList();
        }
        String raw = bannerImagesJson.trim();
        if (!raw.startsWith("[")) {
            return Collections.emptyList();
        }
        try {
            List<String> parsed = JSON.parseArray(raw, String.class);
            if (CollectionUtils.isEmpty(parsed)) {
                return Collections.emptyList();
            }
            List<String> result = new ArrayList<>();
            for (String item : parsed) {
                if (StringUtils.isNotBlank(item)) {
                    result.add(item.trim());
                }
            }
            return result;
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }

    private String formatPrice(BigDecimal price) {
        if (price == null) {
            return "0";
        }
        return price.setScale(0, RoundingMode.HALF_UP).toPlainString();
    }

    private BigDecimal resolveMinPrice(List<GooseMenuItemVo> menuItems) {
        if (CollectionUtils.isEmpty(menuItems)) {
            return BigDecimal.ZERO;
        }
        return menuItems.stream()
                .map(item -> parsePrice(item.getPrice()))
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal parsePrice(String price) {
        if (StringUtils.isBlank(price)) {
            return BigDecimal.ZERO;
        }
        try {
            return new BigDecimal(price.trim());
        } catch (NumberFormatException ex) {
            return BigDecimal.ZERO;
        }
    }

    private Map<Long, Map<String, Object>> loadDecorMapBatch(List<MerchantEntity> merchants) {
        if (CollectionUtils.isEmpty(merchants)) {
            return Collections.emptyMap();
        }
        List<Long> merchantIds = merchants.stream().map(MerchantEntity::getId).collect(Collectors.toList());
        LambdaQueryWrapper<MerchantStoreDecorEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantStoreDecorEntity::getMerchantId, merchantIds);
        List<MerchantStoreDecorEntity> decorEntities = iMerchantStoreDecorService.list(queryWrapper);
        Map<Long, Map<String, Object>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(decorEntities)) {
            for (MerchantStoreDecorEntity entity : decorEntities) {
                result.put(entity.getMerchantId(), GooseDecorUtil.parseDecor(entity.getDecorJson()));
            }
        }
        return result;
    }

    private Map<String, Object> loadDecorMap(Long merchantId) {
        LambdaQueryWrapper<MerchantStoreDecorEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantStoreDecorEntity::getMerchantId, merchantId)
                .last("LIMIT 1");
        MerchantStoreDecorEntity entity = iMerchantStoreDecorService.getOne(queryWrapper, false);
        if (entity == null) {
            return Collections.emptyMap();
        }
        return GooseDecorUtil.parseDecor(entity.getDecorJson());
    }

    private Map<Long, BigDecimal> loadMinPriceMap(List<MerchantEntity> merchants) {
        if (CollectionUtils.isEmpty(merchants)) {
            return Collections.emptyMap();
        }
        List<Long> merchantIds = merchants.stream().map(MerchantEntity::getId).collect(Collectors.toList());
        LambdaQueryWrapper<MerchantServiceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantServiceEntity::getMerchantId, merchantIds)
                .eq(MerchantServiceEntity::getStatus, MerchantServiceStatus.ON_SALE.getCode())
                .select(MerchantServiceEntity::getMerchantId, MerchantServiceEntity::getPrice);
        List<MerchantServiceEntity> services = iMerchantServiceItemService.list(queryWrapper);
        Map<Long, BigDecimal> result = new HashMap<>();
        if (CollectionUtils.isEmpty(services)) {
            return result;
        }
        for (MerchantServiceEntity service : services) {
            BigDecimal price = service.getPrice() == null ? BigDecimal.ZERO : service.getPrice();
            result.merge(service.getMerchantId(), price, (left, right) -> left.compareTo(right) <= 0 ? left : right);
        }
        return result;
    }
}

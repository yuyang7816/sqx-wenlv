package com.sqx.app.lvyou.heritage.goods.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.heritage.enums.HeritageStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.app.lvyou.heritage.goods.db.service.IGoodsService;
import com.sqx.app.lvyou.heritage.goods.db.service.IReviewService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantServiceStatus;
import com.sqx.app.lvyou.heritage.goods.domain.bo.*;
import com.sqx.app.lvyou.heritage.goods.domain.entity.GoodsEntity;
import com.sqx.app.lvyou.heritage.goods.domain.entity.ReviewEntity;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsAppVo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsVo;
import com.sqx.app.lvyou.heritage.goods.domain.vo.ReviewAppVo;
import com.sqx.app.lvyou.favorite.enums.AssetTargetType;
import com.sqx.app.lvyou.favorite.service.FootprintService;
import com.sqx.app.lvyou.feedback.util.FeedbackJsonUtil;
import com.sqx.app.lvyou.heritage.goods.service.GoodsService;
import com.sqx.app.lvyou.heritage.goods.domain.vo.GoodsSpecSkuVo;
import com.sqx.app.lvyou.heritage.goods.util.GoodsJsonUtil;
import com.sqx.app.lvyou.heritage.goods.util.GoodsSpecsUtil;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopShippingVo;
import com.sqx.app.lvyou.heritage.shop.support.HeritageShippingUtil;
import com.sqx.app.lvyou.heritage.util.HeritageJsonUtil;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 非遗商品业务服务实现
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    private static final int STATUS_ONLINE = HeritageStatus.ONLINE.getCode();
    private static final DateTimeFormatter REVIEW_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Resource
    private IGoodsService iGoodsService;

    @Resource
    private IReviewService iReviewService;

    @Resource
    private IHeritageShopService iHeritageShopService;

    @Resource
    private FootprintService footprintService;

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMerchantServiceItemService iMerchantServiceItemService;

    @Override
    public IPage<GoodsAppVo> appPage(GoodsAppListBo listBo) {
        Page<GoodsEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsEntity::getStatus, STATUS_ONLINE)
                .eq(Objects.nonNull(listBo.getShopId()), GoodsEntity::getShopId, listBo.getShopId())
                .like(StringUtils.isNotBlank(listBo.getName()), GoodsEntity::getName, listBo.getName());
        applyLegacyCategoryFilter(queryWrapper, listBo);
        applyAppListSort(queryWrapper, listBo);

        IPage<GoodsEntity> entityPage = iGoodsService.page(page, queryWrapper);
        Map<Long, String> shopNameMap = loadShopNameMap(entityPage.getRecords());
        List<GoodsAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (GoodsEntity record : entityPage.getRecords()) {
                voList.add(toAppVo(record, shopNameMap, false));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public GoodsAppVo appDetail(Long id, Long userId) {
        GoodsEntity entity = getOnlineGoods(id);
        Map<Long, String> shopNameMap = loadShopNameMap(Collections.singletonList(entity));
        GoodsAppVo vo = toAppVo(entity, shopNameMap, true);
        fillShopBrief(vo, entity.getShopId());
        footprintService.recordAsync(userId, AssetTargetType.GOODS.getCode(), id);
        return vo;
    }

    @Override
    public IPage<ReviewAppVo> appReviews(GoodsReviewAppListBo listBo) {
        requireOnlineGoods(listBo.getGoodsId());
        Page<ReviewEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<ReviewEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReviewEntity::getGoodsId, listBo.getGoodsId())
                .orderByDesc(ReviewEntity::getCreateTime)
                .orderByDesc(ReviewEntity::getId);

        IPage<ReviewEntity> entityPage = iReviewService.page(page, queryWrapper);
        List<ReviewAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (ReviewEntity record : entityPage.getRecords()) {
                voList.add(toReviewVo(record));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ReviewAppVo submitReview(Long userId, Long goodsId, GoodsReviewSubmitBo submitBo) {
        requireOnlineGoods(goodsId);
        UserEntity user = requireUser(userId);
        OrderEntity order = requireReviewableOrder(userId, submitBo.getOrderId());
        assertOrderContainsGoods(order);
        assertNotReviewed(submitBo.getOrderId(), userId);

        ReviewEntity entity = new ReviewEntity();
        entity.setGoodsId(goodsId);
        entity.setOrderId(submitBo.getOrderId());
        entity.setUserId(userId);
        entity.setUserName(StringUtils.defaultIfBlank(user.getUserName(), user.getPhone()));
        entity.setContent(StringUtils.trim(submitBo.getContent()));
        entity.setRating(submitBo.getRating());
        entity.setImages(FeedbackJsonUtil.toJson(normalizeReviewImages(submitBo.getImages())));
        iReviewService.save(entity);
        return toReviewVo(entity);
    }

    @Override
    public IPage<GoodsVo> pageInfo(GoodsPageBo pageBo) {
        Page<GoodsEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), GoodsEntity::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getShopId()), GoodsEntity::getShopId, pageBo.getShopId())
                .eq(StringUtils.isNotBlank(pageBo.getCategory()), GoodsEntity::getCategory, pageBo.getCategory())
                .eq(Objects.nonNull(pageBo.getStatus()), GoodsEntity::getStatus, pageBo.getStatus());

        List<Long> shopIds = resolveShopIds(pageBo.getShopName());
        if (shopIds != null) {
            if (shopIds.isEmpty()) {
                return PageUtil.copyPage(page, new ArrayList<>());
            }
            queryWrapper.in(GoodsEntity::getShopId, shopIds);
        }

        queryWrapper.orderByAsc(GoodsEntity::getSort)
                .orderByDesc(GoodsEntity::getCreateTime);

        IPage<GoodsEntity> entityPage = iGoodsService.page(page, queryWrapper);
        Map<Long, String> shopNameMap = loadShopNameMap(entityPage.getRecords());
        List<GoodsVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (GoodsEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, shopNameMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(GoodsSaveBo saveBo) {
        requireShop(saveBo.getShopId());
        GoodsEntity entity = buildEntityFromSave(saveBo);
        iGoodsService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(GoodsUpdateBo updateBo) {
        requireGoods(updateBo.getId());
        requireShop(updateBo.getShopId());
        GoodsEntity entity = buildEntityFromSave(updateBo);
        entity.setId(updateBo.getId());
        iGoodsService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(GoodsInfoBo infoBo) {
        requireGoods(infoBo.getId());
        iGoodsService.removeById(infoBo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(GoodsUpdateStateBo updateStateBo) {
        GoodsEntity goods = requireGoods(updateStateBo.getId());
        goods.setStatus(updateStateBo.getStatus());
        iGoodsService.updateById(goods);
        if (goods.getMerchantServiceId() == null) {
            return;
        }
        MerchantServiceEntity service = iMerchantServiceItemService.getById(goods.getMerchantServiceId());
        if (service == null) {
            return;
        }
        boolean online = Objects.equals(updateStateBo.getStatus(), STATUS_ONLINE);
        service.setStatus(online ? MerchantServiceStatus.ON_SALE.getCode() : MerchantServiceStatus.OFF_SALE.getCode());
        iMerchantServiceItemService.updateById(service);
    }

    private GoodsEntity getOnlineGoods(Long id) {
        GoodsEntity entity = iGoodsService.getById(id);
        if (entity == null || !Objects.equals(entity.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.HERITAGE_GOODS_NOT_FOUND);
        }
        return entity;
    }

    private void requireOnlineGoods(Long id) {
        getOnlineGoods(id);
    }

    private GoodsEntity requireGoods(Long id) {
        GoodsEntity entity = iGoodsService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.HERITAGE_GOODS_NOT_FOUND);
        }
        return entity;
    }

    private HeritageShopEntity requireShop(Long shopId) {
        HeritageShopEntity shop = iHeritageShopService.getById(shopId);
        if (shop == null) {
            throw new BizException(AppError.HERITAGE_SHOP_NOT_FOUND);
        }
        return shop;
    }

    private GoodsEntity buildEntityFromSave(GoodsSaveBo saveBo) {
        GoodsEntity entity = new GoodsEntity();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setSpecs(GoodsJsonUtil.specsToJson(saveBo.getSpecs()));
        entity.setBanners(GoodsJsonUtil.bannersToJson(saveBo.getBanners()));
        if (entity.getSales() == null) {
            entity.setSales(0);
        }
        if (entity.getStock() == null) {
            entity.setStock(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        return entity;
    }

    private GoodsAppVo toAppVo(GoodsEntity entity, Map<Long, String> shopNameMap, boolean includeDetail) {
        GoodsAppVo vo = new GoodsAppVo();
        vo.setId(entity.getId());
        vo.setShopId(entity.getShopId());
        vo.setShopName(shopNameMap.get(entity.getShopId()));
        vo.setName(entity.getName());
        vo.setDesc(entity.getDescription());
        vo.setCover(entity.getCoverImage());
        List<GoodsSpecSkuVo> skus = GoodsJsonUtil.parseSpecSkus(entity.getSpecs(), entity.getPrice(), entity.getStock());
        boolean multiSpec = skus.size() > 1;
        vo.setSpecMode(multiSpec ? GoodsSpecsUtil.MODE_MULTI : GoodsSpecsUtil.MODE_SINGLE);
        vo.setSpecs(skus);
        vo.setPrice(multiSpec ? GoodsSpecsUtil.minPrice(skus, entity.getPrice()) : entity.getPrice());
        vo.setPriceFrom(multiSpec);
        vo.setSales(entity.getSales());
        vo.setStock(multiSpec ? GoodsSpecsUtil.totalStock(skus, entity.getStock()) : entity.getStock());
        vo.setCategory(entity.getCategory());
        if (entity.getRating() != null) {
            vo.setRating(entity.getRating().stripTrailingZeros().toPlainString());
        }
        if (includeDetail) {
            vo.setDetail(entity.getDetail());
            vo.setBanners(GoodsJsonUtil.parseBanners(entity.getBanners()));
            vo.setDetailImages(GoodsJsonUtil.parseDetailImages(entity.getDetailImages()));
        }
        return vo;
    }

    private GoodsVo toAdminVo(GoodsEntity entity, Map<Long, String> shopNameMap) {
        GoodsVo vo = new GoodsVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setShopName(shopNameMap.get(entity.getShopId()));
        vo.setSpecs(GoodsJsonUtil.parseSpecs(entity.getSpecs()));
        vo.setBanners(GoodsJsonUtil.parseBanners(entity.getBanners()));
        vo.setDetailImages(GoodsJsonUtil.parseDetailImages(entity.getDetailImages()));
        vo.setStatusLabel(HeritageStatus.labelOf(entity.getStatus()));
        return vo;
    }

    private ReviewAppVo toReviewVo(ReviewEntity entity) {
        ReviewAppVo vo = new ReviewAppVo();
        vo.setId(entity.getId());
        vo.setUser(entity.getUserName());
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

    private List<Long> resolveShopIds(String shopName) {
        if (StringUtils.isBlank(shopName)) {
            return null;
        }
        LambdaQueryWrapper<HeritageShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(HeritageShopEntity::getName, shopName);
        List<HeritageShopEntity> shops = iHeritageShopService.list(queryWrapper);
        if (CollectionUtils.isEmpty(shops)) {
            return new ArrayList<>();
        }
        return shops.stream().map(HeritageShopEntity::getId).collect(Collectors.toList());
    }

    private UserEntity requireUser(Long userId) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }
        return user;
    }

    private OrderEntity requireReviewableOrder(Long userId, Long orderId) {
        OrderEntity order = iOrderService.getById(orderId);
        if (order == null || !Objects.equals(order.getUserId(), userId)) {
            throw new BizException(AppError.ORDER_NOT_FOUND);
        }
        if (order.getStatus() == null || order.getStatus() != MerchantOrderStatusGroup.COMPLETED) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        if (!MerchantOrderStatusSupport.isGoodsCategory(order.getOrderCategory())) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        return order;
    }

    private void assertOrderContainsGoods(OrderEntity order) {
        if (!MerchantOrderStatusSupport.isGoodsCategory(order.getOrderCategory())) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
        List<OrderItemEntity> items = iOrderItemService.listByOrderIds(Collections.singletonList(order.getId()));
        boolean hasGoodsItem = items.stream().anyMatch(item -> "goods".equals(item.getItemType()));
        if (!hasGoodsItem) {
            throw new BizException(AppError.ORDER_REVIEW_NOT_ALLOWED);
        }
    }

    private void assertNotReviewed(Long orderId, Long userId) {
        LambdaQueryWrapper<ReviewEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReviewEntity::getOrderId, orderId)
                .eq(ReviewEntity::getUserId, userId)
                .last("LIMIT 1");
        if (iReviewService.count(queryWrapper) > 0) {
            throw new BizException(AppError.ORDER_REVIEW_DUPLICATE);
        }
    }

    private Map<Long, String> loadShopNameMap(List<GoodsEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new HashMap<>();
        }
        List<Long> ids = records.stream()
                .map(GoodsEntity::getShopId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        List<HeritageShopEntity> shops = iHeritageShopService.listByIds(ids);
        Map<Long, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(shops)) {
            for (HeritageShopEntity shop : shops) {
                map.put(shop.getId(), shop.getName());
            }
        }
        return map;
    }

    private void fillShopBrief(GoodsAppVo vo, Long shopId) {
        if (vo == null || shopId == null) {
            return;
        }
        HeritageShopEntity shop = iHeritageShopService.getById(shopId);
        if (shop == null) {
            return;
        }
        if (StringUtils.isNotBlank(shop.getName())) {
            vo.setShopName(shop.getName());
        }
        List<String> bannerImages = resolveShopBannerImages(shop);
        vo.setShopBannerImages(bannerImages);
        if (CollectionUtils.isNotEmpty(bannerImages)) {
            vo.setShopCover(bannerImages.get(0));
        } else if (StringUtils.isNotBlank(shop.getCoverImage())) {
            vo.setShopCover(shop.getCoverImage().trim());
        }
        vo.setShopAddress(shop.getAddress());
        vo.setShopPhone(shop.getPhone());
        vo.setShopShipping(HeritageShippingUtil.parse(shop.getShippingConfig()));
    }

    private List<String> resolveShopBannerImages(HeritageShopEntity shop) {
        List<String> bannerImages = HeritageJsonUtil.parseStringList(shop.getBannerImages());
        if (CollectionUtils.isEmpty(bannerImages) && StringUtils.isNotBlank(shop.getCoverImage())) {
            bannerImages = new ArrayList<>();
            bannerImages.add(shop.getCoverImage().trim());
        }
        return bannerImages;
    }

    /** 仅兼容旧版 category=featured 等显式分类筛选；hot/new 改由 sort 控制 */
    private void applyLegacyCategoryFilter(LambdaQueryWrapper<GoodsEntity> queryWrapper, GoodsAppListBo listBo) {
        if (StringUtils.isNotBlank(listBo.getSort()) && !"all".equalsIgnoreCase(listBo.getSort())) {
            return;
        }
        String category = StringUtils.trimToEmpty(listBo.getCategory());
        if (StringUtils.isBlank(category) || "all".equalsIgnoreCase(category)) {
            return;
        }
        if ("hot".equalsIgnoreCase(category) || "new".equalsIgnoreCase(category)) {
            return;
        }
        queryWrapper.eq(GoodsEntity::getCategory, category);
    }

    private void applyAppListSort(LambdaQueryWrapper<GoodsEntity> queryWrapper, GoodsAppListBo listBo) {
        String sort = resolveAppListSort(listBo);
        switch (sort) {
            case "hot":
                queryWrapper.orderByDesc(GoodsEntity::getSales)
                        .orderByDesc(GoodsEntity::getId);
                break;
            case "new":
                queryWrapper.orderByDesc(GoodsEntity::getCreateTime)
                        .orderByDesc(GoodsEntity::getId);
                break;
            case "price":
                if ("desc".equalsIgnoreCase(StringUtils.trimToEmpty(listBo.getPriceOrder()))) {
                    queryWrapper.orderByDesc(GoodsEntity::getPrice)
                            .orderByDesc(GoodsEntity::getId);
                } else {
                    queryWrapper.orderByAsc(GoodsEntity::getPrice)
                            .orderByDesc(GoodsEntity::getId);
                }
                break;
            default:
                queryWrapper.orderByAsc(GoodsEntity::getSort)
                        .orderByDesc(GoodsEntity::getId);
                break;
        }
    }

    private String resolveAppListSort(GoodsAppListBo listBo) {
        String sort = StringUtils.trimToEmpty(listBo.getSort());
        if (StringUtils.isNotBlank(sort) && !"all".equalsIgnoreCase(sort)) {
            return sort.toLowerCase();
        }
        String category = StringUtils.trimToEmpty(listBo.getCategory());
        if ("hot".equalsIgnoreCase(category)) {
            return "hot";
        }
        if ("new".equalsIgnoreCase(category)) {
            return "new";
        }
        return "all";
    }
}

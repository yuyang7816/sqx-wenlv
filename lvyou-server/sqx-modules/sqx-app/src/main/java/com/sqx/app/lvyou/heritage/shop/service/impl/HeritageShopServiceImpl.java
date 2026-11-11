package com.sqx.app.lvyou.heritage.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.heritage.craft.db.service.IHeritageCraftService;
import com.sqx.app.lvyou.heritage.craft.domain.entity.HeritageCraftEntity;
import com.sqx.app.lvyou.heritage.enums.HeritageStatus;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.bo.*;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopAppVo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopVo;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopCategoryService;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopService;
import com.sqx.app.lvyou.heritage.shop.support.HeritageShopCategorySupport;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.heritage.util.HeritageJsonUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
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
 * 非遗店铺业务服务实现
 */
@Service
public class HeritageShopServiceImpl implements HeritageShopService {

    private static final int STATUS_ONLINE = HeritageStatus.ONLINE.getCode();

    @Resource
    private IHeritageShopService iHeritageShopService;

    @Resource
    private IHeritageCraftService iHeritageCraftService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private HeritageShopCategoryService heritageShopCategoryService;

    @Override
    public IPage<HeritageShopAppVo> appPage(HeritageShopAppListBo listBo) {
        Page<HeritageShopEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<HeritageShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HeritageShopEntity::getStatus, STATUS_ONLINE)
                .eq(Objects.nonNull(listBo.getDestinationId()), HeritageShopEntity::getDestinationId, listBo.getDestinationId())
                .like(StringUtils.isNotBlank(listBo.getName()), HeritageShopEntity::getName, listBo.getName())
                .eq(StringUtils.isNotBlank(listBo.getCategory()) && !"all".equalsIgnoreCase(listBo.getCategory()),
                        HeritageShopEntity::getCategory, listBo.getCategory())
                .orderByAsc(HeritageShopEntity::getSort)
                .orderByDesc(HeritageShopEntity::getId);

        IPage<HeritageShopEntity> entityPage = iHeritageShopService.page(page, queryWrapper);
        Map<Long, String> craftNameMap = loadCraftNameMap(entityPage.getRecords());
        List<HeritageShopAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HeritageShopEntity record : entityPage.getRecords()) {
                voList.add(toAppVo(record, craftNameMap, false));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public HeritageShopAppVo appDetail(Long id) {
        HeritageShopEntity entity = getOnlineShop(id);
        Map<Long, String> craftNameMap = loadCraftNameMap(Collections.singletonList(entity));
        return toAppVo(entity, craftNameMap, true);
    }

    @Override
    public IPage<HeritageShopVo> pageInfo(HeritageShopPageBo pageBo) {
        Page<HeritageShopEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<HeritageShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), HeritageShopEntity::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getDestinationId()), HeritageShopEntity::getDestinationId, pageBo.getDestinationId())
                .eq(StringUtils.isNotBlank(pageBo.getCategory()), HeritageShopEntity::getCategory, pageBo.getCategory())
                .eq(Objects.nonNull(pageBo.getStatus()), HeritageShopEntity::getStatus, pageBo.getStatus());

        List<Long> craftIds = resolveCraftIds(pageBo.getCraftName());
        if (craftIds != null) {
            if (craftIds.isEmpty()) {
                return PageUtil.copyPage(page, new ArrayList<>());
            }
            queryWrapper.in(HeritageShopEntity::getCraftId, craftIds);
        }

        queryWrapper.orderByAsc(HeritageShopEntity::getSort)
                .orderByDesc(HeritageShopEntity::getCreateTime);

        IPage<HeritageShopEntity> entityPage = iHeritageShopService.page(page, queryWrapper);
        Map<Long, String> destinationNameMap = loadDestinationNameMap(entityPage.getRecords());
        Map<Long, String> craftNameMap = loadCraftNameMap(entityPage.getRecords());
        List<HeritageShopVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HeritageShopEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, destinationNameMap, craftNameMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public HeritageShopVo info(HeritageShopInfoBo infoBo) {
        HeritageShopEntity entity = requireShop(infoBo.getId());
        Map<Long, String> destinationNameMap = loadDestinationNameMap(Collections.singletonList(entity));
        Map<Long, String> craftNameMap = loadCraftNameMap(Collections.singletonList(entity));
        return toAdminVo(entity, destinationNameMap, craftNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(HeritageShopSaveBo saveBo) {
        HeritageShopEntity entity = buildEntityFromSave(saveBo);
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(saveBo.getLatitude(), saveBo.getLongitude()));
        iHeritageShopService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(HeritageShopUpdateBo updateBo) {
        HeritageShopEntity existing = requireShop(updateBo.getId());
        HeritageShopEntity entity = buildEntityFromSave(updateBo, existing);
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(updateBo.getLatitude(), updateBo.getLongitude()));
        entity.setId(updateBo.getId());
        iHeritageShopService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(HeritageShopInfoBo infoBo) {
        requireShop(infoBo.getId());
        iHeritageShopService.removeById(infoBo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(HeritageShopUpdateStateBo updateStateBo) {
        requireShop(updateStateBo.getId());
        LambdaUpdateWrapper<HeritageShopEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HeritageShopEntity::getId, updateStateBo.getId())
                .set(HeritageShopEntity::getStatus, updateStateBo.getStatus());
        iHeritageShopService.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncCategoryFromMerchant(Long merchantId) {
        if (merchantId == null) {
            return;
        }
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant == null || !MerchantStoreType.CULTURE.getCode().equals(MerchantStoreType.normalize(merchant.getStoreType()))) {
            return;
        }
        String category = HeritageShopCategorySupport.resolveFromMerchant(
                merchant.getShopCategory(), merchant.getCraftId(), iHeritageCraftService::getById);
        HeritageShopEntity shop = findShopByMerchantId(merchantId);
        if (shop == null) {
            return;
        }
        boolean changed = false;
        if (!StringUtils.equals(category, shop.getCategory())) {
            shop.setCategory(category);
            changed = true;
        }
        if (!Objects.equals(merchant.getLatitude(), shop.getLatitude())) {
            shop.setLatitude(merchant.getLatitude());
            changed = true;
        }
        if (!Objects.equals(merchant.getLongitude(), shop.getLongitude())) {
            shop.setLongitude(merchant.getLongitude());
            changed = true;
        }
        if (!StringUtils.equals(merchant.getStoreAddress(), shop.getAddress())) {
            shop.setAddress(merchant.getStoreAddress());
            changed = true;
        }
        if (changed) {
            iHeritageShopService.updateById(shop);
        }
    }

    private HeritageShopEntity getOnlineShop(Long id) {
        HeritageShopEntity entity = iHeritageShopService.getById(id);
        if (entity == null || !Objects.equals(entity.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.HERITAGE_SHOP_NOT_FOUND);
        }
        return entity;
    }

    private HeritageShopEntity requireShop(Long id) {
        HeritageShopEntity entity = iHeritageShopService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.HERITAGE_SHOP_NOT_FOUND);
        }
        return entity;
    }

    private HeritageShopEntity buildEntityFromSave(HeritageShopSaveBo saveBo) {
        return buildEntityFromSave(saveBo, null);
    }

    private HeritageShopEntity buildEntityFromSave(HeritageShopSaveBo saveBo, HeritageShopEntity existing) {
        HeritageShopEntity entity = new HeritageShopEntity();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setTags(HeritageJsonUtil.toJsonArray(saveBo.getTags()));
        entity.setServices(HeritageJsonUtil.toJsonArray(saveBo.getServices()));
        entity.setCategory(resolveShopCategory(saveBo, existing));
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getReviewCount() == null) {
            entity.setReviewCount(0);
        }
        return entity;
    }

    private String resolveShopCategory(HeritageShopSaveBo saveBo, HeritageShopEntity existing) {
        if (StringUtils.isNotBlank(saveBo.getCategory())) {
            String normalized = heritageShopCategoryService.normalize(saveBo.getCategory());
            if (StringUtils.isNotBlank(normalized)) {
                if (!heritageShopCategoryService.isValidForSave(normalized)) {
                    throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_NOT_FOUND);
                }
                return normalized;
            }
        }
        Long merchantId = existing != null ? existing.getMerchantId() : null;
        if (merchantId != null) {
            MerchantEntity merchant = iMerchantService.getById(merchantId);
            if (merchant != null && StringUtils.isNotBlank(merchant.getShopCategory())) {
                return merchant.getShopCategory().trim();
            }
        }
        if (existing != null && StringUtils.isNotBlank(existing.getCategory())) {
            return existing.getCategory();
        }
        if (saveBo.getCraftId() != null) {
            HeritageCraftEntity craft = iHeritageCraftService.getById(saveBo.getCraftId());
            return HeritageShopCategorySupport.resolveFromCraft(craft);
        }
        return HeritageShopCategorySupport.DEFAULT;
    }

    private HeritageShopEntity findShopByMerchantId(Long merchantId) {
        LambdaQueryWrapper<HeritageShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HeritageShopEntity::getMerchantId, merchantId)
                .last("LIMIT 1");
        return iHeritageShopService.getOne(queryWrapper, false);
    }

    private HeritageShopAppVo toAppVo(HeritageShopEntity entity, Map<Long, String> craftNameMap, boolean includeDetail) {
        HeritageShopAppVo vo = new HeritageShopAppVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setAddress(entity.getAddress());
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setTag(entity.getTag());
        vo.setTags(HeritageJsonUtil.parseStringList(entity.getTags()));
        vo.setCategory(entity.getCategory());
        vo.setDesc(entity.getDescription());
        vo.setCoverFrom(entity.getCoverFrom());
        vo.setCoverTo(entity.getCoverTo());
        vo.setCover(entity.getCoverImage());
        vo.setBannerImages(resolveBannerImages(entity));
        vo.setHours(entity.getBusinessHours());
        vo.setPhone(entity.getPhone());
        vo.setRating(entity.getRating());
        vo.setReviewCount(entity.getReviewCount());
        vo.setServices(HeritageJsonUtil.parseStringList(entity.getServices()));
        vo.setCraftId(entity.getCraftId());
        vo.setCraftName(craftNameMap.get(entity.getCraftId()));
        if (includeDetail) {
            vo.setIntro(entity.getIntro());
            vo.setIntroDetail(entity.getIntroDetail());
        } else {
            vo.setIntro(entity.getIntro());
        }
        return vo;
    }

    private List<String> resolveBannerImages(HeritageShopEntity entity) {
        List<String> bannerImages = HeritageJsonUtil.parseStringList(entity.getBannerImages());
        if (CollectionUtils.isEmpty(bannerImages) && StringUtils.isNotBlank(entity.getCoverImage())) {
            bannerImages = new ArrayList<>();
            bannerImages.add(entity.getCoverImage().trim());
        }
        return bannerImages;
    }

    private HeritageShopVo toAdminVo(HeritageShopEntity entity, Map<Long, String> destinationNameMap,
                                     Map<Long, String> craftNameMap) {
        HeritageShopVo vo = new HeritageShopVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setTags(HeritageJsonUtil.parseStringList(entity.getTags()));
        vo.setServices(HeritageJsonUtil.parseStringList(entity.getServices()));
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destinationNameMap, entity.getDestinationId(), entity.getAddress()));
        vo.setCraftName(craftNameMap.get(entity.getCraftId()));
        vo.setStatusLabel(shopStatusLabel(entity.getStatus()));
        return vo;
    }

    /** 店铺状态：1 营业 / 0 歇业（与工艺/商品上下架语义不同） */
    private static String shopStatusLabel(Integer status) {
        if (status == null) {
            return "";
        }
        if (status == 1) {
            return "营业";
        }
        if (status == 0) {
            return "歇业";
        }
        return "";
    }

    private List<Long> resolveCraftIds(String craftName) {
        if (StringUtils.isBlank(craftName)) {
            return null;
        }
        LambdaQueryWrapper<HeritageCraftEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(HeritageCraftEntity::getName, craftName);
        List<HeritageCraftEntity> crafts = iHeritageCraftService.list(queryWrapper);
        if (CollectionUtils.isEmpty(crafts)) {
            return new ArrayList<>();
        }
        return crafts.stream().map(HeritageCraftEntity::getId).collect(Collectors.toList());
    }

    private Map<Long, String> loadDestinationNameMap(List<HeritageShopEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new HashMap<>();
        }
        List<Long> ids = records.stream()
                .map(HeritageShopEntity::getDestinationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
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

    private Map<Long, String> loadCraftNameMap(List<HeritageShopEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new HashMap<>();
        }
        List<Long> ids = records.stream()
                .map(HeritageShopEntity::getCraftId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        List<HeritageCraftEntity> crafts = iHeritageCraftService.listByIds(ids);
        Map<Long, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(crafts)) {
            for (HeritageCraftEntity craft : crafts) {
                map.put(craft.getId(), craft.getName());
            }
        }
        return map;
    }
}

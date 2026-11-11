package com.sqx.app.lvyou.heritage.craft.service.impl;

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
import com.sqx.app.lvyou.heritage.craft.domain.bo.*;
import com.sqx.app.lvyou.heritage.craft.domain.entity.HeritageCraftEntity;
import com.sqx.app.lvyou.heritage.craft.domain.vo.HeritageCraftAppVo;
import com.sqx.app.lvyou.heritage.craft.domain.vo.HeritageCraftVo;
import com.sqx.app.lvyou.heritage.craft.service.HeritageCraftService;
import com.sqx.app.lvyou.heritage.craft.support.HeritageCraftLevelSupport;
import com.sqx.app.lvyou.heritage.enums.HeritageStatus;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import com.sqx.app.lvyou.heritage.shop.support.HeritageShopCategorySupport;
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
 * 非遗工艺业务服务实现
 */
@Service
public class HeritageCraftServiceImpl implements HeritageCraftService {

    private static final int STATUS_ONLINE = HeritageStatus.ONLINE.getCode();

    @Resource
    private IHeritageCraftService iHeritageCraftService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Resource
    private IHeritageShopService iHeritageShopService;

    @Override
    public IPage<HeritageCraftAppVo> appPage(HeritageCraftAppListBo listBo) {
        Page<HeritageCraftEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<HeritageCraftEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HeritageCraftEntity::getStatus, STATUS_ONLINE)
                .eq(Objects.nonNull(listBo.getDestinationId()), HeritageCraftEntity::getDestinationId, listBo.getDestinationId())
                .like(StringUtils.isNotBlank(listBo.getName()), HeritageCraftEntity::getName, listBo.getName())
                .eq(StringUtils.isNotBlank(listBo.getTag()), HeritageCraftEntity::getTag, listBo.getTag())
                .orderByAsc(HeritageCraftEntity::getSort)
                .orderByDesc(HeritageCraftEntity::getId);

        IPage<HeritageCraftEntity> entityPage = iHeritageCraftService.page(page, queryWrapper);
        List<HeritageCraftAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HeritageCraftEntity record : entityPage.getRecords()) {
                voList.add(toAppVo(record, false));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public HeritageCraftAppVo appDetail(Long id) {
        HeritageCraftEntity entity = getOnlineCraft(id);
        return toAppVo(entity, true);
    }

    @Override
    public IPage<HeritageCraftVo> pageInfo(HeritageCraftPageBo pageBo) {
        Page<HeritageCraftEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<HeritageCraftEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), HeritageCraftEntity::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getDestinationId()), HeritageCraftEntity::getDestinationId, pageBo.getDestinationId())
                .eq(StringUtils.isNotBlank(pageBo.getTag()), HeritageCraftEntity::getTag, pageBo.getTag())
                .eq(Objects.nonNull(pageBo.getStatus()), HeritageCraftEntity::getStatus, pageBo.getStatus())
                .orderByAsc(HeritageCraftEntity::getSort)
                .orderByDesc(HeritageCraftEntity::getCreateTime);

        IPage<HeritageCraftEntity> entityPage = iHeritageCraftService.page(page, queryWrapper);
        Map<Long, String> destinationNameMap = loadDestinationNameMap(entityPage.getRecords());
        List<HeritageCraftVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HeritageCraftEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, destinationNameMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public HeritageCraftVo info(HeritageCraftInfoBo infoBo) {
        HeritageCraftEntity entity = requireCraft(infoBo.getId());
        Map<Long, String> destinationNameMap = loadDestinationNameMap(Collections.singletonList(entity));
        return toAdminVo(entity, destinationNameMap);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(HeritageCraftSaveBo saveBo) {
        HeritageCraftEntity entity = buildEntityFromSave(saveBo);
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(saveBo.getLatitude(), saveBo.getLongitude()));
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        iHeritageCraftService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(HeritageCraftUpdateBo updateBo) {
        requireCraft(updateBo.getId());
        HeritageCraftEntity entity = buildEntityFromSave(updateBo);
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(updateBo.getLatitude(), updateBo.getLongitude()));
        entity.setId(updateBo.getId());
        iHeritageCraftService.updateById(entity);
        syncLinkedShopCategories(entity.getId(), entity.getShopCategory());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(HeritageCraftInfoBo infoBo) {
        requireCraft(infoBo.getId());
        iHeritageCraftService.removeById(infoBo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(HeritageCraftUpdateStateBo updateStateBo) {
        requireCraft(updateStateBo.getId());
        LambdaUpdateWrapper<HeritageCraftEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HeritageCraftEntity::getId, updateStateBo.getId())
                .set(HeritageCraftEntity::getStatus, updateStateBo.getStatus());
        iHeritageCraftService.update(updateWrapper);
    }

    private HeritageCraftEntity getOnlineCraft(Long id) {
        HeritageCraftEntity entity = iHeritageCraftService.getById(id);
        if (entity == null || !Objects.equals(entity.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.HERITAGE_CRAFT_NOT_FOUND);
        }
        return entity;
    }

    private HeritageCraftEntity requireCraft(Long id) {
        HeritageCraftEntity entity = iHeritageCraftService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.HERITAGE_CRAFT_NOT_FOUND);
        }
        return entity;
    }

    private HeritageCraftAppVo toAppVo(HeritageCraftEntity entity, boolean includeDetail) {
        HeritageCraftAppVo vo = new HeritageCraftAppVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setDesc(entity.getDescription());
        vo.setLevel(HeritageCraftLevelSupport.resolveShort(entity.getLevelCode(), entity.getLevelLabel()));
        vo.setLevelLabel(HeritageCraftLevelSupport.resolveLabel(entity.getLevelCode(), entity.getLevelLabel()));
        List<String> tags = resolveTags(entity);
        vo.setTags(tags);
        vo.setTag(tags.isEmpty() ? entity.getTag() : tags.get(0));
        vo.setIcon(entity.getIcon());
        vo.setCoverFrom(entity.getCoverFrom());
        vo.setCoverTo(entity.getCoverTo());
        vo.setCover(entity.getCoverImage());
        if (includeDetail) {
            vo.setBannerImages(resolveBannerImages(entity));
            vo.setOverview(entity.getOverview());
            vo.setFeature(entity.getFeature());
            vo.setCulture(entity.getCulture());
        }
        return vo;
    }

    private HeritageCraftEntity buildEntityFromSave(HeritageCraftSaveBo saveBo) {
        HeritageCraftEntity entity = new HeritageCraftEntity();
        BeanUtils.copyProperties(saveBo, entity, "tags", "bannerImages");
        List<String> tags = normalizeTags(saveBo.getTags());
        entity.setTags(HeritageJsonUtil.toJsonArray(tags));
        entity.setTag(tags.isEmpty() ? null : tags.get(0));
        entity.setBannerImages(HeritageJsonUtil.toJsonArray(normalizeBannerImages(saveBo.getBannerImages())));
        entity.setLevelLabel(HeritageCraftLevelSupport.resolveLabel(saveBo.getLevelCode(), saveBo.getLevelLabel()));
        if (StringUtils.isBlank(entity.getShopCategory())) {
            entity.setShopCategory(HeritageShopCategorySupport.DEFAULT);
        }
        return entity;
    }

    private List<String> normalizeBannerImages(List<String> bannerImages) {
        List<String> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(bannerImages)) {
            return result;
        }
        for (String image : bannerImages) {
            if (StringUtils.isBlank(image)) {
                continue;
            }
            String value = image.trim();
            if (!result.contains(value)) {
                result.add(value);
            }
        }
        return result;
    }

    private List<String> resolveBannerImages(HeritageCraftEntity entity) {
        List<String> bannerImages = HeritageJsonUtil.parseStringList(entity.getBannerImages());
        if (CollectionUtils.isEmpty(bannerImages) && StringUtils.isNotBlank(entity.getCoverImage())) {
            bannerImages = new ArrayList<>();
            bannerImages.add(entity.getCoverImage().trim());
        }
        return bannerImages;
    }

    private void syncLinkedShopCategories(Long craftId, String shopCategory) {
        if (craftId == null) {
            return;
        }
        String category = StringUtils.isBlank(shopCategory)
                ? HeritageShopCategorySupport.DEFAULT
                : shopCategory.trim();
        LambdaUpdateWrapper<HeritageShopEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HeritageShopEntity::getCraftId, craftId)
                .set(HeritageShopEntity::getCategory, category);
        iHeritageShopService.update(updateWrapper);
    }

    private List<String> normalizeTags(List<String> tags) {
        List<String> result = new ArrayList<>();
        if (CollectionUtils.isEmpty(tags)) {
            return result;
        }
        for (String tag : tags) {
            if (StringUtils.isBlank(tag)) {
                continue;
            }
            String value = tag.trim();
            if (!result.contains(value)) {
                result.add(value);
            }
        }
        return result;
    }

    private List<String> resolveTags(HeritageCraftEntity entity) {
        List<String> tags = HeritageJsonUtil.parseStringList(entity.getTags());
        if (CollectionUtils.isEmpty(tags) && StringUtils.isNotBlank(entity.getTag())) {
            return Collections.singletonList(entity.getTag().trim());
        }
        return tags;
    }

    private HeritageCraftVo toAdminVo(HeritageCraftEntity entity, Map<Long, String> destinationNameMap) {
        HeritageCraftVo vo = new HeritageCraftVo();
        BeanUtils.copyProperties(entity, vo, "bannerImages");
        vo.setBannerImages(resolveBannerImages(entity));
        List<String> tags = resolveTags(entity);
        vo.setTags(tags);
        vo.setTag(tags.isEmpty() ? entity.getTag() : tags.get(0));
        vo.setLevelLabel(HeritageCraftLevelSupport.resolveLabel(entity.getLevelCode(), entity.getLevelLabel()));
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destinationNameMap, entity.getDestinationId(), entity.getAddress()));
        vo.setStatusLabel(HeritageStatus.labelOf(entity.getStatus()));
        return vo;
    }

    private Map<Long, String> loadDestinationNameMap(List<HeritageCraftEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new HashMap<>();
        }
        List<Long> ids = records.stream()
                .map(HeritageCraftEntity::getDestinationId)
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
}

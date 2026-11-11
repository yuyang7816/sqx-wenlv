package com.sqx.app.lvyou.scenic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.home.util.RecommendTagUtil;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicAppListBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicInfoBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicPageBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicSaveBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicUpdateBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicUpdateStateBo;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicAppDetailVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicAppVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicTagVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicVo;
import com.sqx.app.lvyou.scenic.enums.ScenicStatus;
import com.sqx.app.lvyou.favorite.enums.AssetTargetType;
import com.sqx.app.lvyou.favorite.service.FootprintService;
import com.sqx.app.lvyou.scenic.service.ScenicCategoryService;
import com.sqx.app.lvyou.scenic.service.ScenicService;
import com.sqx.app.lvyou.scenic.util.ScenicJsonUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 旅游景区业务服务实现
 */
@Service
public class ScenicServiceImpl implements ScenicService {

    private static final int STATUS_ONLINE = ScenicStatus.ONLINE.getCode();
    private static final String TAG_TYPE_GREEN = "green";
    private static final String TAG_TYPE_ORANGE = "orange";

    @Resource
    private IScenicService iScenicService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private FootprintService footprintService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Resource
    private ScenicCategoryService scenicCategoryService;

    @Override
    public List<HomeRecommendItemVo> listRecommend(Long destinationId, int limit) {
        if (destinationId == null || limit <= 0) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<ScenicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicEntity::getStatus, STATUS_ONLINE)
                .eq(ScenicEntity::getDestinationId, destinationId)
                .orderByAsc(ScenicEntity::getSort)
                .orderByDesc(ScenicEntity::getId)
                .last("LIMIT " + limit);
        List<ScenicEntity> entities = iScenicService.list(queryWrapper);
        List<HomeRecommendItemVo> items = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (ScenicEntity entity : entities) {
                items.add(toRecommendItem(entity));
            }
        }
        return items;
    }

    @Override
    public IPage<ScenicAppVo> appPage(ScenicAppListBo listBo) {
        if (!scenicCategoryService.isValidFilter(listBo.getCategory())) {
            throw new BizException(AppError.SCENIC_CATEGORY_INVALID);
        }
        Page<ScenicEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        LambdaQueryWrapper<ScenicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicEntity::getStatus, STATUS_ONLINE)
                .eq(Objects.nonNull(listBo.getDestinationId()), ScenicEntity::getDestinationId, listBo.getDestinationId())
                .like(StringUtils.isNotBlank(listBo.getName()), ScenicEntity::getName, listBo.getName());
        if (StringUtils.isNotBlank(listBo.getCategory()) && !"all".equalsIgnoreCase(listBo.getCategory())) {
            queryWrapper.eq(ScenicEntity::getCategory, scenicCategoryService.normalize(listBo.getCategory()));
        }
        queryWrapper.orderByAsc(ScenicEntity::getSort)
                .orderByDesc(ScenicEntity::getId);

        IPage<ScenicEntity> entityPage = iScenicService.page(page, queryWrapper);
        List<ScenicAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (ScenicEntity record : entityPage.getRecords()) {
                voList.add(toAppListVo(record));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public ScenicAppDetailVo appDetail(Long id, Long userId) {
        ScenicEntity entity = getOnlineScenic(id);
        ScenicAppDetailVo vo = toAppDetailVo(entity);
        footprintService.recordAsync(userId, AssetTargetType.SCENIC.getCode(), id);
        return vo;
    }

    @Override
    public IPage<ScenicVo> pageInfo(ScenicPageBo pageBo) {
        Page<ScenicEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<ScenicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), ScenicEntity::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getDestinationId()), ScenicEntity::getDestinationId, pageBo.getDestinationId())
                .eq(StringUtils.isNotBlank(pageBo.getCategory()), ScenicEntity::getCategory, scenicCategoryService.normalize(pageBo.getCategory()))
                .like(StringUtils.isNotBlank(pageBo.getLevelLabel()), ScenicEntity::getLevelLabel, pageBo.getLevelLabel())
                .eq(Objects.nonNull(pageBo.getStatus()), ScenicEntity::getStatus, pageBo.getStatus())
                .orderByAsc(ScenicEntity::getSort)
                .orderByDesc(ScenicEntity::getCreateTime);

        IPage<ScenicEntity> entityPage = iScenicService.page(page, queryWrapper);
        Map<Long, String> destinationNameMap = loadDestinationNameMap(entityPage.getRecords());
        List<ScenicVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (ScenicEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record, destinationNameMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(ScenicSaveBo saveBo) {
        validateCategory(saveBo.getCategory());

        ScenicEntity entity = new ScenicEntity();
        fillEntityFromSaveBo(entity, saveBo);
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(saveBo.getLatitude(), saveBo.getLongitude()));
        fillDefaults(entity);
        iScenicService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(ScenicUpdateBo updateBo) {
        ScenicEntity existing = iScenicService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.SCENIC_NOT_FOUND);
        }
        validateCategory(updateBo.getCategory());

        ScenicEntity entity = new ScenicEntity();
        fillEntityFromSaveBo(entity, updateBo);
        entity.setDestinationId(publishDestinationResolver.requireFromCoordinates(updateBo.getLatitude(), updateBo.getLongitude()));
        entity.setId(updateBo.getId());
        iScenicService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ScenicInfoBo infoBo) {
        ScenicEntity existing = iScenicService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.SCENIC_NOT_FOUND);
        }
        iScenicService.removeById(infoBo.getId());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(ScenicUpdateStateBo updateStateBo) {
        ScenicEntity existing = iScenicService.getById(updateStateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.SCENIC_NOT_FOUND);
        }
        LambdaUpdateWrapper<ScenicEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ScenicEntity::getId, updateStateBo.getId())
                .set(ScenicEntity::getStatus, updateStateBo.getStatus());
        iScenicService.update(new ScenicEntity(), updateWrapper);
    }

    private ScenicEntity getOnlineScenic(Long id) {
        ScenicEntity entity = iScenicService.getById(id);
        if (entity == null || !Objects.equals(entity.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.SCENIC_NOT_FOUND);
        }
        return entity;
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

    private void validateCategory(String category) {
        if (StringUtils.isBlank(category)) {
            return;
        }
        if (!scenicCategoryService.isValidForSave(category)) {
            throw new BizException(AppError.SCENIC_CATEGORY_INVALID);
        }
    }

    private void fillEntityFromSaveBo(ScenicEntity entity, ScenicSaveBo saveBo) {
        BeanUtils.copyProperties(saveBo, entity, "tags", "bannerImages", "featureImages", "tips", "category");
        entity.setCategory(scenicCategoryService.normalize(saveBo.getCategory()));
        entity.setTag(ScenicJsonUtil.toJsonArray(saveBo.getTags()));
        entity.setBannerImages(ScenicJsonUtil.toJsonArray(saveBo.getBannerImages()));
        entity.setFeatureImage(ScenicJsonUtil.toJsonArray(saveBo.getFeatureImages()));
        entity.setTips(ScenicJsonUtil.toJsonArray(saveBo.getTips()));
    }

    private void fillDefaults(ScenicEntity entity) {
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getPriceFrom() == null) {
            entity.setPriceFrom(BigDecimal.ZERO);
        }
    }

    private Map<Long, String> loadDestinationNameMap(List<ScenicEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return new HashMap<>();
        }
        List<Long> destinationIds = records.stream()
                .map(ScenicEntity::getDestinationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(destinationIds)) {
            return new HashMap<>();
        }
        List<DestinationEntity> destinations = iDestinationService.listByIds(destinationIds);
        Map<Long, String> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(destinations)) {
            for (DestinationEntity destination : destinations) {
                map.put(destination.getId(), destination.getName());
            }
        }
        return map;
    }

    private HomeRecommendItemVo toRecommendItem(ScenicEntity entity) {
        HomeRecommendItemVo vo = new HomeRecommendItemVo();
        vo.setId(entity.getId());
        vo.setTitle(entity.getName());
        vo.setCover(StringUtils.defaultString(entity.getCoverImage()));
        vo.setPrice(entity.getPriceFrom());
        vo.setTag(RecommendTagUtil.resolveTag(entity.getTag()));
        vo.setAddress(StringUtils.defaultString(entity.getAddress()));
        return vo;
    }

    private ScenicAppVo toAppListVo(ScenicEntity entity) {
        ScenicAppVo vo = new ScenicAppVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setLevelLabel(StringUtils.defaultString(entity.getLevelLabel()));
        vo.setAddress(StringUtils.defaultString(entity.getAddress()));
        vo.setCategory(entity.getCategory());
        vo.setCategoryLabel(scenicCategoryService.labelOf(entity.getCategory()));
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setTags(ScenicJsonUtil.parseTags(entity.getTag()));
        vo.setPriceFrom(entity.getPriceFrom());
        String cover = StringUtils.defaultString(entity.getCoverImage());
        vo.setCover(cover);
        vo.setCoverImage(cover);
        return vo;
    }

    private ScenicAppDetailVo toAppDetailVo(ScenicEntity entity) {
        ScenicAppDetailVo vo = new ScenicAppDetailVo();
        vo.setId(entity.getId());
        vo.setName(entity.getName());
        vo.setLevelLabel(StringUtils.defaultString(entity.getLevelLabel()));
        vo.setAddress(StringUtils.defaultString(entity.getAddress()));
        vo.setLatitude(entity.getLatitude());
        vo.setLongitude(entity.getLongitude());
        vo.setPhone(StringUtils.defaultString(entity.getPhone()));
        vo.setOpenTime(StringUtils.defaultString(entity.getOpenTime()));
        vo.setCategory(entity.getCategory());
        vo.setPriceFrom(entity.getPriceFrom());
        fillTicketPrice(vo, entity.getPriceFrom());

        String cover = StringUtils.defaultString(entity.getCoverImage());
        vo.setCover(cover);
        vo.setCoverImage(cover);

        List<String> bannerImages = ScenicJsonUtil.parseBannerImages(entity.getBannerImages());
        if (CollectionUtils.isEmpty(bannerImages) && StringUtils.isNotBlank(cover)) {
            bannerImages = new ArrayList<>();
            bannerImages.add(cover);
        }
        vo.setBannerImages(bannerImages);
        vo.setBannerImage(CollectionUtils.isNotEmpty(bannerImages) ? bannerImages.get(0) : cover);

        List<String> featureImages = ScenicJsonUtil.parseFeatureImages(entity.getFeatureImage());
        vo.setFeatureImages(featureImages);
        vo.setFeatureImage(CollectionUtils.isNotEmpty(featureImages) ? featureImages.get(0) : "");
        vo.setFeatureDesc(StringUtils.defaultString(entity.getIntro()));
        vo.setTags(buildStructuredTags(entity));
        vo.setTips(ScenicJsonUtil.parseTips(entity.getTips()));
        return vo;
    }

    private ScenicVo toAdminVo(ScenicEntity entity, Map<Long, String> destinationNameMap) {
        ScenicVo vo = new ScenicVo();
        BeanUtils.copyProperties(entity, vo, "featureImage", "bannerImages", "tips", "tag");
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(
                destinationNameMap, entity.getDestinationId(), entity.getAddress()));
        vo.setCategoryLabel(scenicCategoryService.labelOf(entity.getCategory()));
        vo.setTags(ScenicJsonUtil.parseTags(entity.getTag()));
        vo.setBannerImages(ScenicJsonUtil.parseBannerImages(entity.getBannerImages()));
        vo.setFeatureImages(ScenicJsonUtil.parseFeatureImages(entity.getFeatureImage()));
        vo.setTips(ScenicJsonUtil.parseTips(entity.getTips()));
        vo.setStatusLabel(ScenicStatus.labelOf(entity.getStatus()));
        return vo;
    }

    private void fillTicketPrice(ScenicAppDetailVo vo, BigDecimal priceFrom) {
        if (priceFrom == null || priceFrom.compareTo(BigDecimal.ZERO) <= 0) {
            vo.setTicketFree(true);
            vo.setTicketPrice("免费");
            return;
        }
        vo.setTicketFree(false);
        vo.setTicketPrice("¥" + priceFrom.stripTrailingZeros().toPlainString());
    }

    private List<ScenicTagVo> buildStructuredTags(ScenicEntity entity) {
        List<ScenicTagVo> result = new ArrayList<>();
        long index = 1L;
        if (StringUtils.isNotBlank(entity.getLevelLabel())) {
            ScenicTagVo levelTag = new ScenicTagVo();
            levelTag.setId(index++);
            levelTag.setType(TAG_TYPE_GREEN);
            levelTag.setLabel(entity.getLevelLabel());
            result.add(levelTag);
        }
        List<String> tags = ScenicJsonUtil.parseTags(entity.getTag());
        for (String tag : tags) {
            ScenicTagVo tagVo = new ScenicTagVo();
            tagVo.setId(index++);
            tagVo.setType(index % 2 == 0 ? TAG_TYPE_ORANGE : TAG_TYPE_GREEN);
            tagVo.setLabel(tag);
            result.add(tagVo);
        }
        return result;
    }
}

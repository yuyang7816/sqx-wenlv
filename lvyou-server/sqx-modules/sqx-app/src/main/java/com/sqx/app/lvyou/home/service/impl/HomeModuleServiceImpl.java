package com.sqx.app.lvyou.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.PublishDestinationResolver;
import com.sqx.app.lvyou.home.db.service.IHomeModuleService;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleInfoBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModulePageBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleSaveBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleUpdateBo;
import com.sqx.app.lvyou.home.domain.bo.HomeModuleUpdateStateBo;
import com.sqx.app.lvyou.home.domain.entity.HomeModuleEntity;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleAppVo;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleVo;
import com.sqx.app.lvyou.home.service.HomeModuleService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 首页模块配置服务实现
 */
@Service
public class HomeModuleServiceImpl implements HomeModuleService {

    private static final int STATUS_ENABLED = 1;
    private static final DateTimeFormatter DT_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final Map<String, String> LABEL_KEY_MAP = buildLabelKeyMap();

    private static Map<String, String> buildLabelKeyMap() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("综合推荐", "recommend");
        map.put("推荐", "recommend");
        map.put("酒店住宿", "hotel");
        map.put("酒店", "hotel");
        map.put("线路游", "route");
        map.put("线路", "route");
        map.put("活动体验", "activity");
        map.put("活动", "activity");
        map.put("景点门票", "scenic");
        map.put("景点", "scenic");
        map.put("非遗文创", "heritage");
        map.put("文创", "heritage");
        map.put("好物", "heritage");
        return map;
    }

    @Resource
    private IHomeModuleService iHomeModuleService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private PublishDestinationResolver publishDestinationResolver;

    @Override
    public IPage<HomeModuleVo> pageInfo(HomeModulePageBo pageBo) {
        LambdaQueryWrapper<HomeModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(pageBo.getModuleType())) {
            queryWrapper.eq(HomeModuleEntity::getModuleType, pageBo.getModuleType().trim());
        }
        if (StringUtils.isNotBlank(pageBo.getModuleKey())) {
            queryWrapper.like(HomeModuleEntity::getModuleKey, pageBo.getModuleKey().trim());
        }
        if (StringUtils.isNotBlank(pageBo.getLabel())) {
            queryWrapper.like(HomeModuleEntity::getLabel, pageBo.getLabel().trim());
        }
        if (pageBo.getStatus() != null) {
            queryWrapper.eq(HomeModuleEntity::getStatus, pageBo.getStatus());
        }
        queryWrapper.orderByAsc(HomeModuleEntity::getSort).orderByAsc(HomeModuleEntity::getId);

        Page<HomeModuleEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        IPage<HomeModuleEntity> entityPage = iHomeModuleService.page(page, queryWrapper);

        Map<Long, DestinationEntity> destinationMap = loadDestinationMap(entityPage.getRecords());
        List<HomeModuleVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HomeModuleEntity entity : entityPage.getRecords()) {
                voList.add(toVo(entity, destinationMap));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public void saveInfo(HomeModuleSaveBo saveBo) {
        validateModuleType(saveBo.getModuleType());
        HomeModuleEntity entity = new HomeModuleEntity();
        BeanUtils.copyProperties(saveBo, entity);
        entity.setDestinationId(publishDestinationResolver.tryFromCoordinates(saveBo.getLatitude(), saveBo.getLongitude()));
        entity.setModuleType(saveBo.getModuleType().trim().toLowerCase());
        entity.setModuleKey(resolveModuleKey(
                saveBo.getModuleKey(),
                saveBo.getLabel(),
                saveBo.getLinkPath(),
                entity.getModuleType()
        ));
        iHomeModuleService.save(entity);
    }

    @Override
    public void updateInfo(HomeModuleUpdateBo updateBo) {
        HomeModuleEntity existing = iHomeModuleService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        validateModuleType(updateBo.getModuleType());
        String preservedModuleKey = existing.getModuleKey();
        BeanUtils.copyProperties(updateBo, existing);
        existing.setDestinationId(publishDestinationResolver.tryFromCoordinates(updateBo.getLatitude(), updateBo.getLongitude()));
        existing.setModuleType(updateBo.getModuleType().trim().toLowerCase());
        if (StringUtils.isNotBlank(updateBo.getModuleKey())) {
            existing.setModuleKey(updateBo.getModuleKey().trim());
        } else {
            existing.setModuleKey(preservedModuleKey);
        }
        iHomeModuleService.updateById(existing);
    }

    @Override
    public void updateState(HomeModuleUpdateStateBo updateStateBo) {
        HomeModuleEntity existing = iHomeModuleService.getById(updateStateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        LambdaUpdateWrapper<HomeModuleEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HomeModuleEntity::getId, updateStateBo.getId())
                .set(HomeModuleEntity::getStatus, updateStateBo.getStatus());
        iHomeModuleService.update(updateWrapper);
    }

    @Override
    public void delete(HomeModuleInfoBo infoBo) {
        HomeModuleEntity existing = iHomeModuleService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.INFO_NOT_FOUND);
        }
        iHomeModuleService.removeById(infoBo.getId());
    }

    @Override
    public List<HomeModuleAppVo> listAppModules(String moduleType, Long destinationId) {
        if (StringUtils.isBlank(moduleType)) {
            return new ArrayList<>();
        }
        String normalizedType = moduleType.trim().toLowerCase();

        LambdaQueryWrapper<HomeModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeModuleEntity::getModuleType, normalizedType)
                .eq(HomeModuleEntity::getStatus, STATUS_ENABLED)
                .and(w -> w.isNull(HomeModuleEntity::getDestinationId)
                        .or()
                        .eq(destinationId != null, HomeModuleEntity::getDestinationId, destinationId))
                .orderByAsc(HomeModuleEntity::getSort)
                .orderByAsc(HomeModuleEntity::getId);

        List<HomeModuleEntity> entities = iHomeModuleService.list(queryWrapper);
        List<HomeModuleAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (HomeModuleEntity entity : entities) {
                voList.add(toAppVo(entity));
            }
        }
        return voList;
    }

    @Override
    public List<HomeModuleAppVo> listSelectableTabs() {
        LambdaQueryWrapper<HomeModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeModuleEntity::getModuleType, "tab")
                .eq(HomeModuleEntity::getStatus, STATUS_ENABLED)
                .orderByAsc(HomeModuleEntity::getSort)
                .orderByAsc(HomeModuleEntity::getId);
        List<HomeModuleEntity> entities = iHomeModuleService.list(queryWrapper);
        List<HomeModuleAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (HomeModuleEntity entity : entities) {
                voList.add(toAppVo(entity));
            }
        }
        return voList;
    }

    @Override
    public boolean isValidTabKey(String tabKey) {
        if (StringUtils.isBlank(tabKey)) {
            return false;
        }
        LambdaQueryWrapper<HomeModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeModuleEntity::getModuleType, "tab")
                .eq(HomeModuleEntity::getStatus, STATUS_ENABLED)
                .eq(HomeModuleEntity::getModuleKey, tabKey.trim());
        return iHomeModuleService.count(queryWrapper) > 0;
    }

    @Override
    public String resolveTabLabel(String tabKey) {
        if (StringUtils.isBlank(tabKey)) {
            return null;
        }
        LambdaQueryWrapper<HomeModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeModuleEntity::getModuleType, "tab")
                .eq(HomeModuleEntity::getModuleKey, tabKey.trim())
                .last("LIMIT 1");
        HomeModuleEntity entity = iHomeModuleService.getOne(queryWrapper, false);
        return entity == null ? null : entity.getLabel();
    }

    private String resolveModuleKey(String moduleKey, String label, String linkPath, String moduleType) {
        if (StringUtils.isNotBlank(moduleKey)) {
            return moduleKey.trim();
        }
        String candidate = deriveKeyFromLinkPath(linkPath);
        if (StringUtils.isBlank(candidate)) {
            candidate = deriveKeyFromLabel(label);
        }
        if (StringUtils.isBlank(candidate)) {
            candidate = "module_" + System.currentTimeMillis();
        }
        return ensureUniqueModuleKey(candidate, moduleType);
    }

    private String deriveKeyFromLinkPath(String linkPath) {
        if (StringUtils.isBlank(linkPath)) {
            return null;
        }
        String[] parts = linkPath.trim().toLowerCase().split("/");
        for (int i = 0; i < parts.length; i++) {
            if (!"mobile-design".equals(parts[i]) || i + 1 >= parts.length) {
                continue;
            }
            String segment = parts[i + 1].trim();
            if (StringUtils.isBlank(segment) || isGenericPathSegment(segment)) {
                continue;
            }
            return sanitizeModuleKey(segment);
        }
        for (String part : parts) {
            if (StringUtils.isBlank(part) || isGenericPathSegment(part)) {
                continue;
            }
            String sanitized = sanitizeModuleKey(part);
            if (StringUtils.isNotBlank(sanitized)) {
                return sanitized;
            }
        }
        return null;
    }

    private boolean isGenericPathSegment(String segment) {
        return "list".equals(segment)
                || "index".equals(segment)
                || "detail".equals(segment)
                || "mobile-design".equals(segment)
                || "pages".equals(segment);
    }

    private String deriveKeyFromLabel(String label) {
        if (StringUtils.isBlank(label)) {
            return null;
        }
        String normalizedLabel = label.trim();
        for (Map.Entry<String, String> entry : LABEL_KEY_MAP.entrySet()) {
            if (normalizedLabel.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        String slug = normalizedLabel.toLowerCase().replaceAll("[^a-z0-9]+", "_").replaceAll("^_|_$", "");
        return StringUtils.isBlank(slug) ? null : slug;
    }

    private String sanitizeModuleKey(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        String sanitized = raw.trim().toLowerCase().replaceAll("[^a-z0-9_]+", "_").replaceAll("^_|_$", "");
        if (StringUtils.isBlank(sanitized)) {
            return null;
        }
        if (!sanitized.matches("^[a-z].*")) {
            sanitized = "tab_" + sanitized;
        }
        return sanitized;
    }

    private String ensureUniqueModuleKey(String candidate, String moduleType) {
        String baseKey = sanitizeModuleKey(candidate);
        if (StringUtils.isBlank(baseKey)) {
            baseKey = "module_" + System.currentTimeMillis();
        }
        if (!moduleKeyExists(baseKey, moduleType, null)) {
            return baseKey;
        }
        for (int index = 2; index <= 99; index++) {
            String nextKey = baseKey + "_" + index;
            if (!moduleKeyExists(nextKey, moduleType, null)) {
                return nextKey;
            }
        }
        return baseKey + "_" + System.currentTimeMillis();
    }

    private boolean moduleKeyExists(String moduleKey, String moduleType, Long excludeId) {
        LambdaQueryWrapper<HomeModuleEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HomeModuleEntity::getModuleType, moduleType)
                .eq(HomeModuleEntity::getModuleKey, moduleKey);
        if (excludeId != null) {
            queryWrapper.ne(HomeModuleEntity::getId, excludeId);
        }
        return iHomeModuleService.count(queryWrapper) > 0;
    }

    private void validateModuleType(String moduleType) {
        if (StringUtils.isBlank(moduleType)) {
            throw new BizException(AppError.NOTICE_CATEGORY_INVALID);
        }
        String normalized = moduleType.trim().toLowerCase();
        if (!"grid".equals(normalized) && !"tab".equals(normalized)) {
            throw new BizException(AppError.NOTICE_CATEGORY_INVALID);
        }
    }

    private HomeModuleVo toVo(HomeModuleEntity entity, Map<Long, DestinationEntity> destinationMap) {
        HomeModuleVo vo = new HomeModuleVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setModuleTypeLabel("grid".equals(entity.getModuleType()) ? "宫格入口" : "推荐Tab");
        vo.setStatusLabel(entity.getStatus() != null && entity.getStatus() == STATUS_ENABLED ? "启用" : "禁用");
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().format(DT_FORMAT));
        }
        if (entity.getDestinationId() != null) {
            DestinationEntity destination = destinationMap.get(entity.getDestinationId());
            vo.setDestinationName(DestinationDisplaySupport.resolveLabel(destination));
        }
        return vo;
    }

    private HomeModuleAppVo toAppVo(HomeModuleEntity entity) {
        HomeModuleAppVo vo = new HomeModuleAppVo();
        vo.setKey(entity.getModuleKey());
        vo.setLabel(entity.getLabel());
        vo.setIcon(entity.getIcon());
        vo.setIconColor(entity.getIconColor());
        vo.setBgColor(entity.getBgColor());
        vo.setPath(entity.getLinkPath());
        return vo;
    }

    private Map<Long, DestinationEntity> loadDestinationMap(List<HomeModuleEntity> records) {
        Map<Long, DestinationEntity> map = new HashMap<>();
        if (CollectionUtils.isEmpty(records)) {
            return map;
        }
        List<Long> ids = new ArrayList<>();
        for (HomeModuleEntity record : records) {
            if (record.getDestinationId() != null && !ids.contains(record.getDestinationId())) {
                ids.add(record.getDestinationId());
            }
        }
        if (CollectionUtils.isEmpty(ids)) {
            return map;
        }
        List<DestinationEntity> destinations = iDestinationService.listByIds(ids);
        for (DestinationEntity destination : destinations) {
            map.put(destination.getId(), destination);
        }
        return map;
    }
}

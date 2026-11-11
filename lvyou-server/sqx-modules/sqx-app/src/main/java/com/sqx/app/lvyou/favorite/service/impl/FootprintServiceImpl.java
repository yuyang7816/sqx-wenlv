package com.sqx.app.lvyou.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.lvyou.favorite.db.mapper.FootprintMapper;
import com.sqx.app.lvyou.favorite.domain.entity.FootprintEntity;
import com.sqx.app.lvyou.favorite.domain.vo.AssetItemVo;
import com.sqx.app.lvyou.favorite.service.FootprintService;
import com.sqx.app.lvyou.favorite.support.AssetTargetSnapshot;
import com.sqx.app.lvyou.favorite.support.AssetTargetSupport;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户浏览足迹业务服务实现
 */
@Service
public class FootprintServiceImpl implements FootprintService {

    private static final int MAX_FOOTPRINT_COUNT = 50;

    @Resource
    private FootprintMapper footprintMapper;

    @Resource
    private AssetTargetSupport assetTargetSupport;

    @Override
    public IPage<AssetItemVo> list(Long userId, String targetType, Long page, Long limit) {
        Page<FootprintEntity> pageParam = PageUtil.initPage(page, limit);
        LambdaQueryWrapper<FootprintEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FootprintEntity::getUserId, userId)
                .eq(StringUtils.isNotBlank(targetType), FootprintEntity::getTargetType, targetType)
                .orderByDesc(FootprintEntity::getVisitTime)
                .orderByDesc(FootprintEntity::getId);
        IPage<FootprintEntity> entityPage = footprintMapper.selectPage(pageParam, queryWrapper);

        List<FootprintEntity> records = entityPage.getRecords();
        List<String> types = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            for (FootprintEntity record : records) {
                types.add(record.getTargetType());
                ids.add(record.getTargetId());
            }
        }
        Map<String, AssetTargetSnapshot> snapshotMap = assetTargetSupport.resolveBatch(types, ids);

        List<AssetItemVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            for (FootprintEntity record : records) {
                voList.add(toVo(record, snapshotMap.get(AssetTargetSupport.buildKey(record.getTargetType(), record.getTargetId()))));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Async("asyncServiceExecutor")
    @Transactional(rollbackFor = Exception.class)
    public void recordAsync(Long userId, String targetType, Long targetId) {
        if (userId == null || StringUtils.isBlank(targetType) || targetId == null || targetId <= 0) {
            return;
        }
        try {
            assetTargetSupport.validateTarget(targetType, targetId);
        } catch (Exception ex) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<FootprintEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FootprintEntity::getUserId, userId)
                .eq(FootprintEntity::getTargetType, targetType.trim())
                .eq(FootprintEntity::getTargetId, targetId);
        FootprintEntity existing = footprintMapper.selectOne(queryWrapper);
        if (existing != null) {
            existing.setVisitTime(now);
            footprintMapper.updateById(existing);
        } else {
            FootprintEntity entity = new FootprintEntity();
            entity.setUserId(userId);
            entity.setTargetType(targetType.trim());
            entity.setTargetId(targetId);
            entity.setVisitTime(now);
            footprintMapper.insert(entity);
        }
        trimFootprints(userId);
    }

    private void trimFootprints(Long userId) {
        LambdaQueryWrapper<FootprintEntity> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(FootprintEntity::getUserId, userId);
        Long total = footprintMapper.selectCount(countWrapper);
        if (total == null || total <= MAX_FOOTPRINT_COUNT) {
            return;
        }
        int removeCount = (int) (total - MAX_FOOTPRINT_COUNT);
        LambdaQueryWrapper<FootprintEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FootprintEntity::getUserId, userId)
                .orderByAsc(FootprintEntity::getVisitTime)
                .orderByAsc(FootprintEntity::getId)
                .last("LIMIT " + removeCount);
        List<FootprintEntity> staleList = footprintMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(staleList)) {
            return;
        }
        for (FootprintEntity stale : staleList) {
            footprintMapper.deleteById(stale.getId());
        }
    }

    private AssetItemVo toVo(FootprintEntity entity, AssetTargetSnapshot snapshot) {
        AssetItemVo vo = new AssetItemVo();
        vo.setTargetType(entity.getTargetType());
        vo.setModuleType(entity.getTargetType());
        vo.setTargetId(entity.getTargetId());
        vo.setId(entity.getTargetId());
        if (entity.getVisitTime() != null) {
            vo.setVisitedAt(entity.getVisitTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
        }
        if (snapshot != null) {
            vo.setName(snapshot.getName());
            vo.setAddress(snapshot.getAddress());
            vo.setPath(snapshot.getPath());
            vo.setCover(snapshot.getCover());
            vo.setDesc(snapshot.getDesc());
            vo.setPrice(snapshot.getPrice());
        } else {
            vo.setPath(assetTargetSupport.buildPath(entity.getTargetType(), entity.getTargetId()));
        }
        if (Objects.isNull(vo.getName())) {
            vo.setName("已下架内容");
        }
        return vo;
    }
}

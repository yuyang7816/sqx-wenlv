package com.sqx.app.lvyou.favorite.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.lvyou.favorite.db.mapper.FavoriteMapper;
import com.sqx.app.lvyou.favorite.domain.bo.FavoriteToggleBo;
import com.sqx.app.lvyou.favorite.domain.entity.FavoriteEntity;
import com.sqx.app.lvyou.favorite.domain.vo.AssetItemVo;
import com.sqx.app.lvyou.favorite.domain.vo.FavoriteToggleVo;
import com.sqx.app.lvyou.favorite.service.FavoriteService;
import com.sqx.app.lvyou.favorite.support.AssetTargetSnapshot;
import com.sqx.app.lvyou.favorite.support.AssetTargetSupport;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 用户收藏业务服务实现
 */
@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Resource
    private FavoriteMapper favoriteMapper;

    @Resource
    private AssetTargetSupport assetTargetSupport;

    @Override
    public IPage<AssetItemVo> list(Long userId, String targetType, Long page, Long limit) {
        Page<FavoriteEntity> pageParam = PageUtil.initPage(page, limit);
        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteEntity::getUserId, userId)
                .eq(StringUtils.isNotBlank(targetType), FavoriteEntity::getTargetType, targetType)
                .orderByDesc(FavoriteEntity::getCreateTime)
                .orderByDesc(FavoriteEntity::getId);
        IPage<FavoriteEntity> entityPage = favoriteMapper.selectPage(pageParam, queryWrapper);

        List<FavoriteEntity> records = entityPage.getRecords();
        List<String> types = new ArrayList<>();
        List<Long> ids = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            for (FavoriteEntity record : records) {
                types.add(record.getTargetType());
                ids.add(record.getTargetId());
            }
        }
        Map<String, AssetTargetSnapshot> snapshotMap = assetTargetSupport.resolveBatch(types, ids);

        List<AssetItemVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(records)) {
            for (FavoriteEntity record : records) {
                voList.add(toVo(record, snapshotMap.get(AssetTargetSupport.buildKey(record.getTargetType(), record.getTargetId()))));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FavoriteToggleVo toggle(Long userId, FavoriteToggleBo toggleBo) {
        String targetType = toggleBo.getTargetType().trim();
        Long targetId = toggleBo.getTargetId();
        assetTargetSupport.validateTarget(targetType, targetId);

        LambdaQueryWrapper<FavoriteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FavoriteEntity::getUserId, userId)
                .eq(FavoriteEntity::getTargetType, targetType)
                .eq(FavoriteEntity::getTargetId, targetId);
        FavoriteEntity existing = favoriteMapper.selectOne(queryWrapper);

        FavoriteToggleVo vo = new FavoriteToggleVo();
        if (existing != null) {
            favoriteMapper.deleteById(existing.getId());
            vo.setFavorited(Boolean.FALSE);
            return vo;
        }

        FavoriteEntity entity = new FavoriteEntity();
        entity.setUserId(userId);
        entity.setTargetType(targetType);
        entity.setTargetId(targetId);
        favoriteMapper.insert(entity);
        vo.setFavorited(Boolean.TRUE);
        return vo;
    }

    private AssetItemVo toVo(FavoriteEntity entity, AssetTargetSnapshot snapshot) {
        AssetItemVo vo = new AssetItemVo();
        vo.setTargetType(entity.getTargetType());
        vo.setModuleType(entity.getTargetType());
        vo.setTargetId(entity.getTargetId());
        vo.setId(entity.getTargetId());
        if (entity.getCreateTime() != null) {
            vo.setFavoritedAt(entity.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli());
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

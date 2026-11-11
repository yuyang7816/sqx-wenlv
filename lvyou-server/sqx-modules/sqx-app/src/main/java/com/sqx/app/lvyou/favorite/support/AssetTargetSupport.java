package com.sqx.app.lvyou.favorite.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.activity.db.service.IActivityService;
import com.sqx.app.lvyou.activity.domain.entity.ActivityEntity;
import com.sqx.app.lvyou.favorite.enums.AssetTargetType;
import com.sqx.app.lvyou.goose.support.GooseStoreTypeSupport;
import com.sqx.app.lvyou.heritage.goods.db.service.IGoodsService;
import com.sqx.app.lvyou.heritage.goods.domain.entity.GoodsEntity;
import com.sqx.app.lvyou.heritage.enums.HeritageStatus;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.hotel.enums.HotelStatus;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.route.db.service.IRouteService;
import com.sqx.app.lvyou.route.domain.entity.RouteEntity;
import com.sqx.app.lvyou.route.enums.RouteStatus;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import com.sqx.app.lvyou.scenic.enums.ScenicStatus;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 收藏/足迹目标解析与校验
 */
@Component
public class AssetTargetSupport {

    private static final int MERCHANT_STATUS_NORMAL = 1;
    private static final int ACTIVITY_STATUS_ONLINE = 1;

    @Resource
    private IScenicService iScenicService;

    @Resource
    private IHotelService iHotelService;

    @Resource
    private IRouteService iRouteService;

    @Resource
    private IGoodsService iGoodsService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IActivityService iActivityService;

    public AssetTargetType parseType(String targetType) {
        AssetTargetType type = AssetTargetType.fromCode(targetType);
        if (type == null) {
            throw new BizException(AppError.FAVORITE_TARGET_TYPE_INVALID);
        }
        return type;
    }

    public void validateTarget(String targetType, Long targetId) {
        parseType(targetType);
        if (targetId == null || targetId <= 0) {
            throw new BizException(AppError.FAVORITE_TARGET_NOT_FOUND);
        }
        resolveOne(targetType, targetId, true);
    }

    public Map<String, AssetTargetSnapshot> resolveBatch(Collection<String> targetTypes, Collection<Long> targetIds) {
        Map<String, List<Long>> grouped = new HashMap<>();
        List<String> typeList = new ArrayList<>(targetTypes);
        List<Long> idList = new ArrayList<>(targetIds);
        int size = Math.min(typeList.size(), idList.size());
        for (int i = 0; i < size; i++) {
            String type = typeList.get(i);
            Long id = idList.get(i);
            if (StringUtils.isBlank(type) || id == null) {
                continue;
            }
            grouped.computeIfAbsent(type, key -> new ArrayList<>()).add(id);
        }

        Map<String, AssetTargetSnapshot> result = new HashMap<>();
        for (Map.Entry<String, List<Long>> entry : grouped.entrySet()) {
            String type = entry.getKey();
            List<Long> ids = entry.getValue();
            if (AssetTargetType.fromCode(type) == null || CollectionUtils.isEmpty(ids)) {
                continue;
            }
            result.putAll(loadByType(type, ids));
        }
        return result;
    }

    public String buildPath(String targetType, Long targetId) {
        AssetTargetType type = AssetTargetType.fromCode(targetType);
        if (type == null || targetId == null) {
            return "";
        }
        switch (type) {
            case SCENIC:
                return "/pages/scenic/detail?id=" + targetId;
            case HOTEL:
                return "/pages/hotel/detail?id=" + targetId;
            case ROUTE:
                return "/pages/route/detail?id=" + targetId;
            case GOODS:
                return "/pages/heritage/goods-detail?id=" + targetId;
            case GOOSE:
                return "/pages/goose/detail?id=" + targetId;
            case ACTIVITY:
                return "/pages/activity/detail?id=" + targetId;
            default:
                return "";
        }
    }

    private AssetTargetSnapshot resolveOne(String targetType, Long targetId, boolean strict) {
        Map<String, AssetTargetSnapshot> map = loadByType(targetType, java.util.Collections.singletonList(targetId));
        AssetTargetSnapshot snapshot = map.get(buildKey(targetType, targetId));
        if (snapshot == null && strict) {
            throw new BizException(AppError.FAVORITE_TARGET_NOT_FOUND);
        }
        return snapshot;
    }

    private Map<String, AssetTargetSnapshot> loadByType(String targetType, List<Long> ids) {
        AssetTargetType type = AssetTargetType.fromCode(targetType);
        if (type == null || CollectionUtils.isEmpty(ids)) {
            return new HashMap<>();
        }
        switch (type) {
            case SCENIC:
                return loadScenic(ids);
            case HOTEL:
                return loadHotel(ids);
            case ROUTE:
                return loadRoute(ids);
            case GOODS:
                return loadGoods(ids);
            case GOOSE:
                return loadGoose(ids);
            case ACTIVITY:
                return loadActivity(ids);
            default:
                return new HashMap<>();
        }
    }

    private Map<String, AssetTargetSnapshot> loadScenic(List<Long> ids) {
        List<ScenicEntity> entities = iScenicService.listByIds(ids);
        Map<String, AssetTargetSnapshot> map = new HashMap<>();
        if (CollectionUtils.isEmpty(entities)) {
            return map;
        }
        for (ScenicEntity entity : entities) {
            if (!Objects.equals(entity.getStatus(), ScenicStatus.ONLINE.getCode())) {
                continue;
            }
            AssetTargetSnapshot snapshot = new AssetTargetSnapshot();
            snapshot.setTargetType(AssetTargetType.SCENIC.getCode());
            snapshot.setTargetId(entity.getId());
            snapshot.setName(entity.getName());
            snapshot.setAddress(entity.getAddress());
            snapshot.setCover(entity.getCoverImage());
            snapshot.setDesc(entity.getIntro());
            snapshot.setPrice(entity.getPriceFrom());
            snapshot.setPath(buildPath(AssetTargetType.SCENIC.getCode(), entity.getId()));
            map.put(buildKey(AssetTargetType.SCENIC.getCode(), entity.getId()), snapshot);
        }
        return map;
    }

    private Map<String, AssetTargetSnapshot> loadHotel(List<Long> ids) {
        List<HotelEntity> entities = iHotelService.listByIds(ids);
        Map<String, AssetTargetSnapshot> map = new HashMap<>();
        if (CollectionUtils.isEmpty(entities)) {
            return map;
        }
        for (HotelEntity entity : entities) {
            if (!Objects.equals(entity.getStatus(), HotelStatus.ONLINE.getCode())) {
                continue;
            }
            AssetTargetSnapshot snapshot = new AssetTargetSnapshot();
            snapshot.setTargetType(AssetTargetType.HOTEL.getCode());
            snapshot.setTargetId(entity.getId());
            snapshot.setName(entity.getName());
            snapshot.setAddress(entity.getAddress());
            snapshot.setCover(entity.getCoverImage());
            snapshot.setDesc(entity.getTag());
            snapshot.setPrice(entity.getPriceFrom());
            snapshot.setPath(buildPath(AssetTargetType.HOTEL.getCode(), entity.getId()));
            map.put(buildKey(AssetTargetType.HOTEL.getCode(), entity.getId()), snapshot);
        }
        return map;
    }

    private Map<String, AssetTargetSnapshot> loadRoute(List<Long> ids) {
        List<RouteEntity> entities = iRouteService.listByIds(ids);
        Map<String, AssetTargetSnapshot> map = new HashMap<>();
        if (CollectionUtils.isEmpty(entities)) {
            return map;
        }
        for (RouteEntity entity : entities) {
            if (!Objects.equals(entity.getStatus(), RouteStatus.ONLINE.getCode())) {
                continue;
            }
            AssetTargetSnapshot snapshot = new AssetTargetSnapshot();
            snapshot.setTargetType(AssetTargetType.ROUTE.getCode());
            snapshot.setTargetId(entity.getId());
            snapshot.setName(entity.getName());
            snapshot.setAddress(entity.getMeetPoint());
            snapshot.setCover(entity.getCoverImage());
            snapshot.setDesc(StringUtils.defaultIfBlank(entity.getDescription(), entity.getIntro()));
            snapshot.setPrice(entity.getPriceFrom());
            snapshot.setPath(buildPath(AssetTargetType.ROUTE.getCode(), entity.getId()));
            map.put(buildKey(AssetTargetType.ROUTE.getCode(), entity.getId()), snapshot);
        }
        return map;
    }

    private Map<String, AssetTargetSnapshot> loadGoods(List<Long> ids) {
        List<GoodsEntity> entities = iGoodsService.listByIds(ids);
        Map<String, AssetTargetSnapshot> map = new HashMap<>();
        if (CollectionUtils.isEmpty(entities)) {
            return map;
        }
        for (GoodsEntity entity : entities) {
            if (!Objects.equals(entity.getStatus(), HeritageStatus.ONLINE.getCode())) {
                continue;
            }
            AssetTargetSnapshot snapshot = new AssetTargetSnapshot();
            snapshot.setTargetType(AssetTargetType.GOODS.getCode());
            snapshot.setTargetId(entity.getId());
            snapshot.setName(entity.getName());
            snapshot.setCover(entity.getCoverImage());
            snapshot.setDesc(entity.getDescription());
            snapshot.setPrice(entity.getPrice());
            snapshot.setPath(buildPath(AssetTargetType.GOODS.getCode(), entity.getId()));
            map.put(buildKey(AssetTargetType.GOODS.getCode(), entity.getId()), snapshot);
        }
        return map;
    }

    private Map<String, AssetTargetSnapshot> loadGoose(List<Long> ids) {
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantEntity::getId, ids)
                .eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .in(MerchantEntity::getStoreType, GooseStoreTypeSupport.foodStoreTypes());
        List<MerchantEntity> entities = iMerchantService.list(queryWrapper);
        Map<String, AssetTargetSnapshot> map = new HashMap<>();
        if (CollectionUtils.isEmpty(entities)) {
            return map;
        }
        for (MerchantEntity entity : entities) {
            AssetTargetSnapshot snapshot = new AssetTargetSnapshot();
            snapshot.setTargetType(AssetTargetType.GOOSE.getCode());
            snapshot.setTargetId(entity.getId());
            snapshot.setName(entity.getStoreName());
            snapshot.setAddress(entity.getStoreAddress());
            snapshot.setDesc(entity.getStoreRegion());
            snapshot.setPath(buildPath(AssetTargetType.GOOSE.getCode(), entity.getId()));
            map.put(buildKey(AssetTargetType.GOOSE.getCode(), entity.getId()), snapshot);
        }
        return map;
    }

    private Map<String, AssetTargetSnapshot> loadActivity(List<Long> ids) {
        List<ActivityEntity> entities = iActivityService.listByIds(ids);
        Map<String, AssetTargetSnapshot> map = new HashMap<>();
        if (CollectionUtils.isEmpty(entities)) {
            return map;
        }
        for (ActivityEntity entity : entities) {
            if (!Objects.equals(entity.getStatus(), ACTIVITY_STATUS_ONLINE)) {
                continue;
            }
            AssetTargetSnapshot snapshot = new AssetTargetSnapshot();
            snapshot.setTargetType(AssetTargetType.ACTIVITY.getCode());
            snapshot.setTargetId(entity.getId());
            snapshot.setName(entity.getName());
            snapshot.setAddress(entity.getAddress());
            snapshot.setCover(entity.getCoverImage());
            snapshot.setDesc(entity.getDescription());
            snapshot.setPrice(entity.getPriceFrom());
            snapshot.setPath(buildPath(AssetTargetType.ACTIVITY.getCode(), entity.getId()));
            map.put(buildKey(AssetTargetType.ACTIVITY.getCode(), entity.getId()), snapshot);
        }
        return map;
    }

    public static String buildKey(String targetType, Long targetId) {
        return targetType + "-" + targetId;
    }
}

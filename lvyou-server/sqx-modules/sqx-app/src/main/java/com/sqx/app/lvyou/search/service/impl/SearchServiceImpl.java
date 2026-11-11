package com.sqx.app.lvyou.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.lvyou.activity.db.service.IActivityService;
import com.sqx.app.lvyou.activity.domain.entity.ActivityEntity;
import com.sqx.app.lvyou.activity.enums.ActivityStatus;
import com.sqx.app.lvyou.heritage.enums.HeritageStatus;
import com.sqx.app.lvyou.heritage.goods.db.service.IGoodsService;
import com.sqx.app.lvyou.heritage.goods.domain.entity.GoodsEntity;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import com.sqx.app.lvyou.heritage.util.HeritageJsonUtil;
import com.sqx.app.lvyou.home.util.RecommendTagUtil;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.hotel.enums.HotelStatus;
import com.sqx.app.lvyou.route.db.service.IRouteService;
import com.sqx.app.lvyou.route.domain.entity.RouteEntity;
import com.sqx.app.lvyou.route.enums.RouteStatus;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import com.sqx.app.lvyou.scenic.enums.ScenicStatus;
import com.sqx.app.lvyou.search.domain.vo.SearchGroupVo;
import com.sqx.app.lvyou.search.domain.vo.SearchItemVo;
import com.sqx.app.lvyou.search.domain.vo.SearchResultVo;
import com.sqx.app.lvyou.search.enums.SearchContentType;
import com.sqx.app.lvyou.search.service.SearchService;
import com.sqx.app.lvyou.search.support.SearchSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 综合搜索服务实现：聚合酒店/线路/景区/活动/非遗商品/非遗店铺 LIKE 查询
 */
@Service
public class SearchServiceImpl implements SearchService {

    private static final int SEARCH_LIMIT = 5;

    @Resource
    private IHotelService iHotelService;

    @Resource
    private IRouteService iRouteService;

    @Resource
    private IScenicService iScenicService;

    @Resource
    private IActivityService iActivityService;

    @Resource
    private IGoodsService iGoodsService;

    @Resource
    private IHeritageShopService iHeritageShopService;

    @Resource
    private SearchSupport searchSupport;

    @Override
    public SearchResultVo search(String keywordCode, Long destinationId) {
        String keywordLabel = searchSupport.resolveKeywordLabel(keywordCode);

        SearchResultVo resultVo = new SearchResultVo();
        resultVo.setKeywordCode(keywordCode.trim().toLowerCase());
        resultVo.setKeyword(keywordLabel);
        resultVo.setDestinationId(destinationId);
        resultVo.setGroups(buildGroups(keywordLabel, destinationId));
        return resultVo;
    }

    private List<SearchGroupVo> buildGroups(String keyword, Long destinationId) {
        List<SearchGroupVo> groups = new ArrayList<>();
        groups.add(buildGroup(SearchContentType.HOTEL, searchHotels(keyword, destinationId)));
        groups.add(buildGroup(SearchContentType.ROUTE, searchRoutes(keyword, destinationId)));
        groups.add(buildGroup(SearchContentType.SCENIC, searchScenics(keyword, destinationId)));
        groups.add(buildGroup(SearchContentType.ACTIVITY, searchActivities(keyword, destinationId)));
        groups.add(buildGroup(SearchContentType.GOODS, searchGoods(keyword, destinationId)));
        groups.add(buildGroup(SearchContentType.SHOP, searchShops(keyword, destinationId)));
        return groups;
    }

    private SearchGroupVo buildGroup(SearchContentType contentType, List<SearchItemVo> items) {
        SearchGroupVo groupVo = new SearchGroupVo();
        groupVo.setType(contentType.getType());
        groupVo.setLabel(contentType.getLabel());
        groupVo.setItems(items == null ? new ArrayList<>() : items);
        return groupVo;
    }

    private List<SearchItemVo> searchHotels(String keyword, Long destinationId) {
        LambdaQueryWrapper<HotelEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HotelEntity::getStatus, HotelStatus.ONLINE.getCode())
                .eq(destinationId != null, HotelEntity::getDestinationId, destinationId)
                .like(HotelEntity::getName, keyword)
                .orderByAsc(HotelEntity::getSort)
                .orderByDesc(HotelEntity::getId)
                .last("LIMIT " + SEARCH_LIMIT);
        return toItems(iHotelService.list(queryWrapper), this::toHotelItem);
    }

    private List<SearchItemVo> searchRoutes(String keyword, Long destinationId) {
        LambdaQueryWrapper<RouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteEntity::getStatus, RouteStatus.ONLINE.getCode())
                .eq(destinationId != null, RouteEntity::getDestinationId, destinationId)
                .like(RouteEntity::getName, keyword)
                .orderByAsc(RouteEntity::getSort)
                .orderByDesc(RouteEntity::getId)
                .last("LIMIT " + SEARCH_LIMIT);
        return toItems(iRouteService.list(queryWrapper), this::toRouteItem);
    }

    private List<SearchItemVo> searchScenics(String keyword, Long destinationId) {
        LambdaQueryWrapper<ScenicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicEntity::getStatus, ScenicStatus.ONLINE.getCode())
                .eq(destinationId != null, ScenicEntity::getDestinationId, destinationId)
                .like(ScenicEntity::getName, keyword)
                .orderByAsc(ScenicEntity::getSort)
                .orderByDesc(ScenicEntity::getId)
                .last("LIMIT " + SEARCH_LIMIT);
        return toItems(iScenicService.list(queryWrapper), this::toScenicItem);
    }

    private List<SearchItemVo> searchActivities(String keyword, Long destinationId) {
        LambdaQueryWrapper<ActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityEntity::getStatus, ActivityStatus.ONLINE.getCode())
                .eq(destinationId != null, ActivityEntity::getDestinationId, destinationId)
                .like(ActivityEntity::getName, keyword)
                .orderByAsc(ActivityEntity::getSort)
                .orderByDesc(ActivityEntity::getId)
                .last("LIMIT " + SEARCH_LIMIT);
        return toItems(iActivityService.list(queryWrapper), this::toActivityItem);
    }

    private List<SearchItemVo> searchGoods(String keyword, Long destinationId) {
        List<Long> shopIds = resolveShopIds(destinationId);
        if (shopIds != null && shopIds.isEmpty()) {
            return new ArrayList<>();
        }
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsEntity::getStatus, HeritageStatus.ONLINE.getCode())
                .like(GoodsEntity::getName, keyword)
                .in(shopIds != null, GoodsEntity::getShopId, shopIds)
                .orderByAsc(GoodsEntity::getSort)
                .orderByDesc(GoodsEntity::getId)
                .last("LIMIT " + SEARCH_LIMIT);
        return toItems(iGoodsService.list(queryWrapper), this::toGoodsItem);
    }

    private List<SearchItemVo> searchShops(String keyword, Long destinationId) {
        LambdaQueryWrapper<HeritageShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HeritageShopEntity::getStatus, HeritageStatus.ONLINE.getCode())
                .eq(destinationId != null, HeritageShopEntity::getDestinationId, destinationId)
                .like(HeritageShopEntity::getName, keyword)
                .orderByAsc(HeritageShopEntity::getSort)
                .orderByDesc(HeritageShopEntity::getId)
                .last("LIMIT " + SEARCH_LIMIT);
        return toItems(iHeritageShopService.list(queryWrapper), this::toShopItem);
    }

    /**
     * 商品无目的地字段，通过店铺目的地间接筛选；null 表示不限目的地
     */
    private List<Long> resolveShopIds(Long destinationId) {
        if (destinationId == null) {
            return null;
        }
        LambdaQueryWrapper<HeritageShopEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.select(HeritageShopEntity::getId)
                .eq(HeritageShopEntity::getStatus, HeritageStatus.ONLINE.getCode())
                .eq(HeritageShopEntity::getDestinationId, destinationId);
        List<HeritageShopEntity> shops = iHeritageShopService.list(queryWrapper);
        if (CollectionUtils.isEmpty(shops)) {
            return Collections.emptyList();
        }
        return shops.stream()
                .map(HeritageShopEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private <E> List<SearchItemVo> toItems(List<E> entities, Function<E, SearchItemVo> mapper) {
        List<SearchItemVo> items = new ArrayList<>();
        if (CollectionUtils.isEmpty(entities)) {
            return items;
        }
        for (E entity : entities) {
            items.add(mapper.apply(entity));
        }
        return items;
    }

    private SearchItemVo toHotelItem(HotelEntity entity) {
        SearchItemVo item = new SearchItemVo();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setCover(StringUtils.defaultString(entity.getCoverImage()));
        item.setPrice(entity.getPriceFrom());
        item.setTag(RecommendTagUtil.resolveTag(entity.getTag()));
        item.setAddress(StringUtils.defaultString(entity.getAddress()));
        item.setScore(entity.getScore());
        return item;
    }

    private SearchItemVo toRouteItem(RouteEntity entity) {
        SearchItemVo item = new SearchItemVo();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setCover(StringUtils.defaultString(entity.getCoverImage()));
        item.setPrice(entity.getPriceFrom());
        item.setTag(RecommendTagUtil.resolveTag(entity.getTags()));
        item.setAddress(StringUtils.defaultString(entity.getMeetPoint()));
        return item;
    }

    private SearchItemVo toScenicItem(ScenicEntity entity) {
        SearchItemVo item = new SearchItemVo();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setCover(StringUtils.defaultString(entity.getCoverImage()));
        item.setPrice(entity.getPriceFrom());
        item.setTag(RecommendTagUtil.resolveTag(entity.getTag()));
        item.setAddress(StringUtils.defaultString(entity.getAddress()));
        return item;
    }

    private SearchItemVo toActivityItem(ActivityEntity entity) {
        SearchItemVo item = new SearchItemVo();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setCover(StringUtils.defaultString(entity.getCoverImage()));
        item.setPrice(entity.getPriceFrom());
        item.setTag(RecommendTagUtil.resolveTag(entity.getTag()));
        item.setAddress(StringUtils.defaultString(entity.getAddress()));
        return item;
    }

    private SearchItemVo toGoodsItem(GoodsEntity entity) {
        SearchItemVo item = new SearchItemVo();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setCover(StringUtils.defaultString(entity.getCoverImage()));
        item.setPrice(entity.getPrice());
        item.setTag(StringUtils.defaultString(entity.getDescription()));
        item.setScore(entity.getRating());
        return item;
    }

    private SearchItemVo toShopItem(HeritageShopEntity entity) {
        SearchItemVo item = new SearchItemVo();
        item.setId(entity.getId());
        item.setName(entity.getName());
        item.setCover(StringUtils.defaultString(entity.getCoverImage()));
        item.setTag(resolveShopTag(entity));
        item.setAddress(StringUtils.defaultString(entity.getAddress()));
        item.setScore(entity.getRating());
        return item;
    }

    private String resolveShopTag(HeritageShopEntity entity) {
        List<String> tags = HeritageJsonUtil.parseStringList(entity.getTags());
        if (CollectionUtils.isNotEmpty(tags)) {
            return String.join("·", tags);
        }
        return RecommendTagUtil.resolveTag(entity.getTag());
    }
}

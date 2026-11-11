package com.sqx.app.lvyou.home.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.lvyou.heritage.goods.db.service.IGoodsService;
import com.sqx.app.lvyou.heritage.goods.domain.entity.GoodsEntity;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.home.service.MerchantHomeRecommendService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantServiceItemService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantServiceEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantServiceStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.route.db.service.IRouteService;
import com.sqx.app.lvyou.route.domain.entity.RouteEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 首页推荐商户服务聚合
 */
@Service
public class MerchantHomeRecommendServiceImpl implements MerchantHomeRecommendService {

    private static final int MERCHANT_OPEN = 1;
    private static final int HOME_RECOMMEND_ENABLED = 1;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantServiceItemService iMerchantServiceItemService;

    @Resource
    private IGoodsService iGoodsService;

    @Resource
    private IRouteService iRouteService;

    @Override
    public List<HomeRecommendItemVo> listByTab(Long destinationId, String tabKey, int limit) {
        if (StringUtils.isBlank(tabKey) || limit <= 0) {
            return Collections.emptyList();
        }
        List<Long> merchantIds = listOpenMerchantIds(destinationId);
        if (CollectionUtils.isEmpty(merchantIds)) {
            return Collections.emptyList();
        }

        LambdaQueryWrapper<MerchantServiceEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantServiceEntity::getMerchantId, merchantIds)
                .eq(MerchantServiceEntity::getStatus, MerchantServiceStatus.ON_SALE.getCode())
                .eq(MerchantServiceEntity::getHomeTabKey, tabKey.trim())
                .orderByDesc(MerchantServiceEntity::getSort)
                .orderByDesc(MerchantServiceEntity::getUpdateTime)
                .orderByDesc(MerchantServiceEntity::getId)
                .last("LIMIT " + limit);

        List<MerchantServiceEntity> services = iMerchantServiceItemService.list(queryWrapper);
        if (CollectionUtils.isEmpty(services)) {
            return Collections.emptyList();
        }

        Map<Long, MerchantEntity> merchantMap = loadMerchantMap(services);
        List<HomeRecommendItemVo> items = new ArrayList<>();
        String normalizedTabKey = tabKey.trim();
        for (MerchantServiceEntity service : services) {
            MerchantEntity merchant = merchantMap.get(service.getMerchantId());
            if (merchant == null || !matchesTabStoreType(normalizedTabKey, merchant.getStoreType())) {
                continue;
            }
            items.add(toRecommendItem(service, merchant));
        }
        return items;
    }

    private boolean matchesTabStoreType(String tabKey, String storeType) {
        String normalizedStoreType = MerchantStoreType.normalize(storeType);
        if ("heritage".equals(tabKey) || "goods".equals(tabKey) || "culture".equals(tabKey)) {
            return "culture".equals(normalizedStoreType);
        }
        if ("route".equals(tabKey)) {
            return "travel".equals(normalizedStoreType);
        }
        if ("hotel".equals(tabKey)) {
            return "hotel".equals(normalizedStoreType);
        }
        if ("activity".equals(tabKey)) {
            return !"culture".equals(normalizedStoreType);
        }
        return true;
    }

    private List<Long> listOpenMerchantIds(Long destinationId) {
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getStatus, MERCHANT_OPEN);
        if (destinationId != null) {
            queryWrapper.eq(MerchantEntity::getDestinationId, destinationId);
        }
        List<MerchantEntity> merchants = iMerchantService.list(queryWrapper);
        if (CollectionUtils.isEmpty(merchants)) {
            return Collections.emptyList();
        }
        return merchants.stream().map(MerchantEntity::getId).collect(Collectors.toList());
    }

    private Map<Long, MerchantEntity> loadMerchantMap(List<MerchantServiceEntity> services) {
        List<Long> merchantIds = services.stream()
                .map(MerchantServiceEntity::getMerchantId)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(merchantIds)) {
            return Collections.emptyMap();
        }
        List<MerchantEntity> merchants = iMerchantService.listByIds(merchantIds);
        Map<Long, MerchantEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(merchants)) {
            for (MerchantEntity merchant : merchants) {
                map.put(merchant.getId(), merchant);
            }
        }
        return map;
    }

    private HomeRecommendItemVo toRecommendItem(MerchantServiceEntity service, MerchantEntity merchant) {
        HomeRecommendItemVo vo = new HomeRecommendItemVo();
        vo.setId(resolveRecommendId(merchant, service));
        vo.setTitle(service.getName());
        vo.setCover(StringUtils.defaultString(service.getCoverImage()));
        vo.setPrice(service.getPrice());
        vo.setTag(StringUtils.defaultIfBlank(merchant.getStoreName(), "商户服务"));
        vo.setItemType(resolveItemType(merchant, service));
        vo.setLinkPath(resolveLinkPath(merchant, service));
        return vo;
    }

    private String resolveItemType(MerchantEntity merchant, MerchantServiceEntity service) {
        String storeType = MerchantStoreType.normalize(merchant.getStoreType());
        if ("culture".equals(storeType)) {
            return "heritage";
        }
        if ("travel".equals(storeType)) {
            return "route";
        }
        if ("hotel".equals(storeType)) {
            return "hotel";
        }
        return "merchant";
    }

    private Long resolveRecommendId(MerchantEntity merchant, MerchantServiceEntity service) {
        String storeType = MerchantStoreType.normalize(merchant.getStoreType());
        if ("culture".equals(storeType)) {
            GoodsEntity goods = findGoodsByServiceId(service.getId());
            if (goods != null && goods.getId() != null) {
                return goods.getId();
            }
        }
        if ("travel".equals(storeType)) {
            RouteEntity route = findRouteByServiceId(service.getId());
            if (route != null && route.getId() != null) {
                return route.getId();
            }
        }
        return service.getId();
    }

    private String resolveLinkPath(MerchantEntity merchant, MerchantServiceEntity service) {
        String storeType = MerchantStoreType.normalize(merchant.getStoreType());
        if ("culture".equals(storeType)) {
            GoodsEntity goods = findGoodsByServiceId(service.getId());
            if (goods != null && goods.getId() != null) {
                return "/mobile-design/heritage/goods-detail?id=" + goods.getId();
            }
        }
        if ("travel".equals(storeType)) {
            RouteEntity route = findRouteByServiceId(service.getId());
            if (route != null && route.getId() != null) {
                return "/mobile-design/route/detail?id=" + route.getId();
            }
        }
        return "/pages/merchant/service?merchantId=" + merchant.getId();
    }

    private GoodsEntity findGoodsByServiceId(Long serviceId) {
        if (serviceId == null) {
            return null;
        }
        LambdaQueryWrapper<GoodsEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(GoodsEntity::getMerchantServiceId, serviceId)
                .last("LIMIT 1");
        return iGoodsService.getOne(queryWrapper, false);
    }

    private RouteEntity findRouteByServiceId(Long serviceId) {
        if (serviceId == null) {
            return null;
        }
        LambdaQueryWrapper<RouteEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RouteEntity::getMerchantServiceId, serviceId)
                .last("LIMIT 1");
        return iRouteService.getOne(queryWrapper, false);
    }
}


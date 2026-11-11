package com.sqx.app.lvyou.home.service.impl;

import com.sqx.app.lvyou.activity.service.ActivityService;
import com.sqx.app.lvyou.home.domain.vo.HomeModuleAppVo;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendItemVo;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendTabVo;
import com.sqx.app.lvyou.home.domain.vo.HomeRecommendVo;
import com.sqx.app.lvyou.home.service.HomeModuleService;
import com.sqx.app.lvyou.home.service.HomeRecommendService;
import com.sqx.app.lvyou.home.service.MerchantHomeRecommendService;
import com.sqx.app.lvyou.hotel.service.HotelService;
import com.sqx.app.lvyou.route.service.RouteService;
import com.sqx.app.lvyou.scenic.service.ScenicService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 首页推荐聚合服务实现
 */
@Service
public class HomeRecommendServiceImpl implements HomeRecommendService {

    private static final int RECOMMEND_LIMIT = 4;

    @Resource
    private HotelService hotelService;

    @Resource
    private RouteService routeService;

    @Resource
    private ActivityService activityService;

    @Resource
    private ScenicService scenicService;

    @Resource
    private HomeModuleService homeModuleService;

    @Resource
    private MerchantHomeRecommendService merchantHomeRecommendService;

    @Override
    public HomeRecommendVo recommend(Long destinationId) {
        HomeRecommendVo vo = new HomeRecommendVo();
        vo.setDestinationId(destinationId);
        vo.setTabs(buildTabs(destinationId));
        return vo;
    }

    private List<HomeRecommendTabVo> buildTabs(Long destinationId) {
        List<HomeRecommendItemVo> hotels = tagItems(safeList(hotelService.listRecommend(destinationId, RECOMMEND_LIMIT)), "hotel");
        List<HomeRecommendItemVo> routes = tagItems(safeList(routeService.listRecommend(destinationId, RECOMMEND_LIMIT)), "route");
        List<HomeRecommendItemVo> activities = tagItems(safeList(activityService.listRecommend(destinationId, RECOMMEND_LIMIT)), "activity");
        List<HomeRecommendItemVo> scenics = tagItems(safeList(scenicService.listRecommend(destinationId, RECOMMEND_LIMIT)), "scenic");
        List<HomeRecommendItemVo> heritageItems = tagItems(
                safeList(merchantHomeRecommendService.listByTab(destinationId, "heritage", RECOMMEND_LIMIT)), "heritage");

        List<HomeRecommendItemVo> compositeItems = dedupeItems(buildCompositeItems(
                hotels, routes, activities, scenics, heritageItems, destinationId));

        List<HomeModuleAppVo> tabConfigs = homeModuleService.listAppModules("tab", destinationId);
        if (tabConfigs == null || tabConfigs.isEmpty()) {
            return buildDefaultTabs(destinationId, hotels, routes, activities, scenics, heritageItems, compositeItems);
        }

        List<HomeRecommendTabVo> tabs = new ArrayList<>();
        for (HomeModuleAppVo config : tabConfigs) {
            String key = config.getKey();
            String title = config.getLabel();
            List<HomeRecommendItemVo> items = resolveTabItems(key, destinationId, hotels, routes, activities, scenics,
                    heritageItems, compositeItems);
            tabs.add(buildTab(key, title, config.getPath(), items));
        }
        return tabs;
    }

    private List<HomeRecommendTabVo> buildDefaultTabs(Long destinationId,
                                                      List<HomeRecommendItemVo> hotels,
                                                      List<HomeRecommendItemVo> routes,
                                                      List<HomeRecommendItemVo> activities,
                                                      List<HomeRecommendItemVo> scenics,
                                                      List<HomeRecommendItemVo> heritageItems,
                                                      List<HomeRecommendItemVo> compositeItems) {
        return Arrays.asList(
                buildTab("recommend", "综合推荐", null, compositeItems),
                buildTab("hotel", "酒店住宿", "/mobile-design/hotel/list", safeList(hotels)),
                buildTab("route", "旅游线路", "/mobile-design/route/list", mergeRouteItems(routes, destinationId)),
                buildTab("activity", "精彩活动", "/mobile-design/activity/list", safeList(activities)),
                buildTab("heritage", "非遗好物", "/mobile-design/heritage/index", safeList(heritageItems)),
                buildTab("scenic", "旅游景区", "/mobile-design/scenic/list", safeList(scenics))
        );
    }

    private List<HomeRecommendItemVo> buildCompositeItems(List<HomeRecommendItemVo> hotels,
                                                          List<HomeRecommendItemVo> routes,
                                                          List<HomeRecommendItemVo> activities,
                                                          List<HomeRecommendItemVo> scenics,
                                                          List<HomeRecommendItemVo> heritageItems,
                                                          Long destinationId) {
        List<HomeRecommendItemVo> compositeItems = new ArrayList<>();
        compositeItems.addAll(hotels);
        compositeItems.addAll(routes);
        compositeItems.addAll(activities);
        compositeItems.addAll(scenics);
        compositeItems.addAll(heritageItems);
        compositeItems.addAll(merchantHomeRecommendService.listByTab(destinationId, "recommend", RECOMMEND_LIMIT));
        return compositeItems;
    }

    private List<HomeRecommendItemVo> resolveTabItems(String key,
                                                      Long destinationId,
                                                      List<HomeRecommendItemVo> hotels,
                                                      List<HomeRecommendItemVo> routes,
                                                      List<HomeRecommendItemVo> activities,
                                                      List<HomeRecommendItemVo> scenics,
                                                      List<HomeRecommendItemVo> heritageItems,
                                                      List<HomeRecommendItemVo> compositeItems) {
        if ("recommend".equalsIgnoreCase(key)) {
            return compositeItems;
        }
        if ("hotel".equalsIgnoreCase(key)) {
            return safeList(hotels);
        }
        if ("route".equalsIgnoreCase(key)) {
            return mergeRouteItems(routes, destinationId);
        }
        if ("activity".equalsIgnoreCase(key)) {
            return safeList(activities);
        }
        if ("heritage".equalsIgnoreCase(key) || "goods".equalsIgnoreCase(key) || "culture".equalsIgnoreCase(key)) {
            return safeList(heritageItems);
        }
        if ("scenic".equalsIgnoreCase(key)) {
            return safeList(scenics);
        }
        return merchantHomeRecommendService.listByTab(destinationId, key, RECOMMEND_LIMIT);
    }

    /** 线路 Tab：平台线路优先，商户线路按标题去重，避免同一条线路展示两次 */
    private List<HomeRecommendItemVo> mergeRouteItems(List<HomeRecommendItemVo> platformRoutes, Long destinationId) {
        List<HomeRecommendItemVo> items = new ArrayList<>(safeList(platformRoutes));
        Set<String> titleKeys = new HashSet<>();
        for (HomeRecommendItemVo item : items) {
            collectTitleKey(titleKeys, item);
        }
        for (HomeRecommendItemVo merchantItem : merchantHomeRecommendService.listByTab(destinationId, "route", RECOMMEND_LIMIT)) {
            if (merchantItem == null) {
                continue;
            }
            String titleKey = normalizeTitle(merchantItem.getTitle());
            if (StringUtils.isNotBlank(titleKey) && !titleKeys.add(titleKey)) {
                continue;
            }
            if (StringUtils.isBlank(merchantItem.getItemType())) {
                merchantItem.setItemType("route");
            }
            items.add(merchantItem);
        }
        return items;
    }

    private List<HomeRecommendItemVo> dedupeItems(List<HomeRecommendItemVo> items) {
        if (items == null || items.isEmpty()) {
            return new ArrayList<>();
        }
        List<HomeRecommendItemVo> result = new ArrayList<>();
        Set<String> keys = new HashSet<>();
        for (HomeRecommendItemVo item : items) {
            if (item == null) {
                continue;
            }
            String key = buildDedupeKey(item);
            if (keys.add(key)) {
                result.add(item);
            }
        }
        return result;
    }

    private String buildDedupeKey(HomeRecommendItemVo item) {
        if (item.getId() != null && StringUtils.isNotBlank(item.getItemType())) {
            return item.getItemType().trim().toLowerCase() + "#" + item.getId();
        }
        return "title#" + normalizeTitle(item.getTitle());
    }

    private void collectTitleKey(Set<String> titleKeys, HomeRecommendItemVo item) {
        if (item == null) {
            return;
        }
        String titleKey = normalizeTitle(item.getTitle());
        if (StringUtils.isNotBlank(titleKey)) {
            titleKeys.add(titleKey);
        }
    }

    private String normalizeTitle(String title) {
        return StringUtils.trimToEmpty(title).toLowerCase().replaceAll("\\s+", "");
    }

    private List<HomeRecommendItemVo> tagItems(List<HomeRecommendItemVo> items, String itemType) {
        if (items == null) {
            return new ArrayList<>();
        }
        for (HomeRecommendItemVo item : items) {
            if (item != null && StringUtils.isBlank(item.getItemType())) {
                item.setItemType(itemType);
            }
        }
        return items;
    }

    private HomeRecommendTabVo buildTab(String key, String title, String listPath, List<HomeRecommendItemVo> items) {
        HomeRecommendTabVo tab = new HomeRecommendTabVo();
        tab.setKey(key);
        tab.setTitle(title);
        tab.setListPath(listPath);
        tab.setItems(items == null ? new ArrayList<>() : items);
        return tab;
    }

    private List<HomeRecommendItemVo> safeList(List<HomeRecommendItemVo> items) {
        return items == null ? new ArrayList<>() : items;
    }
}

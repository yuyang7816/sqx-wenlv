package com.sqx.app.lvyou.destination.support;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.map.db.service.IMapPoiService;
import com.sqx.app.base.map.domain.entity.MapPoiEntity;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.activity.db.service.IActivityService;
import com.sqx.app.lvyou.activity.domain.entity.ActivityEntity;
import com.sqx.app.lvyou.canteen.db.service.ICanteenReservationService;
import com.sqx.app.lvyou.canteen.domain.entity.CanteenReservationEntity;
import com.sqx.app.lvyou.heritage.craft.db.service.IHeritageCraftService;
import com.sqx.app.lvyou.heritage.craft.domain.entity.HeritageCraftEntity;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import com.sqx.app.lvyou.home.db.service.IHomeModuleService;
import com.sqx.app.lvyou.home.domain.entity.HomeModuleEntity;
import com.sqx.app.lvyou.hotel.db.service.IHotelService;
import com.sqx.app.lvyou.hotel.domain.entity.HotelEntity;
import com.sqx.app.lvyou.merchant.db.service.IMerchantApplyService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantApplyEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.notice.db.service.INoticeService;
import com.sqx.app.lvyou.notice.domain.entity.NoticeEntity;
import com.sqx.app.lvyou.route.db.service.IRouteService;
import com.sqx.app.lvyou.route.domain.entity.RouteEntity;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import com.sqx.app.lvyou.transport.db.service.ITransportRouteService;
import com.sqx.app.lvyou.transport.domain.entity.TransportRouteEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 目的地删除后：解绑引用，并尽量按经纬度重新匹配在线目的地；无法匹配则清空，列表改展示地址。
 */
@Component
public class DestinationCascadeSupport {

    @Resource
    private DestinationSupport destinationSupport;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantApplyService iMerchantApplyService;

    @Resource
    private IHotelService iHotelService;

    @Resource
    private IRouteService iRouteService;

    @Resource
    private IScenicService iScenicService;

    @Resource
    private IActivityService iActivityService;

    @Resource
    private IHeritageShopService iHeritageShopService;

    @Resource
    private IHeritageCraftService iHeritageCraftService;

    @Resource
    private ITransportRouteService iTransportRouteService;

    @Resource
    private IHomeModuleService iHomeModuleService;

    @Resource
    private INoticeService iNoticeService;

    @Resource
    private ICanteenReservationService iCanteenReservationService;

    @Resource
    private IMapPoiService iMapPoiService;

    public void rebindAfterDelete(Long deletedDestinationId) {
        if (deletedDestinationId == null) {
            return;
        }
        clearUserCurrentDestination(deletedDestinationId);
        Map<Long, Long> merchantRematch = rebindMerchants(deletedDestinationId);
        rebindMerchantApplies(deletedDestinationId);
        rebindHotels(deletedDestinationId);
        rebindRoutes(deletedDestinationId, merchantRematch);
        rebindScenics(deletedDestinationId);
        rebindActivities(deletedDestinationId);
        rebindHeritageShops(deletedDestinationId);
        rebindHeritageCrafts(deletedDestinationId);
        rebindTransportRoutes(deletedDestinationId);
        clearHomeModules(deletedDestinationId);
        clearNotices(deletedDestinationId);
        clearCanteenReservations(deletedDestinationId);
        rebindMapPois(deletedDestinationId);
    }

    private void clearUserCurrentDestination(Long deletedDestinationId) {
        LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserEntity::getCurrentDestinationId, deletedDestinationId)
                .set(UserEntity::getCurrentDestinationId, null);
        iUserService.update(new UserEntity(), updateWrapper);
    }

    private Map<Long, Long> rebindMerchants(Long deletedDestinationId) {
        Map<Long, Long> rematch = new HashMap<>();
        List<MerchantEntity> merchants = listByDestination(iMerchantService, MerchantEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(merchants)) {
            return rematch;
        }
        for (MerchantEntity merchant : merchants) {
            Long nextId = rematchByCoordinates(merchant.getLatitude(), merchant.getLongitude());
            merchant.setDestinationId(nextId);
            iMerchantService.updateById(merchant);
            rematch.put(merchant.getId(), nextId);
        }
        return rematch;
    }

    private void rebindMerchantApplies(Long deletedDestinationId) {
        List<MerchantApplyEntity> applies = listByDestination(iMerchantApplyService,
                MerchantApplyEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(applies)) {
            return;
        }
        for (MerchantApplyEntity apply : applies) {
            apply.setDestinationId(rematchByCoordinates(apply.getLatitude(), apply.getLongitude()));
            iMerchantApplyService.updateById(apply);
        }
    }

    private void rebindHotels(Long deletedDestinationId) {
        List<HotelEntity> hotels = listByDestination(iHotelService, HotelEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(hotels)) {
            return;
        }
        for (HotelEntity hotel : hotels) {
            hotel.setDestinationId(rematchByCoordinates(hotel.getLatitude(), hotel.getLongitude()));
            iHotelService.updateById(hotel);
        }
    }

    private void rebindRoutes(Long deletedDestinationId, Map<Long, Long> merchantRematch) {
        List<RouteEntity> routes = listByDestination(iRouteService, RouteEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(routes)) {
            return;
        }
        for (RouteEntity route : routes) {
            Long nextId = null;
            if (route.getMerchantId() != null && merchantRematch.containsKey(route.getMerchantId())) {
                nextId = merchantRematch.get(route.getMerchantId());
            } else if (route.getMerchantId() != null) {
                MerchantEntity merchant = iMerchantService.getById(route.getMerchantId());
                if (merchant != null) {
                    nextId = merchant.getDestinationId();
                    if (nextId == null) {
                        nextId = rematchByCoordinates(merchant.getLatitude(), merchant.getLongitude());
                    }
                }
            }
            route.setDestinationId(nextId);
            iRouteService.updateById(route);
        }
    }

    private void rebindScenics(Long deletedDestinationId) {
        List<ScenicEntity> scenics = listByDestination(iScenicService, ScenicEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(scenics)) {
            return;
        }
        for (ScenicEntity scenic : scenics) {
            scenic.setDestinationId(rematchByCoordinates(scenic.getLatitude(), scenic.getLongitude()));
            iScenicService.updateById(scenic);
        }
    }

    private void rebindActivities(Long deletedDestinationId) {
        List<ActivityEntity> activities = listByDestination(iActivityService, ActivityEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(activities)) {
            return;
        }
        for (ActivityEntity activity : activities) {
            activity.setDestinationId(rematchByCoordinates(activity.getLatitude(), activity.getLongitude()));
            iActivityService.updateById(activity);
        }
    }

    private void rebindHeritageShops(Long deletedDestinationId) {
        List<HeritageShopEntity> shops = listByDestination(iHeritageShopService,
                HeritageShopEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(shops)) {
            return;
        }
        for (HeritageShopEntity shop : shops) {
            shop.setDestinationId(rematchByCoordinates(shop.getLatitude(), shop.getLongitude()));
            iHeritageShopService.updateById(shop);
        }
    }

    private void rebindHeritageCrafts(Long deletedDestinationId) {
        List<HeritageCraftEntity> crafts = listByDestination(iHeritageCraftService,
                HeritageCraftEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(crafts)) {
            return;
        }
        for (HeritageCraftEntity craft : crafts) {
            craft.setDestinationId(rematchByCoordinates(craft.getLatitude(), craft.getLongitude()));
            iHeritageCraftService.updateById(craft);
        }
    }

    private void rebindTransportRoutes(Long deletedDestinationId) {
        List<TransportRouteEntity> routes = listByDestination(iTransportRouteService,
                TransportRouteEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(routes)) {
            return;
        }
        for (TransportRouteEntity route : routes) {
            route.setDestinationId(rematchByCoordinates(route.getLatitude(), route.getLongitude()));
            iTransportRouteService.updateById(route);
        }
    }

    private void clearHomeModules(Long deletedDestinationId) {
        LambdaUpdateWrapper<HomeModuleEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HomeModuleEntity::getDestinationId, deletedDestinationId)
                .set(HomeModuleEntity::getDestinationId, null);
        iHomeModuleService.update(new HomeModuleEntity(), updateWrapper);
    }

    private void clearNotices(Long deletedDestinationId) {
        LambdaUpdateWrapper<NoticeEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(NoticeEntity::getDestinationId, deletedDestinationId)
                .set(NoticeEntity::getDestinationId, null);
        iNoticeService.update(new NoticeEntity(), updateWrapper);
    }

    private void clearCanteenReservations(Long deletedDestinationId) {
        LambdaUpdateWrapper<CanteenReservationEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CanteenReservationEntity::getDestinationId, deletedDestinationId)
                .set(CanteenReservationEntity::getDestinationId, null);
        iCanteenReservationService.update(new CanteenReservationEntity(), updateWrapper);
    }

    private void rebindMapPois(Long deletedDestinationId) {
        List<MapPoiEntity> pois = listByDestination(iMapPoiService, MapPoiEntity::getDestinationId, deletedDestinationId);
        if (CollectionUtils.isEmpty(pois)) {
            return;
        }
        for (MapPoiEntity poi : pois) {
            poi.setDestinationId(rematchByCoordinates(poi.getLatitude(), poi.getLongitude()));
            iMapPoiService.updateById(poi);
        }
    }

    private Long rematchByCoordinates(BigDecimal latitude, BigDecimal longitude) {
        return destinationSupport.tryResolvePublishDestination(latitude, longitude);
    }

    private <T> List<T> listByDestination(com.baomidou.mybatisplus.extension.service.IService<T> service,
                                          com.baomidou.mybatisplus.core.toolkit.support.SFunction<T, ?> destinationGetter,
                                          Long destinationId) {
        LambdaQueryWrapper<T> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(destinationGetter, destinationId);
        return service.list(queryWrapper);
    }
}

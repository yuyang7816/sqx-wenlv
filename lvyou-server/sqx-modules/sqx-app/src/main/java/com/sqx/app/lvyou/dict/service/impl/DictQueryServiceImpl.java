package com.sqx.app.lvyou.dict.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.lvyou.canteen.support.CanteenReservationStatusSupport;
import com.sqx.app.lvyou.coupon.enums.CouponScope;
import com.sqx.app.lvyou.dict.constants.LvyouDictTypes;
import com.sqx.app.lvyou.dict.domain.vo.DictItemVo;
import com.sqx.app.lvyou.dict.service.DictQueryService;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStoreTypeVo;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.merchant.service.MerchantStoreTypeConfigService;
import com.sqx.app.lvyou.order.support.AppOrderStatusSupport;
import com.sqx.system.db.entity.SysDictEntity;
import com.sqx.system.db.service.ISysDictService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 业务字典查询实现：优先 sys_dict，空则回退内置枚举
 */
@Service
public class DictQueryServiceImpl implements DictQueryService {

    @Resource
    private ISysDictService iSysDictService;

    @Resource
    private MerchantStoreTypeConfigService merchantStoreTypeConfigService;

    @Override
    public List<DictItemVo> listByType(String type) {
        if (StringUtils.isBlank(type)) {
            return Collections.emptyList();
        }
        String normalizedType = type.trim();

        LambdaQueryWrapper<SysDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictEntity::getType, normalizedType)
                .orderByAsc(SysDictEntity::getOrderNum)
                .orderByAsc(SysDictEntity::getId);
        List<SysDictEntity> entities = iSysDictService.list(queryWrapper);
        if (CollectionUtils.isNotEmpty(entities)) {
            List<DictItemVo> items = new ArrayList<>();
            for (SysDictEntity entity : entities) {
                items.add(toVo(entity, normalizedType));
            }
            return items;
        }
        if (LvyouDictTypes.MERCHANT_TYPE.equals(normalizedType)) {
            return merchantTypeFromConfig();
        }
        return fallbackByType(normalizedType);
    }

    @Override
    public String labelOf(String type, String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        List<DictItemVo> items = listByType(type);
        for (DictItemVo item : items) {
            if (code.equalsIgnoreCase(item.getCode())) {
                return item.getLabel();
            }
        }
        return code;
    }

    private DictItemVo toVo(SysDictEntity entity, String dictType) {
        DictItemVo vo = new DictItemVo();
        vo.setCode(entity.getCode());
        if (LvyouDictTypes.MERCHANT_TYPE.equals(dictType)) {
            vo.setLabel(merchantStoreTypeConfigService.labelOf(entity.getCode()));
            vo.setDepositAmount(merchantStoreTypeConfigService.parseDepositAmount(entity.getValue(), entity.getCode()));
        } else {
            vo.setLabel(StringUtils.defaultIfBlank(entity.getName(), entity.getCode()));
        }
        vo.setValue(entity.getCode());
        vo.setSort(entity.getOrderNum());
        return vo;
    }

    private List<DictItemVo> merchantTypeFromConfig() {
        List<MerchantStoreTypeVo> types = merchantStoreTypeConfigService.listActive();
        if (CollectionUtils.isEmpty(types)) {
            return merchantTypeFallback();
        }
        List<DictItemVo> items = new ArrayList<>();
        for (MerchantStoreTypeVo type : types) {
            DictItemVo vo = new DictItemVo();
            vo.setCode(type.getCode());
            vo.setLabel(type.getLabel());
            vo.setValue(type.getCode());
            vo.setSort(type.getSortOrder());
            vo.setDepositAmount(type.getDepositAmount());
            items.add(vo);
        }
        return items;
    }

    private List<DictItemVo> fallbackByType(String type) {
        switch (type) {
            case LvyouDictTypes.MERCHANT_TYPE:
                return merchantTypeFallback();
            case LvyouDictTypes.ORDER_STATUS:
                return orderStatusFallback();
            case LvyouDictTypes.ORDER_FILTER_HOTEL:
                return hotelFilterFallback();
            case LvyouDictTypes.ORDER_FILTER_GOODS:
                return goodsFilterFallback();
            case LvyouDictTypes.ORDER_FILTER_ROUTE:
                return filterFallback("待出行");
            case LvyouDictTypes.RESERVATION_FILTER:
                return reservationFilterFallback();
            case LvyouDictTypes.BANNER_CLASSIFY:
                return bannerClassifyFallback();
            case LvyouDictTypes.COUPON_SCOPE:
                return couponScopeFallback();
            default:
                return Collections.emptyList();
        }
    }

    private List<DictItemVo> merchantTypeFallback() {
        List<DictItemVo> items = new ArrayList<>();
        int sort = 0;
        for (MerchantStoreType storeType : MerchantStoreType.values()) {
            items.add(item(storeType.getCode(), storeType.getLabel(), sort++));
        }
        return items;
    }


    private List<DictItemVo> orderStatusFallback() {
        List<DictItemVo> items = new ArrayList<>();
        items.add(item(AppOrderStatusSupport.PENDING_PAY, "待支付", 0));
        items.add(item(AppOrderStatusSupport.PENDING_CONFIRM, "待确认", 1));
        items.add(item(AppOrderStatusSupport.PENDING_USE, "待使用/待发货", 2));
        items.add(item(AppOrderStatusSupport.PENDING_RECEIVE, "待收货", 3));
        items.add(item(AppOrderStatusSupport.COMPLETED, "已完成", 4));
        items.add(item(AppOrderStatusSupport.CANCELLED, "已取消", 5));
        items.add(item(AppOrderStatusSupport.REFUNDING, "退款中", 6));
        return items;
    }

    private List<DictItemVo> filterFallback(String pendingLabel) {
        List<DictItemVo> items = new ArrayList<>();
        items.add(item("all", "全部", 0));
        items.add(item(AppOrderStatusSupport.PENDING_USE, pendingLabel, 1));
        items.add(item(AppOrderStatusSupport.COMPLETED, "已完成", 2));
        items.add(item(AppOrderStatusSupport.CANCELLED, "已取消", 3));
        return items;
    }

    private List<DictItemVo> hotelFilterFallback() {
        List<DictItemVo> items = new ArrayList<>();
        items.add(item("all", "全部", 0));
        items.add(item(AppOrderStatusSupport.PENDING_USE, "待入住", 1));
        items.add(item(AppOrderStatusSupport.COMPLETED, "已完成", 2));
        items.add(item(AppOrderStatusSupport.CANCELLED, "已取消", 3));
        items.add(item(AppOrderStatusSupport.REFUNDED, "已退款", 4));
        return items;
    }

    private List<DictItemVo> goodsFilterFallback() {
        List<DictItemVo> items = new ArrayList<>();
        items.add(item("all", "全部", 0));
        items.add(item(AppOrderStatusSupport.PENDING_USE, "待发货", 1));
        items.add(item(AppOrderStatusSupport.PENDING_RECEIVE, "待收货", 2));
        items.add(item(AppOrderStatusSupport.COMPLETED, "已完成", 3));
        return items;
    }

    private List<DictItemVo> reservationFilterFallback() {
        List<DictItemVo> items = new ArrayList<>();
        items.add(item("all", "全部", 0));
        items.add(item(CanteenReservationStatusSupport.CONFIRMED, "待到店", 1));
        items.add(item(CanteenReservationStatusSupport.COMPLETED, "已完成", 2));
        items.add(item(CanteenReservationStatusSupport.CANCELLED, "已取消", 3));
        return items;
    }

    private List<DictItemVo> bannerClassifyFallback() {
        List<DictItemVo> items = new ArrayList<>();
        items.add(item("1", "Banner图", 0));
        items.add(item("2", "金刚区", 1));
        return items;
    }

    private List<DictItemVo> couponScopeFallback() {
        List<DictItemVo> items = new ArrayList<>();
        int sort = 0;
        for (CouponScope scope : CouponScope.values()) {
            items.add(item(scope.getCode(), scope.getLabel(), sort++));
        }
        return items;
    }

    private DictItemVo item(String code, String label, int sort) {
        DictItemVo vo = new DictItemVo();
        vo.setCode(code);
        vo.setLabel(label);
        vo.setValue(code);
        vo.setSort(sort);
        return vo;
    }
}

package com.sqx.app.lvyou.canteen.support;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.lvyou.canteen.db.service.ICanteenReservationService;
import com.sqx.app.lvyou.canteen.domain.entity.CanteenReservationEntity;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 餐饮商户统计：已预约数
 */
@Component
public class CanteenMerchantStatsSupport {

    @Resource
    private ICanteenReservationService iCanteenReservationService;

    public Map<Long, Integer> countReservations(Collection<Long> merchantIds) {
        if (CollectionUtils.isEmpty(merchantIds)) {
            return Collections.emptyMap();
        }
        QueryWrapper<CanteenReservationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("merchant_id AS merchantId", "COUNT(*) AS total")
                .in("merchant_id", merchantIds)
                .isNotNull("merchant_id")
                .ne("status", CanteenReservationStatusSupport.CANCELLED)
                .groupBy("merchant_id");
        List<Map<String, Object>> rows = iCanteenReservationService.listMaps(queryWrapper);
        Map<Long, Integer> result = new HashMap<>();
        if (CollectionUtils.isEmpty(rows)) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            Long merchantId = toLong(row.get("merchantId"));
            if (merchantId == null) {
                continue;
            }
            result.put(merchantId, toInt(row.get("total")));
        }
        return result;
    }

    private static Long toLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private static int toInt(Object value) {
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }
}

package com.sqx.app.lvyou.stats.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sqx.app.base.user.cancel.db.service.ICancelAccountService;
import com.sqx.app.base.user.cancel.domain.entity.CancelAccountEntity;
import com.sqx.app.base.user.cancel.enums.CancelAccountStatus;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.canteen.db.service.ICanteenReservationService;
import com.sqx.app.lvyou.canteen.domain.entity.CanteenReservationEntity;
import com.sqx.app.lvyou.canteen.support.CanteenReservationStatusSupport;
import com.sqx.app.lvyou.activity.db.service.IActivityService;
import com.sqx.app.lvyou.activity.domain.entity.ActivityEntity;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.merchant.db.service.IMerchantApplyService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantApplyEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantApplyAuditStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.support.PlatformOrderStatsScope;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import com.sqx.app.lvyou.stats.domain.bo.StatsTrendBo;
import com.sqx.app.lvyou.stats.domain.vo.MenuBadgeVo;
import com.sqx.app.lvyou.stats.domain.vo.StatsBreakdownItemVo;
import com.sqx.app.lvyou.stats.domain.vo.StatsOverviewVo;
import com.sqx.app.lvyou.stats.domain.vo.StatsTrendVo;
import com.sqx.app.lvyou.wallet.db.service.IWithdrawRecordService;
import com.sqx.app.lvyou.wallet.domain.entity.WithdrawRecordEntity;
import com.sqx.app.lvyou.wallet.enums.WithdrawStatus;
import com.sqx.app.lvyou.stats.service.StatsService;
import com.sqx.app.lvyou.stats.support.StatsTrendSupport;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端业务统计实现
 */
@Service
public class StatsServiceImpl implements StatsService {

    private static final Map<String, String> CATEGORY_LABELS = new LinkedHashMap<>();

    private static final Map<Integer, String> STATUS_LABELS = new LinkedHashMap<>();

    static {
        CATEGORY_LABELS.put("hotel", "酒店预订");
        CATEGORY_LABELS.put("route", "旅游线路");
        CATEGORY_LABELS.put("goods", "非遗好物");
        CATEGORY_LABELS.put("service", "商户服务");
        CATEGORY_LABELS.put("culture", "文创商品");
        CATEGORY_LABELS.put("other", "其他");

        STATUS_LABELS.put(MerchantOrderStatusGroup.UNPAID, "待支付");
        STATUS_LABELS.put(MerchantOrderStatusGroup.PENDING_CONFIRM, "待确认");
        STATUS_LABELS.put(MerchantOrderStatusGroup.PENDING_USE, "待使用/待发货");
        STATUS_LABELS.put(MerchantOrderStatusGroup.PENDING_RECEIVE, "待收货");
        STATUS_LABELS.put(MerchantOrderStatusGroup.COMPLETED, "已完成");
        STATUS_LABELS.put(MerchantOrderStatusGroup.CANCELLED, "已取消");
        STATUS_LABELS.put(MerchantOrderStatusGroup.REFUNDING, "退款中");
    }

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMerchantApplyService iMerchantApplyService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private IScenicService iScenicService;

    @Resource
    private IActivityService iActivityService;

    @Resource
    private IWithdrawRecordService iWithdrawRecordService;

    @Resource
    private ICancelAccountService iCancelAccountService;

    @Resource
    private ICanteenReservationService iCanteenReservationService;

    @Override
    public StatsOverviewVo overview() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        StatsOverviewVo vo = new StatsOverviewVo();
        vo.setTodayGmv(sumPaidGmv(todayStart, todayEnd));
        vo.setMonthGmv(sumPaidGmv(monthStart, todayEnd));
        vo.setOrderCount(countPlatformOrders());
        vo.setTodayOrderCount(countOrdersBetween(todayStart, todayEnd));
        vo.setPendingOrderCount(countOrdersByStatuses(Arrays.asList(
                MerchantOrderStatusGroup.UNPAID,
                MerchantOrderStatusGroup.PENDING_CONFIRM,
                MerchantOrderStatusGroup.PENDING_USE,
                MerchantOrderStatusGroup.PENDING_RECEIVE
        )));
        vo.setRefundingOrderCount(countOrdersByStatuses(Collections.singletonList(MerchantOrderStatusGroup.REFUNDING)));
        vo.setPendingMerchantCount(countPendingMerchantApply());
        vo.setNewUserCount(countNewUsers(todayStart, todayEnd));
        vo.setUserCount(iUserService.count());
        vo.setMerchantCount(iMerchantService.count());
        vo.setDestinationOnlineCount(countOnlineDestinations());
        vo.setScenicOnlineCount(countOnlineScenic());
        vo.setActivityOnlineCount(countOnlineActivities());
        vo.setGmv(vo.getMonthGmv());
        vo.setPendingAudit(vo.getPendingMerchantCount());
        return vo;
    }

    @Override
    public MenuBadgeVo menuBadges() {
        MenuBadgeVo vo = new MenuBadgeVo();
        vo.setPendingMerchantCount(countPendingMerchantApply());
        vo.setPendingOrderCount(countOrdersByStatuses(Arrays.asList(
                MerchantOrderStatusGroup.UNPAID,
                MerchantOrderStatusGroup.PENDING_CONFIRM,
                MerchantOrderStatusGroup.PENDING_USE,
                MerchantOrderStatusGroup.PENDING_RECEIVE
        )));
        vo.setRefundingOrderCount(countOrdersByStatuses(Collections.singletonList(MerchantOrderStatusGroup.REFUNDING)));
        vo.setPendingWithdrawCount(countPendingWithdraw());
        vo.setPendingCancelAccountCount(countPendingCancelAccount());
        vo.setPendingCanteenReservationCount(countPendingCanteenReservation());
        return vo;
    }

    @Override
    public StatsTrendVo trend(StatsTrendBo trendBo) {
        int days = trendBo == null || trendBo.getDays() == null ? 30 : trendBo.getDays();
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days - 1L);
        LocalDateTime rangeStart = startDate.atStartOfDay();
        LocalDateTime rangeEnd = endDate.atTime(LocalTime.MAX);

        StatsTrendVo vo = new StatsTrendVo();
        vo.setOrderTrend(StatsTrendSupport.buildOrderTrend(endDate, days, queryDailyOrderCounts(rangeStart, rangeEnd)));
        vo.setGmvTrend(StatsTrendSupport.buildGmvTrend(endDate, days, queryDailyGmvAmounts(rangeStart, rangeEnd)));
        vo.setUserTrend(StatsTrendSupport.buildUserTrend(endDate, days, queryDailyNewUsers(rangeStart, rangeEnd)));
        vo.setCategoryBreakdown(queryCategoryBreakdown());
        vo.setStatusBreakdown(queryStatusBreakdown());
        return vo;
    }

    private BigDecimal sumPaidGmv(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("IFNULL(SUM(pay_amount),0) AS total")
                .in("status", MerchantOrderStatusGroup.PAID)
                .ge("pay_time", start)
                .le("pay_time", end);
        PlatformOrderStatsScope.apply(queryWrapper);
        List<Map<String, Object>> rows = iOrderService.listMaps(queryWrapper);
        return extractDecimal(rows, "total");
    }

    private long countPendingMerchantApply() {
        LambdaQueryWrapper<MerchantApplyEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantApplyEntity::getAuditStatus, MerchantApplyAuditStatus.PENDING.getCode());
        return iMerchantApplyService.count(queryWrapper);
    }

    private long countPendingWithdraw() {
        LambdaQueryWrapper<WithdrawRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WithdrawRecordEntity::getStatus, WithdrawStatus.PENDING);
        return iWithdrawRecordService.count(queryWrapper);
    }

    private long countPendingCancelAccount() {
        LambdaQueryWrapper<CancelAccountEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CancelAccountEntity::getStatus, CancelAccountStatus.PENDING.getCode());
        return iCancelAccountService.count(queryWrapper);
    }

    private long countPendingCanteenReservation() {
        LambdaQueryWrapper<CanteenReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CanteenReservationEntity::getStatus, CanteenReservationStatusSupport.PENDING);
        return iCanteenReservationService.count(queryWrapper);
    }

    private long countNewUsers(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(UserEntity::getCreateTime, start)
                .le(UserEntity::getCreateTime, end);
        return iUserService.count(queryWrapper);
    }

    private long countPlatformOrders() {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        PlatformOrderStatsScope.apply(queryWrapper);
        return iOrderService.count(queryWrapper);
    }

    private long countOrdersBetween(LocalDateTime start, LocalDateTime end) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.ge(OrderEntity::getCreateTime, start)
                .le(OrderEntity::getCreateTime, end);
        PlatformOrderStatsScope.apply(queryWrapper);
        return iOrderService.count(queryWrapper);
    }

    private long countOrdersByStatuses(List<Integer> statuses) {
        if (CollectionUtils.isEmpty(statuses)) {
            return 0L;
        }
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(OrderEntity::getStatus, statuses);
        PlatformOrderStatsScope.apply(queryWrapper);
        return iOrderService.count(queryWrapper);
    }

    private long countOnlineDestinations() {
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getStatus, 1);
        return iDestinationService.count(queryWrapper);
    }

    private long countOnlineScenic() {
        LambdaQueryWrapper<ScenicEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicEntity::getStatus, 1);
        return iScenicService.count(queryWrapper);
    }

    private long countOnlineActivities() {
        LambdaQueryWrapper<ActivityEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ActivityEntity::getStatus, 1);
        return iActivityService.count(queryWrapper);
    }

    private Map<String, Long> queryDailyOrderCounts(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(create_time) AS stat_date", "COUNT(*) AS cnt")
                .ge("create_time", start)
                .le("create_time", end)
                .groupBy("DATE(create_time)");
        PlatformOrderStatsScope.apply(queryWrapper);
        return toCountMap(iOrderService.listMaps(queryWrapper));
    }

    private Map<String, BigDecimal> queryDailyGmvAmounts(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(pay_time) AS stat_date", "IFNULL(SUM(pay_amount),0) AS total")
                .in("status", MerchantOrderStatusGroup.PAID)
                .ge("pay_time", start)
                .le("pay_time", end)
                .groupBy("DATE(pay_time)");
        PlatformOrderStatsScope.apply(queryWrapper);
        return toAmountMap(iOrderService.listMaps(queryWrapper));
    }

    private Map<String, Long> queryDailyNewUsers(LocalDateTime start, LocalDateTime end) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("DATE(create_time) AS stat_date", "COUNT(*) AS cnt")
                .ge("create_time", start)
                .le("create_time", end)
                .groupBy("DATE(create_time)");
        return toCountMap(iUserService.listMaps(queryWrapper));
    }

    private List<StatsBreakdownItemVo> queryCategoryBreakdown() {
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("IFNULL(order_category, 'other') AS category_code",
                        "COUNT(*) AS cnt",
                        "IFNULL(SUM(CASE WHEN status IN (10,20,30,40,60) THEN pay_amount ELSE 0 END),0) AS total")
                .groupBy("IFNULL(order_category, 'other')");
        PlatformOrderStatsScope.apply(queryWrapper);
        List<Map<String, Object>> rows = iOrderService.listMaps(queryWrapper);
        return toCategoryBreakdown(rows);
    }

    private List<StatsBreakdownItemVo> queryStatusBreakdown() {
        QueryWrapper<OrderEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("status AS status_code", "COUNT(*) AS cnt")
                .groupBy("status");
        PlatformOrderStatsScope.apply(queryWrapper);
        List<Map<String, Object>> rows = iOrderService.listMaps(queryWrapper);
        return toStatusBreakdown(rows);
    }

    private List<StatsBreakdownItemVo> toCategoryBreakdown(List<Map<String, Object>> rows) {
        Map<String, StatsBreakdownItemVo> merged = new LinkedHashMap<>();
        for (String code : CATEGORY_LABELS.keySet()) {
            StatsBreakdownItemVo item = new StatsBreakdownItemVo();
            item.setCode(code);
            item.setLabel(CATEGORY_LABELS.get(code));
            item.setCount(0L);
            item.setAmount(BigDecimal.ZERO);
            merged.put(code, item);
        }
        if (CollectionUtils.isNotEmpty(rows)) {
            for (Map<String, Object> row : rows) {
                String rawCode = row.get("category_code") == null ? "other" : String.valueOf(row.get("category_code"));
                String code = CATEGORY_LABELS.containsKey(rawCode) ? rawCode : "other";
                StatsBreakdownItemVo item = merged.get(code);
                item.setCount(item.getCount() + parseLong(row.get("cnt")));
                item.setAmount(item.getAmount().add(extractDecimal(row, "total")));
            }
        }
        List<StatsBreakdownItemVo> result = new ArrayList<>();
        for (StatsBreakdownItemVo item : merged.values()) {
            if (item.getCount() > 0) {
                result.add(item);
            }
        }
        return result;
    }

    private List<StatsBreakdownItemVo> toStatusBreakdown(List<Map<String, Object>> rows) {
        Map<Integer, Long> countMap = new HashMap<>();
        if (CollectionUtils.isNotEmpty(rows)) {
            for (Map<String, Object> row : rows) {
                Integer status = parseInteger(row.get("status_code"));
                if (status == null) {
                    continue;
                }
                countMap.put(status, parseLong(row.get("cnt")));
            }
        }
        List<StatsBreakdownItemVo> result = new ArrayList<>();
        for (Map.Entry<Integer, String> entry : STATUS_LABELS.entrySet()) {
            Long count = countMap.getOrDefault(entry.getKey(), 0L);
            if (count <= 0) {
                continue;
            }
            StatsBreakdownItemVo item = new StatsBreakdownItemVo();
            item.setCode(String.valueOf(entry.getKey()));
            item.setLabel(entry.getValue());
            item.setCount(count);
            result.add(item);
        }
        return result;
    }

    private Map<String, Long> toCountMap(List<Map<String, Object>> rows) {
        Map<String, Long> result = new HashMap<>();
        if (CollectionUtils.isEmpty(rows)) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            String dateKey = StatsTrendSupport.normalizeDateKey(row.get("stat_date"));
            if (dateKey == null) {
                continue;
            }
            result.put(dateKey, parseLong(row.get("cnt")));
        }
        return result;
    }

    private Map<String, BigDecimal> toAmountMap(List<Map<String, Object>> rows) {
        Map<String, BigDecimal> result = new HashMap<>();
        if (CollectionUtils.isEmpty(rows)) {
            return result;
        }
        for (Map<String, Object> row : rows) {
            String dateKey = StatsTrendSupport.normalizeDateKey(row.get("stat_date"));
            if (dateKey == null) {
                continue;
            }
            result.put(dateKey, extractDecimal(row, "total"));
        }
        return result;
    }

    private long parseLong(Object value) {
        if (value == null) {
            return 0L;
        }
        return Long.parseLong(String.valueOf(value));
    }

    private Integer parseInteger(Object value) {
        if (value == null || StringUtils.isBlank(String.valueOf(value))) {
            return null;
        }
        return Integer.parseInt(String.valueOf(value));
    }

    private BigDecimal extractDecimal(List<Map<String, Object>> rows, String key) {
        if (CollectionUtils.isEmpty(rows)) {
            return BigDecimal.ZERO;
        }
        return extractDecimal(rows.get(0), key);
    }

    private BigDecimal extractDecimal(Map<String, Object> row, String key) {
        if (row == null) {
            return BigDecimal.ZERO;
        }
        Object total = row.get(key);
        if (total == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(total));
    }
}

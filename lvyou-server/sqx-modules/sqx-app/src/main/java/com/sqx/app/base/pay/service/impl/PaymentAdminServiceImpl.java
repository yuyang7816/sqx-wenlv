package com.sqx.app.base.pay.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.pay.domain.bo.PaymentAdminPageBo;
import com.sqx.app.base.pay.domain.bo.PaymentConfigUpdateBo;
import com.sqx.app.base.pay.domain.vo.PaymentAdminVo;
import com.sqx.app.base.pay.domain.vo.PaymentConfigAdminVo;
import com.sqx.app.base.pay.enums.BusinessType;
import com.sqx.app.base.pay.service.PaymentAdminService;
import com.sqx.app.base.pay.support.WechatPayConfigMirrorSupport;
import com.sqx.app.base.pay.support.WechatPayConfigMirrorSupport;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.wallet.db.service.IWalletRechargeService;
import com.sqx.app.lvyou.wallet.domain.entity.WalletRechargeEntity;
import com.sqx.app.lvyou.wallet.enums.WalletRechargeStatus;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.enums.PayConfigCache;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;
import com.sqx.system.service.SysConfigService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 管理端支付服务实现
 */
@Service
public class PaymentAdminServiceImpl implements PaymentAdminService {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String PAY_METHOD_WALLET = "wallet";

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IWalletRechargeService iWalletRechargeService;

    @Resource
    private IUserService iUserService;

    @Resource
    private ISysConfigService iSysConfigService;

    @Resource
    private SysConfigService sysConfigService;

    @Override
    public IPage<PaymentAdminVo> page(PaymentAdminPageBo pageBo) {
        String businessType = normalizeBusinessType(pageBo.getBusinessType());
        List<PaymentAdminVo> merged = new ArrayList<>();
        if (businessType == null || BusinessType.TOURISM.getCode().equals(businessType)) {
            merged.addAll(loadOrderPayments(pageBo));
        }
        if (businessType == null || BusinessType.WALLET_RECHARGE.getCode().equals(businessType)) {
            merged.addAll(loadRechargePayments(pageBo));
        }
        merged.sort(Comparator.comparing(PaymentAdminVo::getPayTime, Comparator.nullsLast(String::compareTo)).reversed());

        long pageNum = pageBo.getPage() == null ? 1L : pageBo.getPage();
        long pageSize = pageBo.getLimit() == null ? 10L : pageBo.getLimit();
        int fromIndex = (int) ((pageNum - 1) * pageSize);
        List<PaymentAdminVo> records;
        if (fromIndex >= merged.size()) {
            records = Collections.emptyList();
        } else {
            int toIndex = Math.min(fromIndex + (int) pageSize, merged.size());
            records = merged.subList(fromIndex, toIndex);
        }

        Page<PaymentAdminVo> page = new Page<>(pageNum, pageSize, merged.size());
        page.setRecords(records);
        return page;
    }

    @Override
    public PaymentAdminVo info(String payNo, String businessType) {
        if (StringUtils.isBlank(payNo)) {
            throw new BizException(AppError.PAY_ORDER_NO_REQUIRED);
        }
        String normalizedType = normalizeBusinessType(businessType);
        if (BusinessType.WALLET_RECHARGE.getCode().equals(normalizedType)) {
            WalletRechargeEntity recharge = requireRecharge(payNo.trim());
            return toRechargeVo(recharge, loadUserMapByIds(Collections.singleton(recharge.getUserId())));
        }
        if (BusinessType.TOURISM.getCode().equals(normalizedType)) {
            OrderEntity order = requirePaidOrder(payNo.trim());
            return toOrderVo(order, loadUserMapByIds(Collections.singleton(order.getUserId())));
        }

        WalletRechargeEntity recharge = findRechargeByNo(payNo.trim());
        if (recharge != null && Objects.equals(recharge.getStatus(), WalletRechargeStatus.PAID)) {
            return toRechargeVo(recharge, loadUserMapByIds(Collections.singleton(recharge.getUserId())));
        }
        OrderEntity order = findPaidOrderByNo(payNo.trim());
        if (order != null) {
            return toOrderVo(order, loadUserMapByIds(Collections.singleton(order.getUserId())));
        }
        throw new BizException(AppError.PAY_RECORD_NOT_FOUND);
    }

    @Override
    public List<PaymentConfigAdminVo> listConfig() {
        List<PaymentConfigAdminVo> result = new ArrayList<>();
        for (PayConfigCache configCache : PayConfigCache.values()) {
            PaymentConfigAdminVo vo = new PaymentConfigAdminVo();
            vo.setParamKey(configCache.getCode());
            vo.setDescription(configCache.getDesc());
            SysConfigEntity entity = findConfigByKey(configCache.getCode());
            if (entity != null) {
                vo.setId(entity.getId());
                vo.setParamValue(entity.getParamValue());
                vo.setRemark(entity.getRemark());
                vo.setConfigured(WechatPayConfigMirrorSupport.isConfigured(
                        configCache.getCode(), entity.getParamValue()));
            } else {
                vo.setConfigured(WechatPayConfigMirrorSupport.isConfigured(configCache.getCode(), null));
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public void updateConfig(PaymentConfigUpdateBo updateBo) {
        if (!isPayConfigKey(updateBo.getParamKey())) {
            throw new BizException(AppError.PAY_CONFIG_KEY_INVALID);
        }
        SysConfigEntity entity = iSysConfigService.getById(updateBo.getId());
        if (entity == null || !Objects.equals(entity.getParamKey(), updateBo.getParamKey())) {
            throw new BizException(AppError.CONFIG_NOT_FOUND);
        }
        String paramValue = StringUtils.defaultString(updateBo.getParamValue());
        entity.setParamValue(paramValue);
        sysConfigService.update(entity);
        WechatPayConfigMirrorSupport.syncMirrorValue(iSysConfigService, updateBo.getParamKey(), paramValue);
    }

    private List<PaymentAdminVo> loadOrderPayments(PaymentAdminPageBo pageBo) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(OrderEntity::getPayTime)
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PAID);
        if (Objects.nonNull(pageBo.getId())) {
            queryWrapper.eq(OrderEntity::getId, pageBo.getId());
        }
        if (StringUtils.isNotBlank(pageBo.getPayNo())) {
            queryWrapper.like(OrderEntity::getOrderNo, pageBo.getPayNo().trim());
        }
        if (StringUtils.isNotBlank(pageBo.getPayMethod())) {
            queryWrapper.eq(OrderEntity::getPayMethod, normalizePayMethod(pageBo.getPayMethod()));
        }
        List<Long> userIds = resolveUserIdsByPhone(pageBo.getUserPhone());
        if (userIds != null) {
            if (userIds.isEmpty()) {
                return Collections.emptyList();
            }
            queryWrapper.in(OrderEntity::getUserId, userIds);
        }
        applyPayTimeRange(queryWrapper, pageBo.getPayTimeStart(), pageBo.getPayTimeEnd());
        queryWrapper.orderByDesc(OrderEntity::getPayTime).orderByDesc(OrderEntity::getId);

        List<OrderEntity> orders = iOrderService.list(queryWrapper);
        if (CollectionUtils.isEmpty(orders)) {
            return Collections.emptyList();
        }
        Map<Long, UserEntity> userMap = loadUserMapByIds(orders.stream().map(OrderEntity::getUserId).collect(Collectors.toSet()));
        List<PaymentAdminVo> result = new ArrayList<>();
        for (OrderEntity order : orders) {
            result.add(toOrderVo(order, userMap));
        }
        return result;
    }

    private List<PaymentAdminVo> loadRechargePayments(PaymentAdminPageBo pageBo) {
        LambdaQueryWrapper<WalletRechargeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WalletRechargeEntity::getStatus, WalletRechargeStatus.PAID);
        if (Objects.nonNull(pageBo.getId())) {
            queryWrapper.eq(WalletRechargeEntity::getId, pageBo.getId());
        }
        if (StringUtils.isNotBlank(pageBo.getPayNo())) {
            queryWrapper.like(WalletRechargeEntity::getRechargeNo, pageBo.getPayNo().trim());
        }
        if (StringUtils.isNotBlank(pageBo.getPayMethod())) {
            queryWrapper.eq(WalletRechargeEntity::getPayMethod, normalizePayMethod(pageBo.getPayMethod()));
        }
        List<Long> userIds = resolveUserIdsByPhone(pageBo.getUserPhone());
        if (userIds != null) {
            if (userIds.isEmpty()) {
                return Collections.emptyList();
            }
            queryWrapper.in(WalletRechargeEntity::getUserId, userIds);
        }
        applyRechargePayTimeRange(queryWrapper, pageBo.getPayTimeStart(), pageBo.getPayTimeEnd());
        queryWrapper.orderByDesc(WalletRechargeEntity::getPayTime).orderByDesc(WalletRechargeEntity::getId);

        List<WalletRechargeEntity> recharges = iWalletRechargeService.list(queryWrapper);
        if (CollectionUtils.isEmpty(recharges)) {
            return Collections.emptyList();
        }
        Map<Long, UserEntity> userMap = loadUserMapByIds(recharges.stream().map(WalletRechargeEntity::getUserId).collect(Collectors.toSet()));
        List<PaymentAdminVo> result = new ArrayList<>();
        for (WalletRechargeEntity recharge : recharges) {
            result.add(toRechargeVo(recharge, userMap));
        }
        return result;
    }

    private PaymentAdminVo toOrderVo(OrderEntity order, Map<Long, UserEntity> userMap) {
        PaymentAdminVo vo = new PaymentAdminVo();
        vo.setId(order.getId());
        vo.setPayNo(order.getOrderNo());
        vo.setBusinessType(BusinessType.TOURISM.getCode());
        vo.setBusinessTypeLabel(BusinessType.TOURISM.getDesc());
        vo.setPayMethod(order.getPayMethod());
        vo.setPayMethodLabel(payMethodLabel(order.getPayMethod()));
        vo.setAmount(order.getPayAmount());
        vo.setPayTime(formatTime(order.getPayTime()));
        vo.setOrderCategory(order.getOrderCategory());
        vo.setOrderCategoryLabel(orderCategoryLabel(order.getOrderCategory()));
        vo.setMerchantName(order.getMerchantName());
        vo.setOrderId(order.getId());

        UserEntity user = userMap.get(order.getUserId());
        if (user != null) {
            vo.setUserPhone(user.getPhone());
            vo.setUserName(user.getUserName());
        }
        return vo;
    }

    private PaymentAdminVo toRechargeVo(WalletRechargeEntity recharge, Map<Long, UserEntity> userMap) {
        PaymentAdminVo vo = new PaymentAdminVo();
        vo.setId(recharge.getId());
        vo.setPayNo(recharge.getRechargeNo());
        vo.setBusinessType(BusinessType.WALLET_RECHARGE.getCode());
        vo.setBusinessTypeLabel(BusinessType.WALLET_RECHARGE.getDesc());
        vo.setPayMethod(recharge.getPayMethod());
        vo.setPayMethodLabel(payMethodLabel(recharge.getPayMethod()));
        vo.setAmount(recharge.getAmount());
        vo.setTradeNo(recharge.getTradeNo());
        vo.setPayTime(formatTime(recharge.getPayTime()));

        UserEntity user = userMap.get(recharge.getUserId());
        if (user != null) {
            vo.setUserPhone(user.getPhone());
            vo.setUserName(user.getUserName());
        }
        return vo;
    }

    private OrderEntity requirePaidOrder(String orderNo) {
        OrderEntity order = findPaidOrderByNo(orderNo);
        if (order == null) {
            throw new BizException(AppError.PAY_RECORD_NOT_FOUND);
        }
        return order;
    }

    private WalletRechargeEntity requireRecharge(String rechargeNo) {
        WalletRechargeEntity recharge = findRechargeByNo(rechargeNo);
        if (recharge == null || !Objects.equals(recharge.getStatus(), WalletRechargeStatus.PAID)) {
            throw new BizException(AppError.PAY_RECORD_NOT_FOUND);
        }
        return recharge;
    }

    private OrderEntity findPaidOrderByNo(String orderNo) {
        LambdaQueryWrapper<OrderEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(OrderEntity::getOrderNo, orderNo)
                .isNotNull(OrderEntity::getPayTime)
                .in(OrderEntity::getStatus, MerchantOrderStatusGroup.PAID)
                .last("LIMIT 1");
        return iOrderService.getOne(queryWrapper, false);
    }

    private WalletRechargeEntity findRechargeByNo(String rechargeNo) {
        LambdaQueryWrapper<WalletRechargeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WalletRechargeEntity::getRechargeNo, rechargeNo).last("LIMIT 1");
        return iWalletRechargeService.getOne(queryWrapper, false);
    }

    private SysConfigEntity findConfigByKey(String paramKey) {
        LambdaQueryWrapper<SysConfigEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysConfigEntity::getParamKey, paramKey).last("LIMIT 1");
        return iSysConfigService.getOne(queryWrapper, false);
    }

    private boolean isPayConfigKey(String paramKey) {
        for (PayConfigCache configCache : PayConfigCache.values()) {
            if (Objects.equals(configCache.getCode(), paramKey)) {
                return true;
            }
        }
        return false;
    }

    private List<Long> resolveUserIdsByPhone(String userPhone) {
        if (StringUtils.isBlank(userPhone)) {
            return null;
        }
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getPhone, userPhone.trim());
        List<UserEntity> users = iUserService.list(queryWrapper);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptyList();
        }
        return users.stream().map(UserEntity::getUserId).collect(Collectors.toList());
    }

    private Map<Long, UserEntity> loadUserMapByIds(Set<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, UserEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(users)) {
            for (UserEntity user : users) {
                map.put(user.getUserId(), user);
            }
        }
        return map;
    }

    private void applyPayTimeRange(LambdaQueryWrapper<OrderEntity> queryWrapper, String start, String end) {
        LocalDateTime startTime = parseDateTime(start);
        LocalDateTime endTime = parseDateTime(end);
        if (startTime != null) {
            queryWrapper.ge(OrderEntity::getPayTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(OrderEntity::getPayTime, endTime);
        }
    }

    private void applyRechargePayTimeRange(LambdaQueryWrapper<WalletRechargeEntity> queryWrapper, String start, String end) {
        LocalDateTime startTime = parseDateTime(start);
        LocalDateTime endTime = parseDateTime(end);
        if (startTime != null) {
            queryWrapper.ge(WalletRechargeEntity::getPayTime, startTime);
        }
        if (endTime != null) {
            queryWrapper.le(WalletRechargeEntity::getPayTime, endTime);
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            return LocalDateTime.parse(value.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    private String normalizeBusinessType(String businessType) {
        if (StringUtils.isBlank(businessType)) {
            return null;
        }
        String normalized = StringUtils.lowerCase(businessType.trim());
        if (BusinessType.TOURISM.getCode().equals(normalized) || BusinessType.WALLET_RECHARGE.getCode().equals(normalized)) {
            return normalized;
        }
        return null;
    }

    private String normalizePayMethod(String payMethod) {
        if (StringUtils.isBlank(payMethod)) {
            return null;
        }
        String normalized = StringUtils.lowerCase(payMethod.trim());
        if (Objects.equals(normalized, "wechat")) {
            return PayChannel.WX.getValue();
        }
        if (Objects.equals(normalized, "alipay")) {
            return PayChannel.ALI.getValue();
        }
        return normalized;
    }

    private String payMethodLabel(String payMethod) {
        if (Objects.equals(payMethod, PayChannel.WX.getValue())) {
            return "微信支付";
        }
        if (Objects.equals(payMethod, PayChannel.ALI.getValue())) {
            return "支付宝";
        }
        if (Objects.equals(payMethod, PAY_METHOD_WALLET)) {
            return "余额支付";
        }
        return payMethod;
    }

    private String orderCategoryLabel(String category) {
        if (Objects.equals(category, "hotel")) {
            return "酒店";
        }
        if (Objects.equals(category, "route")) {
            return "线路";
        }
        if (Objects.equals(category, "goods")) {
            return "商品";
        }
        if (Objects.equals(category, "service")) {
            return "服务";
        }
        return category;
    }

    private String formatTime(LocalDateTime time) {
        return time == null ? "" : time.format(TIME_FORMATTER);
    }
}

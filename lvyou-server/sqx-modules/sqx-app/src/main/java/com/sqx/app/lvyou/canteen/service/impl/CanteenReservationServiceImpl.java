package com.sqx.app.lvyou.canteen.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.messages.support.InstationMessageSupport;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.canteen.db.service.ICanteenReservationService;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationAdminActionBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationAdminPageBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationListBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationSubmitBo;
import com.sqx.app.lvyou.canteen.domain.bo.CanteenReservationUpdateBo;
import com.sqx.app.lvyou.canteen.domain.entity.CanteenReservationEntity;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationAdminVo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationPendingCountVo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationSubmitVo;
import com.sqx.app.lvyou.canteen.domain.vo.CanteenReservationVo;
import com.sqx.app.lvyou.canteen.service.CanteenReservationService;
import com.sqx.app.lvyou.canteen.support.CanteenReservationStatusSupport;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.destination.support.DestinationDisplaySupport;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
import com.sqx.app.lvyou.goose.support.GooseStoreTypeSupport;
import com.sqx.app.lvyou.goose.util.GooseDecorUtil;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantStoreDecorService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantStoreDecorEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * 餐饮预约业务服务实现
 */
@Service
public class CanteenReservationServiceImpl implements CanteenReservationService {

    private static final int MERCHANT_STATUS_NORMAL = 1;
    private static final DateTimeFormatter RESERVE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DISPLAY_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private IUserService iUserService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private DestinationSupport destinationSupport;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IMerchantStoreDecorService iMerchantStoreDecorService;

    @Resource
    private ICanteenReservationService iCanteenReservationService;

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IOrderItemService iOrderItemService;

    @Resource
    private InstationMessageSupport instationMessageSupport;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CanteenReservationSubmitVo submit(Long userId, CanteenReservationSubmitBo submitBo) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        LocalDateTime reserveTime = parseReserveTime(submitBo.getReserveTime());
        MerchantEntity merchant = resolveMerchant(submitBo);
        DestinationEntity destination = resolveDestination(user, submitBo, merchant);

        String reservationNo = generateReservationNo();
        CanteenReservationEntity entity = new CanteenReservationEntity();
        entity.setReservationNo(reservationNo);
        entity.setUserId(userId);
        entity.setDestinationId(destination.getId());
        entity.setShopName(StringUtils.trimToNull(submitBo.getShopName()));
        entity.setDishName(StringUtils.trimToNull(submitBo.getDishName()));
        entity.setContactName(StringUtils.trim(submitBo.getContactName()));
        entity.setContactPhone(StringUtils.trim(submitBo.getPhone()));
        entity.setGuestCount(submitBo.getGuestCount() == null ? 1 : submitBo.getGuestCount());
        entity.setReserveTime(reserveTime);
        entity.setAddress(StringUtils.trim(submitBo.getAddress()));
        entity.setRemark(StringUtils.trimToNull(submitBo.getRemark()));
        entity.setStatus(CanteenReservationStatusSupport.PENDING);

        entity.setMerchantId(merchant.getId());
        OrderEntity order = createLinkedOrder(userId, merchant, destination, submitBo, reserveTime, reservationNo);
        entity.setOrderId(order.getId());

        iCanteenReservationService.save(entity);

        instationMessageSupport.sendUserMessage(userId, "餐饮预约提交成功",
                "您的餐饮预约 " + reservationNo + " 已提交，等待确认", entity.getId());
        notifyMerchantReservationEvent(merchant, entity, "new");

        CanteenReservationSubmitVo vo = new CanteenReservationSubmitVo();
        vo.setId(entity.getId());
        vo.setReservationNo(reservationNo);
        vo.setStatus(entity.getStatus());
        vo.setStatusLabel(CanteenReservationStatusSupport.labelOf(entity.getStatus()));
        vo.setShopName(StringUtils.defaultIfBlank(entity.getShopName(), merchant.getStoreName()));
        return vo;
    }

    @Override
    public IPage<CanteenReservationVo> page(Long userId, CanteenReservationListBo listBo) {
        LambdaQueryWrapper<CanteenReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CanteenReservationEntity::getUserId, userId);
        if (StringUtils.isNotBlank(listBo.getStatus())) {
            queryWrapper.eq(CanteenReservationEntity::getStatus,
                    CanteenReservationStatusSupport.normalize(listBo.getStatus()));
        }
        queryWrapper.orderByDesc(CanteenReservationEntity::getCreateTime)
                .orderByDesc(CanteenReservationEntity::getId);

        Page<CanteenReservationEntity> page = PageUtil.initPage(listBo.getPage(), listBo.getLimit());
        IPage<CanteenReservationEntity> entityPage = iCanteenReservationService.page(page, queryWrapper);
        return PageUtil.copyPage(entityPage, toVoList(entityPage.getRecords()));
    }

    @Override
    public CanteenReservationVo detail(Long userId, String id) {
        CanteenReservationEntity entity = requireOwnedReservation(userId, id);
        List<CanteenReservationVo> voList = toVoList(Collections.singletonList(entity));
        return voList.get(0);
    }

    @Override
    public CanteenReservationPendingCountVo pendingCount(Long userId) {
        LambdaQueryWrapper<CanteenReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CanteenReservationEntity::getUserId, userId)
                .eq(CanteenReservationEntity::getStatus, CanteenReservationStatusSupport.PENDING);
        CanteenReservationPendingCountVo vo = new CanteenReservationPendingCountVo();
        vo.setCount(iCanteenReservationService.count(queryWrapper));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CanteenReservationVo cancel(Long userId, String id) {
        CanteenReservationEntity entity = requireOwnedReservation(userId, id);
        assertUserModifiable(entity);
        entity.setStatus(CanteenReservationStatusSupport.CANCELLED);
        iCanteenReservationService.updateById(entity);
        syncLinkedOrderFromAdminAction(entity.getOrderId(), "cancel");
        instationMessageSupport.sendUserMessage(entity.getUserId(), "餐饮预约已取消",
                "您的餐饮预约 " + entity.getReservationNo() + " 已取消", entity.getId());
        MerchantEntity merchant = entity.getMerchantId() == null ? null : iMerchantService.getById(entity.getMerchantId());
        notifyMerchantReservationEvent(merchant, entity, "cancel");
        return detail(userId, String.valueOf(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CanteenReservationVo update(Long userId, String id, CanteenReservationUpdateBo updateBo) {
        CanteenReservationEntity entity = requireOwnedReservation(userId, id);
        assertUserModifiable(entity);
        LocalDateTime reserveTime = parseReserveTime(updateBo.getReserveTime());
        entity.setContactName(StringUtils.trim(updateBo.getContactName()));
        entity.setContactPhone(StringUtils.trim(updateBo.getPhone()));
        entity.setGuestCount(updateBo.getGuestCount() == null ? 1 : updateBo.getGuestCount());
        entity.setReserveTime(reserveTime);
        entity.setAddress(StringUtils.trim(updateBo.getAddress()));
        entity.setRemark(StringUtils.trimToNull(updateBo.getRemark()));
        if (StringUtils.isNotBlank(updateBo.getShopName())) {
            entity.setShopName(StringUtils.trim(updateBo.getShopName()));
        }
        if (updateBo.getDishName() != null) {
            entity.setDishName(StringUtils.trimToNull(updateBo.getDishName()));
        }
        iCanteenReservationService.updateById(entity);
        syncLinkedOrderOnUserUpdate(entity, updateBo, reserveTime);
        instationMessageSupport.sendUserMessage(entity.getUserId(), "餐饮预约已修改",
                "您的餐饮预约 " + entity.getReservationNo() + " 信息已更新", entity.getId());
        return detail(userId, String.valueOf(entity.getId()));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void syncStatusByOrderId(Long orderId, String action) {
        if (orderId == null || StringUtils.isBlank(action)) {
            return;
        }
        CanteenReservationEntity entity = iCanteenReservationService.getByOrderId(orderId);
        if (entity == null) {
            return;
        }
        String normalized = action.trim().toLowerCase();
        if ("confirm".equals(normalized) || "confirmtour".equals(normalized)
                || "confirmreservation".equals(normalized) || "confirmcheckin".equals(normalized)
                || "confirmorder".equals(normalized)) {
            if (CanteenReservationStatusSupport.isPending(entity.getStatus())) {
                entity.setStatus(CanteenReservationStatusSupport.CONFIRMED);
                iCanteenReservationService.updateById(entity);
                instationMessageSupport.sendUserMessage(entity.getUserId(), "餐饮预约已确认",
                        "您的餐饮预约 " + entity.getReservationNo() + " 已确认", entity.getId());
            }
            return;
        }
        if ("complete".equals(normalized) || "confirmcomplete".equals(normalized)) {
            if (!CanteenReservationStatusSupport.CANCELLED.equals(entity.getStatus())
                    && !CanteenReservationStatusSupport.COMPLETED.equals(entity.getStatus())) {
                entity.setStatus(CanteenReservationStatusSupport.COMPLETED);
                iCanteenReservationService.updateById(entity);
                instationMessageSupport.sendUserMessage(entity.getUserId(), "餐饮预约已完成",
                        "您的餐饮预约 " + entity.getReservationNo() + " 已完成", entity.getId());
            }
            return;
        }
        if ("cancel".equals(normalized) || "reject".equals(normalized)) {
            if (!CanteenReservationStatusSupport.COMPLETED.equals(entity.getStatus())) {
                entity.setStatus(CanteenReservationStatusSupport.CANCELLED);
                iCanteenReservationService.updateById(entity);
                instationMessageSupport.sendUserMessage(entity.getUserId(), "餐饮预约已取消",
                        "您的餐饮预约 " + entity.getReservationNo() + " 已取消", entity.getId());
            }
        }
    }

    private OrderEntity createLinkedOrder(Long userId, MerchantEntity merchant, DestinationEntity destination,
                                          CanteenReservationSubmitBo submitBo, LocalDateTime reserveTime,
                                          String reservationNo) {
        String orderNo = generateOrderNo();
        LocalDate reserveDate = reserveTime.toLocalDate();
        String timePart = reserveTime.format(DateTimeFormatter.ofPattern("HH:mm"));

        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchant.getId());
        order.setDestinationId(destination.getId());
        com.sqx.app.lvyou.order.support.OrderTypeSupport.apply(order,
                com.sqx.app.lvyou.order.enums.OrderType.RESTAURANT);
        order.setStatus(MerchantOrderStatusGroup.PENDING_CONFIRM);
        order.setStatusText(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.PENDING_CONFIRM));
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPayAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setContactName(StringUtils.trim(submitBo.getContactName()));
        order.setContactPhone(StringUtils.trim(submitBo.getPhone()));
        order.setGuestCount(submitBo.getGuestCount() == null ? 1 : submitBo.getGuestCount());
        order.setMerchantName(merchant.getStoreName());
        order.setTravelDate(reserveDate);
        order.setRemark(buildOrderRemark(submitBo, reservationNo, timePart));
        iOrderService.save(order);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(order.getId());
        item.setOrderNo(orderNo);
        item.setItemType("restaurant");
        item.setItemName(buildItemName(merchant, submitBo));
        item.setItemSpec(reserveTime.format(DISPLAY_FMT) + " · "
                + (submitBo.getGuestCount() == null ? 1 : submitBo.getGuestCount()) + "人");
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.ZERO);
        item.setTotalPrice(BigDecimal.ZERO);
        iOrderItemService.save(item);
        return order;
    }

    private String buildItemName(MerchantEntity merchant, CanteenReservationSubmitBo submitBo) {
        String shopName = StringUtils.defaultIfBlank(submitBo.getShopName(), merchant.getStoreName());
        if (StringUtils.isNotBlank(submitBo.getDishName())) {
            return shopName + " · " + StringUtils.trim(submitBo.getDishName());
        }
        return shopName + " · 餐饮预约";
    }

    private String buildOrderRemark(CanteenReservationSubmitBo submitBo, String reservationNo, String timePart) {
        StringBuilder builder = new StringBuilder();
        builder.append("餐饮预约单号：").append(reservationNo);
        builder.append("；预约时间：").append(timePart);
        if (StringUtils.isNotBlank(submitBo.getDishName())) {
            builder.append("；预约菜品：").append(StringUtils.trim(submitBo.getDishName()));
        }
        if (StringUtils.isNotBlank(submitBo.getRemark())) {
            builder.append("；").append(StringUtils.trim(submitBo.getRemark()));
        }
        return builder.toString();
    }

    private MerchantEntity resolveMerchant(CanteenReservationSubmitBo submitBo) {
        String merchantKey = StringUtils.trimToNull(submitBo.getMerchantId());
        if (merchantKey == null) {
            merchantKey = StringUtils.trimToNull(submitBo.getShopId());
        }
        MerchantEntity merchant = null;
        if (merchantKey != null) {
            if (StringUtils.isNumeric(merchantKey)) {
                merchant = resolveFoodMerchantById(Long.valueOf(merchantKey));
            }
            if (merchant == null) {
                merchant = findApprovedMerchantByCode(merchantKey);
            }
            if (merchant == null) {
                throw new BizException(AppError.GOOSE_NOT_FOUND);
            }
            return merchant;
        }
        merchant = resolveMerchantByShopName(submitBo.getShopName(), submitBo.getAddress());
        if (merchant == null) {
            throw new BizException(AppError.GOOSE_NOT_FOUND);
        }
        return merchant;
    }

    private MerchantEntity resolveFoodMerchantById(Long merchantId) {
        if (merchantId == null) {
            return null;
        }
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant == null || !Objects.equals(merchant.getStatus(), MERCHANT_STATUS_NORMAL)) {
            return null;
        }
        if (!GooseStoreTypeSupport.isFoodStore(merchant.getStoreType())) {
            return null;
        }
        return merchant;
    }

    private MerchantEntity resolveMerchantByShopName(String shopName, String address) {
        if (StringUtils.isBlank(shopName)) {
            return null;
        }
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getStoreName, shopName.trim())
                .eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .in(MerchantEntity::getStoreType, GooseStoreTypeSupport.foodStoreTypes());
        List<MerchantEntity> merchants = iMerchantService.list(queryWrapper);
        if (CollectionUtils.isEmpty(merchants)) {
            return null;
        }
        if (merchants.size() == 1) {
            return merchants.get(0);
        }
        String normalizedAddress = StringUtils.trimToNull(address);
        if (normalizedAddress != null) {
            for (MerchantEntity merchant : merchants) {
                if (StringUtils.equals(normalizedAddress, StringUtils.trimToNull(merchant.getStoreAddress()))) {
                    return merchant;
                }
            }
        }
        return merchants.get(0);
    }

    private void notifyMerchantReservationEvent(MerchantEntity merchant, CanteenReservationEntity entity, String event) {
        if (merchant == null || merchant.getUserId() == null || entity == null) {
            return;
        }
        String shopLabel = StringUtils.defaultIfBlank(entity.getShopName(), merchant.getStoreName());
        String guestLabel = (entity.getGuestCount() == null ? 1 : entity.getGuestCount()) + "人";
        String timeLabel = entity.getReserveTime() == null ? "" : entity.getReserveTime().format(DISPLAY_FMT);
        String title;
        String content;
        if ("cancel".equals(event)) {
            title = "餐饮预约已取消";
            content = entity.getContactName() + " 取消了 " + shopLabel + " 的预约（" + guestLabel
                    + (StringUtils.isBlank(timeLabel) ? "" : "，" + timeLabel) + "）";
        } else {
            title = "新餐饮预约";
            content = entity.getContactName() + " 预约了 " + shopLabel + "（" + guestLabel
                    + (StringUtils.isBlank(timeLabel) ? "" : "，" + timeLabel) + "），请及时确认";
        }
        instationMessageSupport.sendUserMessage(merchant.getUserId(), title, content, entity.getId());
    }

    private MerchantEntity findApprovedMerchantByCode(String merchantCode) {
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getMerchantCode, StringUtils.trim(merchantCode))
                .eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .in(MerchantEntity::getStoreType, GooseStoreTypeSupport.foodStoreTypes())
                .last("LIMIT 1");
        return iMerchantService.getOne(queryWrapper, false);
    }

    private DestinationEntity resolveDestination(UserEntity user, CanteenReservationSubmitBo submitBo,
                                                 MerchantEntity merchant) {
        if (merchant != null && merchant.getDestinationId() != null) {
            DestinationEntity merchantDestination = iDestinationService.getById(merchant.getDestinationId());
            if (merchantDestination != null) {
                return merchantDestination;
            }
        }
        if (StringUtils.isNotBlank(submitBo.getLat()) && StringUtils.isNotBlank(submitBo.getLng())) {
            Long matchedId = destinationSupport.tryResolveAppDestinationId(
                    submitBo.getLat().trim(), submitBo.getLng().trim());
            if (matchedId != null) {
                DestinationEntity matched = iDestinationService.getById(matchedId);
                if (matched != null) {
                    return matched;
                }
            }
        }
        if (StringUtils.isNotBlank(submitBo.getDestinationId())) {
            Long resolvedId = destinationSupport.resolveDestinationId(submitBo.getDestinationId());
            if (resolvedId != null) {
                DestinationEntity destination = iDestinationService.getById(resolvedId);
                if (destination != null) {
                    return destination;
                }
            }
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        if (user.getCurrentDestinationId() != null) {
            DestinationEntity destination = iDestinationService.getById(user.getCurrentDestinationId());
            if (destination != null) {
                return destination;
            }
        }
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getIsDefault, 1).last("LIMIT 1");
        DestinationEntity destination = iDestinationService.getOne(queryWrapper, false);
        if (destination != null) {
            return destination;
        }
        throw new BizException(AppError.DESTINATION_NOT_FOUND);
    }

    private void assertUserModifiable(CanteenReservationEntity entity) {
        String status = CanteenReservationStatusSupport.normalize(entity.getStatus());
        if (CanteenReservationStatusSupport.CANCELLED.equals(status)
                || CanteenReservationStatusSupport.COMPLETED.equals(status)) {
            throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
        }
    }

    private void syncLinkedOrderOnUserUpdate(CanteenReservationEntity entity,
                                             CanteenReservationUpdateBo updateBo,
                                             LocalDateTime reserveTime) {
        if (entity.getOrderId() == null) {
            return;
        }
        OrderEntity order = iOrderService.getById(entity.getOrderId());
        if (order == null) {
            return;
        }
        String timePart = reserveTime.format(DateTimeFormatter.ofPattern("HH:mm"));
        order.setContactName(StringUtils.trim(updateBo.getContactName()));
        order.setContactPhone(StringUtils.trim(updateBo.getPhone()));
        order.setGuestCount(updateBo.getGuestCount() == null ? 1 : updateBo.getGuestCount());
        order.setTravelDate(reserveTime.toLocalDate());
        order.setRemark(buildOrderRemarkFromUpdate(updateBo, entity.getReservationNo(), timePart));
        iOrderService.updateById(order);

        LambdaQueryWrapper<OrderItemEntity> itemQuery = new LambdaQueryWrapper<>();
        itemQuery.eq(OrderItemEntity::getOrderId, order.getId()).last("LIMIT 1");
        OrderItemEntity item = iOrderItemService.getOne(itemQuery, false);
        if (item == null) {
            return;
        }
        String shopName = StringUtils.defaultIfBlank(updateBo.getShopName(), entity.getShopName());
        item.setItemName(buildItemNameFromUpdate(shopName, updateBo.getDishName()));
        item.setItemSpec(reserveTime.format(DISPLAY_FMT) + " · "
                + (updateBo.getGuestCount() == null ? 1 : updateBo.getGuestCount()) + "人");
        iOrderItemService.updateById(item);
    }

    private String buildItemNameFromUpdate(String shopName, String dishName) {
        String safeShopName = StringUtils.defaultIfBlank(shopName, "餐饮预约");
        if (StringUtils.isNotBlank(dishName)) {
            return safeShopName + " · " + StringUtils.trim(dishName);
        }
        return safeShopName + " · 餐饮预约";
    }

    private String buildOrderRemarkFromUpdate(CanteenReservationUpdateBo updateBo, String reservationNo,
                                              String timePart) {
        StringBuilder builder = new StringBuilder();
        builder.append("餐饮预约单号：").append(reservationNo);
        builder.append("；预约时间：").append(timePart);
        if (StringUtils.isNotBlank(updateBo.getDishName())) {
            builder.append("；预约菜品：").append(StringUtils.trim(updateBo.getDishName()));
        }
        if (StringUtils.isNotBlank(updateBo.getRemark())) {
            builder.append("；").append(StringUtils.trim(updateBo.getRemark()));
        }
        return builder.toString();
    }

    private CanteenReservationEntity requireOwnedReservation(Long userId, String id) {
        CanteenReservationEntity entity = findByKey(id);
        if (entity == null || !Objects.equals(entity.getUserId(), userId)) {
            throw new BizException(AppError.CANTEEN_RESERVATION_NOT_FOUND);
        }
        return entity;
    }

    private CanteenReservationEntity findByKey(String key) {
        if (StringUtils.isBlank(key)) {
            return null;
        }
        if (StringUtils.isNumeric(key)) {
            CanteenReservationEntity byId = iCanteenReservationService.getById(Long.valueOf(key));
            if (byId != null) {
                return byId;
            }
        }
        LambdaQueryWrapper<CanteenReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CanteenReservationEntity::getReservationNo, key).last("LIMIT 1");
        return iCanteenReservationService.getOne(queryWrapper, false);
    }

    private List<CanteenReservationVo> toVoList(List<CanteenReservationEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Map<Long, MerchantEntity> merchantById = loadMerchantMap(records);
        Map<String, MerchantEntity> merchantByShopName = loadMerchantMapByShopName(records);
        Set<Long> merchantIds = new HashSet<>(merchantById.keySet());
        merchantByShopName.values().stream()
                .map(MerchantEntity::getId)
                .filter(Objects::nonNull)
                .forEach(merchantIds::add);
        Map<Long, Map<String, Object>> decorMap = loadDecorMapBatch(merchantIds);
        List<CanteenReservationVo> voList = new ArrayList<>();
        for (CanteenReservationEntity record : records) {
            MerchantEntity merchant = resolveMerchantForRecord(record, merchantById, merchantByShopName);
            Map<String, Object> decor = merchant == null ? null : decorMap.get(merchant.getId());
            voList.add(toVo(record, merchant, decor));
        }
        return voList;
    }

    private MerchantEntity resolveMerchantForRecord(CanteenReservationEntity entity,
                                                    Map<Long, MerchantEntity> merchantById,
                                                    Map<String, MerchantEntity> merchantByShopName) {
        if (entity.getMerchantId() != null) {
            MerchantEntity merchant = merchantById.get(entity.getMerchantId());
            if (merchant != null) {
                return merchant;
            }
        }
        String shopName = StringUtils.trimToNull(entity.getShopName());
        if (shopName == null) {
            return null;
        }
        MerchantEntity merchant = merchantByShopName.get(shopName);
        if (merchant != null) {
            return merchant;
        }
        String address = StringUtils.trimToNull(entity.getAddress());
        if (address == null) {
            return null;
        }
        for (MerchantEntity candidate : merchantByShopName.values()) {
            if (StringUtils.equals(address, StringUtils.trimToNull(candidate.getStoreAddress()))) {
                return candidate;
            }
        }
        return null;
    }

    private Map<String, MerchantEntity> loadMerchantMapByShopName(List<CanteenReservationEntity> records) {
        List<String> shopNames = records.stream()
                .filter(record -> record.getMerchantId() == null)
                .map(CanteenReservationEntity::getShopName)
                .filter(StringUtils::isNotBlank)
                .map(String::trim)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shopNames)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantEntity::getStoreName, shopNames)
                .eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .in(MerchantEntity::getStoreType, GooseStoreTypeSupport.foodStoreTypes());
        List<MerchantEntity> merchants = iMerchantService.list(queryWrapper);
        Map<String, MerchantEntity> merchantMap = new HashMap<>();
        if (CollectionUtils.isEmpty(merchants)) {
            return merchantMap;
        }
        for (MerchantEntity merchant : merchants) {
            merchantMap.putIfAbsent(merchant.getStoreName(), merchant);
        }
        return merchantMap;
    }

    private Map<Long, MerchantEntity> loadMerchantMap(List<CanteenReservationEntity> records) {
        List<Long> merchantIds = records.stream()
                .map(CanteenReservationEntity::getMerchantId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(merchantIds)) {
            return Collections.emptyMap();
        }
        List<MerchantEntity> merchants = iMerchantService.listByIds(merchantIds);
        Map<Long, MerchantEntity> merchantMap = new HashMap<>();
        for (MerchantEntity merchant : merchants) {
            merchantMap.put(merchant.getId(), merchant);
        }
        return merchantMap;
    }

    private CanteenReservationVo toVo(CanteenReservationEntity entity, MerchantEntity merchant,
                                        Map<String, Object> decor) {
        CanteenReservationVo vo = new CanteenReservationVo();
        vo.setId(entity.getId());
        vo.setReservationNo(entity.getReservationNo());
        vo.setShopName(entity.getShopName());
        vo.setDishName(entity.getDishName());
        vo.setContactName(entity.getContactName());
        vo.setPhone(entity.getContactPhone());
        vo.setGuestCount(entity.getGuestCount());
        vo.setReserveTime(entity.getReserveTime() == null ? null : entity.getReserveTime().format(DISPLAY_FMT));
        vo.setAddress(entity.getAddress());
        vo.setRemark(entity.getRemark());
        vo.setStatus(CanteenReservationStatusSupport.normalize(entity.getStatus()));
        vo.setStatusLabel(CanteenReservationStatusSupport.labelOf(entity.getStatus()));
        vo.setCreatedAt(entity.getCreateTime() == null ? null : entity.getCreateTime().format(DISPLAY_FMT));
        if (merchant != null) {
            vo.setMerchantId(merchant.getId());
            vo.setMerchantName(merchant.getStoreName());
            if (StringUtils.isBlank(vo.getShopName())) {
                vo.setShopName(merchant.getStoreName());
            }
            if (StringUtils.isBlank(vo.getAddress())) {
                vo.setAddress(merchant.getStoreAddress());
            }
            Map<String, Object> safeDecor = decor == null ? Collections.emptyMap() : decor;
            String shopCover = GooseDecorUtil.resolveCover(safeDecor);
            if (StringUtils.isNotBlank(shopCover)) {
                vo.setShopCover(shopCover);
            }
        }
        return vo;
    }

    private Map<Long, Map<String, Object>> loadDecorMapBatch(Set<Long> merchantIds) {
        if (CollectionUtils.isEmpty(merchantIds)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<MerchantStoreDecorEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantStoreDecorEntity::getMerchantId, merchantIds);
        List<MerchantStoreDecorEntity> decorEntities = iMerchantStoreDecorService.list(queryWrapper);
        Map<Long, Map<String, Object>> result = new HashMap<>();
        if (CollectionUtils.isNotEmpty(decorEntities)) {
            for (MerchantStoreDecorEntity entity : decorEntities) {
                result.put(entity.getMerchantId(), GooseDecorUtil.parseDecor(entity.getDecorJson()));
            }
        }
        return result;
    }

    private LocalDateTime parseReserveTime(String reserveTime) {
        if (StringUtils.isBlank(reserveTime)) {
            throw new BizException(AppError.CANTEEN_RESERVE_TIME_INVALID);
        }
        try {
            return LocalDateTime.parse(reserveTime.trim(), RESERVE_FMT);
        } catch (DateTimeParseException ex) {
            throw new BizException(AppError.CANTEEN_RESERVE_TIME_INVALID);
        }
    }

    private String generateReservationNo() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "CR" + LocalDateTime.now().format(ORDER_NO_FMT) + suffix;
    }

    private String generateOrderNo() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "RT" + LocalDateTime.now().format(ORDER_NO_FMT) + suffix;
    }

    @Override
    public IPage<CanteenReservationAdminVo> adminPage(CanteenReservationAdminPageBo pageBo) {
        Page<CanteenReservationEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<CanteenReservationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Objects.nonNull(pageBo.getId()), CanteenReservationEntity::getId, pageBo.getId())
                .like(StringUtils.isNotBlank(pageBo.getReservationNo()),
                        CanteenReservationEntity::getReservationNo, pageBo.getReservationNo())
                .like(StringUtils.isNotBlank(pageBo.getShopName()),
                        CanteenReservationEntity::getShopName, pageBo.getShopName())
                .like(StringUtils.isNotBlank(pageBo.getContactPhone()),
                        CanteenReservationEntity::getContactPhone, pageBo.getContactPhone())
                .eq(pageBo.getDestinationId() != null,
                        CanteenReservationEntity::getDestinationId, pageBo.getDestinationId())
                .orderByDesc(CanteenReservationEntity::getCreateTime)
                .orderByDesc(CanteenReservationEntity::getId);

        if (StringUtils.isNotBlank(pageBo.getStatus())) {
            queryWrapper.eq(CanteenReservationEntity::getStatus,
                    CanteenReservationStatusSupport.normalize(pageBo.getStatus()));
        }

        Set<Long> userIdFilter = resolveUserIdFilter(pageBo.getUserPhone());
        if (userIdFilter != null) {
            if (userIdFilter.isEmpty()) {
                return PageUtil.copyPage(page, Collections.emptyList());
            }
            queryWrapper.in(CanteenReservationEntity::getUserId, userIdFilter);
        }

        Set<Long> merchantIdFilter = resolveMerchantIdFilter(pageBo.getMerchantName());
        if (merchantIdFilter != null) {
            if (merchantIdFilter.isEmpty()) {
                return PageUtil.copyPage(page, Collections.emptyList());
            }
            queryWrapper.in(CanteenReservationEntity::getMerchantId, merchantIdFilter);
        }

        IPage<CanteenReservationEntity> entityPage = iCanteenReservationService.page(page, queryWrapper);
        return PageUtil.copyPage(entityPage, toAdminVoList(entityPage.getRecords()));
    }

    @Override
    public CanteenReservationAdminVo adminDetail(Long reservationId) {
        CanteenReservationEntity entity = iCanteenReservationService.getById(reservationId);
        if (entity == null) {
            throw new BizException(AppError.CANTEEN_RESERVATION_NOT_FOUND);
        }
        List<CanteenReservationAdminVo> voList = toAdminVoList(Collections.singletonList(entity));
        return voList.get(0);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CanteenReservationAdminVo adminAction(CanteenReservationAdminActionBo actionBo) {
        CanteenReservationEntity entity = iCanteenReservationService.getById(actionBo.getReservationId());
        if (entity == null) {
            throw new BizException(AppError.CANTEEN_RESERVATION_NOT_FOUND);
        }
        String action = normalizeAdminAction(actionBo.getAction());
        String currentStatus = CanteenReservationStatusSupport.normalize(entity.getStatus());
        switch (action) {
            case "confirm":
                if (!CanteenReservationStatusSupport.isPending(currentStatus)) {
                    throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
                }
                entity.setStatus(CanteenReservationStatusSupport.CONFIRMED);
                break;
            case "cancel":
            case "reject":
                if (CanteenReservationStatusSupport.COMPLETED.equals(currentStatus)) {
                    throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
                }
                entity.setStatus(CanteenReservationStatusSupport.CANCELLED);
                break;
            case "complete":
                if (CanteenReservationStatusSupport.CANCELLED.equals(currentStatus)
                        || CanteenReservationStatusSupport.COMPLETED.equals(currentStatus)) {
                    throw new BizException(AppError.MERCHANT_ORDER_STATUS_INVALID);
                }
                entity.setStatus(CanteenReservationStatusSupport.COMPLETED);
                break;
            default:
                throw new BizException(AppError.MERCHANT_ORDER_ACTION_INVALID);
        }
        if (StringUtils.isNotBlank(actionBo.getReason())) {
            entity.setRemark(appendRemark(entity.getRemark(), "运营操作：" + StringUtils.trim(actionBo.getReason())));
        }
        iCanteenReservationService.updateById(entity);
        syncLinkedOrderFromAdminAction(entity.getOrderId(), action);
        sendAdminActionMessage(entity, action);
        return adminDetail(entity.getId());
    }

    private List<CanteenReservationAdminVo> toAdminVoList(List<CanteenReservationEntity> records) {
        if (CollectionUtils.isEmpty(records)) {
            return Collections.emptyList();
        }
        Map<Long, MerchantEntity> merchantMap = loadMerchantMap(records);
        Map<Long, UserEntity> userMap = loadUserMap(records);
        Map<Long, DestinationEntity> destinationMap = loadDestinationMap(records);
        Map<Long, OrderEntity> orderMap = loadOrderMap(records);

        List<CanteenReservationAdminVo> voList = new ArrayList<>();
        for (CanteenReservationEntity record : records) {
            voList.add(toAdminVo(record,
                    merchantMap.get(record.getMerchantId()),
                    userMap.get(record.getUserId()),
                    destinationMap.get(record.getDestinationId()),
                    orderMap.get(record.getOrderId())));
        }
        return voList;
    }

    private CanteenReservationAdminVo toAdminVo(CanteenReservationEntity entity, MerchantEntity merchant,
                                                UserEntity user, DestinationEntity destination,
                                                OrderEntity order) {
        Map<Long, Map<String, Object>> decorMap = loadDecorMapBatch(
                merchant == null ? Collections.emptySet() : Collections.singleton(merchant.getId()));
        Map<String, Object> decor = merchant == null ? null : decorMap.get(merchant.getId());
        CanteenReservationVo baseVo = toVo(entity, merchant, decor);
        CanteenReservationAdminVo vo = new CanteenReservationAdminVo();
        vo.setId(baseVo.getId());
        vo.setReservationNo(baseVo.getReservationNo());
        vo.setShopName(baseVo.getShopName());
        vo.setDishName(baseVo.getDishName());
        vo.setMerchantName(baseVo.getMerchantName());
        vo.setContactName(baseVo.getContactName());
        vo.setPhone(baseVo.getPhone());
        vo.setGuestCount(baseVo.getGuestCount());
        vo.setReserveTime(baseVo.getReserveTime());
        vo.setAddress(baseVo.getAddress());
        vo.setRemark(baseVo.getRemark());
        vo.setStatus(baseVo.getStatus());
        vo.setStatusLabel(baseVo.getStatusLabel());
        vo.setCreatedAt(baseVo.getCreatedAt());
        vo.setUserId(entity.getUserId());
        vo.setMerchantId(entity.getMerchantId());
        vo.setDestinationId(entity.getDestinationId());
        vo.setOrderId(entity.getOrderId());
        if (user != null) {
            vo.setUserPhone(user.getPhone());
            vo.setUserName(user.getUserName());
        }
        vo.setDestinationName(DestinationDisplaySupport.resolveLabel(destination, entity.getAddress()));
        if (order != null) {
            vo.setOrderNo(order.getOrderNo());
        }
        if (entity.getUpdateTime() != null) {
            vo.setUpdateTime(entity.getUpdateTime().format(DISPLAY_FMT));
        }
        return vo;
    }

    private Map<Long, UserEntity> loadUserMap(List<CanteenReservationEntity> records) {
        List<Long> userIds = records.stream()
                .map(CanteenReservationEntity::getUserId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        List<UserEntity> users = iUserService.listByIds(userIds);
        Map<Long, UserEntity> userMap = new HashMap<>();
        for (UserEntity user : users) {
            userMap.put(user.getUserId(), user);
        }
        return userMap;
    }

    private Map<Long, DestinationEntity> loadDestinationMap(List<CanteenReservationEntity> records) {
        List<Long> destinationIds = records.stream()
                .map(CanteenReservationEntity::getDestinationId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(destinationIds)) {
            return Collections.emptyMap();
        }
        List<DestinationEntity> destinations = iDestinationService.listByIds(destinationIds);
        Map<Long, DestinationEntity> destinationMap = new HashMap<>();
        for (DestinationEntity destination : destinations) {
            destinationMap.put(destination.getId(), destination);
        }
        return destinationMap;
    }

    private Map<Long, OrderEntity> loadOrderMap(List<CanteenReservationEntity> records) {
        List<Long> orderIds = records.stream()
                .map(CanteenReservationEntity::getOrderId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        if (CollectionUtils.isEmpty(orderIds)) {
            return Collections.emptyMap();
        }
        List<OrderEntity> orders = iOrderService.listByIds(orderIds);
        Map<Long, OrderEntity> orderMap = new HashMap<>();
        for (OrderEntity order : orders) {
            orderMap.put(order.getId(), order);
        }
        return orderMap;
    }

    private Set<Long> resolveUserIdFilter(String userPhone) {
        if (StringUtils.isBlank(userPhone)) {
            return null;
        }
        LambdaQueryWrapper<UserEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(UserEntity::getPhone, userPhone.trim())
                .select(UserEntity::getUserId);
        List<UserEntity> users = iUserService.list(queryWrapper);
        if (CollectionUtils.isEmpty(users)) {
            return Collections.emptySet();
        }
        return users.stream()
                .map(UserEntity::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Set<Long> resolveMerchantIdFilter(String merchantName) {
        if (StringUtils.isBlank(merchantName)) {
            return null;
        }
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(MerchantEntity::getStoreName, merchantName.trim())
                .select(MerchantEntity::getId);
        List<MerchantEntity> merchants = iMerchantService.list(queryWrapper);
        if (CollectionUtils.isEmpty(merchants)) {
            return Collections.emptySet();
        }
        return merchants.stream()
                .map(MerchantEntity::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private void syncLinkedOrderFromAdminAction(Long orderId, String action) {
        if (orderId == null) {
            return;
        }
        OrderEntity order = iOrderService.getById(orderId);
        if (order == null || !"restaurant".equals(MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory()))) {
            return;
        }
        int currentStatus = order.getStatus() == null ? MerchantOrderStatusGroup.PENDING_CONFIRM : order.getStatus();
        String category = MerchantOrderStatusSupport.normalizeCategory(order.getOrderCategory());
        switch (action) {
            case "confirm":
                if (currentStatus == MerchantOrderStatusGroup.PENDING_CONFIRM) {
                    order.setStatus(MerchantOrderStatusGroup.PENDING_USE);
                    order.setStatusText(MerchantOrderStatusSupport.statusTextAfterTransition(
                            category, MerchantOrderStatusGroup.PENDING_USE));
                    iOrderService.updateById(order);
                }
                break;
            case "cancel":
            case "reject":
                order.setStatus(MerchantOrderStatusGroup.CANCELLED);
                order.setStatusText(MerchantOrderStatusSupport.statusTextAfterTransition(
                        category, MerchantOrderStatusGroup.CANCELLED));
                iOrderService.updateById(order);
                break;
            case "complete":
                order.setStatus(MerchantOrderStatusGroup.COMPLETED);
                order.setStatusText(MerchantOrderStatusSupport.statusTextAfterTransition(
                        category, MerchantOrderStatusGroup.COMPLETED));
                iOrderService.updateById(order);
                break;
            default:
                break;
        }
    }

    private void sendAdminActionMessage(CanteenReservationEntity entity, String action) {
        String title;
        String content;
        switch (action) {
            case "confirm":
                title = "餐饮预约已确认";
                content = "您的餐饮预约 " + entity.getReservationNo() + " 已由平台确认";
                break;
            case "cancel":
            case "reject":
                title = "餐饮预约已取消";
                content = "您的餐饮预约 " + entity.getReservationNo() + " 已由平台取消";
                break;
            case "complete":
                title = "餐饮预约已完成";
                content = "您的餐饮预约 " + entity.getReservationNo() + " 已完成";
                break;
            default:
                return;
        }
        instationMessageSupport.sendUserMessage(entity.getUserId(), title, content, entity.getId());
    }

    private String normalizeAdminAction(String action) {
        if (StringUtils.isBlank(action)) {
            throw new BizException(AppError.MERCHANT_ORDER_ACTION_INVALID);
        }
        return action.trim().toLowerCase();
    }

    private String appendRemark(String original, String append) {
        if (StringUtils.isBlank(original)) {
            return append;
        }
        return original + "；" + append;
    }
}

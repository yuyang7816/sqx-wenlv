package com.sqx.app.lvyou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderItemService;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.bo.ServiceRecordSubmitBo;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.entity.OrderItemEntity;
import com.sqx.app.lvyou.order.domain.vo.ServiceRecordSubmitVo;
import com.sqx.app.lvyou.order.service.ServiceRecordService;
import com.sqx.app.lvyou.order.enums.OrderType;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.app.lvyou.order.support.OrderTypeSupport;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 服务预约业务服务实现（写入 tb_order，与用户订单共用）
 */
@Service
public class ServiceRecordServiceImpl implements ServiceRecordService {

    private static final int MERCHANT_STATUS_NORMAL = 1;
    private static final DateTimeFormatter ORDER_NO_FMT = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    @Resource
    private IUserService iUserService;

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private IOrderService iOrderService;

    @Resource
    private IOrderItemService iOrderItemService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ServiceRecordSubmitVo submit(Long userId, ServiceRecordSubmitBo submitBo) {
        UserEntity user = iUserService.getById(userId);
        if (user == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        MerchantEntity merchant = requireApprovedMerchant(submitBo.getMerchantId());
        DestinationEntity destination = requireDestination(merchant.getDestinationId());

        String category = MerchantOrderStatusSupport.categoryFromStoreType(merchant.getStoreType());
        String orderNo = generateOrderNo();
        LocalDate serviceDate = parseDate(submitBo.getDate());

        OrderEntity order = new OrderEntity();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setMerchantId(merchant.getId());
        order.setDestinationId(destination.getId());
        OrderType orderType = OrderType.fromCode(category);
        OrderTypeSupport.apply(order, orderType != null ? orderType : OrderType.GOODS);
        order.setStatus(MerchantOrderStatusGroup.PENDING_CONFIRM);
        order.setStatusText(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.PENDING_CONFIRM));
        order.setTotalAmount(BigDecimal.ZERO);
        order.setPayAmount(BigDecimal.ZERO);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setContactName(StringUtils.trim(submitBo.getContactName()));
        order.setContactPhone(StringUtils.trim(submitBo.getPhone()));
        order.setGuestCount(submitBo.getGuestCount() == null ? 1 : submitBo.getGuestCount());
        order.setMerchantName(merchant.getStoreName());
        order.setRemark(buildRemark(submitBo));

        if ("hotel".equals(category) && serviceDate != null) {
            order.setCheckInDate(serviceDate);
            order.setCheckOutDate(serviceDate.plusDays(1));
            order.setNights(1);
        }
        if ("route".equals(category) || "restaurant".equals(category)) {
            if (serviceDate != null) {
                order.setTravelDate(serviceDate);
            }
        }

        iOrderService.save(order);

        OrderItemEntity item = new OrderItemEntity();
        item.setOrderId(order.getId());
        item.setOrderNo(orderNo);
        item.setItemType(category);
        item.setItemName(buildItemName(merchant, submitBo));
        item.setItemSpec(buildItemSpec(submitBo, category));
        item.setQuantity(1);
        item.setUnitPrice(BigDecimal.ZERO);
        item.setTotalPrice(BigDecimal.ZERO);
        iOrderItemService.save(item);

        ServiceRecordSubmitVo vo = new ServiceRecordSubmitVo();
        vo.setOrderId(order.getId());
        vo.setOrderNo(orderNo);
        vo.setStatus(MerchantOrderStatusSupport.PENDING_CONFIRM);
        vo.setStatusLabel(MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.PENDING_CONFIRM));
        vo.setMerchantName(merchant.getStoreName());
        vo.setIntent(submitBo.getIntent());
        return vo;
    }

    private MerchantEntity requireApprovedMerchant(String merchantCode) {
        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getMerchantCode, StringUtils.trim(merchantCode))
                .eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .last("LIMIT 1");
        MerchantEntity merchant = iMerchantService.getOne(queryWrapper, false);
        if (merchant == null) {
            throw new BizException(AppError.SERVICE_RECORD_MERCHANT_INVALID);
        }
        return merchant;
    }

    private DestinationEntity requireDestination(Long destinationId) {
        if (destinationId == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        DestinationEntity destination = iDestinationService.getById(destinationId);
        if (destination == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        return destination;
    }

    private String buildItemName(MerchantEntity merchant, ServiceRecordSubmitBo submitBo) {
        return merchant.getStoreName() + " · " + StringUtils.trim(submitBo.getIntent());
    }

    private String buildItemSpec(ServiceRecordSubmitBo submitBo, String category) {
        if ("hotel".equals(category) && StringUtils.isNotBlank(submitBo.getDate())) {
            return submitBo.getDate() + " · " + defaultGuestCount(submitBo) + "人";
        }
        if (StringUtils.isNotBlank(submitBo.getDate())) {
            return submitBo.getDate();
        }
        return StringUtils.defaultString(submitBo.getIntent());
    }

    private String buildRemark(ServiceRecordSubmitBo submitBo) {
        StringBuilder builder = new StringBuilder();
        builder.append("预约意向：").append(StringUtils.trim(submitBo.getIntent()));
        if (StringUtils.isNotBlank(submitBo.getRemark())) {
            builder.append("；").append(StringUtils.trim(submitBo.getRemark()));
        }
        return builder.toString();
    }

    private int defaultGuestCount(ServiceRecordSubmitBo submitBo) {
        return submitBo.getGuestCount() == null ? 1 : submitBo.getGuestCount();
    }

    private LocalDate parseDate(String dateText) {
        if (StringUtils.isBlank(dateText)) {
            return null;
        }
        return LocalDate.parse(dateText.trim());
    }

    private String generateOrderNo() {
        int suffix = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "SR" + LocalDateTime.now().format(ORDER_NO_FMT) + suffix;
    }
}

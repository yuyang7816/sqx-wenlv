package com.sqx.app.lvyou.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.merchant.db.service.IMerchantDepositPayService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantDepositRefundService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantWalletService;
import com.sqx.app.lvyou.merchant.db.service.IMerchantWalletTransactionService;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositAdjustBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositRefundApplyBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositRefundAuditBo;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantDepositPayEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantDepositRefundEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantWalletEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantDepositPayStatus;
import com.sqx.app.lvyou.merchant.enums.MerchantDepositRefundStatus;
import com.sqx.app.lvyou.merchant.service.MerchantStoreTypeConfigService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantWalletTransactionEntity;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantAdminVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositPayVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositRefundVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantFundRecordVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantWalletSummaryVo;
import com.sqx.app.lvyou.merchant.service.MerchantOrderService;
import com.sqx.app.lvyou.merchant.service.MerchantWalletService;
import com.sqx.app.lvyou.merchant.support.MerchantAuthSupport;
import com.sqx.app.lvyou.wallet.db.service.IWithdrawRecordService;
import com.sqx.app.lvyou.wallet.domain.bo.WalletWithdrawBo;
import com.sqx.app.lvyou.wallet.domain.entity.WithdrawRecordEntity;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawRecordVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawVo;
import com.sqx.app.lvyou.wallet.enums.WalletTxType;
import com.sqx.app.lvyou.wallet.enums.WithdrawMethod;
import com.sqx.app.lvyou.wallet.enums.WithdrawOwnerType;
import com.sqx.app.lvyou.wallet.enums.WithdrawStatus;
import com.sqx.app.lvyou.wallet.service.WalletService;
import com.sqx.app.lvyou.wallet.support.WalletSupport;
import com.sqx.app.lvyou.wallet.support.WithdrawConfigSupport;
import com.sqx.app.lvyou.wallet.support.WithdrawConfigSupport.WithdrawAmountBreakdown;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 商户钱包业务服务实现
 */
@Service
public class MerchantWalletServiceImpl implements MerchantWalletService {

    private static final int TX_SUCCESS = 1;
    private static final int TX_PENDING = 0;
    private static final String DEPOSIT_STATUS_PAID = "paid";
    private static final String DEPOSIT_STATUS_UNPAID = "unpaid";
    private static final String DEPOSIT_STATUS_LABEL_PAID = "已缴纳";
    private static final String DEPOSIT_STATUS_LABEL_UNPAID = "未缴纳";
    private static final String WITHDRAW_PENDING_TIP = "预计 1-3 个工作日到账";
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Resource
    private MerchantAuthSupport merchantAuthSupport;

    @Resource
    private IMerchantWalletService iMerchantWalletService;

    @Resource
    private IMerchantWalletTransactionService iMerchantWalletTransactionService;

    @Resource
    private IWithdrawRecordService iWithdrawRecordService;

    @Resource
    private WithdrawConfigSupport withdrawConfigSupport;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private MerchantStoreTypeConfigService merchantStoreTypeConfigService;

    @Resource
    private IMerchantDepositPayService iMerchantDepositPayService;

    @Resource
    private WalletService walletService;

    @Resource
    private IMerchantDepositRefundService iMerchantDepositRefundService;

    @Lazy
    @Resource
    private MerchantOrderService merchantOrderService;

    @Override
    public MerchantWalletEntity ensureWallet(Long merchantId) {
        LambdaQueryWrapper<MerchantWalletEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantWalletEntity::getMerchantId, merchantId).last("LIMIT 1");
        MerchantWalletEntity wallet = iMerchantWalletService.getOne(queryWrapper, false);
        if (wallet != null) {
            return refreshDepositRequiredFromConfig(wallet);
        }
        MerchantWalletEntity created = new MerchantWalletEntity();
        created.setMerchantId(merchantId);
        created.setBalance(BigDecimal.ZERO);
        created.setPendingSettle(BigDecimal.ZERO);
        created.setTotalIncome(BigDecimal.ZERO);
        created.setDepositRequired(resolveDepositRequired(merchantId));
        created.setDepositPaid(BigDecimal.ZERO);
        iMerchantWalletService.save(created);
        return created;
    }

    /**
     * 未缴足时，将应缴保证金同步为当前门店类型配置（后台改配置后即时生效）。
     * 已缴足则保留钱包内金额，避免覆盖人工调整。
     */
    private MerchantWalletEntity refreshDepositRequiredFromConfig(MerchantWalletEntity wallet) {
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        BigDecimal storedRequired = WalletSupport.scaleAmount(wallet.getDepositRequired());
        if (depositPaid.compareTo(storedRequired) >= 0 && storedRequired.compareTo(BigDecimal.ZERO) > 0) {
            return wallet;
        }
        BigDecimal configRequired = resolveDepositRequired(wallet.getMerchantId());
        if (configRequired.compareTo(storedRequired) == 0) {
            return wallet;
        }
        wallet.setDepositRequired(configRequired);
        iMerchantWalletService.updateById(wallet);
        return wallet;
    }

    @Override
    public boolean isDepositPaid(Long merchantId) {
        MerchantWalletEntity wallet = ensureWallet(merchantId);
        return isDepositPaid(wallet);
    }

    @Override
    public void assertDepositPaid(Long merchantId) {
        MerchantWalletEntity wallet = ensureWallet(merchantId);
        assertDepositPaid(wallet, AppError.MERCHANT_DEPOSIT_REQUIRED);
    }

    @Override
    public MerchantWalletSummaryVo getSummary(Long userId) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        MerchantWalletEntity wallet = ensureWallet(merchant.getId());
        return buildSummaryVo(wallet);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantDepositPayVo createDepositPayOrder(Long userId, MerchantDepositBo depositBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        MerchantWalletEntity wallet = ensureWallet(merchant.getId());
        BigDecimal needPay = resolveNeedPayAmount(wallet);
        BigDecimal payAmount = resolveDepositAmount(depositBo, needPay);
        if (payAmount.compareTo(needPay) > 0) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_AMOUNT_INVALID);
        }

        MerchantDepositPayEntity payEntity = new MerchantDepositPayEntity();
        payEntity.setPayNo(generateDepositPayNo());
        payEntity.setMerchantId(merchant.getId());
        payEntity.setUserId(userId);
        payEntity.setAmount(payAmount);
        payEntity.setStatus(MerchantDepositPayStatus.PENDING_PAY);
        iMerchantDepositPayService.save(payEntity);

        MerchantDepositPayVo vo = new MerchantDepositPayVo();
        vo.setPayNo(payEntity.getPayNo());
        vo.setAmount(payAmount);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantDepositVo payDepositByBalance(Long userId, String payNo) {
        MerchantDepositPayEntity payEntity = requirePendingDepositPay(userId, payNo);
        walletService.deductForMerchantDeposit(userId, payNo, payEntity.getAmount());
        return settleDepositPay(payEntity, "wallet", null);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void creditDepositFromChannel(Long userId, String payNo, String payMethod, String tradeNo) {
        MerchantDepositPayEntity payEntity = findDepositPayByNo(payNo);
        if (payEntity == null) {
            return;
        }
        if (!Objects.equals(payEntity.getUserId(), userId)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_NOT_FOUND);
        }
        if (Objects.equals(payEntity.getStatus(), MerchantDepositPayStatus.PAID)) {
            return;
        }
        if (!Objects.equals(payEntity.getStatus(), MerchantDepositPayStatus.PENDING_PAY)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_STATUS_INVALID);
        }
        settleDepositPay(payEntity, payMethod, tradeNo);
    }

    private MerchantDepositVo settleDepositPay(MerchantDepositPayEntity payEntity, String payMethod, String tradeNo) {
        MerchantWalletEntity wallet = ensureWallet(payEntity.getMerchantId());
        BigDecimal depositRequired = WalletSupport.scaleAmount(wallet.getDepositRequired());
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        BigDecimal payAmount = WalletSupport.scaleAmount(payEntity.getAmount());

        LambdaUpdateWrapper<MerchantDepositPayEntity> payUpdate = new LambdaUpdateWrapper<>();
        payUpdate.eq(MerchantDepositPayEntity::getId, payEntity.getId())
                .eq(MerchantDepositPayEntity::getStatus, MerchantDepositPayStatus.PENDING_PAY)
                .set(MerchantDepositPayEntity::getStatus, MerchantDepositPayStatus.PAID)
                .set(MerchantDepositPayEntity::getPayMethod, payMethod)
                .set(MerchantDepositPayEntity::getPayTime, LocalDateTime.now())
                .set(MerchantDepositPayEntity::getTradeNo, tradeNo);
        if (!iMerchantDepositPayService.update(payUpdate)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_STATUS_INVALID);
        }

        BigDecimal nextDepositPaid = depositPaid.add(payAmount);
        LambdaUpdateWrapper<MerchantWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(MerchantWalletEntity::getId, wallet.getId())
                .set(MerchantWalletEntity::getDepositPaid, nextDepositPaid);
        iMerchantWalletService.update(walletUpdate);

        saveTransaction(payEntity.getMerchantId(), WalletTxType.DEPOSIT, "保证金缴纳", payAmount,
                WalletSupport.scaleAmount(wallet.getBalance()), payEntity.getPayNo(), TX_SUCCESS);

        MerchantDepositVo vo = new MerchantDepositVo();
        vo.setPaid(payAmount);
        vo.setDepositPaid(nextDepositPaid);
        vo.setDepositStatus(resolveDepositStatus(nextDepositPaid, depositRequired));
        return vo;
    }

    private BigDecimal resolveNeedPayAmount(MerchantWalletEntity wallet) {
        BigDecimal depositRequired = WalletSupport.scaleAmount(wallet.getDepositRequired());
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        BigDecimal needPay = depositRequired.subtract(depositPaid);
        if (needPay.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_ALREADY_PAID);
        }
        return needPay;
    }

    private MerchantDepositPayEntity requirePendingDepositPay(Long userId, String payNo) {
        if (StringUtils.isBlank(payNo)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_NO_REQUIRED);
        }
        MerchantDepositPayEntity payEntity = findDepositPayByNo(payNo);
        if (payEntity == null || !Objects.equals(payEntity.getUserId(), userId)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_NOT_FOUND);
        }
        if (!Objects.equals(payEntity.getStatus(), MerchantDepositPayStatus.PENDING_PAY)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_PAY_STATUS_INVALID);
        }
        return payEntity;
    }

    private MerchantDepositPayEntity findDepositPayByNo(String payNo) {
        if (StringUtils.isBlank(payNo)) {
            return null;
        }
        LambdaQueryWrapper<MerchantDepositPayEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantDepositPayEntity::getPayNo, payNo.trim()).last("LIMIT 1");
        return iMerchantDepositPayService.getOne(queryWrapper, false);
    }

    private String generateDepositPayNo() {
        return "MD" + GenUtil.genOrderNo();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawVo createWithdraw(Long userId, WalletWithdrawBo withdrawBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        MerchantWalletEntity wallet = ensureWallet(merchant.getId());
        assertDepositPaid(wallet, AppError.MERCHANT_WITHDRAW_DEPOSIT_REQUIRED);

        String method = WithdrawMethod.normalize(withdrawBo.getMethod());
        if (method == null) {
            throw new BizException(AppError.WALLET_WITHDRAW_METHOD_INVALID);
        }
        validateWithdrawAccount(method, withdrawBo);

        BigDecimal amount = WalletSupport.scaleAmount(withdrawBo.getAmount());
        withdrawConfigSupport.validateApplyAmount(amount);
        WithdrawAmountBreakdown breakdown = withdrawConfigSupport.breakdown(amount);

        BigDecimal currentBalance = WalletSupport.scaleAmount(wallet.getBalance());
        if (currentBalance.compareTo(amount) < 0) {
            throw new BizException(AppError.WALLET_BALANCE_INSUFFICIENT);
        }

        BigDecimal nextBalance = currentBalance.subtract(amount);
        LambdaUpdateWrapper<MerchantWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(MerchantWalletEntity::getId, wallet.getId())
                .ge(MerchantWalletEntity::getBalance, amount)
                .set(MerchantWalletEntity::getBalance, nextBalance);
        if (!iMerchantWalletService.update(walletUpdate)) {
            throw new BizException(AppError.WALLET_BALANCE_INSUFFICIENT);
        }

        String withdrawNo = generateWithdrawNo();
        WithdrawRecordEntity record = buildWithdrawRecord(merchant, breakdown, method, withdrawBo, withdrawNo);
        iWithdrawRecordService.save(record);

        saveTransaction(merchant.getId(), WalletTxType.WITHDRAW, resolveWithdrawTitle(method), amount.negate(),
                nextBalance, withdrawNo, TX_PENDING);

        WalletWithdrawVo vo = new WalletWithdrawVo();
        vo.setWithdrawNo(withdrawNo);
        vo.setAmount(breakdown.getApplyAmount());
        vo.setFeeAmount(breakdown.getFeeAmount());
        vo.setActualAmount(breakdown.getActualAmount());
        vo.setStatus(WithdrawStatus.PENDING);
        vo.setRemark(WITHDRAW_PENDING_TIP);
        return vo;
    }

    @Override
    public IPage<MerchantFundRecordVo> listFundRecords(Long userId, Long page, Long limit, String filter) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        Page<MerchantWalletTransactionEntity> pageParam = PageUtil.initPage(page, limit);
        LambdaQueryWrapper<MerchantWalletTransactionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantWalletTransactionEntity::getMerchantId, merchant.getId());
        String normalizedFilter = StringUtils.lowerCase(StringUtils.trimToEmpty(filter));
        if ("income".equals(normalizedFilter)) {
            queryWrapper.gt(MerchantWalletTransactionEntity::getAmount, BigDecimal.ZERO);
        } else if ("expense".equals(normalizedFilter)) {
            queryWrapper.lt(MerchantWalletTransactionEntity::getAmount, BigDecimal.ZERO);
        }
        queryWrapper.orderByDesc(MerchantWalletTransactionEntity::getCreateTime)
                .orderByDesc(MerchantWalletTransactionEntity::getId);

        IPage<MerchantWalletTransactionEntity> entityPage = iMerchantWalletTransactionService.page(pageParam, queryWrapper);
        List<MerchantFundRecordVo> voList = new ArrayList<>();
        if (entityPage.getRecords() != null) {
            for (MerchantWalletTransactionEntity entity : entityPage.getRecords()) {
                voList.add(toFundRecordVo(entity));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public IPage<WalletWithdrawRecordVo> listWithdrawRecords(Long userId, Long page, Long limit) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        Page<WithdrawRecordEntity> pageParam = PageUtil.initPage(page, limit);
        LambdaQueryWrapper<WithdrawRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WithdrawRecordEntity::getOwnerType, WithdrawOwnerType.MERCHANT)
                .eq(WithdrawRecordEntity::getMerchantId, merchant.getId())
                .orderByDesc(WithdrawRecordEntity::getCreateTime)
                .orderByDesc(WithdrawRecordEntity::getId);

        IPage<WithdrawRecordEntity> entityPage = iWithdrawRecordService.page(pageParam, queryWrapper);
        List<WalletWithdrawRecordVo> voList = new ArrayList<>();
        if (entityPage.getRecords() != null) {
            for (WithdrawRecordEntity entity : entityPage.getRecords()) {
                voList.add(toWithdrawRecordVo(entity));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmWithdrawSuccess(Long merchantId, BigDecimal amount, String withdrawNo) {
        updateTransactionStatus(merchantId, withdrawNo, WithdrawStatus.SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundBalance(Long merchantId, BigDecimal amount, String refNo, String title) {
        creditBalance(merchantId, amount, refNo, title, WalletTxType.INCOME);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void creditSettle(Long merchantId, BigDecimal amount, String refNo, String title) {
        creditBalance(merchantId, amount, refNo, title, WalletTxType.SETTLE);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void debitSettle(Long merchantId, BigDecimal amount, String refNo, String title) {
        if (merchantId == null || amount == null) {
            return;
        }
        BigDecimal debitAmount = WalletSupport.scaleAmount(amount);
        if (debitAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        MerchantWalletEntity wallet = ensureWallet(merchantId);
        BigDecimal currentBalance = WalletSupport.scaleAmount(wallet.getBalance());
        BigDecimal nextBalance = currentBalance.subtract(debitAmount).max(BigDecimal.ZERO);
        LambdaUpdateWrapper<MerchantWalletEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantWalletEntity::getId, wallet.getId())
                .set(MerchantWalletEntity::getBalance, nextBalance);
        iMerchantWalletService.update(updateWrapper);
        saveTransaction(merchantId, WalletTxType.SETTLE, title, debitAmount.negate(), nextBalance, refNo, TX_SUCCESS);
    }

    private void creditBalance(Long merchantId, BigDecimal amount, String refNo, String title, String txType) {
        if (merchantId == null || amount == null) {
            return;
        }
        ensureWallet(merchantId);
        LambdaQueryWrapper<MerchantWalletEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantWalletEntity::getMerchantId, merchantId).last("LIMIT 1");
        MerchantWalletEntity wallet = iMerchantWalletService.getOne(queryWrapper, false);
        if (wallet == null) {
            return;
        }
        BigDecimal creditAmount = WalletSupport.scaleAmount(amount);
        if (creditAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return;
        }
        BigDecimal nextBalance = WalletSupport.scaleAmount(wallet.getBalance()).add(creditAmount);
        LambdaUpdateWrapper<MerchantWalletEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantWalletEntity::getId, wallet.getId())
                .set(MerchantWalletEntity::getBalance, nextBalance);
        iMerchantWalletService.update(updateWrapper);
        saveTransaction(merchantId, txType, title, creditAmount, nextBalance, refNo, TX_SUCCESS);
    }

    @Override
    public void updateTransactionStatus(Long merchantId, String refNo, String status) {
        if (StringUtils.isBlank(refNo)) {
            return;
        }
        LambdaUpdateWrapper<MerchantWalletTransactionEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantWalletTransactionEntity::getMerchantId, merchantId)
                .eq(MerchantWalletTransactionEntity::getRefNo, refNo)
                .set(MerchantWalletTransactionEntity::getStatus, WalletSupport.txStatusToInt(status));
        iMerchantWalletTransactionService.update(updateWrapper);
    }

    private MerchantWalletSummaryVo buildSummaryVo(MerchantWalletEntity wallet) {
        BigDecimal depositRequired = WalletSupport.scaleAmount(wallet.getDepositRequired());
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        String depositStatus = resolveDepositStatus(depositPaid, depositRequired);

        MerchantWalletSummaryVo vo = new MerchantWalletSummaryVo();
        vo.setBalance(WalletSupport.scaleAmount(wallet.getBalance()));
        vo.setPendingSettle(WalletSupport.scaleAmount(wallet.getPendingSettle()));
        BigDecimal totalIncome = merchantOrderService.sumPaidAmount(wallet.getMerchantId(), null, null);
        vo.setTotalIncome(WalletSupport.scaleAmount(totalIncome));
        vo.setDepositRequired(depositRequired);
        vo.setDepositPaid(depositPaid);
        vo.setDepositStatus(depositStatus);
        vo.setDepositStatusLabel(DEPOSIT_STATUS_PAID.equals(depositStatus)
                ? DEPOSIT_STATUS_LABEL_PAID : DEPOSIT_STATUS_LABEL_UNPAID);

        LambdaQueryWrapper<WithdrawRecordEntity> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(WithdrawRecordEntity::getOwnerType, WithdrawOwnerType.MERCHANT)
                .eq(WithdrawRecordEntity::getMerchantId, wallet.getMerchantId());
        vo.setWithdrawCount(Math.toIntExact(iWithdrawRecordService.count(countWrapper)));
        vo.setWithdrawRules(withdrawConfigSupport.getRules());
        MerchantDepositRefundEntity latestRefund = findLatestRefund(wallet.getMerchantId());
        if (latestRefund != null && Objects.equals(latestRefund.getStatus(), MerchantDepositRefundStatus.PENDING)) {
            vo.setPendingDepositRefund(toRefundVo(latestRefund));
            vo.setCanApplyDepositRefund(Boolean.FALSE);
        } else {
            vo.setCanApplyDepositRefund(Boolean.valueOf(canApplyDepositRefund(wallet)));
            if (latestRefund != null && Objects.equals(latestRefund.getStatus(), MerchantDepositRefundStatus.REJECTED)) {
                vo.setRejectedDepositRefund(toRefundVo(latestRefund));
            }
        }
        return vo;
    }

    @Override
    public void fillAdminDepositFields(MerchantAdminVo vo, Long merchantId) {
        if (vo == null || merchantId == null) {
            return;
        }
        fillAdminDepositFields(Collections.singletonList(vo), Collections.singletonList(merchantId));
    }

    @Override
    public void fillAdminDepositFields(List<MerchantAdminVo> voList, List<Long> merchantIds) {
        if (CollectionUtils.isEmpty(voList) || CollectionUtils.isEmpty(merchantIds)) {
            return;
        }
        Map<Long, MerchantWalletEntity> walletMap = loadWalletMap(merchantIds);
        Map<Long, MerchantDepositRefundEntity> pendingRefundMap = loadPendingRefundMap(merchantIds);
        Map<Long, Long> unprocessedMap = new HashMap<>();
        for (Long merchantId : merchantIds) {
            unprocessedMap.put(merchantId, merchantOrderService.countUnprocessedOrders(merchantId));
        }
        for (MerchantAdminVo vo : voList) {
            if (vo == null || vo.getId() == null) {
                continue;
            }
            MerchantWalletEntity wallet = walletMap.get(vo.getId());
            if (wallet == null) {
                wallet = ensureWallet(vo.getId());
            }
            applyWalletFieldsToAdminVo(vo, wallet);
            vo.setUnprocessedOrderCount(unprocessedMap.getOrDefault(vo.getId(), 0L));
            MerchantDepositRefundEntity pendingRefund = pendingRefundMap.get(vo.getId());
            if (pendingRefund != null) {
                vo.setPendingDepositRefundNo(pendingRefund.getRefundNo());
                vo.setPendingDepositRefundAmount(WalletSupport.scaleAmount(pendingRefund.getAmount()));
                vo.setPendingDepositRefundStatus("pending");
            } else {
                vo.setPendingDepositRefundNo(null);
                vo.setPendingDepositRefundAmount(null);
                vo.setPendingDepositRefundStatus(null);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantDepositVo adminAdjustDeposit(MerchantDepositAdjustBo adjustBo) {
        MerchantEntity merchant = requireMerchant(adjustBo.getMerchantId());
        MerchantWalletEntity wallet = ensureWallet(merchant.getId());
        BigDecimal amount = WalletSupport.scaleAmount(adjustBo.getAmount());
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_ADJUST_INVALID);
        }
        String adjustType = StringUtils.lowerCase(StringUtils.trimToEmpty(adjustBo.getAdjustType()));
        BigDecimal depositRequired = WalletSupport.scaleAmount(wallet.getDepositRequired());
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        BigDecimal nextRequired = depositRequired;
        BigDecimal nextPaid = depositPaid;
        String txTitle = "保证金调整";

        switch (adjustType) {
            case "paid_increase":
                nextPaid = depositPaid.add(amount);
                txTitle = "保证金调增";
                break;
            case "paid_decrease":
                if (depositPaid.compareTo(amount) < 0) {
                    throw new BizException(AppError.MERCHANT_DEPOSIT_PAID_INSUFFICIENT);
                }
                nextPaid = depositPaid.subtract(amount);
                txTitle = "保证金调减";
                break;
            case "required_increase":
                nextRequired = depositRequired.add(amount);
                txTitle = "应缴保证金调增";
                break;
            case "required_decrease":
                if (depositRequired.compareTo(amount) < 0) {
                    throw new BizException(AppError.MERCHANT_DEPOSIT_ADJUST_INVALID);
                }
                nextRequired = depositRequired.subtract(amount);
                txTitle = "应缴保证金调减";
                break;
            default:
                throw new BizException(AppError.MERCHANT_DEPOSIT_ADJUST_INVALID);
        }

        LambdaUpdateWrapper<MerchantWalletEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantWalletEntity::getId, wallet.getId())
                .set(MerchantWalletEntity::getDepositRequired, nextRequired)
                .set(MerchantWalletEntity::getDepositPaid, nextPaid);
        iMerchantWalletService.update(updateWrapper);

        if ("paid_increase".equals(adjustType) || "paid_decrease".equals(adjustType)) {
            BigDecimal delta = "paid_increase".equals(adjustType) ? amount : amount.negate();
            saveTransaction(merchant.getId(), WalletTxType.DEPOSIT_ADJUST, txTitle, delta,
                    WalletSupport.scaleAmount(wallet.getBalance()), null, TX_SUCCESS);
        }
        syncMerchantOpenByDeposit(merchant.getId(), nextPaid, nextRequired);

        MerchantDepositVo vo = new MerchantDepositVo();
        vo.setPaid(amount);
        vo.setDepositPaid(nextPaid);
        vo.setDepositStatus(resolveDepositStatus(nextPaid, nextRequired));
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantDepositRefundVo applyDepositRefund(Long userId, MerchantDepositRefundApplyBo applyBo) {
        MerchantEntity merchant = merchantAuthSupport.requireApprovedMerchant(userId);
        assertNoUnprocessedOrders(merchant.getId());
        if (findPendingRefund(merchant.getId()) != null) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_REFUND_PENDING);
        }
        MerchantWalletEntity wallet = ensureWallet(merchant.getId());
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        if (depositPaid.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_REFUND_NO_PAID);
        }

        MerchantDepositRefundEntity entity = new MerchantDepositRefundEntity();
        entity.setRefundNo(generateDepositRefundNo());
        entity.setMerchantId(merchant.getId());
        entity.setUserId(userId);
        entity.setAmount(depositPaid);
        entity.setStatus(MerchantDepositRefundStatus.PENDING);
        entity.setApplyReason(StringUtils.trimToNull(applyBo == null ? null : applyBo.getReason()));
        iMerchantDepositRefundService.save(entity);
        return toRefundVo(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public MerchantDepositRefundVo auditDepositRefund(MerchantDepositRefundAuditBo auditBo) {
        MerchantEntity merchant = requireMerchant(auditBo.getMerchantId());
        MerchantDepositRefundEntity refund = requirePendingRefund(merchant.getId(), auditBo.getRefundNo());
        String action = StringUtils.lowerCase(StringUtils.trimToEmpty(auditBo.getAction()));
        if ("reject".equals(action)) {
            String rejectReason = StringUtils.trimToNull(auditBo.getRemark());
            if (rejectReason == null) {
                throw new BizException(AppError.MERCHANT_DEPOSIT_REFUND_REJECT_REASON_REQUIRED);
            }
            LocalDateTime auditTime = LocalDateTime.now();
            LambdaUpdateWrapper<MerchantDepositRefundEntity> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(MerchantDepositRefundEntity::getId, refund.getId())
                    .eq(MerchantDepositRefundEntity::getStatus, MerchantDepositRefundStatus.PENDING)
                    .set(MerchantDepositRefundEntity::getStatus, MerchantDepositRefundStatus.REJECTED)
                    .set(MerchantDepositRefundEntity::getAuditRemark, rejectReason)
                    .set(MerchantDepositRefundEntity::getAuditTime, auditTime);
            iMerchantDepositRefundService.update(updateWrapper);
            refund.setStatus(MerchantDepositRefundStatus.REJECTED);
            refund.setAuditRemark(rejectReason);
            refund.setAuditTime(auditTime);
            return toRefundVo(refund);
        }
        if (!"approve".equals(action)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_REFUND_AUDIT_INVALID);
        }
        assertNoUnprocessedOrders(merchant.getId());

        MerchantWalletEntity wallet = ensureWallet(merchant.getId());
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        BigDecimal refundAmount = WalletSupport.scaleAmount(refund.getAmount());
        if (depositPaid.compareTo(refundAmount) < 0) {
            refundAmount = depositPaid;
        }
        if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_REFUND_NO_PAID);
        }

        BigDecimal nextPaid = depositPaid.subtract(refundAmount);
        LambdaUpdateWrapper<MerchantWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(MerchantWalletEntity::getId, wallet.getId())
                .set(MerchantWalletEntity::getDepositPaid, nextPaid);
        iMerchantWalletService.update(walletUpdate);

        refundBalance(merchant.getId(), refundAmount, refund.getRefundNo(), "保证金退还");
        MerchantWalletEntity refreshedWallet = ensureWallet(merchant.getId());
        saveTransaction(merchant.getId(), WalletTxType.DEPOSIT_REFUND, "保证金退还",
                refundAmount.negate(), WalletSupport.scaleAmount(refreshedWallet.getBalance()),
                refund.getRefundNo(), TX_SUCCESS);

        LambdaUpdateWrapper<MerchantDepositRefundEntity> refundUpdate = new LambdaUpdateWrapper<>();
        refundUpdate.eq(MerchantDepositRefundEntity::getId, refund.getId())
                .eq(MerchantDepositRefundEntity::getStatus, MerchantDepositRefundStatus.PENDING)
                .set(MerchantDepositRefundEntity::getStatus, MerchantDepositRefundStatus.APPROVED)
                .set(MerchantDepositRefundEntity::getAuditRemark, StringUtils.trimToNull(auditBo.getRemark()))
                .set(MerchantDepositRefundEntity::getAuditTime, LocalDateTime.now())
                .set(MerchantDepositRefundEntity::getAmount, refundAmount);
        iMerchantDepositRefundService.update(refundUpdate);

        syncMerchantOpenByDeposit(merchant.getId(), nextPaid, WalletSupport.scaleAmount(wallet.getDepositRequired()));
        closeMerchantAfterDepositRefund(merchant.getId());

        refund.setStatus(MerchantDepositRefundStatus.APPROVED);
        refund.setAmount(refundAmount);
        return toRefundVo(refund);
    }

    private BigDecimal resolveDepositAmount(MerchantDepositBo depositBo, BigDecimal needPay) {
        if (depositBo == null || depositBo.getAmount() == null) {
            return needPay;
        }
        return WalletSupport.scaleAmount(depositBo.getAmount());
    }

    private boolean isDepositPaid(MerchantWalletEntity wallet) {
        BigDecimal depositRequired = WalletSupport.scaleAmount(wallet.getDepositRequired());
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        return depositPaid.compareTo(depositRequired) >= 0;
    }

    private void assertDepositPaid(MerchantWalletEntity wallet, AppError error) {
        if (!isDepositPaid(wallet)) {
            throw new BizException(error);
        }
    }

    private BigDecimal resolveDepositRequired(Long merchantId) {
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant == null || StringUtils.isBlank(merchant.getStoreType())) {
            return merchantStoreTypeConfigService.resolveDeposit(null);
        }
        return merchantStoreTypeConfigService.resolveDeposit(merchant.getStoreType());
    }

    private String resolveDepositStatus(BigDecimal depositPaid, BigDecimal depositRequired) {
        return depositPaid.compareTo(depositRequired) >= 0 ? DEPOSIT_STATUS_PAID : DEPOSIT_STATUS_UNPAID;
    }

    private WithdrawRecordEntity buildWithdrawRecord(MerchantEntity merchant, WithdrawAmountBreakdown breakdown,
                                                     String method, WalletWithdrawBo withdrawBo, String withdrawNo) {
        WithdrawRecordEntity record = new WithdrawRecordEntity();
        record.setWithdrawNo(withdrawNo);
        record.setOwnerType(WithdrawOwnerType.MERCHANT);
        record.setMerchantId(merchant.getId());
        record.setUserId(merchant.getUserId());
        record.setAmount(breakdown.getApplyAmount());
        record.setFeeAmount(breakdown.getFeeAmount());
        record.setActualAmount(breakdown.getActualAmount());
        record.setMethod(method);
        record.setStatus(WithdrawStatus.PENDING);
        record.setRemark(WITHDRAW_PENDING_TIP);

        if (WithdrawMethod.ALIPAY.equals(method)) {
            String accountRaw = StringUtils.trimToEmpty(withdrawBo.getAlipayAccount());
            record.setAccountRaw(accountRaw);
            record.setAccount(WalletSupport.maskAccount(accountRaw));
            record.setRealName(StringUtils.trimToEmpty(withdrawBo.getAlipayName()));
        } else {
            String qrCodeUrl = StringUtils.trimToEmpty(withdrawBo.getWechatQrCode());
            record.setQrCodeUrl(qrCodeUrl);
            record.setAccount(WalletSupport.resolveWithdrawAccountDisplay(method, null, qrCodeUrl));
        }
        return record;
    }

    private void validateWithdrawAccount(String method, WalletWithdrawBo withdrawBo) {
        if (WithdrawMethod.ALIPAY.equals(method)) {
            if (StringUtils.isBlank(withdrawBo.getAlipayAccount()) || StringUtils.isBlank(withdrawBo.getAlipayName())) {
                throw new BizException(AppError.WALLET_WITHDRAW_ACCOUNT_REQUIRED);
            }
            return;
        }
        if (WithdrawMethod.WECHAT.equals(method)) {
            if (StringUtils.isBlank(withdrawBo.getWechatQrCode())) {
                throw new BizException(AppError.WALLET_WITHDRAW_ACCOUNT_REQUIRED);
            }
            return;
        }
        throw new BizException(AppError.WALLET_WITHDRAW_METHOD_INVALID);
    }

    private MerchantFundRecordVo toFundRecordVo(MerchantWalletTransactionEntity entity) {
        MerchantFundRecordVo vo = new MerchantFundRecordVo();
        vo.setId(entity.getTxNo());
        vo.setType(entity.getTxType());
        vo.setTitle(entity.getTitle());
        vo.setAmount(entity.getAmount());
        vo.setTime(entity.getCreateTime() == null ? "" : entity.getCreateTime().format(TIME_FORMATTER));
        vo.setOrderNo(StringUtils.defaultString(entity.getRefNo()));
        vo.setBalance(entity.getBalanceAfter());
        vo.setStatus(WalletSupport.txStatusToCode(entity.getStatus()));
        return vo;
    }

    private WalletWithdrawRecordVo toWithdrawRecordVo(WithdrawRecordEntity entity) {
        WalletWithdrawRecordVo vo = new WalletWithdrawRecordVo();
        vo.setId(entity.getWithdrawNo());
        vo.setMethod(entity.getMethod());
        vo.setMethodLabel(resolveMethodLabel(entity.getMethod()));
        vo.setAmount(entity.getAmount());
        vo.setFeeAmount(entity.getFeeAmount());
        vo.setActualAmount(entity.getActualAmount() != null ? entity.getActualAmount() : entity.getAmount());
        vo.setAccount(entity.getAccount());
        vo.setRealName(entity.getRealName());
        vo.setQrCode(entity.getQrCodeUrl());
        vo.setTime(entity.getCreateTime() == null ? "" : entity.getCreateTime().format(TIME_FORMATTER));
        vo.setStatus(entity.getStatus());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private String resolveMethodLabel(String method) {
        if (WithdrawMethod.ALIPAY.equals(method)) {
            return "支付宝";
        }
        if (WithdrawMethod.WECHAT.equals(method)) {
            return "微信";
        }
        return method;
    }

    private String resolveWithdrawTitle(String method) {
        if (WithdrawMethod.ALIPAY.equals(method)) {
            return "提现至支付宝";
        }
        return "提现至微信";
    }

    private void saveTransaction(Long merchantId, String txType, String title, BigDecimal amount,
                               BigDecimal balanceAfter, String refNo, int status) {
        MerchantWalletTransactionEntity tx = new MerchantWalletTransactionEntity();
        tx.setMerchantId(merchantId);
        tx.setTxNo(GenUtil.genOrderNo());
        tx.setTxType(txType);
        tx.setTitle(title);
        tx.setAmount(WalletSupport.scaleAmount(amount));
        tx.setBalanceAfter(WalletSupport.scaleAmount(balanceAfter));
        tx.setRefNo(refNo);
        tx.setStatus(status);
        iMerchantWalletTransactionService.save(tx);
    }

    private String generateWithdrawNo() {
        return "WD" + GenUtil.genOrderNo();
    }

    private String generateDepositRefundNo() {
        return "DR" + GenUtil.genOrderNo();
    }

    private MerchantEntity requireMerchant(Long merchantId) {
        MerchantEntity merchant = iMerchantService.getById(merchantId);
        if (merchant == null) {
            throw new BizException(AppError.MERCHANT_NOT_FOUND);
        }
        return merchant;
    }

    private void assertNoUnprocessedOrders(Long merchantId) {
        if (merchantOrderService.countUnprocessedOrders(merchantId) > 0) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_REFUND_BLOCKED_ORDERS);
        }
    }

    private boolean canApplyDepositRefund(MerchantWalletEntity wallet) {
        if (wallet == null) {
            return false;
        }
        if (WalletSupport.scaleAmount(wallet.getDepositPaid()).compareTo(BigDecimal.ZERO) <= 0) {
            return false;
        }
        return merchantOrderService.countUnprocessedOrders(wallet.getMerchantId()) <= 0;
    }

    private MerchantDepositRefundEntity findPendingRefund(Long merchantId) {
        LambdaQueryWrapper<MerchantDepositRefundEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantDepositRefundEntity::getMerchantId, merchantId)
                .eq(MerchantDepositRefundEntity::getStatus, MerchantDepositRefundStatus.PENDING)
                .orderByDesc(MerchantDepositRefundEntity::getId)
                .last("LIMIT 1");
        return iMerchantDepositRefundService.getOne(queryWrapper, false);
    }

    private MerchantDepositRefundEntity findLatestRefund(Long merchantId) {
        if (merchantId == null) {
            return null;
        }
        LambdaQueryWrapper<MerchantDepositRefundEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantDepositRefundEntity::getMerchantId, merchantId)
                .orderByDesc(MerchantDepositRefundEntity::getId)
                .last("LIMIT 1");
        return iMerchantDepositRefundService.getOne(queryWrapper, false);
    }

    private MerchantDepositRefundEntity requirePendingRefund(Long merchantId, String refundNo) {
        if (StringUtils.isBlank(refundNo)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_REFUND_NOT_FOUND);
        }
        LambdaQueryWrapper<MerchantDepositRefundEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantDepositRefundEntity::getMerchantId, merchantId)
                .eq(MerchantDepositRefundEntity::getRefundNo, refundNo.trim())
                .last("LIMIT 1");
        MerchantDepositRefundEntity refund = iMerchantDepositRefundService.getOne(queryWrapper, false);
        if (refund == null || !Objects.equals(refund.getStatus(), MerchantDepositRefundStatus.PENDING)) {
            throw new BizException(AppError.MERCHANT_DEPOSIT_REFUND_NOT_FOUND);
        }
        return refund;
    }

    private Map<Long, MerchantWalletEntity> loadWalletMap(List<Long> merchantIds) {
        if (CollectionUtils.isEmpty(merchantIds)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<MerchantWalletEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantWalletEntity::getMerchantId, merchantIds);
        List<MerchantWalletEntity> wallets = iMerchantWalletService.list(queryWrapper);
        Map<Long, MerchantWalletEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(wallets)) {
            for (MerchantWalletEntity wallet : wallets) {
                map.put(wallet.getMerchantId(), wallet);
            }
        }
        return map;
    }

    private Map<Long, MerchantDepositRefundEntity> loadPendingRefundMap(List<Long> merchantIds) {
        if (CollectionUtils.isEmpty(merchantIds)) {
            return Collections.emptyMap();
        }
        LambdaQueryWrapper<MerchantDepositRefundEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(MerchantDepositRefundEntity::getMerchantId, merchantIds)
                .eq(MerchantDepositRefundEntity::getStatus, MerchantDepositRefundStatus.PENDING);
        List<MerchantDepositRefundEntity> refunds = iMerchantDepositRefundService.list(queryWrapper);
        Map<Long, MerchantDepositRefundEntity> map = new HashMap<>();
        if (CollectionUtils.isNotEmpty(refunds)) {
            for (MerchantDepositRefundEntity refund : refunds) {
                map.putIfAbsent(refund.getMerchantId(), refund);
            }
        }
        return map;
    }

    private void applyWalletFieldsToAdminVo(MerchantAdminVo vo, MerchantWalletEntity wallet) {
        BigDecimal depositRequired = WalletSupport.scaleAmount(wallet.getDepositRequired());
        BigDecimal depositPaid = WalletSupport.scaleAmount(wallet.getDepositPaid());
        String depositStatus = resolveDepositStatus(depositPaid, depositRequired);
        vo.setDepositRequired(depositRequired);
        vo.setDepositPaid(depositPaid);
        vo.setDepositStatus(depositStatus);
        vo.setDepositStatusLabel(DEPOSIT_STATUS_PAID.equals(depositStatus)
                ? DEPOSIT_STATUS_LABEL_PAID : DEPOSIT_STATUS_LABEL_UNPAID);
    }

    private MerchantDepositRefundVo toRefundVo(MerchantDepositRefundEntity entity) {
        MerchantDepositRefundVo vo = new MerchantDepositRefundVo();
        vo.setRefundNo(entity.getRefundNo());
        vo.setAmount(WalletSupport.scaleAmount(entity.getAmount()));
        vo.setApplyReason(entity.getApplyReason());
        vo.setAuditRemark(entity.getAuditRemark());
        vo.setCreateTime(entity.getCreateTime() == null ? "" : entity.getCreateTime().format(TIME_FORMATTER));
        vo.setAuditTime(entity.getAuditTime() == null ? "" : entity.getAuditTime().format(TIME_FORMATTER));
        if (Objects.equals(entity.getStatus(), MerchantDepositRefundStatus.APPROVED)) {
            vo.setStatus("approved");
            vo.setStatusLabel("已通过");
        } else if (Objects.equals(entity.getStatus(), MerchantDepositRefundStatus.REJECTED)) {
            vo.setStatus("rejected");
            vo.setStatusLabel("已驳回");
        } else {
            vo.setStatus("pending");
            vo.setStatusLabel("待审核");
        }
        return vo;
    }

    private void syncMerchantOpenByDeposit(Long merchantId, BigDecimal depositPaid, BigDecimal depositRequired) {
        if (depositPaid.compareTo(depositRequired) >= 0) {
            return;
        }
        LambdaUpdateWrapper<MerchantEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantEntity::getId, merchantId)
                .set(MerchantEntity::getIsOpen, 0);
        iMerchantService.update(updateWrapper);
    }

    private void closeMerchantAfterDepositRefund(Long merchantId) {
        LambdaUpdateWrapper<MerchantEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(MerchantEntity::getId, merchantId)
                .set(MerchantEntity::getIsOpen, 0);
        iMerchantService.update(updateWrapper);
    }
}

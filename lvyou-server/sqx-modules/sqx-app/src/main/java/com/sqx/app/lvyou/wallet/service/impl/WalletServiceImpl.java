package com.sqx.app.lvyou.wallet.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.support.AppOrderStatusSupport;
import com.sqx.app.lvyou.order.support.OrderPaymentSupport;
import com.sqx.app.lvyou.wallet.db.service.IUserWalletService;
import com.sqx.app.lvyou.wallet.db.service.IUserWalletTransactionService;
import com.sqx.app.lvyou.wallet.db.service.IWalletRechargeService;
import com.sqx.app.lvyou.wallet.db.service.IWithdrawRecordService;
import com.sqx.app.lvyou.wallet.domain.bo.WalletPayBo;
import com.sqx.app.lvyou.wallet.domain.bo.WalletRechargeBo;
import com.sqx.app.lvyou.wallet.domain.bo.WalletWithdrawBo;
import com.sqx.app.lvyou.wallet.domain.entity.UserWalletEntity;
import com.sqx.app.lvyou.wallet.domain.entity.UserWalletTransactionEntity;
import com.sqx.app.lvyou.wallet.domain.entity.WalletRechargeEntity;
import com.sqx.app.lvyou.wallet.domain.entity.WithdrawRecordEntity;
import com.sqx.app.lvyou.wallet.domain.vo.WalletPayVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletRechargeVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletSummaryVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletTransactionVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawRecordVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawVo;
import com.sqx.app.lvyou.wallet.enums.WalletRechargeStatus;
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
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户钱包业务服务实现
 */
@Service
public class WalletServiceImpl implements WalletService {

    private static final String PAY_METHOD_WALLET = "wallet";
    private static final int TX_SUCCESS = 1;
    private static final int TX_PENDING = 0;
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String WITHDRAW_PENDING_TIP = "预计 1-3 个工作日到账";

    @Resource
    private IUserWalletService iUserWalletService;

    @Resource
    private IWalletRechargeService iWalletRechargeService;

    @Resource
    private IUserWalletTransactionService iUserWalletTransactionService;

    @Resource
    private IWithdrawRecordService iWithdrawRecordService;

    @Resource
    private OrderPaymentSupport orderPaymentSupport;

    @Resource
    private WithdrawConfigSupport withdrawConfigSupport;

    @Override
    public UserWalletEntity ensureWallet(Long userId) {
        LambdaQueryWrapper<UserWalletEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserWalletEntity::getUserId, userId).last("LIMIT 1");
        UserWalletEntity wallet = iUserWalletService.getOne(queryWrapper, false);
        if (wallet != null) {
            return wallet;
        }
        UserWalletEntity created = new UserWalletEntity();
        created.setUserId(userId);
        created.setBalance(WalletSupport.scaleAmount(BigDecimal.ZERO));
        created.setTotalRecharge(WalletSupport.scaleAmount(BigDecimal.ZERO));
        created.setTotalConsume(WalletSupport.scaleAmount(BigDecimal.ZERO));
        created.setTotalWithdraw(WalletSupport.scaleAmount(BigDecimal.ZERO));
        iUserWalletService.save(created);
        return created;
    }

    @Override
    public WalletSummaryVo getSummary(Long userId) {
        UserWalletEntity wallet = ensureWallet(userId);
        WalletSummaryVo vo = new WalletSummaryVo();
        vo.setBalance(WalletSupport.formatMoney(wallet.getBalance()));
        vo.setAvailable(WalletSupport.formatMoney(wallet.getBalance()));
        vo.setTotalRecharge(WalletSupport.formatMoney(wallet.getTotalRecharge()));
        vo.setTotalConsume(WalletSupport.formatMoney(wallet.getTotalConsume()));
        vo.setTotalWithdraw(WalletSupport.formatMoney(wallet.getTotalWithdraw()));

        LambdaQueryWrapper<WithdrawRecordEntity> countWrapper = new LambdaQueryWrapper<>();
        countWrapper.eq(WithdrawRecordEntity::getOwnerType, WithdrawOwnerType.USER)
                .eq(WithdrawRecordEntity::getUserId, userId);
        vo.setWithdrawCount(Math.toIntExact(iWithdrawRecordService.count(countWrapper)));
        vo.setWithdrawRules(withdrawConfigSupport.getRules());
        return vo;
    }

    @Override
    public IPage<WalletTransactionVo> listTransactions(Long userId, Long page, Long limit, String filter) {
        Page<UserWalletTransactionEntity> pageParam = PageUtil.initPage(page, limit);
        LambdaQueryWrapper<UserWalletTransactionEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserWalletTransactionEntity::getUserId, userId);
        String normalizedFilter = StringUtils.lowerCase(StringUtils.trimToEmpty(filter));
        if ("income".equals(normalizedFilter)) {
            queryWrapper.gt(UserWalletTransactionEntity::getAmount, BigDecimal.ZERO);
        } else if ("expense".equals(normalizedFilter)) {
            queryWrapper.lt(UserWalletTransactionEntity::getAmount, BigDecimal.ZERO);
        }
        queryWrapper.orderByDesc(UserWalletTransactionEntity::getCreateTime)
                .orderByDesc(UserWalletTransactionEntity::getId);

        IPage<UserWalletTransactionEntity> entityPage = iUserWalletTransactionService.page(pageParam, queryWrapper);
        List<WalletTransactionVo> voList = new ArrayList<>();
        if (entityPage.getRecords() != null) {
            for (UserWalletTransactionEntity entity : entityPage.getRecords()) {
                voList.add(toTransactionVo(entity));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    public IPage<WalletWithdrawRecordVo> listWithdrawRecords(Long userId, Long page, Long limit) {
        Page<WithdrawRecordEntity> pageParam = PageUtil.initPage(page, limit);
        LambdaQueryWrapper<WithdrawRecordEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WithdrawRecordEntity::getOwnerType, WithdrawOwnerType.USER)
                .eq(WithdrawRecordEntity::getUserId, userId)
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
    public WalletRechargeVo createRecharge(Long userId, WalletRechargeBo rechargeBo) {
        ensureWallet(userId);
        BigDecimal amount = WalletSupport.scaleAmount(rechargeBo.getAmount());
        WalletRechargeEntity recharge = new WalletRechargeEntity();
        recharge.setRechargeNo(generateRechargeNo());
        recharge.setUserId(userId);
        recharge.setAmount(amount);
        recharge.setStatus(WalletRechargeStatus.PENDING_PAY);
        iWalletRechargeService.save(recharge);

        WalletRechargeVo vo = new WalletRechargeVo();
        vo.setRechargeNo(recharge.getRechargeNo());
        vo.setAmount(amount);
        vo.setStatus("pending_pay");
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public WalletWithdrawVo createWithdraw(Long userId, WalletWithdrawBo withdrawBo) {
        String method = WithdrawMethod.normalize(withdrawBo.getMethod());
        if (method == null) {
            throw new BizException(AppError.WALLET_WITHDRAW_METHOD_INVALID);
        }
        validateWithdrawAccount(method, withdrawBo);

        BigDecimal amount = WalletSupport.scaleAmount(withdrawBo.getAmount());
        withdrawConfigSupport.validateApplyAmount(amount);
        WithdrawAmountBreakdown breakdown = withdrawConfigSupport.breakdown(amount);

        UserWalletEntity wallet = ensureWallet(userId);
        BigDecimal currentBalance = WalletSupport.scaleAmount(wallet.getBalance());
        if (currentBalance.compareTo(amount) < 0) {
            throw new BizException(AppError.WALLET_BALANCE_INSUFFICIENT);
        }

        BigDecimal nextBalance = currentBalance.subtract(amount);
        LambdaUpdateWrapper<UserWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(UserWalletEntity::getId, wallet.getId())
                .ge(UserWalletEntity::getBalance, amount)
                .set(UserWalletEntity::getBalance, nextBalance);
        if (!iUserWalletService.update(walletUpdate)) {
            throw new BizException(AppError.WALLET_BALANCE_INSUFFICIENT);
        }

        String withdrawNo = generateWithdrawNo();
        WithdrawRecordEntity record = buildWithdrawRecord(userId, breakdown, method, withdrawBo, withdrawNo);
        iWithdrawRecordService.save(record);

        saveTransaction(userId, WalletTxType.WITHDRAW, resolveWithdrawTitle(method), amount.negate(),
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
    @Transactional(rollbackFor = Exception.class)
    public WalletPayVo payByBalance(Long userId, WalletPayBo payBo) {
        OrderEntity order = orderPaymentSupport.requireUnpaidOrder(userId, payBo.getOrderNo());
        BigDecimal payAmount = WalletSupport.scaleAmount(order.getPayAmount());
        deductForOrder(userId, order.getOrderNo(), payAmount);
        orderPaymentSupport.markOrderPaid(order, PAY_METHOD_WALLET, null);

        WalletPayVo vo = new WalletPayVo();
        vo.setOrderNo(order.getOrderNo());
        vo.setStatus(AppOrderStatusSupport.PENDING_CONFIRM);
        vo.setPayMethod(PAY_METHOD_WALLET);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void creditRecharge(Long userId, String rechargeNo, String payMethod, String tradeNo) {
        WalletRechargeEntity recharge = requireRecharge(rechargeNo);
        if (!Objects.equals(recharge.getUserId(), userId)) {
            throw new BizException(AppError.WALLET_RECHARGE_NOT_FOUND);
        }
        if (Objects.equals(recharge.getStatus(), WalletRechargeStatus.PAID)) {
            return;
        }
        if (!Objects.equals(recharge.getStatus(), WalletRechargeStatus.PENDING_PAY)) {
            throw new BizException(AppError.WALLET_RECHARGE_STATUS_INVALID);
        }

        UserWalletEntity wallet = ensureWallet(userId);
        BigDecimal amount = WalletSupport.scaleAmount(recharge.getAmount());
        BigDecimal nextBalance = WalletSupport.scaleAmount(wallet.getBalance()).add(amount);
        BigDecimal nextRecharge = WalletSupport.scaleAmount(wallet.getTotalRecharge()).add(amount);

        LambdaUpdateWrapper<UserWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(UserWalletEntity::getId, wallet.getId())
                .set(UserWalletEntity::getBalance, nextBalance)
                .set(UserWalletEntity::getTotalRecharge, nextRecharge);
        iUserWalletService.update(walletUpdate);

        LambdaUpdateWrapper<WalletRechargeEntity> rechargeUpdate = new LambdaUpdateWrapper<>();
        rechargeUpdate.eq(WalletRechargeEntity::getId, recharge.getId())
                .eq(WalletRechargeEntity::getStatus, WalletRechargeStatus.PENDING_PAY)
                .set(WalletRechargeEntity::getStatus, WalletRechargeStatus.PAID)
                .set(WalletRechargeEntity::getPayMethod, payMethod)
                .set(WalletRechargeEntity::getPayTime, LocalDateTime.now())
                .set(WalletRechargeEntity::getTradeNo, tradeNo);
        iWalletRechargeService.update(rechargeUpdate);

        saveTransaction(userId, WalletTxType.RECHARGE, "账户充值", amount, nextBalance, rechargeNo, TX_SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductForOrder(Long userId, String orderNo, BigDecimal amount) {
        UserWalletEntity wallet = ensureWallet(userId);
        BigDecimal payAmount = WalletSupport.scaleAmount(amount);
        BigDecimal currentBalance = WalletSupport.scaleAmount(wallet.getBalance());
        if (currentBalance.compareTo(payAmount) < 0) {
            throw new BizException(AppError.WALLET_BALANCE_INSUFFICIENT);
        }
        BigDecimal nextBalance = currentBalance.subtract(payAmount);
        BigDecimal nextConsume = WalletSupport.scaleAmount(wallet.getTotalConsume()).add(payAmount);

        LambdaUpdateWrapper<UserWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(UserWalletEntity::getId, wallet.getId())
                .ge(UserWalletEntity::getBalance, payAmount)
                .set(UserWalletEntity::getBalance, nextBalance)
                .set(UserWalletEntity::getTotalConsume, nextConsume);
        if (!iUserWalletService.update(walletUpdate)) {
            throw new BizException(AppError.WALLET_BALANCE_INSUFFICIENT);
        }
        saveTransaction(userId, WalletTxType.CONSUME, "订单支付", payAmount.negate(), nextBalance, orderNo, TX_SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductForMerchantDeposit(Long userId, String payNo, BigDecimal amount) {
        UserWalletEntity wallet = ensureWallet(userId);
        BigDecimal payAmount = WalletSupport.scaleAmount(amount);
        BigDecimal currentBalance = WalletSupport.scaleAmount(wallet.getBalance());
        if (currentBalance.compareTo(payAmount) < 0) {
            throw new BizException(AppError.WALLET_BALANCE_INSUFFICIENT);
        }
        BigDecimal nextBalance = currentBalance.subtract(payAmount);
        BigDecimal nextConsume = WalletSupport.scaleAmount(wallet.getTotalConsume()).add(payAmount);

        LambdaUpdateWrapper<UserWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(UserWalletEntity::getId, wallet.getId())
                .ge(UserWalletEntity::getBalance, payAmount)
                .set(UserWalletEntity::getBalance, nextBalance)
                .set(UserWalletEntity::getTotalConsume, nextConsume);
        if (!iUserWalletService.update(walletUpdate)) {
            throw new BizException(AppError.WALLET_BALANCE_INSUFFICIENT);
        }
        saveTransaction(userId, WalletTxType.CONSUME, "保证金缴纳", payAmount.negate(), nextBalance, payNo, TX_SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void refundBalance(Long userId, BigDecimal amount, String refNo, String title) {
        creditIncome(userId, amount, refNo, title);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void creditCommission(Long userId, BigDecimal amount, String refNo, String title) {
        creditIncome(userId, amount, refNo, title);
    }

    private void creditIncome(Long userId, BigDecimal amount, String refNo, String title) {
        UserWalletEntity wallet = ensureWallet(userId);
        BigDecimal incomeAmount = WalletSupport.scaleAmount(amount);
        BigDecimal nextBalance = WalletSupport.scaleAmount(wallet.getBalance()).add(incomeAmount);

        LambdaUpdateWrapper<UserWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(UserWalletEntity::getId, wallet.getId())
                .set(UserWalletEntity::getBalance, nextBalance);
        iUserWalletService.update(walletUpdate);

        saveTransaction(userId, WalletTxType.INCOME, title, incomeAmount, nextBalance, refNo, TX_SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void confirmWithdrawSuccess(Long userId, BigDecimal amount, String withdrawNo) {
        UserWalletEntity wallet = ensureWallet(userId);
        BigDecimal withdrawAmount = WalletSupport.scaleAmount(amount);
        BigDecimal nextWithdraw = WalletSupport.scaleAmount(wallet.getTotalWithdraw()).add(withdrawAmount);

        LambdaUpdateWrapper<UserWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(UserWalletEntity::getId, wallet.getId())
                .set(UserWalletEntity::getTotalWithdraw, nextWithdraw);
        iUserWalletService.update(walletUpdate);

        updateTransactionStatus(userId, withdrawNo, WithdrawStatus.SUCCESS);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminSetBalance(Long userId, BigDecimal newBalance, String remark) {
        BigDecimal targetBalance = WalletSupport.scaleAmount(newBalance);
        if (targetBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("余额不能为负数");
        }

        UserWalletEntity wallet = ensureWallet(userId);
        BigDecimal currentBalance = WalletSupport.scaleAmount(wallet.getBalance());
        if (currentBalance.compareTo(targetBalance) == 0) {
            return;
        }

        BigDecimal delta = targetBalance.subtract(currentBalance);
        applyAdminBalanceChange(userId, wallet, delta, targetBalance, remark);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void adminAdjustBalance(Long userId, BigDecimal delta, String remark) {
        BigDecimal change = WalletSupport.scaleAmount(delta);
        if (change.compareTo(BigDecimal.ZERO) == 0) {
            return;
        }

        UserWalletEntity wallet = ensureWallet(userId);
        BigDecimal currentBalance = WalletSupport.scaleAmount(wallet.getBalance());
        BigDecimal targetBalance = currentBalance.add(change);
        if (targetBalance.compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("余额不足，无法减少");
        }
        applyAdminBalanceChange(userId, wallet, change, targetBalance, remark);
    }

    private void applyAdminBalanceChange(Long userId, UserWalletEntity wallet, BigDecimal delta,
                                         BigDecimal targetBalance, String remark) {
        LambdaUpdateWrapper<UserWalletEntity> walletUpdate = new LambdaUpdateWrapper<>();
        walletUpdate.eq(UserWalletEntity::getId, wallet.getId())
                .set(UserWalletEntity::getBalance, targetBalance);
        iUserWalletService.update(walletUpdate);

        String title = StringUtils.defaultIfBlank(remark, "管理员调整余额");
        saveTransaction(userId, WalletTxType.ADMIN_ADJUST, title, delta, targetBalance,
                "ADM" + GenUtil.genOrderNo(), TX_SUCCESS);
    }

    @Override
    public void updateTransactionStatus(Long userId, String refNo, String status) {
        if (StringUtils.isBlank(refNo)) {
            return;
        }
        LambdaUpdateWrapper<UserWalletTransactionEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserWalletTransactionEntity::getUserId, userId)
                .eq(UserWalletTransactionEntity::getRefNo, refNo)
                .set(UserWalletTransactionEntity::getStatus, WalletSupport.txStatusToInt(status));
        iUserWalletTransactionService.update(updateWrapper);
    }

    private WithdrawRecordEntity buildWithdrawRecord(Long userId, WithdrawAmountBreakdown breakdown, String method,
                                                     WalletWithdrawBo withdrawBo, String withdrawNo) {
        WithdrawRecordEntity record = new WithdrawRecordEntity();
        record.setWithdrawNo(withdrawNo);
        record.setOwnerType(WithdrawOwnerType.USER);
        record.setUserId(userId);
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

    private WalletTransactionVo toTransactionVo(UserWalletTransactionEntity entity) {
        WalletTransactionVo vo = new WalletTransactionVo();
        vo.setId(entity.getTxNo());
        vo.setType(entity.getTxType());
        vo.setTitle(entity.getTitle());
        vo.setAmount(entity.getAmount());
        vo.setTime(entity.getCreateTime() == null ? "" : entity.getCreateTime().format(TIME_FORMATTER));
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
        vo.setActualAmount(resolveActualAmount(entity));
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

    private BigDecimal resolveActualAmount(WithdrawRecordEntity entity) {
        if (entity.getActualAmount() != null) {
            return entity.getActualAmount();
        }
        return entity.getAmount();
    }

    private String resolveWithdrawTitle(String method) {
        if (WithdrawMethod.ALIPAY.equals(method)) {
            return "提现至支付宝";
        }
        return "提现至微信";
    }

    private WalletRechargeEntity requireRecharge(String rechargeNo) {
        if (StringUtils.isBlank(rechargeNo)) {
            throw new BizException(AppError.WALLET_RECHARGE_NOT_FOUND);
        }
        LambdaQueryWrapper<WalletRechargeEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(WalletRechargeEntity::getRechargeNo, rechargeNo).last("LIMIT 1");
        WalletRechargeEntity recharge = iWalletRechargeService.getOne(queryWrapper, false);
        if (recharge == null) {
            throw new BizException(AppError.WALLET_RECHARGE_NOT_FOUND);
        }
        return recharge;
    }

    private void saveTransaction(Long userId, String txType, String title, BigDecimal amount,
                                 BigDecimal balanceAfter, String refNo, int status) {
        UserWalletTransactionEntity tx = new UserWalletTransactionEntity();
        tx.setUserId(userId);
        tx.setTxNo(GenUtil.genOrderNo());
        tx.setTxType(txType);
        tx.setTitle(title);
        tx.setAmount(WalletSupport.scaleAmount(amount));
        tx.setBalanceAfter(WalletSupport.scaleAmount(balanceAfter));
        tx.setRefNo(refNo);
        tx.setStatus(status);
        iUserWalletTransactionService.save(tx);
    }

    private String generateRechargeNo() {
        return "RC" + GenUtil.genOrderNo();
    }

    private String generateWithdrawNo() {
        return "WD" + GenUtil.genOrderNo();
    }
}

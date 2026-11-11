package com.sqx.app.lvyou.wallet.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.wallet.domain.bo.WalletPayBo;
import com.sqx.app.lvyou.wallet.domain.bo.WalletRechargeBo;
import com.sqx.app.lvyou.wallet.domain.bo.WalletWithdrawBo;
import com.sqx.app.lvyou.wallet.domain.entity.UserWalletEntity;
import com.sqx.app.lvyou.wallet.domain.vo.WalletPayVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletRechargeVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletSummaryVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletTransactionVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawRecordVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawVo;

import java.math.BigDecimal;

/**
 * 用户钱包业务服务
 */
public interface WalletService {

    UserWalletEntity ensureWallet(Long userId);

    WalletSummaryVo getSummary(Long userId);

    IPage<WalletTransactionVo> listTransactions(Long userId, Long page, Long limit, String filter);

    IPage<WalletWithdrawRecordVo> listWithdrawRecords(Long userId, Long page, Long limit);

    WalletRechargeVo createRecharge(Long userId, WalletRechargeBo rechargeBo);

    WalletWithdrawVo createWithdraw(Long userId, WalletWithdrawBo withdrawBo);

    WalletPayVo payByBalance(Long userId, WalletPayBo payBo);

    void creditRecharge(Long userId, String rechargeNo, String payMethod, String tradeNo);

    void deductForOrder(Long userId, String orderNo, BigDecimal amount);

    void deductForMerchantDeposit(Long userId, String payNo, BigDecimal amount);

    void refundBalance(Long userId, BigDecimal amount, String refNo, String title);

    void creditCommission(Long userId, BigDecimal amount, String refNo, String title);

    void confirmWithdrawSuccess(Long userId, BigDecimal amount, String withdrawNo);

    void updateTransactionStatus(Long userId, String refNo, String status);

    /**
     * 管理员将用户钱包余额设置为指定值
     */
    void adminSetBalance(Long userId, BigDecimal newBalance, String remark);

    /**
     * 管理员增减用户钱包余额（delta 正数为增加，负数为减少）
     */
    void adminAdjustBalance(Long userId, BigDecimal delta, String remark);
}

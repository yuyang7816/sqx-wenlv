package com.sqx.app.lvyou.merchant.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositAdjustBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositRefundApplyBo;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantDepositRefundAuditBo;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantWalletEntity;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantAdminVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositPayVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositRefundVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantDepositVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantFundRecordVo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantWalletSummaryVo;
import com.sqx.app.lvyou.wallet.domain.bo.WalletWithdrawBo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawRecordVo;
import com.sqx.app.lvyou.wallet.domain.vo.WalletWithdrawVo;

import java.math.BigDecimal;

/**
 * 商户钱包业务服务
 */
public interface MerchantWalletService {

    MerchantWalletEntity ensureWallet(Long merchantId);

    /** 是否已缴足保证金 */
    boolean isDepositPaid(Long merchantId);

    /** 校验已缴足保证金（未缴足则抛出业务异常） */
    void assertDepositPaid(Long merchantId);

    MerchantWalletSummaryVo getSummary(Long userId);

    MerchantDepositPayVo createDepositPayOrder(Long userId, MerchantDepositBo depositBo);

    MerchantDepositVo payDepositByBalance(Long userId, String payNo);

    void creditDepositFromChannel(Long userId, String payNo, String payMethod, String tradeNo);

    WalletWithdrawVo createWithdraw(Long userId, WalletWithdrawBo withdrawBo);

    IPage<MerchantFundRecordVo> listFundRecords(Long userId, Long page, Long limit, String filter);

    IPage<WalletWithdrawRecordVo> listWithdrawRecords(Long userId, Long page, Long limit);

    void confirmWithdrawSuccess(Long merchantId, BigDecimal amount, String withdrawNo);

    void refundBalance(Long merchantId, BigDecimal amount, String refNo, String title);

    /** 订单结算入账 */
    void creditSettle(Long merchantId, BigDecimal amount, String refNo, String title);

    /** 订单结算冲正（退款扣回） */
    void debitSettle(Long merchantId, BigDecimal amount, String refNo, String title);

    void updateTransactionStatus(Long merchantId, String refNo, String status);

    void fillAdminDepositFields(MerchantAdminVo vo, Long merchantId);

    void fillAdminDepositFields(java.util.List<MerchantAdminVo> voList, java.util.List<Long> merchantIds);

    MerchantDepositVo adminAdjustDeposit(MerchantDepositAdjustBo adjustBo);

    MerchantDepositRefundVo applyDepositRefund(Long userId, MerchantDepositRefundApplyBo applyBo);

    MerchantDepositRefundVo auditDepositRefund(MerchantDepositRefundAuditBo auditBo);
}

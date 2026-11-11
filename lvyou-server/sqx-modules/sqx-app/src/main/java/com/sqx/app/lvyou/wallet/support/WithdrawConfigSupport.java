package com.sqx.app.lvyou.wallet.support;

import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.lvyou.wallet.domain.vo.WithdrawRulesVo;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 提现规则（最小/最大金额、手续费）读取与计算
 */
@Component
public class WithdrawConfigSupport {

    private static final BigDecimal DEFAULT_MIN = new BigDecimal("10.00");
    private static final BigDecimal DEFAULT_MAX = new BigDecimal("50000.00");
    private static final BigDecimal DEFAULT_FEE_RATE = BigDecimal.ZERO;

    public WithdrawRulesVo getRules() {
        WithdrawRulesVo vo = new WithdrawRulesVo();
        vo.setMinAmount(WalletSupport.formatMoney(getMinAmount()));
        vo.setMaxAmount(WalletSupport.formatMoney(getMaxAmount()));
        vo.setFeeRate(formatFeeRate(getFeeRatePercent()));
        return vo;
    }

    public BigDecimal getMinAmount() {
        return parsePositiveAmount(
                ConfigUtil.getConfigValueOrDefault(SysConfigConstant.Withdraw.WITHDRAW_MIN),
                DEFAULT_MIN);
    }

    public BigDecimal getMaxAmount() {
        return parsePositiveAmount(
                ConfigUtil.getConfigValueOrDefault(SysConfigConstant.Withdraw.WITHDRAW_MAX),
                DEFAULT_MAX);
    }

    /** 手续费比例（百分比，1 表示 1%） */
    public BigDecimal getFeeRatePercent() {
        return parseFeeRate(ConfigUtil.getConfigValueOrDefault(SysConfigConstant.Withdraw.WITHDRAW_FEE_RATE));
    }

    public WithdrawAmountBreakdown breakdown(BigDecimal applyAmount) {
        BigDecimal amount = WalletSupport.scaleAmount(applyAmount);
        BigDecimal feeAmount = calculateFee(amount);
        BigDecimal actualAmount = WalletSupport.scaleAmount(amount.subtract(feeAmount));
        WithdrawAmountBreakdown breakdown = new WithdrawAmountBreakdown();
        breakdown.setApplyAmount(amount);
        breakdown.setFeeAmount(feeAmount);
        breakdown.setActualAmount(actualAmount);
        return breakdown;
    }

    public void validateApplyAmount(BigDecimal applyAmount) {
        BigDecimal amount = WalletSupport.scaleAmount(applyAmount);
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("请输入有效的提现金额");
        }

        BigDecimal minAmount = getMinAmount();
        if (amount.compareTo(minAmount) < 0) {
            throw new BizException(String.format("提现金额不能低于%s元", WalletSupport.formatMoney(minAmount)));
        }

        BigDecimal maxAmount = getMaxAmount();
        if (maxAmount.compareTo(BigDecimal.ZERO) > 0 && amount.compareTo(maxAmount) > 0) {
            throw new BizException(String.format("提现金额不能超过%s元", WalletSupport.formatMoney(maxAmount)));
        }

        WithdrawAmountBreakdown breakdown = breakdown(amount);
        if (breakdown.getActualAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BizException("扣除手续费后到账金额须大于0");
        }
    }

    public BigDecimal calculateFee(BigDecimal applyAmount) {
        BigDecimal amount = WalletSupport.scaleAmount(applyAmount);
        BigDecimal rate = getFeeRatePercent();
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return amount.multiply(rate)
                .divide(new BigDecimal("100"), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal parsePositiveAmount(String raw, BigDecimal defaultValue) {
        if (StringUtils.isBlank(raw)) {
            return defaultValue;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.compareTo(BigDecimal.ZERO) <= 0) {
                return defaultValue;
            }
            return WalletSupport.scaleAmount(value);
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private BigDecimal parseFeeRate(String raw) {
        if (StringUtils.isBlank(raw)) {
            return DEFAULT_FEE_RATE;
        }
        try {
            BigDecimal value = new BigDecimal(raw.trim());
            if (value.compareTo(BigDecimal.ZERO) < 0) {
                return DEFAULT_FEE_RATE;
            }
            if (value.compareTo(new BigDecimal("100")) > 0) {
                return new BigDecimal("100");
            }
            return value.setScale(2, RoundingMode.HALF_UP);
        } catch (NumberFormatException ex) {
            return DEFAULT_FEE_RATE;
        }
    }

    private String formatFeeRate(BigDecimal rate) {
        if (rate == null) {
            return "0.00";
        }
        return rate.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    public static class WithdrawAmountBreakdown {
        private BigDecimal applyAmount;
        private BigDecimal feeAmount;
        private BigDecimal actualAmount;

        public BigDecimal getApplyAmount() {
            return applyAmount;
        }

        public void setApplyAmount(BigDecimal applyAmount) {
            this.applyAmount = applyAmount;
        }

        public BigDecimal getFeeAmount() {
            return feeAmount;
        }

        public void setFeeAmount(BigDecimal feeAmount) {
            this.feeAmount = feeAmount;
        }

        public BigDecimal getActualAmount() {
            return actualAmount;
        }

        public void setActualAmount(BigDecimal actualAmount) {
            this.actualAmount = actualAmount;
        }
    }
}

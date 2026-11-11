package com.sqx.app.lvyou.merchant.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.lvyou.dict.constants.LvyouDictTypes;
import com.sqx.app.lvyou.merchant.domain.bo.MerchantStoreTypeSaveBo;
import com.sqx.app.lvyou.merchant.domain.vo.MerchantStoreTypeVo;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.merchant.service.MerchantStoreTypeConfigService;
import com.sqx.common.core.exception.BizException;
import com.sqx.system.db.entity.SysDictEntity;
import com.sqx.system.db.service.ISysDictService;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 商户入驻门店类型配置
 */
@Service
public class MerchantStoreTypeConfigServiceImpl implements MerchantStoreTypeConfigService {

    private static final BigDecimal DEFAULT_DEPOSIT = new BigDecimal("5000.00");
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");
    private static final Pattern LEGACY_TYPE_CODE = Pattern.compile("^[a-z_]+$");
    /** 常见繁体字形，用于识别 sys_dict 中错误编码的名称 */
    private static final Pattern TRADITIONAL_HINT = Pattern.compile(
            "[禮飲臺國際資訊網絡聯繫設開關顯選確認刪編輯創發評論點擊載獲處響驗註錄頭暱稱關絲動態歷訂單優會員權幫隱對於為與無這間價觀預約詳細圖視頻轉贊復還來說現內從後進過見實應該將帶讓變當雖難樣類標籤識語檔導專業數據調試執務環機構組織維護檢測帳戶瀏覽頁碼總條麵須備僅陸陽陰額餘]"
    );

    @Resource
    private ISysDictService iSysDictService;

    @Override
    public List<MerchantStoreTypeVo> listAll() {
        return loadFromDb(true);
    }

    @Override
    public List<MerchantStoreTypeVo> listActive() {
        return loadFromDb(false);
    }

    @Override
    public boolean isValid(String code) {
        if (StringUtils.isBlank(code)) {
            return false;
        }
        if ("goods".equalsIgnoreCase(code)) {
            return isValid(MerchantStoreType.CULTURE.getCode());
        }
        if (MerchantStoreType.isLegacyGoose(code)) {
            return false;
        }
        String normalized = normalizeCode(code);
        for (MerchantStoreTypeVo item : listActive()) {
            if (normalized.equals(item.getCode())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String labelOf(String code) {
        if (MerchantStoreType.isLegacyGoose(code)) {
            return MerchantStoreType.RESTAURANT.getLabel();
        }
        String normalized = normalizeCode(code);
        for (MerchantStoreTypeVo item : listActive()) {
            if (normalized.equals(item.getCode())) {
                return resolveLabel(normalized, item.getLabel());
            }
        }
        return MerchantStoreType.labelOfFallback(normalized);
    }

    @Override
    public BigDecimal parseDepositAmount(String rawValue, String code) {
        return parseDeposit(rawValue, code);
    }

    @Override
    public BigDecimal resolveDeposit(String code) {
        if (StringUtils.isBlank(code)) {
            return DEFAULT_DEPOSIT;
        }
        String normalized = MerchantStoreType.normalize(code);
        for (MerchantStoreTypeVo item : listActive()) {
            if (normalized.equals(item.getCode())) {
                return scaleDeposit(item.getDepositAmount());
            }
        }
        return DEFAULT_DEPOSIT;
    }

    @Override
    public void save(MerchantStoreTypeSaveBo saveBo) {
        validatePayload(saveBo, false);
        SysDictEntity entity = new SysDictEntity();
        entity.setType(LvyouDictTypes.MERCHANT_TYPE);
        entity.setCode(normalizeCode(saveBo.getCode()));
        entity.setName(StringUtils.trim(saveBo.getLabel()));
        entity.setValue(formatDeposit(saveBo.getDepositAmount()));
        entity.setOrderNum(saveBo.getSortOrder() == null ? 0 : saveBo.getSortOrder());
        entity.setRemark(StringUtils.trimToNull(saveBo.getRemark()));
        iSysDictService.save(entity);
    }

    @Override
    public void update(MerchantStoreTypeSaveBo saveBo) {
        if (saveBo.getId() == null) {
            throw new BizException("类型ID不能为空");
        }
        validatePayload(saveBo, true);
        SysDictEntity existing = iSysDictService.getById(saveBo.getId());
        if (existing == null || !LvyouDictTypes.MERCHANT_TYPE.equals(existing.getType())) {
            throw new BizException("入驻类型不存在");
        }
        existing.setCode(normalizeCode(saveBo.getCode()));
        existing.setName(StringUtils.trim(saveBo.getLabel()));
        existing.setValue(formatDeposit(saveBo.getDepositAmount()));
        existing.setOrderNum(saveBo.getSortOrder() == null ? 0 : saveBo.getSortOrder());
        existing.setRemark(StringUtils.trimToNull(saveBo.getRemark()));
        iSysDictService.updateById(existing);
    }

    @Override
    public void delete(Long id) {
        if (id == null) {
            throw new BizException("类型ID不能为空");
        }
        SysDictEntity existing = iSysDictService.getById(id);
        if (existing == null || !LvyouDictTypes.MERCHANT_TYPE.equals(existing.getType())) {
            throw new BizException("入驻类型不存在");
        }
        iSysDictService.removeById(id);
    }

    private List<MerchantStoreTypeVo> loadFromDb(boolean includeEmpty) {
        LambdaQueryWrapper<SysDictEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysDictEntity::getType, LvyouDictTypes.MERCHANT_TYPE)
                .orderByAsc(SysDictEntity::getOrderNum)
                .orderByAsc(SysDictEntity::getId);
        List<SysDictEntity> entities = iSysDictService.list(queryWrapper);
        if (CollectionUtils.isEmpty(entities)) {
            return defaultTypes();
        }
        List<MerchantStoreTypeVo> items = new ArrayList<>();
        for (SysDictEntity entity : entities) {
            MerchantStoreTypeVo vo = toVo(entity);
            if (includeEmpty || StringUtils.isNotBlank(vo.getCode())) {
                items.add(vo);
            }
        }
        return items.isEmpty() ? defaultTypes() : items;
    }

    private MerchantStoreTypeVo toVo(SysDictEntity entity) {
        MerchantStoreTypeVo vo = new MerchantStoreTypeVo();
        vo.setId(entity.getId());
        String code = StringUtils.defaultString(entity.getCode());
        vo.setCode(code);
        vo.setLabel(resolveLabel(code, StringUtils.defaultIfBlank(entity.getName(), code)));
        vo.setDepositAmount(parseDeposit(entity.getValue(), code));
        vo.setSortOrder(entity.getOrderNum());
        vo.setRemark(entity.getRemark());
        return vo;
    }

    private String resolveLabel(String code, String rawLabel) {
        String normalized = MerchantStoreType.normalize(code);
        String fallback = MerchantStoreType.labelOfFallback(normalized);
        if (isGarbledLabel(rawLabel) || containsTraditionalHint(rawLabel)) {
            return fallback;
        }
        return StringUtils.defaultIfBlank(rawLabel, fallback);
    }

    private boolean isGarbledLabel(String text) {
        if (StringUtils.isBlank(text)) {
            return true;
        }
        return text.trim().matches("^[?？]+$");
    }

    private boolean containsTraditionalHint(String text) {
        return StringUtils.isNotBlank(text) && TRADITIONAL_HINT.matcher(text).find();
    }

    private List<MerchantStoreTypeVo> defaultTypes() {
        List<MerchantStoreTypeVo> items = new ArrayList<>();
        int sort = 0;
        for (MerchantStoreType storeType : MerchantStoreType.values()) {
            MerchantStoreTypeVo vo = new MerchantStoreTypeVo();
            vo.setCode(storeType.getCode());
            vo.setLabel(storeType.getLabel());
            vo.setDepositAmount(defaultDepositFor(storeType.getCode()));
            vo.setSortOrder(sort++);
            items.add(vo);
        }
        return items;
    }

    private BigDecimal defaultDepositFor(String code) {
        switch (code) {
            case "hotel":
                return new BigDecimal("10000.00");
            case "restaurant":
                return new BigDecimal("3000.00");
            case "travel":
                return new BigDecimal("5000.00");
            case "culture":
                return new BigDecimal("2000.00");
            default:
                return DEFAULT_DEPOSIT;
        }
    }

    private void validatePayload(MerchantStoreTypeSaveBo saveBo, boolean updating) {
        if (saveBo == null) {
            throw new BizException("入驻类型不能为空");
        }
        String code = normalizeCode(saveBo.getCode());
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new BizException("类型编码仅支持小写字母、数字、下划线，且以字母开头");
        }
        String label = StringUtils.trim(saveBo.getLabel());
        if (containsTraditionalHint(label)) {
            throw new BizException("类型名称请使用简体中文");
        }
        if (saveBo.getDepositAmount() == null || saveBo.getDepositAmount().compareTo(BigDecimal.ZERO) < 0) {
            throw new BizException("保证金金额不正确");
        }
        LambdaQueryWrapper<SysDictEntity> duplicateQuery = new LambdaQueryWrapper<>();
        duplicateQuery.eq(SysDictEntity::getType, LvyouDictTypes.MERCHANT_TYPE)
                .eq(SysDictEntity::getCode, code);
        if (updating && saveBo.getId() != null) {
            duplicateQuery.ne(SysDictEntity::getId, saveBo.getId());
        }
        if (iSysDictService.count(duplicateQuery) > 0) {
            throw new BizException("类型编码已存在");
        }
    }

    private String normalizeCode(String code) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(code), Locale.ROOT);
    }

    private BigDecimal parseDeposit(String rawValue, String code) {
        if (StringUtils.isBlank(rawValue)) {
            return defaultDepositFor(StringUtils.defaultString(code));
        }
        String value = rawValue.trim();
        if (LEGACY_TYPE_CODE.matcher(value).matches() && !value.matches("\\d+(\\.\\d+)?")) {
            return defaultDepositFor(value);
        }
        try {
            return scaleDeposit(new BigDecimal(value));
        } catch (NumberFormatException ex) {
            return defaultDepositFor(StringUtils.defaultString(code));
        }
    }

    private String formatDeposit(BigDecimal amount) {
        return scaleDeposit(amount).toPlainString();
    }

    private BigDecimal scaleDeposit(BigDecimal amount) {
        if (amount == null) {
            return DEFAULT_DEPOSIT;
        }
        return amount.setScale(2, RoundingMode.HALF_UP);
    }
}

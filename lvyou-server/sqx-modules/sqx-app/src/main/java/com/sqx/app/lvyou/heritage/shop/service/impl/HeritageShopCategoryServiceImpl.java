package com.sqx.app.lvyou.heritage.shop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.heritage.craft.db.service.IHeritageCraftService;
import com.sqx.app.lvyou.heritage.craft.domain.entity.HeritageCraftEntity;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopCategoryService;
import com.sqx.app.lvyou.heritage.shop.db.service.IHeritageShopService;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryInfoBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryPageBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategorySaveBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryUpdateBo;
import com.sqx.app.lvyou.heritage.shop.domain.bo.HeritageShopCategoryUpdateStateBo;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopCategoryEntity;
import com.sqx.app.lvyou.heritage.shop.domain.entity.HeritageShopEntity;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopCategoryOptionVo;
import com.sqx.app.lvyou.heritage.shop.domain.vo.HeritageShopCategoryVo;
import com.sqx.app.lvyou.heritage.shop.service.HeritageShopCategoryService;
import com.sqx.app.lvyou.heritage.shop.support.HeritageShopCategorySupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * 非遗好店分类业务服务实现
 */
@Service
public class HeritageShopCategoryServiceImpl implements HeritageShopCategoryService {

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

    @Resource
    private IHeritageShopCategoryService iHeritageShopCategoryService;

    @Resource
    private IHeritageCraftService iHeritageCraftService;

    @Resource
    private IHeritageShopService iHeritageShopService;

    @Override
    public IPage<HeritageShopCategoryVo> pageInfo(HeritageShopCategoryPageBo pageBo) {
        LambdaQueryWrapper<HeritageShopCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), HeritageShopCategoryEntity::getName, pageBo.getName())
                .like(StringUtils.isNotBlank(pageBo.getCode()), HeritageShopCategoryEntity::getCode, pageBo.getCode())
                .eq(Objects.nonNull(pageBo.getStatus()), HeritageShopCategoryEntity::getStatus, pageBo.getStatus())
                .orderByAsc(HeritageShopCategoryEntity::getSort)
                .orderByAsc(HeritageShopCategoryEntity::getId);

        Page<HeritageShopCategoryEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        IPage<HeritageShopCategoryEntity> entityPage = iHeritageShopCategoryService.page(page, queryWrapper);
        List<HeritageShopCategoryVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HeritageShopCategoryEntity entity : entityPage.getRecords()) {
                voList.add(toVo(entity));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(HeritageShopCategorySaveBo saveBo) {
        String code = resolveCodeForSave(saveBo.getCode(), saveBo.getName());
        validateCodeFormat(code);
        assertCodeUnique(code, null);

        HeritageShopCategoryEntity entity = new HeritageShopCategoryEntity();
        entity.setCode(code);
        entity.setName(StringUtils.trim(saveBo.getName()));
        entity.setSort(saveBo.getSort() == null ? 0 : saveBo.getSort());
        entity.setStatus(normalizeStatus(saveBo.getStatus()));
        iHeritageShopCategoryService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(HeritageShopCategoryUpdateBo updateBo) {
        HeritageShopCategoryEntity existing = requireCategory(updateBo.getId());
        String code = StringUtils.isNotBlank(updateBo.getCode())
                ? normalizeCode(updateBo.getCode())
                : existing.getCode();
        validateCodeFormat(code);
        if (!Objects.equals(existing.getCode(), code)) {
            throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_CODE_IMMUTABLE);
        }

        existing.setName(StringUtils.trim(updateBo.getName()));
        existing.setSort(updateBo.getSort() == null ? 0 : updateBo.getSort());
        existing.setStatus(normalizeStatus(updateBo.getStatus()));
        iHeritageShopCategoryService.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(HeritageShopCategoryUpdateStateBo updateStateBo) {
        requireCategory(updateStateBo.getId());
        LambdaUpdateWrapper<HeritageShopCategoryEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(HeritageShopCategoryEntity::getId, updateStateBo.getId())
                .set(HeritageShopCategoryEntity::getStatus, normalizeStatus(updateStateBo.getStatus()));
        iHeritageShopCategoryService.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(HeritageShopCategoryInfoBo infoBo) {
        HeritageShopCategoryEntity existing = requireCategory(infoBo.getId());
        LambdaQueryWrapper<HeritageCraftEntity> craftWrapper = new LambdaQueryWrapper<>();
        craftWrapper.eq(HeritageCraftEntity::getShopCategory, existing.getCode());
        if (iHeritageCraftService.count(craftWrapper) > 0) {
            throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_IN_USE);
        }
        LambdaQueryWrapper<HeritageShopEntity> shopWrapper = new LambdaQueryWrapper<>();
        shopWrapper.eq(HeritageShopEntity::getCategory, existing.getCode());
        if (iHeritageShopService.count(shopWrapper) > 0) {
            throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_IN_USE);
        }
        iHeritageShopCategoryService.removeById(infoBo.getId());
    }

    @Override
    public List<HeritageShopCategoryOptionVo> listEnabledOptions() {
        LambdaQueryWrapper<HeritageShopCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HeritageShopCategoryEntity::getStatus, STATUS_ENABLED)
                .orderByAsc(HeritageShopCategoryEntity::getSort)
                .orderByAsc(HeritageShopCategoryEntity::getId);
        List<HeritageShopCategoryEntity> entities = iHeritageShopCategoryService.list(queryWrapper);
        List<HeritageShopCategoryOptionVo> options = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (HeritageShopCategoryEntity entity : entities) {
                HeritageShopCategoryOptionVo vo = new HeritageShopCategoryOptionVo();
                vo.setCode(entity.getCode());
                vo.setName(entity.getName());
                vo.setSort(entity.getSort());
                options.add(vo);
            }
        }
        return options;
    }

    @Override
    public boolean isValidFilter(String code) {
        if (StringUtils.isBlank(code) || "all".equalsIgnoreCase(code)) {
            return true;
        }
        return findByCode(normalizeCode(code)) != null;
    }

    @Override
    public boolean isValidForSave(String code) {
        if (StringUtils.isBlank(code)) {
            return true;
        }
        HeritageShopCategoryEntity entity = findByCode(normalizeCode(code));
        return entity != null && Objects.equals(entity.getStatus(), STATUS_ENABLED);
    }

    @Override
    public String normalize(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        String normalized = normalizeCode(code);
        HeritageShopCategoryEntity entity = findByCode(normalized);
        return entity == null ? normalized : entity.getCode();
    }

    @Override
    public String labelOf(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        HeritageShopCategoryEntity entity = findByCode(normalizeCode(code));
        if (entity != null) {
            return entity.getName();
        }
        return code;
    }

    @Override
    public String resolveDefaultCode() {
        List<HeritageShopCategoryOptionVo> options = listEnabledOptions();
        if (CollectionUtils.isNotEmpty(options)) {
            return options.get(0).getCode();
        }
        return HeritageShopCategorySupport.DEFAULT;
    }

    private HeritageShopCategoryEntity requireCategory(Long id) {
        HeritageShopCategoryEntity entity = iHeritageShopCategoryService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_NOT_FOUND);
        }
        return entity;
    }

    private HeritageShopCategoryEntity findByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        LambdaQueryWrapper<HeritageShopCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HeritageShopCategoryEntity::getCode, code).last("LIMIT 1");
        return iHeritageShopCategoryService.getOne(queryWrapper, false);
    }

    private void assertCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<HeritageShopCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HeritageShopCategoryEntity::getCode, code);
        if (excludeId != null) {
            queryWrapper.ne(HeritageShopCategoryEntity::getId, excludeId);
        }
        if (iHeritageShopCategoryService.count(queryWrapper) > 0) {
            throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_CODE_DUPLICATE);
        }
    }

    private void validateCodeFormat(String code) {
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new BizException(AppError.HERITAGE_SHOP_CATEGORY_CODE_INVALID);
        }
    }

    private String resolveCodeForSave(String rawCode, String name) {
        if (StringUtils.isNotBlank(rawCode)) {
            return normalizeCode(rawCode);
        }
        return generateCodeFromName(name);
    }

    private String generateCodeFromName(String name) {
        String trimmed = StringUtils.trimToEmpty(name);
        if (StringUtils.isNotBlank(trimmed)) {
            String slug = trimmed.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "_")
                    .replaceAll("^_+|_+$", "");
            if (StringUtils.isNotBlank(slug) && CODE_PATTERN.matcher(slug).matches()) {
                return ensureUniqueCode(slug);
            }
            return ensureUniqueCode("c_" + DigestUtils.md5Hex(trimmed).substring(0, 8));
        }
        return ensureUniqueCode("cat_" + System.currentTimeMillis());
    }

    private String ensureUniqueCode(String base) {
        String code = base;
        int suffix = 2;
        while (findByCode(code) != null) {
            code = base + "_" + suffix;
            suffix++;
        }
        return code;
    }

    private String normalizeCode(String code) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(code));
    }

    private int normalizeStatus(Integer status) {
        return Objects.equals(status, STATUS_ENABLED) ? STATUS_ENABLED : STATUS_DISABLED;
    }

    private HeritageShopCategoryVo toVo(HeritageShopCategoryEntity entity) {
        HeritageShopCategoryVo vo = new HeritageShopCategoryVo();
        vo.setId(entity.getId());
        vo.setCode(entity.getCode());
        vo.setName(entity.getName());
        vo.setSort(entity.getSort());
        vo.setStatus(entity.getStatus());
        vo.setStatusLabel(Objects.equals(entity.getStatus(), STATUS_ENABLED) ? "启用" : "禁用");
        vo.setCreateTime(entity.getCreateTime());
        vo.setUpdateTime(entity.getUpdateTime());
        return vo;
    }
}

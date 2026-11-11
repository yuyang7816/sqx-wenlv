package com.sqx.app.lvyou.scenic.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.scenic.db.service.IScenicCategoryService;
import com.sqx.app.lvyou.scenic.db.service.IScenicService;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryInfoBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryPageBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategorySaveBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryUpdateBo;
import com.sqx.app.lvyou.scenic.domain.bo.ScenicCategoryUpdateStateBo;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicCategoryEntity;
import com.sqx.app.lvyou.scenic.domain.entity.ScenicEntity;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicCategoryOptionVo;
import com.sqx.app.lvyou.scenic.domain.vo.ScenicCategoryVo;
import com.sqx.app.lvyou.scenic.enums.ScenicCategory;
import com.sqx.app.lvyou.scenic.service.ScenicCategoryService;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
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
 * 景区分类业务服务实现
 */
@Service
public class ScenicCategoryServiceImpl implements ScenicCategoryService {

    private static final int STATUS_ENABLED = 1;
    private static final int STATUS_DISABLED = 0;
    private static final Pattern CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_]*$");

    @Resource
    private IScenicCategoryService iScenicCategoryService;

    @Resource
    private IScenicService iScenicService;

    @Override
    public IPage<ScenicCategoryVo> pageInfo(ScenicCategoryPageBo pageBo) {
        LambdaQueryWrapper<ScenicCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), ScenicCategoryEntity::getName, pageBo.getName())
                .like(StringUtils.isNotBlank(pageBo.getCode()), ScenicCategoryEntity::getCode, pageBo.getCode())
                .eq(Objects.nonNull(pageBo.getStatus()), ScenicCategoryEntity::getStatus, pageBo.getStatus())
                .orderByAsc(ScenicCategoryEntity::getSort)
                .orderByAsc(ScenicCategoryEntity::getId);

        Page<ScenicCategoryEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        IPage<ScenicCategoryEntity> entityPage = iScenicCategoryService.page(page, queryWrapper);
        List<ScenicCategoryVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (ScenicCategoryEntity entity : entityPage.getRecords()) {
                voList.add(toVo(entity));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(ScenicCategorySaveBo saveBo) {
        String code = normalizeCode(saveBo.getCode());
        validateCodeFormat(code);
        assertCodeUnique(code, null);

        ScenicCategoryEntity entity = new ScenicCategoryEntity();
        entity.setCode(code);
        entity.setName(StringUtils.trim(saveBo.getName()));
        entity.setSort(saveBo.getSort() == null ? 0 : saveBo.getSort());
        entity.setStatus(normalizeStatus(saveBo.getStatus()));
        iScenicCategoryService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(ScenicCategoryUpdateBo updateBo) {
        ScenicCategoryEntity existing = requireCategory(updateBo.getId());
        String code = normalizeCode(updateBo.getCode());
        validateCodeFormat(code);
        // 编码创建后不可变更，避免历史景区数据失联
        if (!Objects.equals(existing.getCode(), code)) {
            throw new BizException(AppError.SCENIC_CATEGORY_CODE_IMMUTABLE);
        }

        existing.setName(StringUtils.trim(updateBo.getName()));
        existing.setSort(updateBo.getSort() == null ? 0 : updateBo.getSort());
        existing.setStatus(normalizeStatus(updateBo.getStatus()));
        iScenicCategoryService.updateById(existing);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(ScenicCategoryUpdateStateBo updateStateBo) {
        requireCategory(updateStateBo.getId());
        LambdaUpdateWrapper<ScenicCategoryEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(ScenicCategoryEntity::getId, updateStateBo.getId())
                .set(ScenicCategoryEntity::getStatus, normalizeStatus(updateStateBo.getStatus()));
        iScenicCategoryService.update(updateWrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(ScenicCategoryInfoBo infoBo) {
        ScenicCategoryEntity existing = requireCategory(infoBo.getId());
        LambdaQueryWrapper<ScenicEntity> usedWrapper = new LambdaQueryWrapper<>();
        usedWrapper.eq(ScenicEntity::getCategory, existing.getCode());
        if (iScenicService.count(usedWrapper) > 0) {
            throw new BizException(AppError.SCENIC_CATEGORY_IN_USE);
        }
        iScenicCategoryService.removeById(infoBo.getId());
    }

    @Override
    public List<ScenicCategoryOptionVo> listEnabledOptions() {
        LambdaQueryWrapper<ScenicCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicCategoryEntity::getStatus, STATUS_ENABLED)
                .orderByAsc(ScenicCategoryEntity::getSort)
                .orderByAsc(ScenicCategoryEntity::getId);
        List<ScenicCategoryEntity> entities = iScenicCategoryService.list(queryWrapper);
        List<ScenicCategoryOptionVo> options = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (ScenicCategoryEntity entity : entities) {
                ScenicCategoryOptionVo vo = new ScenicCategoryOptionVo();
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
        ScenicCategoryEntity entity = findByCode(normalizeCode(code));
        return entity != null && Objects.equals(entity.getStatus(), STATUS_ENABLED);
    }

    @Override
    public String normalize(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        String normalized = normalizeCode(code);
        ScenicCategoryEntity entity = findByCode(normalized);
        return entity == null ? normalized : entity.getCode();
    }

    @Override
    public String labelOf(String code) {
        if (StringUtils.isBlank(code)) {
            return "";
        }
        ScenicCategoryEntity entity = findByCode(normalizeCode(code));
        if (entity != null) {
            return entity.getName();
        }
        // 兼容历史枚举文案
        String legacy = ScenicCategory.labelOf(code);
        return StringUtils.isNotBlank(legacy) ? legacy : code;
    }

    private ScenicCategoryEntity requireCategory(Long id) {
        ScenicCategoryEntity entity = iScenicCategoryService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.SCENIC_CATEGORY_NOT_FOUND);
        }
        return entity;
    }

    private ScenicCategoryEntity findByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        LambdaQueryWrapper<ScenicCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicCategoryEntity::getCode, code).last("LIMIT 1");
        return iScenicCategoryService.getOne(queryWrapper, false);
    }

    private void assertCodeUnique(String code, Long excludeId) {
        LambdaQueryWrapper<ScenicCategoryEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ScenicCategoryEntity::getCode, code);
        if (excludeId != null) {
            queryWrapper.ne(ScenicCategoryEntity::getId, excludeId);
        }
        if (iScenicCategoryService.count(queryWrapper) > 0) {
            throw new BizException(AppError.SCENIC_CATEGORY_CODE_DUPLICATE);
        }
    }

    private void validateCodeFormat(String code) {
        if (!CODE_PATTERN.matcher(code).matches()) {
            throw new BizException(AppError.SCENIC_CATEGORY_CODE_INVALID);
        }
    }

    private String normalizeCode(String code) {
        return StringUtils.lowerCase(StringUtils.trimToEmpty(code));
    }

    private int normalizeStatus(Integer status) {
        return Objects.equals(status, STATUS_ENABLED) ? STATUS_ENABLED : STATUS_DISABLED;
    }

    private ScenicCategoryVo toVo(ScenicCategoryEntity entity) {
        ScenicCategoryVo vo = new ScenicCategoryVo();
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

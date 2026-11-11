package com.sqx.app.lvyou.help.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.help.db.service.IHelpFaqService;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqInfoBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqPageBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqSaveBo;
import com.sqx.app.lvyou.help.domain.bo.HelpFaqUpdateBo;
import com.sqx.app.lvyou.help.domain.entity.HelpFaqEntity;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqListVo;
import com.sqx.app.lvyou.help.domain.vo.HelpFaqVo;
import com.sqx.app.lvyou.help.enums.HelpFaqCategory;
import com.sqx.app.lvyou.help.enums.HelpFaqState;
import com.sqx.app.lvyou.help.service.HelpFaqService;
import com.sqx.app.lvyou.help.util.HelpFaqGroupUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 帮助中心 FAQ 业务服务实现
 */
@Service
public class HelpFaqServiceImpl implements HelpFaqService {

    private static final int ENABLED = HelpFaqState.ENABLED.getCode();

    @Resource
    private IHelpFaqService iHelpFaqService;

    @Override
    public HelpFaqListVo appList() {
        LambdaQueryWrapper<HelpFaqEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(HelpFaqEntity::getState, ENABLED)
                .orderByAsc(HelpFaqEntity::getCategoryCode)
                .orderByAsc(HelpFaqEntity::getSort)
                .orderByAsc(HelpFaqEntity::getId);
        List<HelpFaqEntity> entities = iHelpFaqService.list(queryWrapper);
        return HelpFaqGroupUtil.groupByCategory(entities);
    }

    @Override
    public IPage<HelpFaqVo> pageInfo(HelpFaqPageBo pageBo) {
        Page<HelpFaqEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<HelpFaqEntity> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.isNotBlank(pageBo.getCategoryCode())) {
            queryWrapper.eq(HelpFaqEntity::getCategoryCode, pageBo.getCategoryCode().trim());
        }
        if (StringUtils.isNotBlank(pageBo.getQuestion())) {
            queryWrapper.like(HelpFaqEntity::getQuestion, pageBo.getQuestion().trim());
        }
        if (pageBo.getState() != null) {
            queryWrapper.eq(HelpFaqEntity::getState, pageBo.getState());
        }
        queryWrapper.orderByAsc(HelpFaqEntity::getCategoryCode)
                .orderByAsc(HelpFaqEntity::getSort)
                .orderByDesc(HelpFaqEntity::getId);

        IPage<HelpFaqEntity> entityPage = iHelpFaqService.page(page, queryWrapper);
        List<HelpFaqVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (HelpFaqEntity record : entityPage.getRecords()) {
                voList.add(toVo(record));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(HelpFaqSaveBo saveBo) {
        HelpFaqEntity entity = buildEntity(saveBo);
        iHelpFaqService.save(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(HelpFaqUpdateBo updateBo) {
        HelpFaqEntity existing = requireEntity(updateBo.getId());
        HelpFaqEntity entity = buildEntity(updateBo);
        entity.setId(existing.getId());
        iHelpFaqService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(HelpFaqInfoBo infoBo) {
        requireEntity(infoBo.getId());
        iHelpFaqService.removeById(infoBo.getId());
    }

    private HelpFaqEntity requireEntity(Long id) {
        HelpFaqEntity entity = iHelpFaqService.getById(id);
        if (entity == null) {
            throw new BizException(AppError.HELP_FAQ_NOT_FOUND);
        }
        return entity;
    }

    private HelpFaqEntity buildEntity(HelpFaqSaveBo saveBo) {
        String categoryCode = StringUtils.trimToEmpty(saveBo.getCategoryCode());
        if (!HelpFaqCategory.isValid(categoryCode)) {
            throw new BizException(AppError.HELP_FAQ_CATEGORY_INVALID);
        }
        if (!HelpFaqState.isValid(saveBo.getState())) {
            throw new BizException(AppError.HELP_FAQ_STATE_INVALID);
        }

        HelpFaqEntity entity = new HelpFaqEntity();
        entity.setCategoryCode(categoryCode);
        entity.setCategoryName(HelpFaqCategory.resolveLabel(categoryCode));
        entity.setQuestion(StringUtils.trim(saveBo.getQuestion()));
        entity.setAnswer(StringUtils.trim(saveBo.getAnswer()));
        entity.setSort(saveBo.getSort() == null ? 0 : saveBo.getSort());
        entity.setState(saveBo.getState());
        return entity;
    }

    private HelpFaqVo toVo(HelpFaqEntity entity) {
        HelpFaqVo vo = new HelpFaqVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setStateLabel(HelpFaqState.resolveLabel(entity.getState()));
        if (StringUtils.isBlank(vo.getCategoryName())) {
            vo.setCategoryName(HelpFaqCategory.resolveLabel(entity.getCategoryCode()));
        }
        return vo;
    }
}

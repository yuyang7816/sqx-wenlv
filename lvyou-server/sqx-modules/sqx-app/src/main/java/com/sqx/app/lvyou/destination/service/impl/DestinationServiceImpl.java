package com.sqx.app.lvyou.destination.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.lvyou.destination.db.service.IDestinationService;
import com.sqx.app.lvyou.destination.domain.bo.*;
import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import com.sqx.app.lvyou.merchant.db.service.IMerchantService;
import com.sqx.app.lvyou.merchant.domain.entity.MerchantEntity;
import com.sqx.app.lvyou.merchant.enums.MerchantStoreType;
import com.sqx.app.lvyou.destination.domain.vo.DestinationAppVo;
import com.sqx.app.lvyou.destination.domain.vo.DestinationMerchantVo;
import com.sqx.app.lvyou.destination.domain.vo.DestinationVo;
import com.sqx.app.lvyou.destination.enums.DestinationStatus;
import com.sqx.app.lvyou.destination.service.DestinationService;
import com.sqx.app.lvyou.destination.support.DestinationCascadeSupport;
import com.sqx.app.lvyou.destination.support.DestinationSupport;
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
import java.util.Objects;

/**
 * 目的地业务服务实现
 */
@Service
public class DestinationServiceImpl implements DestinationService {

    private static final int STATUS_ONLINE = DestinationStatus.ONLINE.getCode();

    @Resource
    private IDestinationService iDestinationService;

    @Resource
    private IUserService iUserService;

    @Resource
    private IMerchantService iMerchantService;

    @Resource
    private DestinationSupport destinationSupport;

    @Resource
    private DestinationCascadeSupport destinationCascadeSupport;

    private static final int MERCHANT_STATUS_NORMAL = 1;

    @Override
    public List<DestinationAppVo> listOnline() {
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getStatus, STATUS_ONLINE)
                .orderByAsc(DestinationEntity::getSort)
                .orderByAsc(DestinationEntity::getId);
        List<DestinationEntity> entities = iDestinationService.list(queryWrapper);
        List<DestinationAppVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (DestinationEntity entity : entities) {
                voList.add(toAppVo(entity));
            }
        }
        return voList;
    }

    @Override
    public DestinationAppVo getCurrent(String lat, String lng) {
        Long destinationId = destinationSupport.resolveAppDestinationId(lat, lng);
        DestinationEntity destination = iDestinationService.getById(destinationId);
        if (destination == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        return toAppVo(destination);
    }

    @Override
    public List<DestinationMerchantVo> listCurrentMerchants(String lat, String lng) {
        Long destinationId = destinationSupport.resolveAppDestinationId(lat, lng);
        if (destinationId == null) {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<MerchantEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MerchantEntity::getDestinationId, destinationId)
                .eq(MerchantEntity::getStatus, MERCHANT_STATUS_NORMAL)
                .orderByAsc(MerchantEntity::getStoreName)
                .orderByAsc(MerchantEntity::getId);
        List<MerchantEntity> merchants = iMerchantService.list(queryWrapper);
        List<DestinationMerchantVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(merchants)) {
            for (MerchantEntity merchant : merchants) {
                voList.add(toMerchantVo(merchant));
            }
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DestinationAppVo switchDestination(Long userId, DestinationSwitchBo switchBo) {
        DestinationEntity destination = getByDestinationCode(switchBo.getDestinationCode());
        if (destination == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        if (!Objects.equals(destination.getStatus(), STATUS_ONLINE)) {
            throw new BizException(AppError.DESTINATION_NOT_ONLINE);
        }

        LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserEntity::getUserId, userId)
                .set(UserEntity::getCurrentDestinationId, destination.getId());
        iUserService.update(new UserEntity(), updateWrapper);

        return toAppVo(destination);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveInfo(DestinationSaveBo saveBo) {
        ensureDestinationCodeUnique(saveBo.getDestinationCode(), null);
        if (Objects.equals(saveBo.getIsDefault(), 1)) {
            clearDefaultFlag(null);
        }

        DestinationEntity entity = new DestinationEntity();
        BeanUtils.copyProperties(saveBo, entity);
        if (entity.getMerchantCount() == null) {
            entity.setMerchantCount(0);
        }
        if (entity.getResourceCount() == null) {
            entity.setResourceCount(0);
        }
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getIsDefault() == null) {
            entity.setIsDefault(0);
        }
        iDestinationService.save(entity);
    }

    @Override
    public IPage<DestinationVo> pageInfo(DestinationPageBo pageBo) {
        Page<DestinationEntity> page = PageUtil.initPage(pageBo.getPage(), pageBo.getLimit());
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotBlank(pageBo.getName()), DestinationEntity::getName, pageBo.getName())
                .eq(Objects.nonNull(pageBo.getId()), DestinationEntity::getId, pageBo.getId())
                .eq(StringUtils.isNotBlank(pageBo.getDestinationCode()), DestinationEntity::getDestinationCode, pageBo.getDestinationCode())
                .eq(Objects.nonNull(pageBo.getStatus()), DestinationEntity::getStatus, pageBo.getStatus())
                .orderByAsc(DestinationEntity::getSort)
                .orderByDesc(DestinationEntity::getCreateTime);

        IPage<DestinationEntity> entityPage = iDestinationService.page(page, queryWrapper);
        List<DestinationVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entityPage.getRecords())) {
            for (DestinationEntity record : entityPage.getRecords()) {
                voList.add(toAdminVo(record));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateInfo(DestinationUpdateBo updateBo) {
        DestinationEntity existing = iDestinationService.getById(updateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        ensureDestinationCodeUnique(updateBo.getDestinationCode(), updateBo.getId());
        if (Objects.equals(updateBo.getIsDefault(), 1)) {
            clearDefaultFlag(updateBo.getId());
        }

        DestinationEntity entity = new DestinationEntity();
        BeanUtils.copyProperties(updateBo, entity);
        iDestinationService.updateById(entity);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(DestinationInfoBo infoBo) {
        DestinationEntity existing = iDestinationService.getById(infoBo.getId());
        if (existing == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        Long deletedId = infoBo.getId();
        iDestinationService.removeById(deletedId);
        // 先删目的地，再解绑/重匹配引用，避免其它列表仍展示已删目的地
        destinationCascadeSupport.rebindAfterDelete(deletedId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateState(DestinationUpdateStateBo updateStateBo) {
        DestinationEntity existing = iDestinationService.getById(updateStateBo.getId());
        if (existing == null) {
            throw new BizException(AppError.DESTINATION_NOT_FOUND);
        }
        LambdaUpdateWrapper<DestinationEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(DestinationEntity::getId, updateStateBo.getId())
                .set(DestinationEntity::getStatus, updateStateBo.getStatus());
        iDestinationService.update(new DestinationEntity(), updateWrapper);
    }

    private DestinationEntity resolveCurrentDestination(Long currentDestinationId) {
        if (currentDestinationId != null) {
            DestinationEntity destination = iDestinationService.getById(currentDestinationId);
            if (destination != null) {
                return destination;
            }
        }
        return getDefaultDestination();
    }

    private DestinationEntity getDefaultDestination() {
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getIsDefault, 1)
                .orderByAsc(DestinationEntity::getSort)
                .last("LIMIT 1");
        DestinationEntity destination = iDestinationService.getOne(queryWrapper, false);
        if (destination != null) {
            return destination;
        }
        queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getStatus, STATUS_ONLINE)
                .orderByAsc(DestinationEntity::getSort)
                .last("LIMIT 1");
        destination = iDestinationService.getOne(queryWrapper, false);
        if (destination != null) {
            return destination;
        }
        return emptyDestination();
    }

    private DestinationEntity emptyDestination() {
        DestinationEntity entity = new DestinationEntity();
        entity.setDestinationCode("");
        entity.setName("");
        entity.setShortName("");
        entity.setIntro("");
        entity.setStatus(STATUS_ONLINE);
        entity.setIsDefault(0);
        entity.setMerchantCount(0);
        entity.setResourceCount(0);
        return entity;
    }

    private DestinationEntity getByDestinationCode(String destinationCode) {
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getDestinationCode, destinationCode);
        return iDestinationService.getOne(queryWrapper, false);
    }

    private void ensureDestinationCodeUnique(String destinationCode, Long excludeId) {
        LambdaQueryWrapper<DestinationEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DestinationEntity::getDestinationCode, destinationCode);
        if (excludeId != null) {
            queryWrapper.ne(DestinationEntity::getId, excludeId);
        }
        if (iDestinationService.count(queryWrapper) > 0) {
            throw new BizException(AppError.DESTINATION_CODE_DUPLICATE);
        }
    }

    private void clearDefaultFlag(Long excludeId) {
        LambdaUpdateWrapper<DestinationEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.set(DestinationEntity::getIsDefault, 0);
        if (excludeId != null) {
            updateWrapper.ne(DestinationEntity::getId, excludeId);
        }
        iDestinationService.update(new DestinationEntity(), updateWrapper);
    }

    private DestinationAppVo toAppVo(DestinationEntity entity) {
        DestinationAppVo vo = new DestinationAppVo();
        vo.setId(entity.getDestinationCode());
        vo.setName(entity.getName());
        vo.setShortName(entity.getShortName());
        vo.setIntro(entity.getIntro());
        vo.setStatus(DestinationStatus.labelOf(entity.getStatus()));
        vo.setDefaultDestination(Objects.equals(entity.getIsDefault(), 1));
        vo.setMerchantCount(entity.getMerchantCount() == null ? 0 : entity.getMerchantCount());
        vo.setResourceCount(entity.getResourceCount() == null ? 0 : entity.getResourceCount());
        return vo;
    }

    private DestinationVo toAdminVo(DestinationEntity entity) {
        DestinationVo vo = new DestinationVo();
        BeanUtils.copyProperties(entity, vo);
        vo.setStatusLabel(DestinationStatus.labelOf(entity.getStatus()));
        return vo;
    }

    private DestinationMerchantVo toMerchantVo(MerchantEntity merchant) {
        String typeKey = MerchantStoreType.normalize(merchant.getStoreType());
        DestinationMerchantVo vo = new DestinationMerchantVo();
        vo.setId(merchant.getMerchantCode());
        vo.setTypeKey(typeKey);
        vo.setName(merchant.getStoreName());
        vo.setType(MerchantStoreType.labelOf(typeKey));
        vo.setAddress(StringUtils.defaultIfBlank(merchant.getStoreAddress(), merchant.getStoreRegion()));
        vo.setService(serviceLabelOf(typeKey));
        return vo;
    }

    private String serviceLabelOf(String typeKey) {
        if ("hotel".equals(typeKey)) {
            return "商户自营预订";
        }
        if ("restaurant".equals(typeKey)) {
            return "商户预约/到店";
        }
        if ("travel".equals(typeKey)) {
            return "线路预约";
        }
        return "商户预约";
    }
}

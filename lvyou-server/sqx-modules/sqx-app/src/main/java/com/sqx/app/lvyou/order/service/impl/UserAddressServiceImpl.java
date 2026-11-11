package com.sqx.app.lvyou.order.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.lvyou.order.db.mapper.UserAddressMapper;
import com.sqx.app.lvyou.order.domain.bo.AddressDeleteBo;
import com.sqx.app.lvyou.order.domain.bo.AddressSaveBo;
import com.sqx.app.lvyou.order.domain.entity.UserAddressEntity;
import com.sqx.app.lvyou.order.domain.vo.AddressVo;
import com.sqx.app.lvyou.order.service.UserAddressService;
import com.sqx.app.lvyou.order.util.AddressRegionUtil;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 用户收货地址业务服务实现
 */
@Service
public class UserAddressServiceImpl implements UserAddressService {

    private static final int DEFAULT_YES = 1;
    private static final int DEFAULT_NO = 0;

    @Resource
    private UserAddressMapper userAddressMapper;

    @Override
    public List<AddressVo> listByUser(Long userId) {
        LambdaQueryWrapper<UserAddressEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddressEntity::getUserId, userId)
                .orderByDesc(UserAddressEntity::getIsDefault)
                .orderByDesc(UserAddressEntity::getUpdateTime)
                .orderByDesc(UserAddressEntity::getId);

        List<UserAddressEntity> entities = userAddressMapper.selectList(queryWrapper);
        List<AddressVo> voList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(entities)) {
            for (UserAddressEntity entity : entities) {
                voList.add(toVo(entity));
            }
        }
        return voList;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long save(Long userId, AddressSaveBo saveBo) {
        AddressRegionUtil.RegionParts regionParts = AddressRegionUtil.parseRegion(saveBo.getRegion());
        boolean setDefault = Boolean.TRUE.equals(saveBo.getIsDefault());

        if (saveBo.getId() != null) {
            UserAddressEntity existing = requireAddress(userId, saveBo.getId());
            if (setDefault) {
                clearDefault(userId, existing.getId());
            }
            UserAddressEntity entity = buildEntity(userId, saveBo, regionParts, setDefault, existing);
            userAddressMapper.updateById(entity);
            ensureDefaultExists(userId);
            return existing.getId();
        }

        if (setDefault) {
            clearDefault(userId, null);
        } else {
            setDefault = !hasAnyAddress(userId);
        }

        UserAddressEntity entity = buildEntity(userId, saveBo, regionParts, setDefault, null);
        userAddressMapper.insert(entity);
        return entity.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long userId, AddressDeleteBo deleteBo) {
        UserAddressEntity existing = requireAddress(userId, deleteBo.getId());
        userAddressMapper.deleteById(existing.getId());
        ensureDefaultExists(userId);
    }

    private UserAddressEntity requireAddress(Long userId, Long addressId) {
        UserAddressEntity entity = userAddressMapper.selectById(addressId);
        if (entity == null || !Objects.equals(entity.getUserId(), userId)) {
            throw new BizException(AppError.ORDER_ADDRESS_NOT_FOUND);
        }
        return entity;
    }

    private UserAddressEntity buildEntity(Long userId, AddressSaveBo saveBo,
                                          AddressRegionUtil.RegionParts regionParts,
                                          boolean setDefault, UserAddressEntity existing) {
        UserAddressEntity entity = existing != null ? existing : new UserAddressEntity();
        entity.setUserId(userId);
        entity.setReceiverName(saveBo.getName().trim());
        entity.setReceiverPhone(saveBo.getPhone().trim());
        entity.setProvince(regionParts.getProvince());
        entity.setCity(regionParts.getCity());
        entity.setDistrict(regionParts.getDistrict());
        entity.setDetailAddress(saveBo.getDetail().trim());
        entity.setIsDefault(setDefault ? DEFAULT_YES : DEFAULT_NO);
        return entity;
    }

    private void clearDefault(Long userId, Long excludeId) {
        LambdaUpdateWrapper<UserAddressEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserAddressEntity::getUserId, userId)
                .eq(UserAddressEntity::getIsDefault, DEFAULT_YES)
                .ne(excludeId != null, UserAddressEntity::getId, excludeId)
                .set(UserAddressEntity::getIsDefault, DEFAULT_NO);
        userAddressMapper.update(null, updateWrapper);
    }

    private boolean hasAnyAddress(Long userId) {
        LambdaQueryWrapper<UserAddressEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddressEntity::getUserId, userId);
        return userAddressMapper.selectCount(queryWrapper) > 0;
    }

    private void ensureDefaultExists(Long userId) {
        LambdaQueryWrapper<UserAddressEntity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(UserAddressEntity::getUserId, userId)
                .orderByDesc(UserAddressEntity::getUpdateTime)
                .orderByDesc(UserAddressEntity::getId);
        List<UserAddressEntity> entities = userAddressMapper.selectList(queryWrapper);
        if (CollectionUtils.isEmpty(entities)) {
            return;
        }
        boolean hasDefault = entities.stream()
                .anyMatch(item -> Objects.equals(item.getIsDefault(), DEFAULT_YES));
        if (hasDefault) {
            return;
        }
        UserAddressEntity first = entities.get(0);
        first.setIsDefault(DEFAULT_YES);
        userAddressMapper.updateById(first);
    }

    private AddressVo toVo(UserAddressEntity entity) {
        AddressVo vo = new AddressVo();
        vo.setId(entity.getId());
        vo.setName(entity.getReceiverName());
        vo.setPhone(entity.getReceiverPhone());
        vo.setRegion(AddressRegionUtil.formatRegion(entity.getProvince(), entity.getCity(), entity.getDistrict()));
        vo.setDetail(entity.getDetailAddress());
        vo.setIsDefault(Objects.equals(entity.getIsDefault(), DEFAULT_YES));
        return vo;
    }
}

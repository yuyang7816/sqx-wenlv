package com.sqx.app.base.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.bo.DeleteUserBO;
import com.sqx.app.base.user.domain.bo.ForgetPasswordBO;
import com.sqx.app.base.user.domain.bo.ListUserBO;
import com.sqx.app.base.user.domain.bo.UpdateUserBO;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.domain.vo.UserVo;
import com.sqx.app.base.user.service.UserService;
import com.sqx.app.base.utils.AvatarSupport;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.app.lvyou.invite.support.InviteSupport;
import com.sqx.app.lvyou.wallet.service.WalletService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.mybatis.utils.PageUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/4/24
 */
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private IUserService iUserService;
    @Resource
    private VerifyCodeService verifyCodeService;
    @Resource
    private WalletService walletService;

    @Override
    public Result<UserVo> info(Long userId) {
        UserEntity userEntity = iUserService.getById(userId);
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        UserVo vo = toUserVo(userEntity);
        return Result.success(vo);
    }

    @Override
    public Result<Void> forgetPassword(ForgetPasswordBO forgetPasswordBO) {
        UserEntity userEntity = iUserService.getById(forgetPasswordBO.getUserId());
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        if (!verifyCodeService.verify(userEntity.getPhone(), forgetPasswordBO.getCode())) {
            throw new BizException(AppError.VERIFY_CODE_ERROR);
        }

        iUserService.update(new UserEntity(), new LambdaUpdateWrapper<UserEntity>()
                .eq(UserEntity::getUserId, forgetPasswordBO.getUserId())
                .set(UserEntity::getPassword, DigestUtils.md5Hex(forgetPasswordBO.getPassword()))
        );

        return Result.success();
    }

    @Override
    public IPage<UserVo> list(ListUserBO listUserBO) {
        Page<UserEntity> initPage = PageUtil.initPage(listUserBO.getPage(), listUserBO.getLimit());

        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(StringUtils.isNotBlank(listUserBO.getPhone()), UserEntity::getPhone, listUserBO.getPhone())
                .eq(StringUtils.isNotBlank(listUserBO.getUsername()), UserEntity::getUserName, listUserBO.getUsername())
                .eq(Objects.nonNull(listUserBO.getStatus()), UserEntity::getStatus, listUserBO.getStatus())
                .eq(Objects.nonNull(listUserBO.getUserType()), UserEntity::getUserType, listUserBO.getUserType())
                .orderByDesc(UserEntity::getCreateTime);

        Page<UserEntity> entityPage = iUserService.page(initPage, queryWrapper);
        List<UserVo> voList = new ArrayList<>();
        if (entityPage.getRecords() != null) {
            for (UserEntity entity : entityPage.getRecords()) {
                voList.add(toUserVo(entity));
            }
        }
        return PageUtil.copyPage(entityPage, voList);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(UpdateUserBO updateUserBO) {
        UserEntity existing = iUserService.getById(updateUserBO.getUserId());
        if (existing == null) {
            throw new BizException(AppError.USER_NOT_FOUND);
        }

        LambdaUpdateWrapper<UserEntity> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(UserEntity::getUserId, updateUserBO.getUserId());
        boolean hasProfileUpdate = false;
        if (StringUtils.isNotBlank(updateUserBO.getUserName())) {
            updateWrapper.set(UserEntity::getUserName, updateUserBO.getUserName().trim());
            hasProfileUpdate = true;
        }
        if (updateUserBO.getStatus() != null) {
            updateWrapper.set(UserEntity::getStatus, updateUserBO.getStatus());
            hasProfileUpdate = true;
        }
        if (StringUtils.isNotBlank(updateUserBO.getAvatar())) {
            updateWrapper.set(UserEntity::getAvatar, updateUserBO.getAvatar());
            hasProfileUpdate = true;
        }
        if (updateUserBO.getSex() != null) {
            updateWrapper.set(UserEntity::getSex, updateUserBO.getSex());
            hasProfileUpdate = true;
        }
        if (updateUserBO.getAge() != null) {
            updateWrapper.set(UserEntity::getAge, updateUserBO.getAge());
            hasProfileUpdate = true;
        }
        if (hasProfileUpdate) {
            iUserService.update(updateWrapper);
        }

        if (updateUserBO.getCommissionRateLevel1() != null) {
            iUserService.update(new LambdaUpdateWrapper<UserEntity>()
                    .eq(UserEntity::getUserId, updateUserBO.getUserId())
                    .set(UserEntity::getCommissionRateLevel1,
                            InviteSupport.parseStoredRate(updateUserBO.getCommissionRateLevel1())));
        }
        if (updateUserBO.getCommissionRateLevel2() != null) {
            iUserService.update(new LambdaUpdateWrapper<UserEntity>()
                    .eq(UserEntity::getUserId, updateUserBO.getUserId())
                    .set(UserEntity::getCommissionRateLevel2,
                            InviteSupport.parseStoredRate(updateUserBO.getCommissionRateLevel2())));
        }

        if (StringUtils.isNotBlank(updateUserBO.getWalletAdjustType())
                && StringUtils.isNotBlank(updateUserBO.getWalletAdjustAmount())) {
            BigDecimal amount = new BigDecimal(updateUserBO.getWalletAdjustAmount().trim());
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new BizException("调整金额必须大于0");
            }
            boolean decrease = "decrease".equalsIgnoreCase(updateUserBO.getWalletAdjustType().trim());
            BigDecimal delta = decrease ? amount.negate() : amount;
            String remark = decrease ? "管理员减少余额" : "管理员增加余额";
            walletService.adminAdjustBalance(updateUserBO.getUserId(), delta, remark);
        } else if (updateUserBO.getBalance() != null) {
            walletService.adminSetBalance(updateUserBO.getUserId(),
                    new BigDecimal(updateUserBO.getBalance().trim()), "管理员调整余额");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public void delete(DeleteUserBO deleteUserBO) {
        iUserService.removeById(deleteUserBO.getUserId());

        // 其他需要删除的信息
    }

    private UserVo toUserVo(UserEntity userEntity) {
        UserVo vo = new UserVo();
        BeanUtils.copyProperties(userEntity, vo);
        vo.setAvatar(AvatarSupport.resolve(userEntity.getAvatar()));
        vo.setBalance(walletService.getSummary(userEntity.getUserId()).getBalance());
        vo.setDefaultCommissionRateLevel1(InviteSupport.formatRatePlain(resolveDefaultRateLevel1()));
        vo.setDefaultCommissionRateLevel2(InviteSupport.formatRatePlain(resolveDefaultRateLevel2()));
        return vo;
    }

    private BigDecimal resolveDefaultRateLevel1() {
        return InviteSupport.parseRate(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_COMMISSION_RATE_LEVEL1),
                InviteSupport.defaultRateLevel1());
    }

    private BigDecimal resolveDefaultRateLevel2() {
        return InviteSupport.parseRate(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_COMMISSION_RATE_LEVEL2),
                InviteSupport.defaultRateLevel2());
    }

    private String getOptionalConfigValue(String key) {
        try {
            return SysConfigUtil.getConfigValue(key);
        } catch (Exception ex) {
            return null;
        }
    }
}

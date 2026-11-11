package com.sqx.app.base.auth.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sqx.app.base.auth.domain.bo.*;
import com.sqx.app.base.auth.domain.vo.LoginVO;
import com.sqx.app.base.auth.domain.vo.PlatformAuthConfigVo;
import com.sqx.app.base.auth.domain.vo.WxLoginVo;
import com.sqx.app.base.auth.service.AppAuthService;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.user.db.service.IUserService;
import com.sqx.app.base.user.domain.entity.UserEntity;
import com.sqx.app.base.user.enums.UserStatus;
import com.sqx.app.base.user.enums.UserType;
import com.sqx.app.base.utils.RequestUtil;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.app.base.utils.WeChatMaConfigSupport;
import com.sqx.app.base.utils.wx.WxJsLoginRes;
import com.sqx.app.base.utils.wx.WxMpOAuthRes;
import com.sqx.app.base.utils.wx.WxUserPhone;
import com.sqx.app.base.utils.wx.WxUtil;
import com.sqx.app.base.verifyCode.service.VerifyCodeService;
import com.sqx.app.lvyou.config.LvyouAppProperties;
import com.sqx.app.lvyou.invite.service.InviteService;
import com.sqx.app.lvyou.wallet.service.WalletService;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import com.sqx.common.core.utils.Md5Utils;
import com.sqx.common.shiro.utils.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/4/22
 */
@Service
public class AppAuthServiceImpl implements AppAuthService {

    @Resource
    private IUserService iUserService;
    @Resource
    private JwtUtil jwtUtil;
    @Resource
    private VerifyCodeService verifyCodeService;
    @Resource
    private WxUtil wxUtil;
    @Resource
    private WalletService walletService;
    @Resource
    private InviteService inviteService;
    @Resource
    private LvyouAppProperties lvyouAppProperties;

    @Override
    public Result<LoginVO> phonePasswordLogin(PhonePasswordLoginBO phonePasswordLoginBO) {
        UserEntity userEntity = getUserByPhone(phonePasswordLoginBO.getPhoneNo());
        if (Objects.isNull(userEntity)) {
            throw new BizException(AppError.PHONE_NOT_REGISTER);
        }

        if (1 != userEntity.getStatus()) {
            throw new BizException(AppError.ACCOUNT_ABNORMAL);
        }

        String encryptPassword = Md5Utils.md5s(phonePasswordLoginBO.getPassword());

        if (!Objects.equals(encryptPassword, userEntity.getPassword())) {
            throw new BizException(AppError.PHONE_OR_PASSWORD_ERROR);
        }

        LoginVO vo = buildLoginResult(userEntity);
        return Result.success(vo);
    }

    @Override
    public Result<LoginVO> phoneCodeLogin(PhoneCodeLoginBO phoneCodeLoginBO) {
        // 校验验证码
        if (!verifyCodeService.verify(phoneCodeLoginBO.getPhoneNo(), phoneCodeLoginBO.getVerifyCode())) {
            throw new BizException(AppError.VERIFY_CODE_ERROR);
        }

        UserEntity userEntity = getUserByPhone(phoneCodeLoginBO.getPhoneNo());
        if (Objects.isNull(userEntity)) {
            // 注册流程
            UserEntity userInfo = registerPhone(phoneCodeLoginBO.getPhoneNo(), null, null, null, null,
                    phoneCodeLoginBO.getSuperiorInvitationCode());
            LoginVO vo = buildLoginResult(userInfo);
            return Result.success(vo);
        }

        if (1 != userEntity.getStatus()) {
            throw new BizException(AppError.ACCOUNT_ABNORMAL);
        }

        LoginVO vo = buildLoginResult(userEntity);
        return Result.success(vo);
    }

    @Override
    public Result<LoginVO> phoneRegister(PhoneRegisterBO phoneRegisterBO) {
        if (!verifyCodeService.verify(phoneRegisterBO.getPhoneNo(), phoneRegisterBO.getCode())) {
            throw new BizException(AppError.VERIFY_CODE_ERROR);
        }

        UserEntity userInfo = registerPhone(phoneRegisterBO.getPhoneNo(), phoneRegisterBO.getPassword(), phoneRegisterBO.getUsername(), phoneRegisterBO.getSysPhone(), phoneRegisterBO.getPlatform(), phoneRegisterBO.getSuperiorInvitationCode());

        LoginVO vo = buildLoginResult(userInfo);
        return Result.success(vo);
    }

    @Override
    public Result<Void> resetPassword(ResetPasswordBO resetPasswordBO) {
        if (!verifyCodeService.verify(resetPasswordBO.getPhone(), resetPasswordBO.getCode())) {
            throw new BizException(AppError.VERIFY_CODE_ERROR);
        }

        if (!checkPhoneExist(resetPasswordBO.getPhone())) {
            throw new BizException(AppError.PHONE_NOT_REGISTER);
        }

        UpdateWrapper<UserEntity> updateWrapper = new UpdateWrapper<>();
        updateWrapper.lambda()
                .eq(UserEntity::getPhone, resetPasswordBO.getPhone())
                .set(UserEntity::getPassword, Md5Utils.md5s(resetPasswordBO.getPassword()));

        iUserService.update(new UserEntity(), updateWrapper);

        return Result.success();
    }

    private boolean checkPhoneExist(String phone) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getPhone, phone);
        return iUserService.exists(queryWrapper);
    }

    /**
     * 手机号注册
     * @param phoneNo
     * @param sysPhone
     * @param platform
     * @param superiorInvitationCode
     * @return
     */
    private UserEntity registerPhone(String phoneNo, String password, String username, Integer sysPhone, String platform, String superiorInvitationCode) {
        UserEntity userEntity = getUserByPhone(phoneNo);
        if (Objects.nonNull(userEntity)) {
            throw new BizException(AppError.PHONE_REGISTERED);
        }

        UserEntity userInfo = new UserEntity();
        userInfo.setUserName(StringUtils.isNotBlank(username) ? username : phoneNo);
        userInfo.setPhone(phoneNo);
        userInfo.setSysPhone(sysPhone);
        userInfo.setStatus(UserStatus.ENABLE.getCode());
        userInfo.setPlatform(platform);
        userInfo.setUserType(UserType.USER.getCode());
        userInfo.setIsVip(0);
        userInfo.setInvitationCode(GenUtil.genInviteCode(phoneNo));
        userInfo.setSuperiorInvitationCode(superiorInvitationCode);

        if (StringUtils.isNotBlank(password)) {
            String encryptPassword = Md5Utils.md5s(password);
            userInfo.setPassword(encryptPassword);
        }

        iUserService.save(userInfo);

        /* 其他业务 */
        registerBiz(userInfo.getUserId(), superiorInvitationCode);

        return userInfo;
    }

    private void registerBiz(Long userId, String superiorInvitationCode) {
        walletService.ensureWallet(userId);
        inviteService.bindRelationOnRegister(userId, superiorInvitationCode);
    }

    @Override
    public Result<WxLoginVo> wxLogin(String code) {
        String appId = WeChatMaConfigSupport.resolveMaAppId();
        String secret = WeChatMaConfigSupport.resolveMaSecret();
        WxJsLoginRes wxJsLoginRes = wxUtil.wxJsLogin(appId, secret, code);

        WxLoginVo vo = new WxLoginVo();
        vo.setSessionKey(wxJsLoginRes.getSessionKey());
        vo.setUnionid(wxJsLoginRes.getUnionid());
        vo.setErrmsg(wxJsLoginRes.getErrmsg());
        vo.setOpenid(wxJsLoginRes.getOpenid());
        vo.setErrmsg(wxJsLoginRes.getErrmsg());

        // 是否绑定手机号
        UserEntity userEntity = iUserService.getByOpenId(wxJsLoginRes.getOpenid());
        if (Objects.isNull(userEntity) || StringUtils.isBlank(userEntity.getPhone())) {
            vo.setBindPhone(false);
        } else {
            LoginVO loginVO = buildLoginResult(userEntity);
            vo.setLoginVO(loginVO);
            vo.setUserEntity(userEntity);
            vo.setBindPhone(true);
        }

        return Result.success(vo);
    }

    @Override
    public Result<WxLoginVo> wxMpLogin(String code) {
        if (!lvyouAppProperties.isMpLoginEnabled()) {
            throw new BizException("公众号登录未开启");
        }
        String appId = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_MP_APPID, "");
        String secret = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_MP_SECRET, "");
        WxMpOAuthRes oauthRes = wxUtil.wxMpOAuth(appId, secret, code);
        return Result.success(buildOAuthLoginVo(oauthRes.getOpenid(), oauthRes.getUnionid(), true));
    }

    @Override
    public Result<WxLoginVo> wxAppLogin(String code) {
        String appId = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_APP_WX_APPID, "");
        String secret = SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_APP_WX_SECRET, "");
        if (StringUtils.isBlank(appId) || StringUtils.isBlank(secret)) {
            appId = SysConfigUtil.getConfigValue("pay_wx_app_id", "");
        }
        WxMpOAuthRes oauthRes = wxUtil.wxMpOAuth(appId, secret, code);
        return Result.success(buildOAuthLoginVo(oauthRes.getOpenid(), oauthRes.getUnionid(), false));
    }

    @Override
    public Result<PlatformAuthConfigVo> platformAuthConfig() {
        PlatformAuthConfigVo vo = new PlatformAuthConfigVo();
        vo.setMaAppId(WeChatMaConfigSupport.resolveMaAppId());
        vo.setMpAppId(SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_MP_APPID, ""));
        vo.setAppWxAppId(StringUtils.defaultIfBlank(
                SysConfigUtil.getConfigValue(SysConfigConstant.Sys.SYS_USER_APP_WX_APPID, ""),
                SysConfigUtil.getConfigValue("pay_wx_app_id", "")));
        vo.setMpLoginEnabled(lvyouAppProperties.isMpLoginEnabled());
        vo.setWechatMpEnabled(SysConfigUtil.getBooleanConfigValue(SysConfigConstant.Kefu.WECHAT_MP_ENABLED, false));
        return Result.success(vo);
    }

    @Override
    public Result<LoginVO> wxMpRegister(WxMpRegisterBo wxMpRegisterBo) {
        if (!lvyouAppProperties.isMpLoginEnabled()) {
            throw new BizException("公众号登录未开启");
        }
        UserEntity userEntity = iUserService.getByMpOpenId(wxMpRegisterBo.getOpenId());
        if (Objects.nonNull(userEntity)) {
            if (StringUtils.isBlank(userEntity.getPhone()) && StringUtils.isNotBlank(wxMpRegisterBo.getPhone())) {
                userEntity.setPhone(wxMpRegisterBo.getPhone());
                iUserService.updateById(userEntity);
            }
            return Result.success(buildLoginResult(userEntity));
        }

        UserEntity userByPhone = getUserByPhone(wxMpRegisterBo.getPhone());
        if (Objects.nonNull(userByPhone)) {
            if (StringUtils.isBlank(userByPhone.getMpOpenId())) {
                userByPhone.setMpOpenId(wxMpRegisterBo.getOpenId());
                iUserService.updateById(userByPhone);
            }
            return Result.success(buildLoginResult(userByPhone));
        }

        UserEntity userInfo = new UserEntity();
        userInfo.setUserName(StringUtils.defaultIfBlank(wxMpRegisterBo.getUsername(), wxMpRegisterBo.getPhone()));
        userInfo.setAvatar(wxMpRegisterBo.getAvatar());
        userInfo.setPhone(wxMpRegisterBo.getPhone());
        userInfo.setStatus(UserStatus.ENABLE.getCode());
        userInfo.setPlatform("MP");
        userInfo.setUserType(UserType.USER.getCode());
        userInfo.setIsVip(0);
        userInfo.setInvitationCode(GenUtil.genInviteCode(wxMpRegisterBo.getPhone()));
        String inviteCode = StringUtils.defaultIfBlank(wxMpRegisterBo.getInviteCode(), wxMpRegisterBo.getSuperiorInvitationCode());
        userInfo.setSuperiorInvitationCode(inviteCode);
        userInfo.setMpOpenId(wxMpRegisterBo.getOpenId());
        userInfo.setCreateTime(LocalDateTime.now());
        iUserService.save(userInfo);
        registerBiz(userInfo.getUserId(), inviteCode);
        return Result.success(buildLoginResult(userInfo));
    }

    private WxLoginVo buildOAuthLoginVo(String openId, String unionId, boolean mpAccount) {
        WxLoginVo vo = new WxLoginVo();
        vo.setOpenid(openId);
        vo.setUnionid(unionId);
        UserEntity userEntity = mpAccount ? iUserService.getByMpOpenId(openId) : iUserService.getByOpenId(openId);
        if (Objects.isNull(userEntity) || StringUtils.isBlank(userEntity.getPhone())) {
            vo.setBindPhone(false);
        } else {
            vo.setBindPhone(true);
            vo.setLoginVO(buildLoginResult(userEntity));
            vo.setUserEntity(userEntity);
        }
        return vo;
    }

    @Override
    public Result<LoginVO> wxJsRegister(WxJsRegisterBo wxJsRegisterBo) {
        UserEntity userEntity = iUserService.getByOpenId(wxJsRegisterBo.getOpenId());
        if (Objects.nonNull(userEntity)) {
            if (StringUtils.isBlank(userEntity.getPhone())) {
                userEntity.setPhone(wxJsRegisterBo.getPhone());
                iUserService.updateById(userEntity);
            }

            // 已注册过, 直接登录
            LoginVO vo = buildLoginResult(userEntity);
            return Result.success(vo);
        }

        UserEntity userByPhone = getUserByPhone(wxJsRegisterBo.getPhone());
        if (Objects.nonNull(userByPhone)) {

            if (StringUtils.isBlank(userByPhone.getMaOpenId())) {
                userByPhone.setMaOpenId(wxJsRegisterBo.getOpenId());
                iUserService.updateById(userByPhone);
            }

            // 已注册过, 直接登录
            LoginVO vo = buildLoginResult(userByPhone);
            return Result.success(vo);
        }

        UserEntity userInfo = new UserEntity();
        userInfo.setUserName(wxJsRegisterBo.getUsername());
        userInfo.setAvatar(wxJsRegisterBo.getAvatar());
        userInfo.setPhone(wxJsRegisterBo.getPhone());
        userInfo.setStatus(UserStatus.ENABLE.getCode());
        userInfo.setPlatform("JS");
        userInfo.setUserType(UserType.USER.getCode());
        userInfo.setIsVip(0);
        userInfo.setInvitationCode(GenUtil.genInviteCode(wxJsRegisterBo.getPhone()));
        userInfo.setSuperiorInvitationCode(wxJsRegisterBo.getSuperiorInvitationCode());
        userInfo.setMaOpenId(wxJsRegisterBo.getOpenId());
        userInfo.setCreateTime(LocalDateTime.now());

        iUserService.save(userInfo);

        /* 其他业务 */
        registerBiz(userInfo.getUserId(), wxJsRegisterBo.getSuperiorInvitationCode());

        LoginVO vo = buildLoginResult(userInfo);
        return Result.success(vo);
    }

    @Override
    public Result<Object> decryptPhone(DecryptPhoneBo decryptPhoneBo) {
        String decryptS5 = wxUtil.decryptS5(decryptPhoneBo.getDecryptData(), decryptPhoneBo.getKey(), decryptPhoneBo.getIv());
        return Result.success(JSONObject.parseObject(decryptS5));
    }

    @Override
    public WxUserPhone getWxUserPhoneNumber(String code, String openId) {
        WxUserPhone wxUserPhone = wxUtil.getUserPhoneNumber(code, openId, RequestUtil.getClientType());

        return wxUserPhone;
    }

    /**
     * 构建登录返回值
     * @param userEntity 用户信息
     * @return
     */
    private LoginVO buildLoginResult(UserEntity userEntity) {
        LoginVO vo = new LoginVO();

        //生成token
        String token = jwtUtil.generateToken(userEntity.getUserId());
        vo.setToken(token);
        vo.setExpire(jwtUtil.getExpire());
        vo.setUser(userEntity);
        return vo;

    }

    /**
     * 手机号获取用户信息
     * @param phoneNo 手机号
     * @return
     */
    private UserEntity getUserByPhone(String phoneNo) {
        QueryWrapper<UserEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(UserEntity::getPhone, phoneNo);

        return iUserService.getOne(queryWrapper);
    }
}

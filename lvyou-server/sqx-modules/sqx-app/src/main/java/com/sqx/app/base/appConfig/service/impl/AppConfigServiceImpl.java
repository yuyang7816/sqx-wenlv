package com.sqx.app.base.appConfig.service.impl;

import com.sqx.app.base.appConfig.domain.bo.SysConfigInfoBO;
import com.sqx.app.base.appConfig.domain.vo.AgreementVO;
import com.sqx.app.base.appConfig.domain.vo.AppConfigInfoVO;
import com.sqx.app.base.appConfig.domain.vo.InviteConfigVO;
import com.sqx.app.base.appConfig.domain.vo.KefuConfigVO;
import com.sqx.app.base.appConfig.domain.vo.SysConfigVO;
import com.sqx.app.base.appConfig.service.AppConfigService;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.enums.AppError;
import com.sqx.app.base.utils.AvatarSupport;
import com.sqx.app.base.utils.SysConfigUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.file.utils.FileUtil;
import com.sqx.common.redis.utils.ConfigUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author chengming
 * @since 2025/6/9
 */
@Service
public class AppConfigServiceImpl implements AppConfigService {

    private static final String DEFAULT_PRODUCT_NAME = "省钱兄文旅";

    private static final Map<String, String[]> AGREEMENT_KEY_CHAIN = new HashMap<>();

    static {
        AGREEMENT_KEY_CHAIN.put(SysConfigConstant.Agreement.API_KEY_USER,
                new String[]{SysConfigConstant.Agreement.AGREEMENT_USER, SysConfigConstant.Agreement.LEGACY_PROT_USER});
        AGREEMENT_KEY_CHAIN.put(SysConfigConstant.Agreement.API_KEY_PRIVACY,
                new String[]{SysConfigConstant.Agreement.AGREEMENT_PRIVACY, SysConfigConstant.Agreement.LEGACY_PROT_PRIVACY});
        AGREEMENT_KEY_CHAIN.put(SysConfigConstant.Agreement.API_KEY_ABOUT,
                new String[]{SysConfigConstant.Agreement.AGREEMENT_ABOUT, SysConfigConstant.Agreement.LEGACY_SYS_ABOUT});
    }

    @Override
    public SysConfigVO info(SysConfigInfoBO sysConfigInfoBO) {
        String cacheConfigValue = SysConfigUtil.getConfigValue(sysConfigInfoBO.getKey());
        if (Objects.nonNull(cacheConfigValue)) {
            SysConfigVO vo = new SysConfigVO();
            vo.setValue(cacheConfigValue);
            return vo;
        }

        String configValue = ConfigUtil.getConfigValue(sysConfigInfoBO.getKey());
        if (Objects.isNull(configValue)) {
            throw new BizException(AppError.CONFIG_NOT_FOUND);
        }

        SysConfigVO vo = new SysConfigVO();
        vo.setValue(configValue);
        return vo;
    }

    @Override
    public AppConfigInfoVO getAppConfigInfo() {
        AppConfigInfoVO vo = new AppConfigInfoVO();
        vo.setProductName(getProductName());
        vo.setDefaultAvatar(AvatarSupport.getDefaultAvatarUrl());

        KefuConfigVO kefu = new KefuConfigVO();
        kefu.setPhone(getOptionalConfigValue(SysConfigConstant.Kefu.KEFU_PHONE));
        kefu.setWechat(getOptionalConfigValue(SysConfigConstant.Kefu.KEFU_WECHAT));
        kefu.setWechatQrcode(resolvePublicMediaUrl(getOptionalConfigValue(SysConfigConstant.Kefu.KEFU_WECHAT_QRCODE)));
        kefu.setWeworkUrl(getOptionalConfigValue(SysConfigConstant.Kefu.KEFU_WEWORK_URL));
        kefu.setWeworkCorpId(getOptionalConfigValue(SysConfigConstant.Kefu.KEFU_WEWORK_CORP_ID));
        kefu.setType(getOptionalConfigValue(SysConfigConstant.Kefu.KEFU_TYPE));
        vo.setKefu(kefu);
        vo.setAiAssistantEnabled(resolveAiAssistantEnabled());
        vo.setWechatMpEnabled(resolveWechatMpEnabled());

        InviteConfigVO invite = new InviteConfigVO();
        invite.setPosterImage(resolvePublicMediaUrl(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_POSTER)));
        invite.setCopyText(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_COPY_TEXT));
        invite.setRebateLevel1(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_REBATE_LEVEL1));
        invite.setRebateLevel2(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_REBATE_LEVEL2));
        invite.setShareLink(getOptionalConfigValue(SysConfigConstant.Invite.INVITE_SHARE_LINK));
        vo.setInvite(invite);

        return vo;
    }

    @Override
    public AgreementVO getAgreement(String key) {
        if (StringUtils.isBlank(key)) {
            throw new BizException(AppError.CONFIG_NOT_FOUND);
        }

        String normalizedKey = key.trim().toLowerCase();
        String[] paramKeys = AGREEMENT_KEY_CHAIN.get(normalizedKey);
        if (paramKeys == null) {
            throw new BizException(AppError.CONFIG_NOT_FOUND);
        }

        String content = getFirstNonBlankConfigValue(paramKeys);
        if (StringUtils.isBlank(content)) {
            throw new BizException(AppError.CONFIG_NOT_FOUND);
        }

        AgreementVO vo = new AgreementVO();
        vo.setKey(normalizedKey);
        vo.setContent(content);
        return vo;
    }

    private String getProductName() {
        String value = ConfigUtil.getConfigValueOrDefault(SysConfigConstant.Sys.SYS_SERVER_NAME);
        if (StringUtils.isBlank(value)) {
            return DEFAULT_PRODUCT_NAME;
        }
        return value.trim();
    }

    private String getOptionalConfigValue(String paramKey) {
        String value = ConfigUtil.getConfigValueOrDefault(paramKey);
        return StringUtils.defaultString(value, "");
    }

    /** 配置中的图片路径转为 CDN 可访问 URL */
    private String resolvePublicMediaUrl(String value) {
        if (StringUtils.isBlank(value)) {
            return "";
        }
        String trimmed = value.trim();
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }
        String domain = ConfigUtil.getConfigValue("file_cdn_domain");
        return FileUtil.buildPublicUrl(domain, trimmed);
    }

    private Boolean resolveAiAssistantEnabled() {
        String value = getOptionalConfigValue(SysConfigConstant.Kefu.AI_ASSISTANT_ENABLED);
        if (StringUtils.isBlank(value)) {
            return Boolean.TRUE;
        }
        String normalized = value.trim().toLowerCase();
        return !"0".equals(normalized) && !"false".equals(normalized) && !"off".equals(normalized);
    }

    private Boolean resolveWechatMpEnabled() {
        String value = getOptionalConfigValue(SysConfigConstant.Kefu.WECHAT_MP_ENABLED);
        if (StringUtils.isBlank(value)) {
            return Boolean.TRUE;
        }
        String normalized = value.trim().toLowerCase();
        return !"0".equals(normalized) && !"false".equals(normalized) && !"off".equals(normalized);
    }

    private String getFirstNonBlankConfigValue(String... paramKeys) {
        for (String paramKey : paramKeys) {
            String value = ConfigUtil.getConfigValueOrDefault(paramKey);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return null;
    }
}

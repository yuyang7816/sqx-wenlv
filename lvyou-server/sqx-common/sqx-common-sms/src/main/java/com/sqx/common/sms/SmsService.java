package com.sqx.common.sms;

import com.sqx.common.redis.utils.ConfigUtil;
import com.sqx.common.sms.enums.SmsProviderEnum;
import com.sqx.common.sms.factory.SmsFactory;
import com.sqx.common.sms.provider.ISmsProvider;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

/**
 * @author chengming
 * @since 2025/6/11
 */
@Component
public class SmsService {

    /**
     * 发送短信
     * @param phone 手机号
     * @param content 内容
     */
    public void sendSms(String phone, String content) {
        ISmsProvider smsService = SmsFactory.getSmsService(resolveProvider());
        smsService.sendSms(phone, content);
    }

    /**
     * 优先 sms_provider；mock/空/非法时回退 sms_platform，再回退默认短信宝。
     */
    static String resolveProvider() {
        String provider = ConfigUtil.getConfigValueOrDefault("sms_provider");
        if (isSupportedProvider(provider)) {
            return provider;
        }

        provider = ConfigUtil.getConfigValueOrDefault("sms_platform");
        if (isSupportedProvider(provider)) {
            return provider;
        }

        return SmsFactory.DEFAULT_PROVIDER;
    }

    private static boolean isSupportedProvider(String provider) {
        if (StringUtils.isBlank(provider) || "mock".equalsIgnoreCase(provider.trim())) {
            return false;
        }
        for (SmsProviderEnum item : SmsProviderEnum.values()) {
            if (item.getCode().equals(provider.trim())) {
                return true;
            }
        }
        return false;
    }
}

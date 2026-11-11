package com.sqx.app.base.pay.controller;

import com.alipay.api.AlipayApiException;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.sqx.app.base.auth.auth.annotation.Login;
import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.app.base.auth.auth.interceptor.AuthorizationInterceptor;
import com.sqx.app.base.constants.BaseConstant;
import com.sqx.app.base.pay.business.service.BusinessService;
import com.sqx.app.base.pay.domain.BusinessParam;
import com.sqx.app.base.pay.enums.BusinessType;
import com.sqx.app.base.pay.support.PayConfigSupport;
import com.sqx.app.base.pay.support.WechatPayConfigMirrorSupport;
import com.sqx.app.base.utils.RequestUtil;
import com.sqx.common.core.domain.Result;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.GenUtil;
import com.sqx.common.pay.domain.UnifiedPayRequest;
import com.sqx.common.pay.enums.PayChannel;
import com.sqx.common.pay.enums.PayTradeType;
import com.sqx.common.pay.service.IPayService;
import com.sqx.common.redis.utils.ConfigUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Objects;

/**
 * 统一支付接口
 */
@Slf4j
@Tag(name = "统一支付接口", description = "统一支付接口")
@RestController
@RequestMapping(BaseConstant.BASE_APP_PREFIX + "/pay")
public class PayController {

    private static final String NOTIFY_URL = BaseConstant.BASE_APP_PREFIX + "/pay/notify/%s";
    private static final String H5_DOMAIN_KEY = "sys_h5_domain";

    @Resource
    private IPayService iPayService;
    @Resource
    private BusinessService businessService;
    @Resource
    private PayConfigSupport payConfigSupport;

    @Login
    @Operation(description = "支付配置就绪检查")
    @GetMapping("config-check")
    public Result<Map<String, Object>> configCheck() {
        return Result.success(payConfigSupport.buildReadinessReport());
    }

    @Operation(description = "统一支付")
    @Login
    @PostMapping("pay")
    public Result<String> pay(@RequestAttribute(AuthorizationInterceptor.USER_KEY) Long userId,
                              BusinessParam param,
                              HttpServletRequest request) throws WxPayException, AlipayApiException {
        if (param.getUserId() == null) {
            param.setUserId(userId);
        }
        businessService.process(param);
        UnifiedPayRequest payRequest = convertRequest(param, request);
        String resp = iPayService.unifiedPay(payRequest);
        return Result.success(resp);
    }

    private UnifiedPayRequest convertRequest(BusinessParam param, HttpServletRequest request) {
        PayChannel payChannel = PayChannel.fromCode(param.getChannel());
        UnifiedPayRequest.UnifiedPayRequestBuilder builder = UnifiedPayRequest.builder()
                .orderNo(StringUtils.isBlank(param.getOrderNo()) ? GenUtil.genOrderNo() : param.getOrderNo())
                .tradeType(param.getTradeType())
                .amount(param.getAmount())
                .channel(param.getChannel())
                .returnUrl(resolveReturnUrl(param.getRedirectUri()))
                .clientIp(RequestUtil.getClientIp())
                .openId(param.getOpenId())
                .notifyUrl(resolveNotifyUrl(payChannel, request));

        BusinessType businessType = BusinessType.fromCode(param.getBusinessType());
        builder.subject(businessType.getDesc());

        if (Objects.equals(payChannel, PayChannel.WX)) {
            builder.appId(getWxAppId(param.getTradeType()));
        }

        return builder.build();
    }

    private String resolveNotifyUrl(PayChannel payChannel, HttpServletRequest request) {
        return resolveApiBaseUrl(request) + String.format(NOTIFY_URL, payChannel.getValue());
    }

    private String resolveApiBaseUrl(HttpServletRequest request) {
        String apiBase = payConfigSupport.resolveApiBaseUrl();
        if (StringUtils.isNotBlank(apiBase)) {
            return apiBase;
        }

        String requestBase = buildRequestBaseUrl(request);
        if (StringUtils.isNotBlank(requestBase)) {
            log.warn("未配置支付回调域名，使用当前请求地址作为回调前缀: {}", requestBase);
            return requestBase;
        }

        throw new BizException("请配置 sys_h5_domain 或 file_public_base_url");
    }

    private String buildRequestBaseUrl(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        String scheme = request.getScheme();
        String host = request.getServerName();
        int port = request.getServerPort();
        String context = StringUtils.defaultString(request.getContextPath());
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);
        String portPart = defaultPort ? "" : (":" + port);
        return scheme + "://" + host + portPart + context;
    }

    private String resolveReturnUrl(String redirectUri) {
        if (StringUtils.isBlank(redirectUri)) {
            return redirectUri;
        }
        if (redirectUri.startsWith("http://") || redirectUri.startsWith("https://")) {
            return redirectUri;
        }

        String h5Domain = ConfigUtil.getConfigValueOrDefault(H5_DOMAIN_KEY);
        if (StringUtils.isBlank(h5Domain)) {
            return redirectUri;
        }

        String path = redirectUri.startsWith("/") ? redirectUri : ("/" + redirectUri);
        return StringUtils.removeEnd(h5Domain.trim(), "/") + "/#" + path;
    }

    private String getWxAppId(String tradeType) {
        try {
            if (Objects.equals(tradeType, PayTradeType.JSAPI.getCode())) {
                return WechatPayConfigMirrorSupport.resolveJsapiAppId();
            }
            if (Objects.equals(tradeType, PayTradeType.H5.getCode())) {
                return WechatPayConfigMirrorSupport.resolveH5AppId();
            }
            if (Objects.equals(tradeType, PayTradeType.APP.getCode())) {
                return WechatPayConfigMirrorSupport.resolveAppAppId();
            }
        } catch (IllegalArgumentException ex) {
            throw new BizException(ex.getMessage());
        }
        log.error("不支持的交易类型: {}", tradeType);
        throw new BizException("不支持的交易类型");
    }

    @Operation(hidden = true)
    @PublicAccess
    @PostMapping("/notify/{channel}")
    public String handleNotify(HttpServletRequest request, @PathVariable String channel) {
        try {
            PayChannel payChannel = PayChannel.fromCode(channel);
            return iPayService.notify(request, payChannel.getValue());
        } catch (Exception e) {
            log.error("处理 {} 回调异常", channel, e);
            return "fail";
        }
    }
}

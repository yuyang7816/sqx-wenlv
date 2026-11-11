package com.sqx.app.lvyou.config;

import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.base.pay.support.WechatPayConfigMirrorSupport;
import com.sqx.common.pay.constants.PayConstant;
import com.sqx.common.pay.enums.PayConfigCache;
import com.sqx.common.redis.utils.RedisKeys;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.service.ISysConfigService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;

/**
 * 启动时补全文旅相关系统配置，兼容 aiPhoto_server 历史键并自动迁移。
 */
@Slf4j
@Component
@Order(1)
public class LvyouSysConfigInitializer implements ApplicationRunner {

    @Resource
    private ISysConfigService sysConfigService;

    private static final String USER_H5_DOMAIN = "http://127.0.0.1:9186";

    @Override
    public void run(ApplicationArguments args) {
        ensureDomainConfig();
        ensureMpLoginConfig();
        ensureWechatMaConfig();
        ensureAliyunExpressConfig();
        ensureMapConfig();
        ensureAlipayPayConfig();
        ensureWechatPayConfig();
    }

    private void ensureDomainConfig() {
        ensureSimpleConfig(SysConfigConstant.Sys.SYS_H5_DOMAIN, USER_H5_DOMAIN,
                "用户端 H5 域名", "sys", "域名");
        ensureSimpleConfig("sys_domain", USER_H5_DOMAIN,
                "用户端业务域名", "sys", "域名");
        ensureSimpleConfig("file_public_base_url", USER_H5_DOMAIN + "/sqx_fast",
                "公网 API 前缀（支付回调、文件访问）", "sys", "域名");
    }

    private void ensureMpLoginConfig() {
        ensureSimpleConfig(SysConfigConstant.Sys.SYS_MP_LOGIN_ENABLED, "1",
                "是否开启公众号登录：1=开启，0=关闭。关闭后 H5 不展示微信登录入口。",
                "switch", "开关配置");
    }

    private void ensureWechatMaConfig() {
        ensureConfigWithLegacy(SysConfigConstant.Sys.SYS_WECHAT_JSAPI_APPID,
                resolveLegacyValue(Arrays.asList("sys_user_ma_appid", "pay_wx_jsapi_app_id")),
                "微信小程序 AppId（登录/支付/消息统一使用）", "sys", "系统配置");
        ensureConfigWithLegacy(SysConfigConstant.Sys.SYS_WECHAT_JSAPI_SECRET,
                resolveLegacyValue(Arrays.asList("sys_user_ma_secret")),
                "微信小程序 Secret（登录/支付/消息统一使用）", "sys", "系统配置");
        ensureConfigWithLegacy(SysConfigConstant.Sys.SYS_USER_MP_APPID,
                resolveLegacyValue(Arrays.asList("sys_user_mp_appid")),
                "用户端公众号 AppId", "sys", "系统配置");
        ensureConfigWithLegacy(SysConfigConstant.Sys.SYS_USER_MP_SECRET,
                resolveLegacyValue(Arrays.asList("sys_user_mp_secret")),
                "用户端公众号 Secret", "sys", "系统配置");
        ensureConfigWithLegacy(SysConfigConstant.Sys.SYS_USER_APP_WX_APPID,
                resolveLegacyValue(Arrays.asList("sys_user_app_wx_appid", "pay_wx_app_id")),
                "用户端 App 微信 AppId", "sys", "系统配置");
        ensureConfigWithLegacy(SysConfigConstant.Sys.SYS_USER_APP_WX_SECRET,
                resolveLegacyValue(Arrays.asList("sys_user_app_wx_secret")),
                "用户端 App 微信 Secret", "sys", "系统配置");
    }

    private void ensureMapConfig() {
        ensureSimpleConfig("map_platform", "tencent",
                "地图平台（腾讯/天地图/谷歌/高德）", "map", "地图配置");
        ensureSimpleConfig("map_tencent_key", "",
                "腾讯地图密钥", "map", "地图配置");
        ensureSimpleConfig("google_map_key", "",
                "谷歌地图密钥", "map", "地图配置");
        normalizeConfigMeta("map_platform", "地图平台（腾讯/天地图/谷歌/高德）", "map", "地图配置");
        normalizeConfigMeta("map_tencent_key", "腾讯地图密钥", "map", "地图配置");
        normalizeConfigMeta("google_map_key", "谷歌地图密钥", "map", "地图配置");
    }

    private void ensureAliyunExpressConfig() {
        ensureConfigWithLegacy(SysConfigConstant.Logistics.ALIYUN_EXPRESS_APP_CODE,
                resolveLegacyValue(Arrays.asList(
                        "aiphoto_aliyun_express_appcode",
                        "aliyun_express_appcode",
                        "阿里云快递查询AppCode"
                )),
                "阿里云快递查询 AppCode，用于同步物流轨迹；配额用尽或未配置时展示订单基础物流信息",
                "logistics", "物流配置");
        ensureSimpleConfig(SysConfigConstant.Logistics.ALIYUN_EXPRESS_API_URL,
                "https://wuliu.market.alicloudapi.com/kdi",
                "阿里云快递查询接口地址，一般无需修改",
                "logistics", "物流配置");
    }

    private void ensureAlipayPayConfig() {
        ensurePayConfig(PayConfigCache.ALI_APPID.getCode(), resolveLegacyAlipayValue(
                new String[]{"pay_ali_appid", "企业支付宝APPID"},
                new String[]{"企业支付宝APPID", "支付宝AppId"}),
                "支付宝应用 AppId，用于 H5/App 充值与订单支付");
        ensurePayConfig(PayConfigCache.ALI_PUBLIC_KEY.getCode(), resolveLegacyAlipayValue(
                new String[]{"pay_ali_public_key", "企业支付宝公钥"},
                new String[]{"企业支付宝公钥", "支付宝公钥"}),
                "支付宝公钥（公钥模式必填）");
        ensurePayConfig(PayConfigCache.ALI_PRIVATE_KEY.getCode(), resolveLegacyAlipayValue(
                new String[]{"pay_ali_private_key", "企业支付宝商户秘钥", "企业支付宝商户密钥"},
                new String[]{"企业支付宝商户", "支付宝应用私钥", "支付宝私钥"}),
                "支付宝应用私钥（必填）");
        ensurePayConfig(PayConfigCache.ALI_MODE.getCode(),
                StringUtils.defaultIfBlank(resolveLegacyAlipayValue(
                        new String[]{"pay_ali_mode"}, new String[]{"支付宝支付方式"}), "1"),
                "支付宝支付模式：1=公钥模式，2=证书模式");
        ensurePayConfig(PayConfigCache.ALI_SANDBOX.getCode(),
                StringUtils.defaultIfBlank(resolveLegacyAlipayValue(
                        new String[]{"pay_ali_sandbox"}, new String[]{"支付宝沙箱"}), "2"),
                "支付宝沙箱模式：1=开启，2=关闭");
        ensurePayConfig(PayConfigCache.ALI_APP_CERT_PATH.getCode(), resolveLegacyAlipayValue(
                new String[]{"pay_ali_app_cert_path"}, new String[]{"应用公钥证书"}),
                "应用公钥证书路径（证书模式必填）");
        ensurePayConfig(PayConfigCache.ALI_ALIPAY_PUBLIC_CERT_PATH.getCode(), resolveLegacyAlipayValue(
                new String[]{"pay_ali_alipay_public_cert_path"}, new String[]{"支付宝公钥证书路径"}),
                "支付宝公钥证书路径（证书模式必填）");
        ensurePayConfig(PayConfigCache.ALI_ROOT_CERT_PATH.getCode(), resolveLegacyAlipayValue(
                new String[]{"pay_ali_root_cert_path"}, new String[]{"支付宝根证书路径"}),
                "支付宝根证书路径（证书模式必填）");
    }

    private void ensureWechatPayConfig() {
        String mchId = resolveLegacyValue(Arrays.asList(
                PayConstant.Wx.MCH_ID, PayConfigCache.WX_MCH_ID.getCode()));
        String mchKey = resolveLegacyValue(Arrays.asList(
                PayConstant.Wx.MCH_KEY, PayConfigCache.WX_MCH_KEY.getCode()));
        String keyPath = resolveLegacyValue(Arrays.asList(
                PayConstant.Wx.KEY_PATH, PayConfigCache.WX_KEY_PATH.getCode()));
        String maAppId = resolveLegacyValue(Arrays.asList(
                PayConstant.Wx.User.MA_APP_ID, PayConfigCache.WX_JSAPI_APP_ID.getCode(),
                "sys_user_ma_appid", "pay_wx_jsapi_app_id"));
        String h5AppId = resolveLegacyValue(Arrays.asList(
                PayConstant.Wx.User.MP_APP_ID, PayConfigCache.WX_H5_APP_ID.getCode(),
                "pay_wx_h5_app_id", "sys_user_mp_appid"));
        String appAppId = resolveLegacyValue(Arrays.asList(
                PayConstant.Wx.User.APP_ID, PayConfigCache.WX_APP_ID.getCode(),
                "sys_user_app_wx_appid", "pay_wx_app_id"));

        ensureConfigWithLegacy(PayConstant.Wx.MCH_ID, mchId,
                "微信支付商户号（SDK 运行时读取）", "pay", "支付配置");
        ensureConfigWithLegacy(PayConstant.Wx.MCH_KEY, mchKey,
                "微信支付商户秘钥（SDK 运行时读取）", "pay", "支付配置");
        ensureConfigWithLegacy(PayConstant.Wx.KEY_PATH, keyPath,
                "微信支付 p12 证书路径（SDK 运行时读取）", "pay", "支付配置");
        ensureConfigWithLegacy(PayConstant.Wx.User.MA_APP_ID, maAppId,
                "微信小程序 AppId（登录/支付）", "sys", "系统配置");
        ensureConfigWithLegacy(PayConstant.Wx.User.MP_APP_ID, h5AppId,
                "微信公众号/H5 AppId（支付 SDK 读取）", "sys", "系统配置");
        ensureConfigWithLegacy(PayConstant.Wx.User.APP_ID, appAppId,
                "微信 App AppId（支付 SDK 读取）", "sys", "系统配置");

        ensurePayConfig(PayConfigCache.WX_JSAPI_APP_ID.getCode(),
                StringUtils.defaultIfBlank(maAppId, resolveLegacyValue(Arrays.asList("pay_wx_jsapi_app_id"))),
                "微信支付 JSAPI（小程序）AppId");
        ensurePayConfig(PayConfigCache.WX_H5_APP_ID.getCode(),
                StringUtils.defaultIfBlank(h5AppId, resolveLegacyValue(Arrays.asList("pay_wx_h5_app_id"))),
                "微信支付 H5 AppId");
        ensurePayConfig(PayConfigCache.WX_APP_ID.getCode(),
                StringUtils.defaultIfBlank(appAppId, resolveLegacyValue(Arrays.asList("pay_wx_app_id"))),
                "微信支付 App AppId");
        ensurePayConfig(PayConfigCache.WX_MCH_ID.getCode(), mchId, "微信支付商户号（兼容键）");
        ensurePayConfig(PayConfigCache.WX_MCH_KEY.getCode(), mchKey, "微信支付商户秘钥（兼容键）");
        ensurePayConfig(PayConfigCache.WX_KEY_PATH.getCode(), keyPath, "微信支付 p12 证书路径（兼容键）");
        WechatPayConfigMirrorSupport.syncAllMirrorPairs(sysConfigService);
    }

    private String resolveLegacyAlipayValue(String[] legacyKeys, String[] remarkKeywords) {
        for (String legacyKey : legacyKeys) {
            String value = readConfigValue(legacyKey);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        for (String keyword : remarkKeywords) {
            SysConfigEntity legacyByRemark = sysConfigService.getOne(
                    Wrappers.<SysConfigEntity>lambdaQuery()
                            .like(SysConfigEntity::getRemark, keyword)
                            .last("LIMIT 1"),
                    false
            );
            if (legacyByRemark != null && StringUtils.isNotBlank(legacyByRemark.getParamValue())) {
                return StringUtils.trim(legacyByRemark.getParamValue());
            }
        }
        return "";
    }

    private String resolveLegacyValue(List<String> legacyKeys) {
        for (String legacyKey : legacyKeys) {
            String value = readConfigValue(legacyKey);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return "";
    }

    private String readConfigValue(String paramKey) {
        SysConfigEntity config = sysConfigService.getOne(
                Wrappers.<SysConfigEntity>lambdaQuery()
                        .eq(SysConfigEntity::getParamKey, paramKey)
                        .last("LIMIT 1"),
                false
        );
        return config != null ? StringUtils.trimToEmpty(config.getParamValue()) : "";
    }

    private void normalizeConfigMeta(String paramKey, String remark, String paramType, String paramTypeDesc) {
        SysConfigEntity existing = sysConfigService.getOne(
                Wrappers.<SysConfigEntity>lambdaQuery()
                        .eq(SysConfigEntity::getParamKey, paramKey)
                        .last("LIMIT 1"),
                false
        );
        if (existing == null) {
            return;
        }
        boolean changed = false;
        if (!StringUtils.equals(existing.getRemark(), remark)) {
            existing.setRemark(remark);
            changed = true;
        }
        if (!StringUtils.equals(existing.getParamType(), paramType)) {
            existing.setParamType(paramType);
            changed = true;
        }
        if (!StringUtils.equals(existing.getParamTypeDesc(), paramTypeDesc)) {
            existing.setParamTypeDesc(paramTypeDesc);
            changed = true;
        }
        if (changed) {
            sysConfigService.updateById(existing);
            cacheConfig(existing);
            log.info("已修正系统配置文案 {}", paramKey);
        }
    }

    private void ensureConfigWithLegacy(String paramKey, String legacyValue, String remark,
                                        String paramType, String paramTypeDesc) {
        SysConfigEntity existing = sysConfigService.getOne(
                Wrappers.<SysConfigEntity>lambdaQuery()
                        .eq(SysConfigEntity::getParamKey, paramKey)
                        .last("LIMIT 1"),
                false
        );
        if (existing != null) {
            if (StringUtils.isBlank(existing.getParamValue()) && StringUtils.isNotBlank(legacyValue)) {
                existing.setParamValue(legacyValue);
                sysConfigService.updateById(existing);
                cacheConfig(existing);
                log.info("已从历史配置补全 {}", paramKey);
            }
            return;
        }
        SysConfigEntity config = new SysConfigEntity();
        config.setParamKey(paramKey);
        config.setParamValue(StringUtils.defaultString(legacyValue));
        config.setStatus("1");
        config.setRemark(remark);
        config.setParamType(paramType);
        config.setParamTypeDesc(paramTypeDesc);
        sysConfigService.save(config);
        cacheConfig(config);
        log.info("已初始化系统配置 {}", paramKey);
    }

    private void ensurePayConfig(String paramKey, String defaultValue, String remark) {
        ensureConfigWithLegacy(paramKey, defaultValue, remark, "pay", "支付配置");
    }

    private void ensureSimpleConfig(String paramKey, String defaultValue, String remark,
                                    String paramType, String paramTypeDesc) {
        ensureConfigWithLegacy(paramKey, defaultValue, remark, paramType, paramTypeDesc);
    }

    private void cacheConfig(SysConfigEntity config) {
        RedisUtil.setCacheObject(RedisKeys.SYS_CONFIG_KEY + config.getParamKey(), JSONObject.toJSONString(config));
    }
}

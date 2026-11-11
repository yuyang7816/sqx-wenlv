package com.sqx.app.base.constants;

/**
 * 系统配置键（与 sys_config.param_key、管理端 SystemConfigView 保持一致）
 *
 * @author chengming
 * @since 2025/12/19
 */
public interface SysConfigConstant {

    /**
     * 系统基础配置
     */
    interface Sys {
        /** 产品名称（用户端/管理端展示，短信/邮件签名） */
        String SYS_SERVER_NAME = "sys_server_name";

        /** 默认头像 URL（用户未上传头像时使用） */
        String SYS_DEFAULT_AVATAR = "sys_default_avatar";

        /** 微信小程序 appId（登录/支付/消息等统一使用） */
        String SYS_WECHAT_JSAPI_APPID = "sys_wechat_jsapi_appid";

        /** 微信小程序 secret（登录/支付/消息等统一使用） */
        String SYS_WECHAT_JSAPI_SECRET = "sys_wechat_jsapi_secret";

        /** @deprecated 请使用 {@link #SYS_WECHAT_JSAPI_APPID} */
        @Deprecated
        String SYS_USER_MA_APPID = SYS_WECHAT_JSAPI_APPID;

        /** @deprecated 请使用 {@link #SYS_WECHAT_JSAPI_SECRET} */
        @Deprecated
        String SYS_USER_MA_SECRET = SYS_WECHAT_JSAPI_SECRET;

        /** 用户端公众号 appid */
        String SYS_USER_MP_APPID = "sys_user_mp_appid";

        /** 用户端公众号 secret */
        String SYS_USER_MP_SECRET = "sys_user_mp_secret";

        /** 用户端 App 微信 appid */
        String SYS_USER_APP_WX_APPID = "sys_user_app_wx_appid";

        /** 用户端 App 微信 secret */
        String SYS_USER_APP_WX_SECRET = "sys_user_app_wx_secret";

        /** H5 服务域名 */
        String SYS_H5_DOMAIN = "sys_h5_domain";

        /** 是否开启公众号登录：1=开启，0=关闭 */
        String SYS_MP_LOGIN_ENABLED = "sys_mp_login_enabled";
    }

    /**
     * 物流查询（阿里云快递 API）
     */
    interface Logistics {
        /** 阿里云快递查询 AppCode */
        String ALIYUN_EXPRESS_APP_CODE = "lvyou_aliyun_express_appcode";

        /** 阿里云快递查询接口地址 */
        String ALIYUN_EXPRESS_API_URL = "lvyou_aliyun_express_api_url";
    }

    /**
     * 客服配置（C 端 service 页 / App.vue 缓存）
     */
    interface Kefu {
        /** 客服电话 */
        String KEFU_PHONE = "sys_kefu_phone";

        /** 客服微信号 */
        String KEFU_WECHAT = "sys_kefu_wechat";

        /** 客服微信二维码图片 URL */
        String KEFU_WECHAT_QRCODE = "sys_kefu_wechat_qrcode";

        /** 企业微信客服链接 */
        String KEFU_WEWORK_URL = "sys_kefu_wework_url";

        /** 企业微信 corpId */
        String KEFU_WEWORK_CORP_ID = "sys_kefu_wework_corp_id";

        /** 客服类型（phone/wechat/wework/im 等，可选） */
        String KEFU_TYPE = "sys_kefu_type";

        /** 是否启用 AI 助手入口（1 启用 / 0 关闭，与客服形式独立） */
        String AI_ASSISTANT_ENABLED = "sys_ai_assistant_enabled";

        /** 是否启用 H5 微信公众号支付（1 启用 / 0 关闭） */
        String WECHAT_MP_ENABLED = "sys_wechat_mp_enabled";
    }

    /**
     * AI 问答（豆包大模型，见 sql/migration/V20260924_lvyou_ai_doubao_config.sql）
     */
    interface Ai {
        /** 供应商：local_template / doubao */
        String PLATFORM = "ai_platform";

        /** 豆包 Ark API Key */
        String DOUBAO_API_KEY = "ai_doubao_api_key";

        /** 豆包模型 ID */
        String DOUBAO_MODEL = "ai_doubao_model";

        /** 豆包 Chat Completions 地址 */
        String DOUBAO_ENDPOINT = "ai_doubao_endpoint";
    }

    /**
     * 协议正文（富文本 HTML，管理端 SystemConfigView 维护）
     */
    interface Agreement {
        /** 用户服务协议 */
        String AGREEMENT_USER = "agreement_user";

        /** 隐私政策 */
        String AGREEMENT_PRIVACY = "agreement_privacy";

        /** 关于我们 */
        String AGREEMENT_ABOUT = "agreement_about";

        /** 协议 API 路径 key：user */
        String API_KEY_USER = "user";

        /** 协议 API 路径 key：privacy */
        String API_KEY_PRIVACY = "privacy";

        /** 协议 API 路径 key：about */
        String API_KEY_ABOUT = "about";

        /** 历史键：用户协议（迁移兼容） */
        String LEGACY_PROT_USER = "prot_user";

        /** 历史键：隐私政策（迁移兼容） */
        String LEGACY_PROT_PRIVACY = "prot_privacy";

        /** 历史键：关于我们（迁移兼容） */
        String LEGACY_SYS_ABOUT = "sys_about_me";
    }

    /**
     * 提现规则（用户/商户共用）
     */
    interface Withdraw {
        /** 最低提现金额（元） */
        String WITHDRAW_MIN = "sys_withdraw_min";

        /** 最高提现金额（元） */
        String WITHDRAW_MAX = "sys_withdraw_max";

        /** 手续费比例（%，如 1 表示 1%） */
        String WITHDRAW_FEE_RATE = "sys_withdraw_rate";
    }

    /**
     * 实时公交（apizero 代理）
     */
    interface Transport {
        /** 是否启用实时公交（1 启用 / 0 关闭） */
        String BUS_REALTIME_ENABLED = "sys_bus_realtime_enabled";

        /** apizero API Key（Bearer Token） */
        String BUS_REALTIME_API_KEY = "sys_bus_realtime_api_key";
    }

    /**
     * 订单结算与线路退改配置
     */
    interface Order {
        String PLATFORM_COMMISSION_RATE_HOTEL = "sys_platform_commission_rate_hotel";
        String PLATFORM_COMMISSION_RATE_ROUTE = "sys_platform_commission_rate_route";
        String ROUTE_REFUND_RATIO_BEFORE_TRIP = "sys_route_refund_ratio_before_trip";
        String ROUTE_REFUND_RATIO_ON_TRIP_DAY = "sys_route_refund_ratio_on_trip_day";
        String ROUTE_REFUND_RATIO_AFTER_TRIP = "sys_route_refund_ratio_after_trip";
        String ROUTE_REFUND_RATIO_NOSHOW = "sys_route_refund_ratio_noshow";
        String ROUTE_MEET_HOLD_HOUR = "sys_route_meet_hold_hour";
        String ROUTE_REFUND_RULE_SUMMARY = "sys_route_refund_rule_summary";
    }

    /**
     * 邀请分销展示配置（海报/文案/返佣说明）
     */
    interface Invite {
        /** 邀请海报图片 URL */
        String INVITE_POSTER = "invite_poster_image";

        /** 邀请复制文案（支持 {code} 占位） */
        String INVITE_COPY_TEXT = "invite_copy_text";

        /** 一级返佣说明文案 */
        String INVITE_REBATE_LEVEL1 = "invite_rebate_level1";

        /** 二级返佣说明文案 */
        String INVITE_REBATE_LEVEL2 = "invite_rebate_level2";

        /** 邀请分享链接前缀 */
        String INVITE_SHARE_LINK = "invite_share_link";

        /** 一级邀请佣金比例（数值，如 0.05） */
        String INVITE_COMMISSION_RATE_LEVEL1 = "invite_commission_rate_level1";

        /** 二级邀请佣金比例（数值，如 0.02） */
        String INVITE_COMMISSION_RATE_LEVEL2 = "invite_commission_rate_level2";
    }
}

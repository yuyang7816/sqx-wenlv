package com.sqx.app.lvyou.order.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.sqx.app.base.constants.SysConfigConstant;
import com.sqx.app.lvyou.merchant.enums.MerchantOrderStatusGroup;
import com.sqx.app.lvyou.order.db.service.IOrderService;
import com.sqx.app.lvyou.order.domain.entity.OrderEntity;
import com.sqx.app.lvyou.order.domain.vo.OrderLogisticsTrackVo;
import com.sqx.app.lvyou.order.domain.vo.OrderLogisticsVo;
import com.sqx.app.lvyou.order.service.OrderLogisticsService;
import com.sqx.app.lvyou.order.support.MerchantOrderStatusSupport;
import com.sqx.app.lvyou.order.support.OrderLogisticsRemarkSupport;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.Md5Utils;
import com.sqx.common.core.utils.http.OkHttpUtil;
import com.sqx.system.db.entity.SysConfigEntity;
import com.sqx.system.db.mapper.SysConfigDao;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 物流查询服务（阿里云快递 API + 快递100 兜底，逻辑对齐 aiPhoto_server）
 */
@Slf4j
@Service
public class OrderLogisticsServiceImpl implements OrderLogisticsService {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String DEFAULT_ALIYUN_EXPRESS_API_URL = "https://wuliu.market.alicloudapi.com/kdi";
    private static final String KUAIDI100_QUERY_URL = "https://poll.kuaidi100.com/poll/query.do";
    private static final String QUERY_TIP_NOT_CONFIGURED = "暂未配置物流查询服务，当前展示订单基础物流信息";
    private static final String QUERY_TIP_QUERY_FAILED = "暂无法同步实时物流轨迹，请复制运单号到快递公司官网或客服查询";

    private static final Map<String, String> KUAIDI100_COMPANY_CODE_MAP = new LinkedHashMap<>();
    private static final Map<String, String> ALIYUN_COMPANY_CODE_MAP = new LinkedHashMap<>();

    static {
        KUAIDI100_COMPANY_CODE_MAP.put("顺丰速运", "shunfeng");
        KUAIDI100_COMPANY_CODE_MAP.put("顺丰", "shunfeng");
        KUAIDI100_COMPANY_CODE_MAP.put("中通快递", "zhongtong");
        KUAIDI100_COMPANY_CODE_MAP.put("中通", "zhongtong");
        KUAIDI100_COMPANY_CODE_MAP.put("圆通速递", "yuantong");
        KUAIDI100_COMPANY_CODE_MAP.put("圆通", "yuantong");
        KUAIDI100_COMPANY_CODE_MAP.put("韵达快递", "yunda");
        KUAIDI100_COMPANY_CODE_MAP.put("韵达", "yunda");
        KUAIDI100_COMPANY_CODE_MAP.put("申通快递", "shentong");
        KUAIDI100_COMPANY_CODE_MAP.put("申通", "shentong");
        KUAIDI100_COMPANY_CODE_MAP.put("EMS", "ems");
        KUAIDI100_COMPANY_CODE_MAP.put("邮政快递包裹", "youzhengguonei");
        KUAIDI100_COMPANY_CODE_MAP.put("邮政", "youzhengguonei");
        KUAIDI100_COMPANY_CODE_MAP.put("京东物流", "jd");
        KUAIDI100_COMPANY_CODE_MAP.put("京东", "jd");
        KUAIDI100_COMPANY_CODE_MAP.put("极兔速递", "jtexpress");
        KUAIDI100_COMPANY_CODE_MAP.put("极兔", "jtexpress");
        KUAIDI100_COMPANY_CODE_MAP.put("德邦快递", "debangkuaidi");
        KUAIDI100_COMPANY_CODE_MAP.put("德邦", "debangkuaidi");

        ALIYUN_COMPANY_CODE_MAP.put("顺丰速运", "sf");
        ALIYUN_COMPANY_CODE_MAP.put("顺丰", "sf");
        ALIYUN_COMPANY_CODE_MAP.put("中通快递", "zto");
        ALIYUN_COMPANY_CODE_MAP.put("中通", "zto");
        ALIYUN_COMPANY_CODE_MAP.put("圆通速递", "yto");
        ALIYUN_COMPANY_CODE_MAP.put("圆通", "yto");
        ALIYUN_COMPANY_CODE_MAP.put("韵达快递", "yd");
        ALIYUN_COMPANY_CODE_MAP.put("韵达", "yd");
        ALIYUN_COMPANY_CODE_MAP.put("申通快递", "sto");
        ALIYUN_COMPANY_CODE_MAP.put("申通", "sto");
        ALIYUN_COMPANY_CODE_MAP.put("EMS", "ems");
        ALIYUN_COMPANY_CODE_MAP.put("邮政快递包裹", "yzpy");
        ALIYUN_COMPANY_CODE_MAP.put("邮政", "yzpy");
        ALIYUN_COMPANY_CODE_MAP.put("京东物流", "jd");
        ALIYUN_COMPANY_CODE_MAP.put("京东", "jd");
        ALIYUN_COMPANY_CODE_MAP.put("极兔速递", "jt");
        ALIYUN_COMPANY_CODE_MAP.put("极兔", "jt");
        ALIYUN_COMPANY_CODE_MAP.put("德邦快递", "db");
        ALIYUN_COMPANY_CODE_MAP.put("德邦", "db");
    }

    @Resource
    private IOrderService iOrderService;
    @Resource
    private SysConfigDao sysConfigDao;

    @Override
    public OrderLogisticsVo queryLogistics(Long userId, Long orderId) {
        OrderEntity order = iOrderService.getById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        if (userId != null && !Objects.equals(order.getUserId(), userId)) {
            throw new BizException("无权查看该订单物流");
        }
        if (!MerchantOrderStatusSupport.isGoodsCategory(order.getOrderCategory())) {
            throw new BizException("仅实物商品订单支持查询物流");
        }
        if (!isLogisticsQueryable(order.getStatus())) {
            throw new BizException("当前订单状态暂不支持查询物流");
        }

        OrderLogisticsRemarkSupport.LogisticsInfo logisticsInfo = OrderLogisticsRemarkSupport.parseFromRemark(order.getRemark());
        OrderLogisticsVo vo = buildBaseVo(order, logisticsInfo);

        if (!isShipped(order)) {
            vo.setShipped(false);
            vo.setThirdPartyQuery(false);
            vo.setCurrentStatus("商家待发货，完成后将安排快递发出");
            vo.setTracks(buildPreparingTracks(order));
            return vo;
        }

        vo.setShipped(true);
        List<OrderLogisticsTrackVo> thirdPartyTracks = queryThirdPartyTracks(order, vo, logisticsInfo);
        if (!thirdPartyTracks.isEmpty()) {
            vo.setThirdPartyQuery(true);
            vo.setQueryTip(null);
            vo.setTracks(thirdPartyTracks);
            vo.setCurrentStatus(thirdPartyTracks.get(0).getContext());
            return vo;
        }

        vo.setThirdPartyQuery(false);
        if (StringUtils.isBlank(vo.getQueryTip())) {
            vo.setQueryTip(resolveFallbackQueryTip(logisticsInfo));
        }
        vo.setTracks(buildShippedFallbackTracks(order, logisticsInfo));
        vo.setCurrentStatus(vo.getTracks().get(0).getContext());
        return vo;
    }

    private boolean isLogisticsQueryable(Integer status) {
        if (status == null) {
            return false;
        }
        return status == MerchantOrderStatusGroup.PENDING_CONFIRM
                || status == MerchantOrderStatusGroup.PENDING_USE
                || status == MerchantOrderStatusGroup.PENDING_RECEIVE
                || status == MerchantOrderStatusGroup.COMPLETED;
    }

    private boolean isShipped(OrderEntity order) {
        Integer status = order.getStatus();
        return status != null && (status == MerchantOrderStatusGroup.PENDING_RECEIVE
                || status == MerchantOrderStatusGroup.COMPLETED);
    }

    private OrderLogisticsVo buildBaseVo(OrderEntity order, OrderLogisticsRemarkSupport.LogisticsInfo logisticsInfo) {
        OrderLogisticsVo vo = new OrderLogisticsVo();
        vo.setOrderId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setOrderStatus(order.getStatus());
        vo.setStatusText(StringUtils.defaultIfBlank(order.getStatusText(),
                MerchantOrderStatusSupport.labelOfApiStatus(MerchantOrderStatusSupport.toApiStatus(order))));
        if (logisticsInfo != null) {
            vo.setLogisticsCompany(logisticsInfo.getCompany());
            vo.setLogisticsNo(logisticsInfo.getTrackingNo());
        }
        vo.setReceiverName(order.getReceiverName());
        vo.setReceiverPhone(order.getReceiverPhone());
        vo.setFullAddress(StringUtils.trimToNull(order.getReceiverAddress()));
        return vo;
    }

    private List<OrderLogisticsTrackVo> buildPreparingTracks(OrderEntity order) {
        List<OrderLogisticsTrackVo> tracks = new ArrayList<>();
        if (order.getPayTime() != null) {
            tracks.add(buildTrack(order.getPayTime().format(FORMATTER), "支付成功，商家已收到订单", "paid"));
        }
        tracks.add(buildTrack(formatTime(order.getUpdateTime()), "商家备货中，完成后将安排快递发货", "preparing"));
        tracks.add(buildTrack("", "等待商家发货", "waiting_ship"));
        Collections.reverse(tracks);
        return tracks;
    }

    private List<OrderLogisticsTrackVo> buildShippedFallbackTracks(OrderEntity order,
                                                                   OrderLogisticsRemarkSupport.LogisticsInfo logisticsInfo) {
        List<OrderLogisticsTrackVo> tracks = new ArrayList<>();
        if (Objects.equals(order.getStatus(), MerchantOrderStatusGroup.COMPLETED)) {
            tracks.add(buildTrack(formatTime(order.getUpdateTime()), "用户已确认收货，订单完成", "received"));
        }
        String trackingNo = logisticsInfo != null ? logisticsInfo.getTrackingNo() : "";
        String company = logisticsInfo != null ? logisticsInfo.getCompany() : "";
        if (StringUtils.isNotBlank(trackingNo)) {
            tracks.add(buildTrack(formatTime(order.getUpdateTime()),
                    "包裹已发出（" + StringUtils.defaultString(company, "快递公司") + " " + trackingNo + "），暂无法同步实时轨迹",
                    "shipped"));
        } else {
            tracks.add(buildTrack(formatTime(order.getUpdateTime()), "商家已安排发货，运单号待更新", "shipped"));
        }
        if (order.getPayTime() != null) {
            tracks.add(buildTrack(order.getPayTime().format(FORMATTER), "支付成功，商家开始备货", "paid"));
        }
        tracks.add(buildTrack(formatTime(order.getCreateTime()), "订单已提交", "created"));
        return tracks;
    }

    private List<OrderLogisticsTrackVo> queryThirdPartyTracks(OrderEntity order,
                                                              OrderLogisticsVo vo,
                                                              OrderLogisticsRemarkSupport.LogisticsInfo logisticsInfo) {
        String trackingNo = logisticsInfo != null ? logisticsInfo.getTrackingNo() : "";
        if (StringUtils.isBlank(trackingNo)) {
            vo.setQueryTip("商家已发货，运单号待更新，暂无法查询物流轨迹");
            return Collections.emptyList();
        }
        String company = logisticsInfo != null ? logisticsInfo.getCompany() : "";
        String appCode = resolveAliyunExpressAppCode();
        if (StringUtils.isNotBlank(appCode)) {
            List<OrderLogisticsTrackVo> aliyunTracks = queryAliyunTracks(order, trackingNo, company, appCode, vo);
            if (!aliyunTracks.isEmpty()) {
                return aliyunTracks;
            }
            if (StringUtils.isBlank(vo.getQueryTip())) {
                vo.setQueryTip(QUERY_TIP_QUERY_FAILED);
            }
        } else {
            vo.setQueryTip(QUERY_TIP_NOT_CONFIGURED);
        }
        List<OrderLogisticsTrackVo> kuaidi100Tracks = queryKuaidi100Tracks(order, trackingNo, company);
        if (!kuaidi100Tracks.isEmpty()) {
            vo.setQueryTip(null);
            return kuaidi100Tracks;
        }
        return Collections.emptyList();
    }

    /**
     * 调用阿里云云市场快递查询：GET https://wuliu.market.alicloudapi.com/kdi
     * Header: Authorization: APPCODE {appCode}
     * Query: no=运单号[&type=快递公司代码]
     */
    private List<OrderLogisticsTrackVo> queryAliyunTracks(OrderEntity order,
                                                          String trackingNo,
                                                          String company,
                                                          String appCode,
                                                          OrderLogisticsVo vo) {
        String apiUrl = resolveAliyunExpressApiUrl();
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "APPCODE " + appCode);
        Map<String, String> params = new HashMap<>();
        String companyCode = resolveCompanyCode(company, ALIYUN_COMPANY_CODE_MAP);
        params.put("no", buildAliyunTrackingNo(trackingNo, company, companyCode, order));
        if (StringUtils.isNotBlank(companyCode)) {
            params.put("type", companyCode);
        }
        try {
            String responseBody = safeHttpGet(apiUrl, params, headers);
            if (StringUtils.isBlank(responseBody)) {
                return Collections.emptyList();
            }
            JSONObject responseJson = JSONObject.parseObject(responseBody);
            String apiStatus = responseJson != null ? StringUtils.defaultString(responseJson.getString("status")) : "";
            if (responseJson == null || !"0".equals(apiStatus)) {
                String apiMsg = responseJson != null ? responseJson.getString("msg") : null;
                log.warn("阿里云物流查询失败 orderId={} status={} msg={}", order.getId(), apiStatus, apiMsg);
                if (vo != null) {
                    vo.setQueryTip(mapAliyunErrorTip(apiStatus, apiMsg));
                }
                return Collections.emptyList();
            }
            JSONObject result = responseJson.getJSONObject("result");
            JSONArray list = result != null ? result.getJSONArray("list") : null;
            if (list == null || list.isEmpty()) {
                if (vo != null) {
                    vo.setQueryTip("暂未查询到物流轨迹，请稍后再试或到快递公司官网查询");
                }
                return Collections.emptyList();
            }
            if (vo != null && StringUtils.isBlank(vo.getLogisticsCompany()) && result != null) {
                vo.setLogisticsCompany(StringUtils.trimToNull(result.getString("expName")));
            }
            String deliveryStatus = result != null ? result.getString("deliverystatus") : null;
            List<OrderLogisticsTrackVo> tracks = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                JSONObject item = list.getJSONObject(i);
                if (item == null) {
                    continue;
                }
                OrderLogisticsTrackVo track = new OrderLogisticsTrackVo();
                track.setTime(item.getString("time"));
                track.setContext(StringUtils.defaultIfBlank(item.getString("status"), item.getString("context")));
                track.setStatus(i == 0 ? mapDeliveryStatus(deliveryStatus) : "transit");
                tracks.add(track);
            }
            return tracks;
        } catch (Exception e) {
            log.warn("查询阿里云物流失败 orderId={}", order.getId(), e);
            return Collections.emptyList();
        }
    }

    /** 顺丰等需「运单号:手机号后四位」格式 */
    private String buildAliyunTrackingNo(String trackingNo, String company, String companyCode, OrderEntity order) {
        if (StringUtils.isBlank(trackingNo) || trackingNo.contains(":")) {
            return trackingNo;
        }
        boolean sfExpress = "sf".equalsIgnoreCase(companyCode)
                || StringUtils.containsAny(StringUtils.defaultString(company), "顺丰", "SF");
        if (!sfExpress) {
            return trackingNo;
        }
        String phone = StringUtils.defaultIfBlank(order.getReceiverPhone(), order.getContactPhone());
        if (StringUtils.isNotBlank(phone) && phone.length() >= 4) {
            return trackingNo + ":" + phone.substring(phone.length() - 4);
        }
        return trackingNo;
    }

    private String mapAliyunErrorTip(String apiStatus, String apiMsg) {
        if (StringUtils.isBlank(apiStatus)) {
            return QUERY_TIP_QUERY_FAILED;
        }
        switch (apiStatus) {
            case "201":
                return "快递单号有误，请核对商户填写的运单号";
            case "203":
                return "快递公司不存在，请确认商户选择的物流公司";
            case "204":
                return "未能识别快递公司，请商户补充正确的物流公司名称";
            case "205":
                return "暂未查询到物流信息，包裹可能刚发出，请稍后再试";
            case "207":
                return "该运单号暂无法查询，请复制单号到快递公司官网查询";
            default:
                if (StringUtils.isNotBlank(apiMsg) && !"ok".equalsIgnoreCase(apiMsg)) {
                    return apiMsg;
                }
                return QUERY_TIP_QUERY_FAILED;
        }
    }

    private String mapDeliveryStatus(String deliveryStatus) {
        if (StringUtils.isBlank(deliveryStatus)) {
            return "transit";
        }
        switch (deliveryStatus) {
            case "0":
                return "collected";
            case "1":
                return "transit";
            case "2":
                return "delivering";
            case "3":
                return "signed";
            case "4":
                return "failed";
            case "5":
                return "problem";
            case "6":
                return "returned";
            default:
                return "transit";
        }
    }

    private String resolveAliyunExpressApiUrl() {
        for (String key : Arrays.asList(
                SysConfigConstant.Logistics.ALIYUN_EXPRESS_API_URL,
                "aliyun_express_api_url",
                "aiphoto_aliyun_express_api_url"
        )) {
            String value = getConfigValue(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        return DEFAULT_ALIYUN_EXPRESS_API_URL;
    }

    private List<OrderLogisticsTrackVo> queryKuaidi100Tracks(OrderEntity order, String trackingNo, String company) {
        if (StringUtils.isAnyBlank(company, trackingNo)) {
            return Collections.emptyList();
        }
        String customer = getConfigValue("kuaidi100_customer");
        String key = getConfigValue("kuaidi100_key");
        if (StringUtils.isAnyBlank(customer, key)) {
            return Collections.emptyList();
        }
        String companyCode = resolveCompanyCode(company, KUAIDI100_COMPANY_CODE_MAP);
        if (StringUtils.isBlank(companyCode)) {
            log.warn("未识别物流公司编码 company={}", company);
            return Collections.emptyList();
        }
        try {
            JSONObject param = new JSONObject();
            param.put("com", companyCode);
            param.put("num", trackingNo);
            if (StringUtils.isNotBlank(order.getReceiverPhone()) && order.getReceiverPhone().length() >= 4) {
                param.put("phone", order.getReceiverPhone().substring(order.getReceiverPhone().length() - 4));
            }
            String paramJson = param.toJSONString();
            String sign = Md5Utils.md5Encrypt32Upper(paramJson + key + customer);
            Map<String, String> form = new HashMap<>();
            form.put("customer", customer);
            form.put("sign", sign);
            form.put("param", paramJson);
            String responseBody = OkHttpUtil.doPostForm(KUAIDI100_QUERY_URL, form);
            JSONObject responseJson = JSONObject.parseObject(responseBody);
            if (responseJson == null || !"200".equals(responseJson.getString("status"))) {
                log.warn("快递100查询失败 orderId={} response={}", order.getId(), responseBody);
                return Collections.emptyList();
            }
            JSONArray data = responseJson.getJSONArray("data");
            if (data == null || data.isEmpty()) {
                return Collections.emptyList();
            }
            List<OrderLogisticsTrackVo> tracks = new ArrayList<>();
            for (int i = 0; i < data.size(); i++) {
                JSONObject item = data.getJSONObject(i);
                OrderLogisticsTrackVo track = new OrderLogisticsTrackVo();
                track.setTime(item.getString("ftime"));
                track.setContext(item.getString("context"));
                track.setStatus(item.getString("status"));
                tracks.add(track);
            }
            return tracks;
        } catch (Exception e) {
            log.warn("查询快递100物流失败 orderId={}", order.getId(), e);
            return Collections.emptyList();
        }
    }

    private String resolveFallbackQueryTip(OrderLogisticsRemarkSupport.LogisticsInfo logisticsInfo) {
        String trackingNo = logisticsInfo != null ? logisticsInfo.getTrackingNo() : "";
        if (StringUtils.isBlank(trackingNo)) {
            return "商家已发货，运单号待更新，暂无法查询物流轨迹";
        }
        if (StringUtils.isBlank(resolveAliyunExpressAppCode())) {
            return QUERY_TIP_NOT_CONFIGURED;
        }
        return QUERY_TIP_QUERY_FAILED;
    }

    private String resolveAliyunExpressAppCode() {
        for (String key : Arrays.asList(
                SysConfigConstant.Logistics.ALIYUN_EXPRESS_APP_CODE,
                "aiphoto_aliyun_express_appcode",
                "aliyun_express_appcode",
                "阿里云快递查询AppCode"
        )) {
            String value = getConfigValue(key);
            if (StringUtils.isNotBlank(value)) {
                return value;
            }
        }
        SysConfigEntity config = sysConfigDao.selectOne(new LambdaQueryWrapper<SysConfigEntity>()
                .like(SysConfigEntity::getRemark, "阿里云快递")
                .last("limit 1"));
        return config != null ? StringUtils.trimToEmpty(config.getParamValue()) : "";
    }

    private String resolveCompanyCode(String companyName, Map<String, String> companyCodeMap) {
        if (StringUtils.isBlank(companyName)) {
            return null;
        }
        String trimmed = companyName.trim();
        if (companyCodeMap.containsKey(trimmed)) {
            return companyCodeMap.get(trimmed);
        }
        for (Map.Entry<String, String> entry : companyCodeMap.entrySet()) {
            if (trimmed.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    private OrderLogisticsTrackVo buildTrack(String time, String context, String status) {
        OrderLogisticsTrackVo track = new OrderLogisticsTrackVo();
        track.setTime(StringUtils.defaultIfBlank(time, "待更新"));
        track.setContext(context);
        track.setStatus(status);
        return track;
    }

    private String formatTime(java.time.LocalDateTime time) {
        return time != null ? time.format(FORMATTER) : "";
    }

    private String safeHttpGet(String url, Map<String, String> params, Map<String, String> headers) {
        try {
            return OkHttpUtil.doGet(url, params, headers);
        } catch (Exception e) {
            log.warn("HTTP GET 失败 url={}", url, e);
            return null;
        }
    }

    private String getConfigValue(String key) {
        SysConfigEntity config = sysConfigDao.selectOne(new LambdaQueryWrapper<SysConfigEntity>()
                .eq(SysConfigEntity::getParamKey, key).last("limit 1"));
        return config != null ? StringUtils.trimToEmpty(config.getParamValue()) : "";
    }
}

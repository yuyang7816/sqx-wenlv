package com.sqx.app.lvyou.transport.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.lvyou.transport.config.BusRealtimeProperties;
import com.sqx.app.lvyou.transport.support.BusRealtimeConfigSupport;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * apizero 实时公交 HTTP 客户端
 */
@Slf4j
@Component
public class BusRealtimeClient {

    private static final MediaType JSON_MEDIA = MediaType.get("application/json; charset=utf-8");

    private final OkHttpClient httpClient = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(20, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS)
            .build();

    @Resource
    private BusRealtimeProperties properties;

    @Resource
    private BusRealtimeConfigSupport busRealtimeConfigSupport;

    public JSONObject post(Map<String, Object> payload) {
        if (!busRealtimeConfigSupport.isEnabled()) {
            return errorBody(-1, "实时公交未启用");
        }
        String apiKey = busRealtimeConfigSupport.getApiKey();
        if (StringUtils.isBlank(apiKey)) {
            return errorBody(-1, "实时公交 API Key 未配置，请在管理后台「平台配置 → 公共交通」填写");
        }
        String url = properties.getBaseUrl();
        String json = JSON.toJSONString(payload);
        RequestBody body = RequestBody.create(json, JSON_MEDIA);
        Request.Builder builder = new Request.Builder().url(url).post(body);
        builder.header("Authorization", "Bearer " + apiKey);
        builder.header("Content-Type", "application/json");
        try (Response response = httpClient.newCall(builder.build()).execute()) {
            ResponseBody responseBody = response.body();
            if (responseBody == null) {
                log.warn("bus-realtime empty body, http={}", response.code());
                return errorBody(response.code(), "实时公交响应为空");
            }
            String text = responseBody.string();
            if (StringUtils.isBlank(text)) {
                return errorBody(response.code(), "实时公交响应为空");
            }
            return JSON.parseObject(text);
        } catch (Exception ex) {
            log.warn("bus-realtime request failed: {}", ex.getMessage());
            return errorBody(-1, "实时公交请求失败");
        }
    }

    public JSONObject cities() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "cities");
        return post(payload);
    }

    public JSONObject stations(String city, String keyword) {
        return stations(city, keyword, null, null);
    }

    public JSONObject stations(String city, String keyword, String lat, String lng) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "stations");
        payload.put("city", city);
        payload.put("station", StringUtils.defaultString(keyword));
        if (StringUtils.isNotBlank(lat) && StringUtils.isNotBlank(lng)) {
            payload.put("lat", lat);
            payload.put("lng", lng);
        }
        return post(payload);
    }

    public JSONObject query(String city, String station, int direction) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "query");
        payload.put("city", city);
        payload.put("station", station);
        payload.put("direction", String.valueOf(direction));
        return post(payload);
    }

    public JSONObject line(String city, String line, int direction) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("type", "line");
        payload.put("city", city);
        payload.put("line", line);
        payload.put("direction", String.valueOf(direction));
        return post(payload);
    }

    private JSONObject errorBody(int httpCode, String msg) {
        JSONObject result = new JSONObject();
        result.put("code", httpCode == 200 ? 5000 : httpCode);
        result.put("msg", msg);
        result.put("data", null);
        return result;
    }
}

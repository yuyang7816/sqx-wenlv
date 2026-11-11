package com.sqx.app.lvyou.transport.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.sqx.app.lvyou.transport.domain.vo.TransportDirectionVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportRealtimeUpdateVo;
import com.sqx.app.lvyou.transport.domain.vo.TransportTrackInfoVo;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 公交 JSON 字段解析工具
 */
public final class TransportJsonUtil {

    private TransportJsonUtil() {
    }

    public static List<TransportDirectionVo> parseDirections(String raw) {
        List<TransportDirectionVo> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        try {
            List<JSONObject> items = JSON.parseArray(raw.trim(), JSONObject.class);
            if (items == null) {
                return result;
            }
            for (JSONObject item : items) {
                if (item == null) {
                    continue;
                }
                TransportDirectionVo vo = new TransportDirectionVo();
                vo.setLabel(item.getString("label"));
                vo.setFrom(item.getString("from"));
                vo.setTo(item.getString("to"));
                vo.setFirstBus(item.getString("firstBus"));
                vo.setLastBus(item.getString("lastBus"));
                vo.setInterval(item.getString("interval"));
                result.add(vo);
            }
        } catch (Exception ignored) {
            // ignore invalid json
        }
        return result;
    }

    public static TransportTrackInfoVo parseTrackInfo(String raw) {
        if (StringUtils.isBlank(raw)) {
            return null;
        }
        try {
            JSONObject item = JSON.parseObject(raw.trim());
            if (item == null) {
                return null;
            }
            TransportTrackInfoVo vo = new TransportTrackInfoVo();
            vo.setCurrentStation(item.getString("currentStation"));
            vo.setNextStation(item.getString("nextStation"));
            vo.setEta(item.getString("eta"));
            vo.setSpeed(item.getString("speed"));
            vo.setVehicleNo(item.getString("vehicleNo"));
            return vo;
        } catch (Exception ignored) {
            return null;
        }
    }

    public static List<TransportRealtimeUpdateVo> parseRealtimeUpdates(String raw) {
        List<TransportRealtimeUpdateVo> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        try {
            List<JSONObject> items = JSON.parseArray(raw.trim(), JSONObject.class);
            if (items == null) {
                return result;
            }
            for (JSONObject item : items) {
                if (item == null) {
                    continue;
                }
                TransportRealtimeUpdateVo vo = new TransportRealtimeUpdateVo();
                vo.setId(item.getString("id"));
                vo.setLabel(item.getString("label"));
                vo.setValue(item.getString("value"));
                vo.setTime(item.getString("time"));
                result.add(vo);
            }
        } catch (Exception ignored) {
            // ignore invalid json
        }
        return result;
    }

    public static List<String> parseTimes(String raw) {
        List<String> result = new ArrayList<>();
        if (StringUtils.isBlank(raw)) {
            return result;
        }
        try {
            List<String> items = JSON.parseArray(raw.trim(), String.class);
            if (items != null) {
                for (String item : items) {
                    if (StringUtils.isNotBlank(item)) {
                        result.add(item.trim());
                    }
                }
            }
        } catch (Exception ignored) {
            // ignore invalid json
        }
        return result;
    }

    public static String toJson(Object value) {
        if (value == null) {
            return null;
        }
        return JSON.toJSONString(value);
    }
}

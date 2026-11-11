package com.sqx.app.base.map.strategy;

import com.alibaba.fastjson.JSONObject;
import com.sqx.app.base.map.domain.vo.AddressVo;
import com.sqx.app.base.map.support.MapCoordinateUtil;
import com.sqx.common.core.exception.BizException;
import com.sqx.common.core.utils.http.OkHttpUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * BigDataCloud 开放逆地理编码（无 Key，主平台失败时兜底）
 */
public class BigDataCloudMapPlatformStrategy implements IMapPlatformStrategy {

    @Override
    public AddressVo reverseGeocode(String lat, String lng) {
        if (StringUtils.isBlank(lat) || StringUtils.isBlank(lng)) {
            throw new IllegalArgumentException("经纬度不能为空");
        }

        String api = "https://api.bigdatacloud.net/data/reverse-geocode-client";
        String[] wgs84 = MapCoordinateUtil.gcj02ToWgs84String(lat, lng);
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("latitude", wgs84 == null ? lat : wgs84[0]);
        paramMap.put("longitude", wgs84 == null ? lng : wgs84[1]);
        paramMap.put("localityLanguage", "zh");

        Map<String, String> headers = new HashMap<>();
        headers.put("User-Agent", "rongeyou-app/1.0");

        String resp = OkHttpUtil.doGet(api, paramMap, headers);
        if (StringUtils.isBlank(resp)) {
            throw new BizException("备用地图解析失败: empty response");
        }

        JSONObject respJson = JSONObject.parseObject(resp);
        String province = respJson.getString("principalSubdivision");
        String city = respJson.getString("city");
        String district = respJson.getString("locality");

        AddressVo vo = new AddressVo();
        vo.setCountry(respJson.getString("countryName"));
        vo.setProvince(province);
        vo.setCity(city);
        vo.setDistrict(district);
        vo.setAddress(joinAddress(province, city, district));
        return vo;
    }

    private String joinAddress(String province, String city, String district) {
        StringBuilder builder = new StringBuilder();
        appendPart(builder, province);
        appendPart(builder, city);
        appendPart(builder, district);
        return builder.toString();
    }

    private void appendPart(StringBuilder builder, String part) {
        if (org.apache.commons.lang3.StringUtils.isBlank(part)) {
            return;
        }
        if (builder.indexOf(part) >= 0) {
            return;
        }
        builder.append(part);
    }
}

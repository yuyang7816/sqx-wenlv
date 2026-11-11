package com.sqx.app.lvyou.search.support;

import com.sqx.app.base.enums.AppError;
import com.sqx.common.core.exception.BizException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 搜索参数解析：客户端仅传 ASCII keywordCode，服务端映射为中文检索词
 */
@Component
public class SearchSupport {

    private static final Pattern KEYWORD_CODE_PATTERN = Pattern.compile("^[a-z][a-z0-9_-]{0,63}$");

    private static final Map<String, String> KEYWORD_CODE_TO_LABEL;

    static {
        Map<String, String> map = new HashMap<>();
        map.put("wanling_guzhen", "万灵古镇");
        map.put("rongchang_lue", "卤鹅");
        map.put("hotel_stay", "酒店住宿");
        map.put("heritage_experience", "非遗体验");
        map.put("antao_town", "安陶小镇");
        map.put("bus_route", "公交线路");
        map.put("guchang_town", "古昌镇");
        map.put("rongchang_museum", "博物馆");
        map.put("scenic", "景区");
        map.put("hotel", "酒店");
        map.put("route", "线路");
        map.put("heritage", "非遗");
        KEYWORD_CODE_TO_LABEL = Collections.unmodifiableMap(map);
    }

    /**
     * @param keywordCode 客户端搜索编码（仅 ASCII）
     * @return 服务端用于 LIKE 检索的中文关键词
     */
    public String resolveKeywordLabel(String keywordCode) {
        if (StringUtils.isBlank(keywordCode)) {
            throw new BizException(AppError.SEARCH_KEYWORD_CODE_REQUIRED);
        }
        String normalized = keywordCode.trim().toLowerCase();
        if (!KEYWORD_CODE_PATTERN.matcher(normalized).matches()) {
            throw new BizException(AppError.SEARCH_KEYWORD_CODE_INVALID);
        }
        return KEYWORD_CODE_TO_LABEL.getOrDefault(normalized, normalized);
    }
}

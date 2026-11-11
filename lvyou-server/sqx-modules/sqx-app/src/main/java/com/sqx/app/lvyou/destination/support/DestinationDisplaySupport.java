package com.sqx.app.lvyou.destination.support;

import com.sqx.app.lvyou.destination.domain.entity.DestinationEntity;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * 目的地展示文案：目的地已删除或不存在时，回退为业务地址。
 */
public final class DestinationDisplaySupport {

    private DestinationDisplaySupport() {
    }

    public static String resolveLabel(String destinationName, String... addressFallbacks) {
        if (StringUtils.isNotBlank(destinationName)) {
            return destinationName.trim();
        }
        if (addressFallbacks != null) {
            for (String fallback : addressFallbacks) {
                if (StringUtils.isNotBlank(fallback)) {
                    return fallback.trim();
                }
            }
        }
        return "";
    }

    public static String resolveLabel(Map<Long, String> nameMap, Long destinationId, String... addressFallbacks) {
        String name = null;
        if (destinationId != null && nameMap != null) {
            name = nameMap.get(destinationId);
        }
        return resolveLabel(name, addressFallbacks);
    }

    public static String resolveLabel(DestinationEntity destination, String... addressFallbacks) {
        return resolveLabel(destination == null ? null : destination.getName(), addressFallbacks);
    }
}

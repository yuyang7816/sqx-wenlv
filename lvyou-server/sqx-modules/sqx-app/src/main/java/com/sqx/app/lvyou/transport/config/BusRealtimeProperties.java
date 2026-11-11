package com.sqx.app.lvyou.transport.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * apizero 实时公交配置
 */
@Data
@Component
@ConfigurationProperties(prefix = "sqx.bus-realtime")
public class BusRealtimeProperties {

    /** API 网关地址 */
    private String baseUrl = "https://v1.apizero.cn/api/bus-realtime";
}

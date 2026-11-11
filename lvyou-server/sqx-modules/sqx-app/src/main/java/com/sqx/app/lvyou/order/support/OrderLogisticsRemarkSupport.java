package com.sqx.app.lvyou.order.support;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * 从订单备注解析物流信息（与前端 order-ui.js 保持一致）
 */
public final class OrderLogisticsRemarkSupport {

    private static final String MARKER = "物流信息：";

    private OrderLogisticsRemarkSupport() {
    }

    @Getter
    public static class LogisticsInfo {
        private final String company;
        private final String trackingNo;
        private final String raw;

        public LogisticsInfo(String company, String trackingNo, String raw) {
            this.company = company;
            this.trackingNo = trackingNo;
            this.raw = raw;
        }
    }

    public static LogisticsInfo parseFromRemark(String remark) {
        String text = StringUtils.defaultString(remark);
        int idx = text.indexOf(MARKER);
        if (idx < 0) {
            return null;
        }
        String segment = text.substring(idx + MARKER.length());
        int semicolon = segment.indexOf('；');
        if (semicolon >= 0) {
            segment = segment.substring(0, semicolon);
        }
        segment = segment.trim();
        if (segment.isEmpty()) {
            return null;
        }
        String[] parts = segment.split("\\s+");
        if (parts.length >= 2) {
            String trackingNo = parts[parts.length - 1];
            StringBuilder companyBuilder = new StringBuilder();
            for (int i = 0; i < parts.length - 1; i++) {
                if (i > 0) {
                    companyBuilder.append(' ');
                }
                companyBuilder.append(parts[i]);
            }
            return new LogisticsInfo(companyBuilder.toString(), trackingNo, segment);
        }
        return new LogisticsInfo("", segment, segment);
    }
}

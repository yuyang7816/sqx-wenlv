package com.sqx.common.file.utils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author chengming
 * @since 2025/4/29
 */
public class FileUtil {

    /**
     * 获取文件扩展名
     * @param filename 文件名
     * @return 文件扩展名
     */
    public static String getFileExtension(String filename) {
        if (StringUtils.isBlank(filename)) {
            return "";
        }

        int dotIndex = filename.lastIndexOf(46);
        return -1 == dotIndex ? "" : filename.substring(dotIndex + 1);
    }

    /**
     * 对象存储 key / 对外 URL 路径统一使用正斜杠（避免 Windows File.separator 污染 URL）
     */
    public static String toPublicPath(String... segments) {
        if (segments == null || segments.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (String segment : segments) {
            if (StringUtils.isBlank(segment)) {
                continue;
            }
            String normalized = segment.replace('\\', '/');
            for (String part : normalized.split("/")) {
                if (StringUtils.isBlank(part)) {
                    continue;
                }
                if (builder.length() > 0) {
                    builder.append('/');
                }
                builder.append(part);
            }
        }
        return builder.toString();
    }

    /**
     * 拼接 CDN/域名与对象路径，输出可直接访问的 URL
     */
    public static String buildPublicUrl(String domain, String objectPath) {
        if (StringUtils.isBlank(objectPath)) {
            return "";
        }
        String path = objectPath.replace('\\', '/');
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        if (StringUtils.isBlank(domain)) {
            return path.startsWith("/") ? path : "/" + path;
        }
        String base = domain.trim().replaceAll("/+$", "");
        String relative = path.startsWith("/") ? path : "/" + path;
        return base + relative;
    }
}

package com.sqx.common.shiro.support;

import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户权限字符串解析
 */
public final class UserPermissionSupport {

    private UserPermissionSupport() {
    }

    public static Set<String> toPermissionSet(Collection<String> permsList) {
        Set<String> permsSet = new HashSet<>();
        if (permsList == null) {
            return permsSet;
        }
        for (String perms : permsList) {
            if (StringUtils.isBlank(perms)) {
                continue;
            }
            Arrays.stream(perms.trim().split(","))
                    .map(String::trim)
                    .filter(StringUtils::isNotBlank)
                    .forEach(permsSet::add);
        }
        return permsSet;
    }
}

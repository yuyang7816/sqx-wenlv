package com.sqx.common.shiro.service;

import java.util.Set;

/**
 * 从数据库加载用户权限（由 sqx-system 模块实现，避免权限 Redis 缓存过期导致鉴权失败）
 */
public interface UserPermissionLoader {

    Set<String> loadPermissions(long userId);
}

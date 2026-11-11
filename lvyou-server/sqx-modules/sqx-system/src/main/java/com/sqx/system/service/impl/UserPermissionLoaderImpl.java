package com.sqx.system.service.impl;

import com.alibaba.fastjson2.JSONObject;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.common.shiro.constant.ShiroConstant;
import com.sqx.common.shiro.model.AuthSysMenu;
import com.sqx.common.shiro.service.UserPermissionLoader;
import com.sqx.common.shiro.support.UserPermissionSupport;
import com.sqx.system.db.mapper.SysUserDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 每次鉴权/导航时从数据库加载最新权限，并同步 Redis 缓存
 */
@Component
public class UserPermissionLoaderImpl implements UserPermissionLoader {

    @Resource
    private SysUserDao sysUserDao;

    @Override
    public Set<String> loadPermissions(long userId) {
        List<String> permsList = sysUserDao.queryAllPerms(userId);
        Set<String> permissions = UserPermissionSupport.toPermissionSet(permsList);
        cachePermissions(userId, permsList);
        return permissions;
    }

    private void cachePermissions(long userId, List<String> permsList) {
        List<AuthSysMenu> menuObjects = new ArrayList<>();
        if (permsList != null) {
            for (String perm : permsList) {
                if (StringUtils.isNotBlank(perm)) {
                    AuthSysMenu menu = new AuthSysMenu();
                    menu.setPerms(perm.trim());
                    menuObjects.add(menu);
                }
            }
        }
        RedisUtil.setCacheObject(
                String.format(ShiroConstant.AUTH_USER_MENU, userId),
                JSONObject.toJSONString(menuObjects)
        );
    }
}

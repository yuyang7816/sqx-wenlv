package com.sqx.common.shiro.service.impl;


import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.sqx.common.redis.utils.RedisUtil;
import com.sqx.common.shiro.constant.ShiroConstant;
import com.sqx.common.shiro.model.AuthSysMenu;
import com.sqx.common.shiro.service.ShiroService;
import com.sqx.common.shiro.service.UserPermissionLoader;
import com.sqx.common.shiro.support.UserPermissionSupport;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author chengming
 * @since 2024/7/11
 */
@Service
public class ShiroServiceImpl implements ShiroService {

    @Autowired(required = false)
    private UserPermissionLoader userPermissionLoader;

    @Override
    public Set<String> getUserPermissions(long userId) {
        if (userPermissionLoader != null) {
            return userPermissionLoader.loadPermissions(userId);
        }
        return loadPermissionsFromCache(userId);
    }

    private Set<String> loadPermissionsFromCache(long userId) {
        List<String> permsList = new ArrayList<>();
        String cacheKey = String.format(ShiroConstant.AUTH_USER_MENU, userId);
        String menuListJson = RedisUtil.getCacheObject(cacheKey);
        if (StringUtils.isNotBlank(menuListJson)) {
            List<AuthSysMenu> menuList = JSONObject.parseObject(menuListJson, new TypeReference<List<AuthSysMenu>>() {});
            if (menuList != null) {
                for (AuthSysMenu menu : menuList) {
                    if (StringUtils.isNotBlank(menu.getPerms())) {
                        permsList.add(menu.getPerms());
                    }
                }
            }
        }
        return UserPermissionSupport.toPermissionSet(permsList);
    }
}

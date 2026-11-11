package com.sqx.app.base.auth.auth.annotation;

import java.lang.annotation.*;

/**
 * 用户端公开接口标记：无需 App JWT 即可访问（显式白名单）。
 * /app/** 默认须登录；标注本注解的方法或 Controller 类可匿名访问。
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PublicAccess {
}

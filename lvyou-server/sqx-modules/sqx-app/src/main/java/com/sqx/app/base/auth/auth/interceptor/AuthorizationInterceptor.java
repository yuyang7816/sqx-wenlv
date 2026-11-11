package com.sqx.app.base.auth.auth.interceptor;


import com.sqx.app.base.auth.auth.annotation.PublicAccess;
import com.sqx.common.core.exception.SqxException;
import com.sqx.common.shiro.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * App JWT 鉴权拦截器：/app/** 默认须登录，{@link PublicAccess} 标注的接口为显式白名单。
 */
@Component
public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
    @Autowired
    private JwtUtil jwtUtil;

    public static final String USER_KEY = "userId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        String token = resolveToken(request);
        if (isPublicAccess(handlerMethod)) {
            attachUserIdIfValid(request, token);
            return true;
        }

        //凭证为空
        if (StringUtils.isBlank(token)) {
            throw new SqxException(jwtUtil.getHeader() + "不能为空", HttpStatus.UNAUTHORIZED.value());
        }

        Claims claims = jwtUtil.getClaimByToken(token);
        if (claims == null || jwtUtil.isTokenExpired(claims.getExpiration())) {
            throw new SqxException(jwtUtil.getHeader() + "失效，请重新登录", HttpStatus.UNAUTHORIZED.value());
        }

        //设置userId到request里，后续根据userId，获取用户信息
        request.setAttribute(USER_KEY, Long.valueOf(claims.getSubject()));

        return true;
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(jwtUtil.getHeader());
        if (StringUtils.isBlank(token)) {
            token = request.getParameter(jwtUtil.getHeader());
        }
        return token;
    }

    /**
     * 公开接口可选解析 token，便于详情页返回登录态相关字段（如是否已报名）。
     */
    private void attachUserIdIfValid(HttpServletRequest request, String token) {
        if (StringUtils.isBlank(token)) {
            return;
        }
        Claims claims = jwtUtil.getClaimByToken(token);
        if (claims == null || jwtUtil.isTokenExpired(claims.getExpiration())) {
            return;
        }
        request.setAttribute(USER_KEY, Long.valueOf(claims.getSubject()));
    }

    private boolean isPublicAccess(HandlerMethod handlerMethod) {
        if (handlerMethod.getMethodAnnotation(PublicAccess.class) != null) {
            return true;
        }
        return handlerMethod.getBeanType().getAnnotation(PublicAccess.class) != null;
    }
}

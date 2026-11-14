package com.campus.market.config;

import com.campus.market.common.BizException;
import com.campus.market.util.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 登录拦截器：有 token 就解析出当前用户，标记 @RequireLogin 的接口强制校验。
 */
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            try {
                Claims claims = jwtUtil.parse(header.substring(7));
                CurrentUser.set(Long.valueOf(claims.getSubject()), claims.get("username", String.class));
            } catch (Exception ignored) {
                // token 失效按未登录处理
            }
        }

        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            boolean needLogin = method.hasMethodAnnotation(RequireLogin.class)
                    || method.getBeanType().isAnnotationPresent(RequireLogin.class);
            if (needLogin && CurrentUser.getUserId() == null) {
                throw new BizException(401, "请先登录");
            }
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        CurrentUser.clear();
    }
}

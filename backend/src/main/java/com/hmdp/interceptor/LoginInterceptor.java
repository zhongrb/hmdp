package com.hmdp.interceptor;

import com.hmdp.exception.UnauthorizedException;
import com.hmdp.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 这里依赖刷新拦截器预先写入 UserHolder；只要当前线程没有登录用户，就统一按未登录处理。
        if (UserHolder.getUser() == null) {
            throw new UnauthorizedException("请先完成登录后再继续操作");
        }
        return true;
    }
}

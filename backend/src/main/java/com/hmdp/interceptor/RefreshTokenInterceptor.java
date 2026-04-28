package com.hmdp.interceptor;

import com.hmdp.config.AuthProperties;
import com.hmdp.dto.UserDTO;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class RefreshTokenInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;
    private final AuthProperties authProperties;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = resolveToken(request);
        if (!StringUtils.hasText(token)) {
            return true;
        }

        String tokenKey = RedisConstants.LOGIN_TOKEN_KEY + token;
        Map<Object, Object> entries = stringRedisTemplate.opsForHash().entries(tokenKey);
        if (entries.isEmpty()) {
            return true;
        }

        UserDTO user = UserDTO.builder()
                .id(parseLong(entries.get("userId")))
                .nickName((String) entries.get("nickName"))
                .icon((String) entries.get("icon"))
                .build();
        UserHolder.saveUser(user);
        stringRedisTemplate.expire(tokenKey, Duration.ofMinutes(authProperties.getTokenTtlMinutes()));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        UserHolder.removeUser();
    }

    private String resolveToken(HttpServletRequest request) {
        String token = request.getHeader(authProperties.getTokenHeader());
        if (!StringUtils.hasText(token)) {
            return null;
        }
        String prefix = authProperties.getTokenPrefix();
        if (StringUtils.hasText(prefix) && token.startsWith(prefix + " ")) {
            return token.substring(prefix.length() + 1);
        }
        return token;
    }

    private Long parseLong(Object value) {
        return value == null ? null : Long.parseLong(String.valueOf(value));
    }
}

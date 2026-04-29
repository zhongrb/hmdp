package com.hmdp.filter;

import com.hmdp.utils.RedisConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class UvRecordFilter extends OncePerRequestFilter {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String visitorId = request.getHeader("X-Visitor-Id");
        // 统计 UV 时优先复用前端传入的访客标识，没有则退化到 IP，再退化为本次请求的临时标识。
        if (!StringUtils.hasText(visitorId)) {
            visitorId = request.getRemoteAddr();
        }
        if (!StringUtils.hasText(visitorId)) {
            visitorId = UUID.randomUUID().toString();
        }
        String key = RedisConstants.UV_KEY + LocalDate.now().format(DATE_FORMATTER);
        // HyperLogLog 只关注去重后的访客规模，不要求逐条可追溯，适合这种按天聚合的 UV 统计。
        stringRedisTemplate.opsForHyperLogLog().add(key, visitorId);
        log.info("记录访客 UV，visitorId={}, key={}", visitorId, key);
        filterChain.doFilter(request, response);
    }
}

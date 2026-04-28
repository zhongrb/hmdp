package com.hmdp.config;

import com.hmdp.interceptor.LoginInterceptor;
import com.hmdp.interceptor.RefreshTokenInterceptor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private static final List<String> DEFAULT_EXCLUDE_PATHS = List.of(
            "/error",
            "/favicon.ico"
    );

    private final AuthProperties authProperties;
    private final RefreshTokenInterceptor refreshTokenInterceptor;
    private final LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(refreshTokenInterceptor).addPathPatterns("/**");

        registry.addInterceptor(loginInterceptor)
                .excludePathPatterns(authProperties.getPublicPaths())
                .excludePathPatterns(DEFAULT_EXCLUDE_PATHS);
    }
}

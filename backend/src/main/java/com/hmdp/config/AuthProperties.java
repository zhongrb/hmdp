package com.hmdp.config;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.auth")
public class AuthProperties {

    private String tokenHeader = "authorization";
    private String tokenPrefix = "Bearer";
    private long tokenTtlMinutes = 30;
    private long refreshThresholdMinutes = 10;
    // 这里维护的是“允许匿名访问”的接口路径，未命中的请求默认进入登录拦截链路。
    private List<String> publicPaths = new ArrayList<>();
}

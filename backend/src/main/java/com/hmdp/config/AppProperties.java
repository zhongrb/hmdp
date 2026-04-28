package com.hmdp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private final Sms sms = new Sms();
    private final Geo geo = new Geo();

    @Data
    public static class Sms {
        private String provider = "mock";
        private String signName = "本地生活";
        private int codeTtlMinutes = 5;
        private int codeLength = 6;
        private String accessKey;
        private String secretKey;
        private String templateCode;
    }

    @Data
    public static class Geo {
        private int nearbyPageSize = 10;
    }
}

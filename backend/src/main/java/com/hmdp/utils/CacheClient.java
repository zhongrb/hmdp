package com.hmdp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.exception.BizException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheClient {

    // 用固定占位值缓存空结果，避免同一个不存在的数据持续穿透到数据库。
    private static final String NULL_PLACEHOLDER = "__null__";

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T queryWithPassThrough(String keyPrefix,
                                      Long id,
                                      Class<T> type,
                                      Duration ttl,
                                      Supplier<T> dbFallback) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        // 先识别空值占位，命中后直接返回，避免重复访问数据库查询不存在的数据。
        if (NULL_PLACEHOLDER.equals(json)) {
            log.info("缓存命中空值占位，key={}", key);
            return null;
        }
        if (StringUtils.hasText(json)) {
            return deserialize(json, type);
        }

        T value = dbFallback.get();
        if (value == null) {
            log.info("数据库未查到数据，写入空值缓存，key={}", key);
            // 空值缓存时间保持较短，既能挡住穿透流量，也能减少真实数据后续补录时的滞后。
            stringRedisTemplate.opsForValue().set(key, NULL_PLACEHOLDER, 2, TimeUnit.MINUTES);
            return null;
        }
        stringRedisTemplate.opsForValue().set(key, serialize(value), ttl);
        log.info("缓存重建完成，key={}", key);
        return value;
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException exception) {
            throw new BizException("缓存数据解析失败");
        }
    }

    private String serialize(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new BizException("缓存数据序列化失败");
        }
    }
}

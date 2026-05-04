package com.hmdp.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hmdp.exception.BizException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;
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

    private static final String NULL_PLACEHOLDER = "__null__";
    private static final Duration NULL_CACHE_TTL = Duration.ofMinutes(2);
    private static final Duration LOCK_TTL = Duration.ofSeconds(10);
    private static final int TTL_JITTER_SECONDS = 300;
    private static final ExecutorService CACHE_REBUILD_EXECUTOR = Executors.newFixedThreadPool(5);

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    public <T> T queryWithPassThrough(String keyPrefix,
                                      Long id,
                                      Class<T> type,
                                      Duration ttl,
                                      Supplier<T> dbFallback) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (NULL_PLACEHOLDER.equals(json)) {
            log.info("缓存命中空值占位，key={}", key);
            return null;
        }
        if (StringUtils.hasText(json)) {
            return deserialize(json, type);
        }

        T value = dbFallback.get();
        if (value == null) {
            cacheNullValue(key);
            return null;
        }
        set(key, value, ttl);
        log.info("缓存重建完成，key={}", key);
        return value;
    }

    public <T> T queryWithLogicalExpire(String keyPrefix,
                                        String lockKeyPrefix,
                                        Long id,
                                        Class<T> type,
                                        Duration logicalExpire,
                                        Supplier<T> dbFallback) {
        String key = keyPrefix + id;
        String json = stringRedisTemplate.opsForValue().get(key);
        if (NULL_PLACEHOLDER.equals(json)) {
            log.info("热点缓存命中空值占位，key={}", key);
            return null;
        }
        if (!StringUtils.hasText(json)) {
            T value = dbFallback.get();
            if (value == null) {
                cacheNullValue(key);
                return null;
            }
            setWithLogicalExpire(key, value, logicalExpire);
            log.info("热点缓存首次写入完成，key={}", key);
            return value;
        }

        RedisData redisData = deserialize(json, RedisData.class);
        T value = convertRedisData(redisData, type);
        if (redisData.getExpireTime() != null && redisData.getExpireTime().isAfter(LocalDateTime.now())) {
            return value;
        }

        String lockKey = lockKeyPrefix + id;
        boolean locked = tryLock(lockKey);
        if (locked) {
            log.info("热点缓存已过期，准备异步重建，key={}", key);
            CACHE_REBUILD_EXECUTOR.submit(() -> {
                try {
                    T freshValue = dbFallback.get();
                    if (freshValue == null) {
                        cacheNullValue(key);
                        return;
                    }
                    setWithLogicalExpire(key, freshValue, logicalExpire);
                    log.info("热点缓存异步重建完成，key={}", key);
                } catch (Exception exception) {
                    log.error("热点缓存异步重建失败，key={}", key, exception);
                } finally {
                    unlock(lockKey);
                }
            });
        } else {
            log.info("热点缓存重建锁竞争中，直接返回旧值，key={}", key);
        }
        return value;
    }

    public void set(String key, Object value, Duration ttl) {
        stringRedisTemplate.opsForValue().set(key, serialize(value), withJitter(ttl));
    }

    public void setWithLogicalExpire(String key, Object value, Duration logicalExpire) {
        RedisData redisData = new RedisData();
        redisData.setData(value);
        redisData.setExpireTime(LocalDateTime.now().plus(withJitter(logicalExpire)));
        Duration physicalTtl = withJitter(logicalExpire.plusMinutes(30));
        stringRedisTemplate.opsForValue().set(key, serialize(redisData), physicalTtl);
    }

    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    private void cacheNullValue(String key) {
        log.info("数据库未查到数据，写入空值缓存，key={}", key);
        stringRedisTemplate.opsForValue().set(key, NULL_PLACEHOLDER, NULL_CACHE_TTL);
    }

    private boolean tryLock(String lockKey) {
        Boolean success = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOCK_TTL);
        return Boolean.TRUE.equals(success);
    }

    private void unlock(String lockKey) {
        stringRedisTemplate.delete(lockKey);
    }

    private Duration withJitter(Duration baseTtl) {
        long jitterSeconds = ThreadLocalRandom.current().nextLong(TTL_JITTER_SECONDS + 1L);
        return baseTtl.plusSeconds(jitterSeconds);
    }

    private <T> T convertRedisData(RedisData redisData, Class<T> type) {
        if (redisData == null || redisData.getData() == null) {
            return null;
        }
        return objectMapper.convertValue(redisData.getData(), type);
    }

    private <T> T deserialize(String json, Class<T> type) {
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException exception) {
            throw new BizException("缓存数据解析失败");
        }
    }

    private JsonNode deserializeTree(String json) {
        try {
            return objectMapper.readTree(json);
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

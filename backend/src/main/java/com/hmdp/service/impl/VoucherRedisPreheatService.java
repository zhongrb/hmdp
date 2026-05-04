package com.hmdp.service.impl;

import com.hmdp.entity.Voucher;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.utils.RedisConstants;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherRedisPreheatService {

    private final VoucherMapper voucherMapper;
    private final StringRedisTemplate stringRedisTemplate;

    public boolean ensureVoucherStock(Long voucherId) {
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucherId;
        Boolean exists = stringRedisTemplate.hasKey(stockKey);
        if (Boolean.TRUE.equals(exists)) {
            return true;
        }
        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            return false;
        }
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getStock() == null || voucher.getStock() <= 0
                || voucher.getBeginTime() == null || voucher.getEndTime() == null
                || voucher.getBeginTime().isAfter(now) || voucher.getEndTime().isBefore(now)) {
            return false;
        }
        long ttlSeconds = Duration.between(now, voucher.getEndTime()).getSeconds();
        if (ttlSeconds <= 0) {
            return false;
        }
        String orderKey = RedisConstants.SECKILL_ORDER_KEY + voucherId;
        stringRedisTemplate.opsForValue().set(stockKey, voucher.getStock().toString(), ttlSeconds, TimeUnit.SECONDS);
        stringRedisTemplate.expire(orderKey, ttlSeconds, TimeUnit.SECONDS);
        log.info("按需补齐优惠券库存到 Redis，voucherId={}, stock={}, ttl={}秒",
                voucher.getId(), voucher.getStock(), ttlSeconds);
        return true;
    }
}

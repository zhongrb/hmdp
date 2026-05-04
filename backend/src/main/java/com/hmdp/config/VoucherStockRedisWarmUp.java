package com.hmdp.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.entity.Voucher;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.utils.RedisConstants;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherStockRedisWarmUp implements CommandLineRunner {

    private final VoucherMapper voucherMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public void run(String... args) {
        LocalDateTime now = LocalDateTime.now();
        List<Voucher> vouchers = voucherMapper.selectList(new LambdaQueryWrapper<Voucher>()
                .gt(Voucher::getEndTime, now)
                .gt(Voucher::getStock, 0));
        for (Voucher voucher : vouchers) {
            long ttlSeconds = Duration.between(now, voucher.getEndTime()).getSeconds();
            if (ttlSeconds <= 0) {
                continue;
            }
            String stockKey = RedisConstants.SECKILL_STOCK_KEY + voucher.getId();
            String orderKey = RedisConstants.SECKILL_ORDER_KEY + voucher.getId();
            stringRedisTemplate.opsForValue().set(
                    stockKey,
                    voucher.getStock().toString(),
                    ttlSeconds,
                    TimeUnit.SECONDS
            );
            stringRedisTemplate.expire(orderKey, ttlSeconds, TimeUnit.SECONDS);
            log.info("预热优惠券库存到 Redis，voucherId={}, stock={}, ttl={}秒",
                    voucher.getId(), voucher.getStock(), ttlSeconds);
        }
    }
}

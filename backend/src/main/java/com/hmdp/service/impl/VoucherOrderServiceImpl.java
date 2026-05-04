package com.hmdp.service.impl;

import com.hmdp.dto.SeckillVoucherMessage;
import com.hmdp.exception.BizException;
import com.hmdp.service.VoucherOrderService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.SnowflakeIdWorker;
import com.hmdp.utils.UserHolder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherOrderServiceImpl implements VoucherOrderService {

    private final StringRedisTemplate stringRedisTemplate;
    private final DefaultRedisScript<Long> seckillScript;
    private final SnowflakeIdWorker snowflakeIdWorker;
    private final VoucherOrderMessageSender voucherOrderMessageSender;
    private final VoucherRedisPreheatService voucherRedisPreheatService;

    @Override
    @Transactional
    public Long claimSeckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        boolean stocked = voucherRedisPreheatService.ensureVoucherStock(voucherId);
        if (!stocked) {
            throw new BizException("活动尚未开始或已结束");
        }
        Long orderId = snowflakeIdWorker.nextId();
        Long result = stringRedisTemplate.execute(
                seckillScript,
                List.of(RedisConstants.SECKILL_STOCK_KEY + voucherId, RedisConstants.SECKILL_ORDER_KEY + voucherId),
                String.valueOf(userId)
        );
        if (result == null) {
            throw new BizException("系统繁忙，请稍后重试");
        }
        if (result == 1L) {
            log.info("秒杀库存不足，voucherId={}, userId={}", voucherId, userId);
            throw new BizException("优惠券已抢完，下次记得早点来");
        }
        if (result == 2L) {
            log.info("用户重复领券，voucherId={}, userId={}", voucherId, userId);
            throw new BizException("请勿重复领取同一张优惠券");
        }
        if (result == 3L) {
            throw new BizException("活动尚未开始或已结束");
        }
        if (result == 4L) {
            log.warn("秒杀库存未预热或已过期，voucherId={}, userId={}", voucherId, userId);
            throw new BizException("活动尚未开始或已结束");
        }
        voucherOrderMessageSender.send(new SeckillVoucherMessage(orderId, userId, voucherId));
        return orderId;
    }
}

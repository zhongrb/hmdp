package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.entity.Voucher;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.exception.BizException;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.service.VoucherOrderService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class VoucherOrderServiceImpl implements VoucherOrderService {

    private final StringRedisTemplate stringRedisTemplate;
    private final VoucherMapper voucherMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final DefaultRedisScript<Long> seckillScript;
    private final RedissonClient redissonClient;

    @Override
    @Transactional
    public Long claimSeckillVoucher(Long voucherId) {
        Long userId = UserHolder.getUser().getId();
        Long result = stringRedisTemplate.execute(
                seckillScript,
                List.of(RedisConstants.SECKILL_STOCK_KEY + voucherId, RedisConstants.SECKILL_ORDER_KEY + voucherId),
                String.valueOf(voucherId),
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

        // Lua 先在 Redis 层完成库存与一人一单预校验，这里再用用户粒度分布式锁兜底，避免并发请求同时落库。
        RLock lock = redissonClient.getLock("lock:order:" + userId);
        lock.lock();
        try {
            return createVoucherOrder(voucherId, userId);
        } finally {
            lock.unlock();
        }
    }

    private Long createVoucherOrder(Long voucherId, Long userId) {
        // 即使 Redis 预校验已经放行，这里仍要以数据库结果做最终兜底，防止极端并发或缓存异常导致重复下单。
        Long existed = voucherOrderMapper.selectCount(new LambdaQueryWrapper<VoucherOrder>()
                .eq(VoucherOrder::getUserId, userId)
                .eq(VoucherOrder::getVoucherId, voucherId));
        if (existed != null && existed > 0) {
            log.info("数据库兜底拦截重复领券，voucherId={}, userId={}", voucherId, userId);
            throw new BizException("请勿重复领取同一张优惠券");
        }

        Voucher voucher = voucherMapper.selectById(voucherId);
        if (voucher == null) {
            throw new BizException("优惠券不存在");
        }
        LocalDateTime now = LocalDateTime.now();
        if (voucher.getBeginTime().isAfter(now) || voucher.getEndTime().isBefore(now)) {
            throw new BizException("活动尚未开始或已结束");
        }

        VoucherOrder order = new VoucherOrder();
        order.setUserId(userId);
        order.setVoucherId(voucherId);
        order.setStatus(0);
        voucherOrderMapper.insert(order);
        log.info("秒杀订单创建成功，orderId={}, voucherId={}, userId={}", order.getId(), voucherId, userId);
        return order.getId();
    }
}

package com.hmdp.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hmdp.config.RabbitMqConfig;
import com.hmdp.dto.SeckillVoucherMessage;
import com.hmdp.entity.VoucherOrder;
import com.hmdp.mapper.VoucherMapper;
import com.hmdp.mapper.VoucherOrderMapper;
import com.hmdp.utils.RedisConstants;
import com.rabbitmq.client.Channel;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class VoucherOrderConsumer {

    private final VoucherMapper voucherMapper;
    private final VoucherOrderMapper voucherOrderMapper;
    private final RedissonClient redissonClient;

    @RabbitListener(queues = RabbitMqConfig.SECKILL_ORDER_QUEUE)
    public void consumeSeckillOrder(SeckillVoucherMessage message, Message rawMessage, Channel channel) throws IOException {
        long deliveryTag = rawMessage.getMessageProperties().getDeliveryTag();
        try {
            handleMessage(message);
            channel.basicAck(deliveryTag, false);
            log.info("秒杀订单消费成功，orderId={}, voucherId={}, userId={}",
                    message.orderId(), message.voucherId(), message.userId());
        } catch (IllegalStateException exception) {
            log.warn("秒杀订单消费失败并进入死信队列，orderId={}, voucherId={}, userId={}",
                    message.orderId(), message.voucherId(), message.userId(), exception);
            channel.basicReject(deliveryTag, false);
        } catch (Exception exception) {
            log.error("秒杀订单消费异常并进入死信队列，orderId={}, voucherId={}, userId={}",
                    message.orderId(), message.voucherId(), message.userId(), exception);
            channel.basicReject(deliveryTag, false);
        }
    }

    @Transactional
    public void handleMessage(SeckillVoucherMessage message) {
        RLock lock = redissonClient.getLock(RedisConstants.LOCK_ORDER_KEY + message.userId());
        lock.lock();
        try {
            Long existed = voucherOrderMapper.selectCount(new LambdaQueryWrapper<VoucherOrder>()
                    .eq(VoucherOrder::getUserId, message.userId())
                    .eq(VoucherOrder::getVoucherId, message.voucherId()));
            if (existed != null && existed > 0) {
                log.info("数据库查询拦截重复消费，orderId={}, voucherId={}, userId={}",
                        message.orderId(), message.voucherId(), message.userId());
                return;
            }

            int updated = voucherMapper.deductStock(message.voucherId());
            if (updated != 1) {
                throw new IllegalStateException("数据库库存扣减失败");
            }

            VoucherOrder order = new VoucherOrder();
            order.setId(message.orderId());
            order.setUserId(message.userId());
            order.setVoucherId(message.voucherId());
            order.setStatus(0);
            try {
                voucherOrderMapper.insert(order);
            } catch (DuplicateKeyException duplicateKeyException) {
                log.info("数据库唯一索引拦截重复消费，orderId={}, voucherId={}, userId={}",
                        message.orderId(), message.voucherId(), message.userId());
            }
        } finally {
            lock.unlock();
        }
    }
}
